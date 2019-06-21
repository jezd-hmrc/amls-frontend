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

package controllers.businessdetails

import audit.AddressConversions._
import audit.{AddressCreatedEvent, AddressModifiedEvent}
import cats.data.OptionT
import cats.implicits._
import com.google.inject.Inject
import connectors.DataCacheConnector
import controllers.BaseController
import forms.{EmptyForm, Form2, InvalidForm, ValidForm}
import models.businessdetails.{BusinessDetails, CorrespondenceAddress, CorrespondenceAddressUk}
import play.api.mvc.Request
import services.AutoCompleteService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult.Success
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import views.html.businessdetails._

import scala.concurrent.Future

class CorrespondenceAddressUkController @Inject ()(
                                                 val dataConnector: DataCacheConnector,
                                                 val authConnector: AuthConnector,
                                                 val auditConnector: AuditConnector,
                                                 val autoCompleteService: AutoCompleteService
                                                 ) extends BaseController {

  def get(edit: Boolean = false) = Authorised.async {
    implicit authContext => implicit request =>
      dataConnector.fetch[BusinessDetails](BusinessDetails.key) map {
        response =>
          val form: Form2[CorrespondenceAddressUk] = (for {
            businessDetails <- response
            correspondenceAddress <- businessDetails.correspondenceAddress
            ukAddress <- correspondenceAddress.ukAddress
          } yield Form2[CorrespondenceAddressUk](ukAddress)).getOrElse(EmptyForm)
          Ok(correspondence_address_uk(form, edit))
      }
  }

  def post(edit: Boolean = false) = Authorised.async {
    implicit authContext => implicit request => {
      Form2[CorrespondenceAddressUk](request.body) match {
        case f: InvalidForm =>
          Future.successful(BadRequest(correspondence_address_uk(f, edit)))
        case ValidForm(_, data) =>
          val doUpdate = for {
            businessDetails:BusinessDetails <- OptionT(dataConnector.fetch[BusinessDetails](BusinessDetails.key))
            _ <- OptionT.liftF(dataConnector.save[BusinessDetails](BusinessDetails.key, businessDetails.correspondenceAddress(CorrespondenceAddress(Some(data), None))))
            _ <- OptionT.liftF(auditAddressChange(data, businessDetails.correspondenceAddress.flatMap(a => a.ukAddress), edit)) orElse OptionT.some(Success)
          } yield edit match {
            case true => Redirect(routes.SummaryController.get())
            case _ => Redirect(routes.SummaryController.get())
          }

          doUpdate getOrElse InternalServerError("Could not update correspondence address")
      }
    }
  }

  def auditAddressChange(currentAddress: CorrespondenceAddressUk, oldAddress: Option[CorrespondenceAddressUk], edit: Boolean)
                        (implicit hc: HeaderCarrier, request: Request[_]): Future[AuditResult] = {
    if (edit) {
      auditConnector.sendEvent(AddressModifiedEvent(currentAddress, oldAddress))
    } else {
      auditConnector.sendEvent(AddressCreatedEvent(currentAddress))
    }
  }
}