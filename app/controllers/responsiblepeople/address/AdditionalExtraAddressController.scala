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

import audit.AddressConversions._
import audit.{AddressCreatedEvent, AddressModifiedEvent}
import cats.data._
import cats.implicits._
import com.google.inject.Inject
import connectors.DataCacheConnector
import controllers.DefaultBaseController
import controllers.responsiblepeople.routes
import forms.{Form2, InvalidForm, ValidForm}
import models.responsiblepeople._
import play.api.mvc.{AnyContent, Request}
import services.AutoCompleteService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult.Success
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import utils.{AuthAction, ControllerHelper, RepeatingSection}
import views.html.responsiblepeople.address.additional_extra_address

import scala.concurrent.Future

class AdditionalExtraAddressController @Inject()(
                                                   val dataCacheConnector: DataCacheConnector,
                                                   authAction: AuthAction,
                                                   auditConnector: AuditConnector,
                                                   autoCompleteService: AutoCompleteService
                                                 ) extends RepeatingSection with DefaultBaseController {

  def get(index: Int, edit: Boolean = false, flow: Option[String] = None) = authAction.async {
    implicit request =>
      getData[ResponsiblePerson](request.credId, index) map {
        case Some(ResponsiblePerson(Some(personName),_,_,_,_,_,_,_,_,Some(ResponsiblePersonAddressHistory(_,_,Some(additionalExtraAddress))),_,_,_,_,_,_,_,_,_,_,_, _)) =>
          Ok(additional_extra_address(Form2[ResponsiblePersonAddress](additionalExtraAddress), edit, index, flow, personName.titleName))
        case Some(ResponsiblePerson(Some(personName),_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_)) =>
          Ok(additional_extra_address(Form2(ResponsiblePersonAddressHistory.default()), edit, index, flow, personName.titleName))
        case _ => NotFound(notFoundView)
      }
  }

  def post(index: Int, edit: Boolean = false, flow: Option[String] = None) =
    authAction.async {
      implicit request =>
        (Form2[ResponsiblePersonCurrentAddress](request.body) match {
          case f: InvalidForm =>
            Future.successful(Redirect(routes.AdditionalExtraAddressUKController.get(index, edit, flow)))
          case ValidForm(_, data) => {
            Future.successful(Redirect(routes.AdditionalExtraAddressUKController.get(index, edit, flow)))
          }}).recoverWith {
          case _: IndexOutOfBoundsException => Future.successful(NotFound(notFoundView))
        }
    }

//  private def updateAndRedirect(credId: String, data: ResponsiblePersonAddress, index: Int, edit: Boolean, flow: Option[String])
//                               (implicit request: Request[AnyContent]) = {
//    val doUpdate = () => updateDataStrict[ResponsiblePerson](credId, index) { res =>
//      res.addressHistory(
//        res.addressHistory match {
//          case Some(a) => a.additionalExtraAddress(data)
//          case _ => ResponsiblePersonAddressHistory(additionalExtraAddress = Some(data))
//        }
//      )
//    } map { _ =>
//      data.timeAtAddress match {
//        case Some(_) if edit => Redirect(routes.DetailedAnswersController.get(index, flow))
//        case _ => Redirect(routes.TimeAtAdditionalExtraAddressController.get(index, edit, flow))
//
//      }
//    }
//
//    val block = for {
//      rp <- OptionT(getData[ResponsiblePerson](credId, index))
//      result <- OptionT.liftF(doUpdate())
//      _ <- OptionT.liftF(auditAddressChange(data.personAddress, rp, edit))
//    } yield result
//
//    block getOrElse NotFound(notFoundView)
//  }
//
//  private def auditAddressChange(newAddress: PersonAddress, model: ResponsiblePerson, edit: Boolean)
//                                (implicit hc: HeaderCarrier, request: Request[_]): Future[AuditResult] = {
//    if (edit) {
//      val oldAddress = for {
//        history <- model.addressHistory
//        addr <- history.additionalExtraAddress
//      } yield addr
//
//      oldAddress.fold[Future[AuditResult]](Future.successful(Success)) { addr =>
//        auditConnector.sendEvent(AddressModifiedEvent(newAddress, Some(addr.personAddress)))
//      }
//    }
//    else {
//      auditConnector.sendEvent(AddressCreatedEvent(newAddress))
//    }
//  }
}