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

import com.google.inject.Inject
import connectors.DataCacheConnector
import controllers.DefaultBaseController
import controllers.responsiblepeople.routes
import forms.{Form2, InvalidForm, ValidForm}
import models.responsiblepeople.TimeAtAddress.{OneToThreeYears, ThreeYearsPlus}
import models.responsiblepeople._
import play.api.mvc.{AnyContent, Request}
import utils.{AuthAction, ControllerHelper, RepeatingSection}
import views.html.responsiblepeople.address.time_at_additional_address

import scala.concurrent.Future

class TimeAtAdditionalAddressController @Inject () (
                                                   val dataCacheConnector: DataCacheConnector,
                                                   authAction: AuthAction
                                                   ) extends RepeatingSection with DefaultBaseController {

  final val DefaultAddressHistory = ResponsiblePersonAddress(PersonAddressUK("", "", None, None, ""), None)

  def get(index: Int, edit: Boolean = false, flow: Option[String] = None) = authAction.async {
    implicit request =>
        getData[ResponsiblePerson](request.credId, index) map {
          case Some(ResponsiblePerson(Some(personName),_,_,_,_,_,_,_,_,Some(ResponsiblePersonAddressHistory(_,Some(ResponsiblePersonAddress(_,Some(additionalAddress))),_)),_,_,_,_,_,_,_,_,_,_,_, _)) =>
            Ok(time_at_additional_address(Form2[TimeAtAddress](additionalAddress), edit, index, flow, personName.titleName))
          case Some(ResponsiblePerson(Some(personName),_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_)) =>
            Ok(time_at_additional_address(Form2(DefaultAddressHistory), edit, index, flow, personName.titleName))
          case _ => NotFound(notFoundView)
        }
  }

  def post(index: Int, edit: Boolean = false, flow: Option[String] = None) = authAction.async {
    implicit request =>
      (Form2[TimeAtAddress](request.body) match {
        case f: InvalidForm =>
          //         getData[ResponsiblePerson](request.credId, index) map { rp =>
          //            BadRequest(time_at_address(f, edit, index, flow, ControllerHelper.rpTitleName(rp)))
          //          }
          Future.successful(Redirect(routes.AdditionalExtraAddressController.get(index, edit, flow)))
        case ValidForm(_, data) =>
          //          {
          //            getData[ResponsiblePerson](request.credId, index) flatMap { responsiblePerson =>
          //              (for {
          //                rp <- responsiblePerson
          //                addressHistory <- rp.addressHistory
          //                currentAddress <- addressHistory.currentAddress
          //              } yield {
          //                val currentAddressWithTime = currentAddress.copy(
          //                  timeAtAddress = Some(data)
          //                )
          //                doUpdate(request.credId, index, currentAddressWithTime).flatMap { _ =>
          //                  for {
          //                    status <- statusService.getStatus(request.amlsRefNumber, request.accountTypeId, request.credId)
          //                  } yield {
          //                    redirectTo(index, data, rp, status, edit, flow)
          //                  }
          //                }
          //              }) getOrElse Future.successful(NotFound(notFoundView))
          //            }
          //          }
          Future.successful(Redirect(routes.AdditionalExtraAddressController.get(index, edit, flow)))
      }).recoverWith {
        case _: IndexOutOfBoundsException => Future.successful(NotFound(notFoundView))
      }
  }

  private def redirectTo(index: Int, edit: Boolean, flow: Option[String], data: TimeAtAddress) = {
    data match {
      case ThreeYearsPlus | OneToThreeYears if !edit => Redirect(controllers.responsiblepeople.routes.PositionWithinBusinessController.get(index, edit, flow))
      case ThreeYearsPlus | OneToThreeYears if edit => Redirect(controllers.responsiblepeople.routes.DetailedAnswersController.get(index, flow))
      case _ => Redirect(routes.AdditionalExtraAddressController.get(index, edit, flow))
    }
  }

  private def doUpdate(credId: String, index: Int, data: ResponsiblePersonAddress)(implicit request: Request[AnyContent]) = {
    updateDataStrict[ResponsiblePerson](credId, index) { res =>
      res.addressHistory(
        res.addressHistory match {
          case Some(a) if data.timeAtAddress.contains(ThreeYearsPlus) | data.timeAtAddress.contains(OneToThreeYears) =>
            a.additionalAddress(data).removeAdditionalExtraAddress
          case Some(a) => a.additionalAddress(data)
          case _ => ResponsiblePersonAddressHistory(additionalAddress = Some(data))
        })
    }
  }
}