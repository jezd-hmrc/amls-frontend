/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.responsiblepeople.address

import cats.data._
import cats.implicits._
import com.google.inject.Inject
import connectors.DataCacheConnector
import controllers.DefaultBaseController
import forms.{EmptyForm, Form2, InvalidForm, ValidForm}
import models.responsiblepeople._
import play.api.mvc.{AnyContent, Request}
import services.AutoCompleteService
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import utils.{AuthAction, ControllerHelper, RepeatingSection}
import views.html.responsiblepeople.address.additional_extra_address_NonUK

import scala.concurrent.Future

class AdditionalExtraAddressNonUKController @Inject()(val dataCacheConnector: DataCacheConnector,
                                                      authAction: AuthAction,
                                                      implicit val auditConnector: AuditConnector,
                                                      autoCompleteService: AutoCompleteService) extends RepeatingSection with DefaultBaseController {

  def get(index: Int, edit: Boolean = false, flow: Option[String] = None) = authAction.async {
    implicit request =>
      getData[ResponsiblePerson](request.credId, index) map {
        case Some(ResponsiblePerson(Some(personName),_,_,_,_,_,_,_,_,Some(ResponsiblePersonAddressHistory(_,_,Some(additionalExtraAddress))),_,_,_,_,_,_,_,_,_,_,_, _)) =>
          Ok(additional_extra_address_NonUK(Form2[ResponsiblePersonAddress](additionalExtraAddress), edit, index, flow, personName.titleName, autoCompleteService.getCountries))
        case Some(ResponsiblePerson(Some(personName),_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_)) =>
          Ok(additional_extra_address_NonUK(EmptyForm, edit, index, flow, personName.titleName, autoCompleteService.getCountries))
        case _ => NotFound(notFoundView)
      }
  }

  def post(index: Int, edit: Boolean = false, flow: Option[String] = None) =
    authAction.async {
      implicit request =>
        (Form2[ResponsiblePersonAddress](request.body) match {
          case f: InvalidForm =>
            getData[ResponsiblePerson](request.credId, index) map { rp =>
              BadRequest(additional_extra_address_NonUK(f, edit, index, flow, ControllerHelper.rpTitleName(rp), autoCompleteService.getCountries))
            }
          case ValidForm(_, data) => {
            getData[ResponsiblePerson](request.credId, index) flatMap { responsiblePerson =>
              (for {
                rp <- responsiblePerson
                addressHistory <- rp.addressHistory
                additionalExtraAddress <- addressHistory.additionalExtraAddress
              } yield {
                val additionalExtraAddressWithTime = data.copy(timeAtAddress = additionalExtraAddress.timeAtAddress)
                updateAndRedirect(request.credId, additionalExtraAddressWithTime, index, edit, flow)
              }) getOrElse updateAndRedirect(request.credId, data, index, edit, flow)
            }
          }}).recoverWith {
          case _: IndexOutOfBoundsException => Future.successful(NotFound(notFoundView))
        }
    }

  private def updateAndRedirect(credId: String, data: ResponsiblePersonAddress, index: Int, edit: Boolean, flow: Option[String])
                               (implicit request: Request[AnyContent]) = {
    val doUpdate = () => updateDataStrict[ResponsiblePerson](credId, index) { res =>
      res.addressHistory(
        res.addressHistory match {
          case Some(a) => a.additionalExtraAddress(data)
          case _ => ResponsiblePersonAddressHistory(additionalExtraAddress = Some(data))
        }
      )
    } map { _ =>
      data.timeAtAddress match {
        case Some(_) if edit => Redirect(controllers.responsiblepeople.routes.DetailedAnswersController.get(index, flow))
        case _ => Redirect(routes.TimeAtAdditionalExtraAddressController.get(index, edit, flow))

      }
    }

    (for {
      rp <- OptionT(getData[ResponsiblePerson](credId, index))
      result <- OptionT.liftF(doUpdate())
      _ <- OptionT.liftF(AddressHelper.auditPreviousExtraAddressChange(data.personAddress, rp, edit))
    } yield result).getOrElse(NotFound(notFoundView))
  }
}