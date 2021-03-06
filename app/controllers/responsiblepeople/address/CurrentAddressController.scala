/*
 * Copyright 2020 HM Revenue & Customs
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

import com.google.inject.Inject
import connectors.DataCacheConnector
import controllers.{AmlsBaseController, CommonPlayDependencies}
import forms.{EmptyForm, Form2, InvalidForm, ValidForm}
import models.responsiblepeople._
import play.api.mvc.MessagesControllerComponents
import services.AutoCompleteService
import uk.gov.hmrc.http.HeaderCarrier
import utils.{AuthAction, ControllerHelper, DateOfChangeHelper, RepeatingSection}
import views.html.responsiblepeople.address.current_address

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CurrentAddressController @Inject ()(val dataCacheConnector: DataCacheConnector,
                                          autoCompleteService: AutoCompleteService,
                                          authAction: AuthAction,
                                          val ds: CommonPlayDependencies,
                                          val cc: MessagesControllerComponents,
                                          current_address: current_address,
                                          implicit val error: views.html.error) extends AmlsBaseController(ds, cc) with RepeatingSection with DateOfChangeHelper with AddressHelper {

  def get(index: Int, edit: Boolean = false, flow: Option[String] = None) = authAction.async {
    implicit request =>
      getData[ResponsiblePerson](request.credId, index) map {
        case Some(ResponsiblePerson(Some(personName), _, _, _, _, _, _, _, _,
        Some(ResponsiblePersonAddressHistory(Some(currentAddress), _, _)), _, _, _, _, _, _, _, _, _, _, _, _))
        => Ok(current_address(Form2[ResponsiblePersonCurrentAddress](currentAddress), edit, index, flow, personName.titleName))
        case Some(ResponsiblePerson(Some(personName), _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _))
        => Ok(current_address(EmptyForm, edit, index, flow, personName.titleName))
        case _
        => NotFound(notFoundView)
      }
  }

  def post(index: Int, edit: Boolean = false, flow: Option[String] = None) =
    authAction.async {
      implicit request =>
        (Form2[ResponsiblePersonCurrentAddress]
          (request.body)(ResponsiblePersonCurrentAddress.addressFormRule(PersonAddress.formRule(AddressType.Current))) match {
          case f: InvalidForm if f.data.get("isUK").isDefined
          => processForm(ResponsiblePersonCurrentAddress(modelFromForm(f), None, None), request.credId, index, edit, flow)
          case f: InvalidForm
          => getData[ResponsiblePerson](request.credId, index) map { rp =>
            BadRequest(current_address(f, edit, index, flow, ControllerHelper.rpTitleName(rp)))
          }
          case ValidForm(_, data)
          => processForm(data, request.credId, index, edit, flow)
        }).recoverWith {
          case _: IndexOutOfBoundsException => Future.successful(NotFound(notFoundView))
        }
    }

  private def processForm(data: ResponsiblePersonCurrentAddress, credId: String, index: Int, edit: Boolean, flow: Option[String])
                         (implicit hc: HeaderCarrier) = {

    updateDataStrict[ResponsiblePerson](credId, index) { res =>
      (res.addressHistory, data.personAddress) match {
        case (None, _)
        => res.addressHistory(ResponsiblePersonAddressHistory(Some(data)))
        case (Some(rph), addrUk: PersonAddressUK) if !ResponsiblePersonAddressHistory.isRPCurrentAddressInUK(rph.currentAddress)
        => res.addressHistory(rph.copy(currentAddress = Some(ResponsiblePersonCurrentAddress(addrUk, rph.currentAddress.flatMap(_.timeAtAddress)))))
        case (Some(rph), addrNonUK: PersonAddressNonUK) if ResponsiblePersonAddressHistory.isRPCurrentAddressInUK(rph.currentAddress)
        => res.addressHistory(rph.copy(currentAddress = Some(ResponsiblePersonCurrentAddress(addrNonUK, rph.currentAddress.flatMap(_.timeAtAddress)))))
        case (_, _) => res
      }
    } map { _ =>
      if (data.personAddress.isInstanceOf[PersonAddressUK]) {
        Redirect(routes.CurrentAddressUKController.get(index, edit, flow))
      } else {
        Redirect(routes.CurrentAddressNonUKController.get(index, edit, flow))
      }
    }
  }
}