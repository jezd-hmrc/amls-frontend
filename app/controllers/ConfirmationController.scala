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

package controllers

import cats.data.OptionT
import cats.implicits._
import connectors.{AmlsConnector, DataCacheConnector, KeystoreConnector, _}
import javax.inject.{Inject, Singleton}
import models.ResponseType.AmendOrVariationResponseType
import models.confirmation.{BreakdownRow, Currency}
import models.status._
import models.{FeeResponse, SubmissionRequestStatus}
import play.api.Logger
import play.api.mvc.{AnyContent, MessagesControllerComponents, Request, Result}
import services.{AuthEnrolmentsService, FeeResponseService, StatusService, _}
import uk.gov.hmrc.http.HeaderCarrier
import utils.{AuthAction, BusinessName, FeeHelper}
import views.html.confirmation._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ConfirmationController @Inject()(authAction: AuthAction,
                                       val ds: CommonPlayDependencies,
                                       private[controllers] val keystoreConnector: KeystoreConnector,
                                       private[controllers] implicit val dataCacheConnector: DataCacheConnector,
                                       private[controllers] implicit val amlsConnector: AmlsConnector,
                                       private[controllers] implicit val statusService: StatusService,
                                       private[controllers] val authenticator: AuthenticatorConnector,
                                       private[controllers] val enrolmentService: AuthEnrolmentsService,
                                       private[controllers] val confirmationService: ConfirmationService,
                                       val cc: MessagesControllerComponents,
                                       val feeHelper: FeeHelper) extends AmlsBaseController(ds, cc) {

  val prefix = "[ConfirmationController]"

  def get() = authAction.async {
      implicit request =>
        // $COVERAGE-OFF$
        Logger.debug(s"[$prefix] - begin get()...")
        // $COVERAGE-ON$
        for {
          _ <- authenticator.refreshProfile
          status <- statusService.getStatus(request.amlsRefNumber, request.accountTypeId, request.credId)
          submissionRequestStatus <- dataCacheConnector.fetch[SubmissionRequestStatus](request.credId, SubmissionRequestStatus.key)
          result <- resultFromStatus(status, submissionRequestStatus, request.amlsRefNumber, request.accountTypeId, request.credId, request.groupIdentifier)
          _ <- keystoreConnector.setConfirmationStatus
        } yield result
  }

  private def showRenewalConfirmation(fees: FeeResponse, breakdownRows: Future[Option[Seq[BreakdownRow]]],
                                      status: SubmissionStatus, submissionRequestStatus: Option[SubmissionRequestStatus], credId: String)
                                     (implicit hc: HeaderCarrier, request: Request[AnyContent]) = {

    confirmationService.isRenewalDefined(credId) flatMap { isRenewalDefined =>
      breakdownRows map {
        case maybeRows@Some(rows) if fees.toPay(status, submissionRequestStatus) > 0 =>
          if (isRenewalDefined) {
            Ok(confirm_renewal(fees.paymentReference,
              fees.totalFees,
              rows,
              fees.toPay(status, submissionRequestStatus),
              controllers.payments.routes.WaysToPayController.get().url)).some
          } else {
            Ok(confirm_amendvariation(fees.paymentReference,
              fees.totalFees,
              fees.toPay(status, submissionRequestStatus),
              maybeRows,
              controllers.payments.routes.WaysToPayController.get().url)).some
          }
        case _ => None
      }
    }
  }

  private def showAmendmentVariationConfirmation(fees: FeeResponse, breakdownRows: Future[Option[Seq[BreakdownRow]]],
                                                 status: SubmissionStatus, submissionRequestStatus: Option[SubmissionRequestStatus])
                                                (implicit hc: HeaderCarrier, request: Request[AnyContent]) = {

    breakdownRows map { maybeRows =>
      val amount = fees.toPay(status, submissionRequestStatus)

      Ok(confirm_amendvariation(fees.paymentReference,
        Currency(fees.totalFees),
        amount,
        maybeRows,
        controllers.payments.routes.WaysToPayController.get().url)).some
    }
  }

  private def resultFromStatus(status: SubmissionStatus, submissionRequestStatus: Option[SubmissionRequestStatus],
                               amlsRegistrationNumber: Option[String], accountTypeId: (String, String), credId: String, groupIdentifier: Option[String])
                              (implicit hc: HeaderCarrier, request: Request[AnyContent], statusService: StatusService): Future[Result] = {

    // $COVERAGE-OFF$
    Logger.debug(s"[$prefix][resultFromStatus] - Begin get fee response...)")
    // $COVERAGE-ON$

    OptionT.liftF(feeHelper.retrieveFeeResponse(amlsRegistrationNumber, accountTypeId, groupIdentifier, prefix)) flatMap {
      case Some(fees) if fees.paymentReference.isDefined && fees.toPay(status, submissionRequestStatus) > 0 =>
        // $COVERAGE-OFF$
        Logger.debug(s"[$prefix][resultFromStatus] - Fee found)")
        // $COVERAGE-ON$
        lazy val breakdownRows = confirmationService.getBreakdownRows(credId, status, fees)

        // $COVERAGE-OFF$
        Logger.debug(s"[$prefix][resultFromStatus] - fees: $fees")
        // $COVERAGE-ON$

        status match {
          case SubmissionReadyForReview | SubmissionDecisionApproved if fees.responseType equals AmendOrVariationResponseType =>
            OptionT(showAmendmentVariationConfirmation(fees, breakdownRows, status, submissionRequestStatus))
          case ReadyForRenewal(_) | RenewalSubmitted(_) =>
            OptionT(showRenewalConfirmation(fees, breakdownRows, status, submissionRequestStatus, credId))
          case _ =>
            OptionT.liftF(breakdownRows) map { maybeRows =>
              Ok(confirmation_new(fees.paymentReference, fees.totalFees, maybeRows, controllers.payments.routes.WaysToPayController.get().url))
            }
        }

      case _ =>
        // $COVERAGE-OFF$
        Logger.debug(s"[$prefix][resultFromStatus] - No fee found)")
        // $COVERAGE-ON$
        for {
          name <- BusinessName.getBusinessNameFromAmls(amlsRegistrationNumber, accountTypeId, credId)
      } yield {
        Ok(confirmation_no_fee(name))}
    } getOrElse InternalServerError("Could not determine a response")
  }
}