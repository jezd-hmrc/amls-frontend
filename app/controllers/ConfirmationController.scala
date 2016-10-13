package controllers

import config.AMLSAuthConnector
import models.status.SubmissionReadyForReview
import services.{StatusService, SubmissionService}

import scala.concurrent.Future

trait ConfirmationController extends BaseController {

  private[controllers] def subscriptionService: SubmissionService

  val statusService: StatusService

  def get() = Authorised.async {
    implicit authContext => implicit request =>
      statusService.getStatus flatMap {
        case SubmissionReadyForReview => {
          subscriptionService.getAmendment flatMap {
            case Some((regNo, total, rows, difference)) =>
              difference match {
                case Some(currency) if currency.value > 0 => Future.successful(Ok(views.html.confirmation.confirm_amendment(regNo, total, rows, difference)))
                case _ => Future.successful(Ok(views.html.confirmation.confirmation_amendment_no_fee(regNo)))
              }
            case None =>
              Future.failed(new Exception("Could not get AMLSRegNo"))
          }
        }
        case _ => {
          subscriptionService.getSubscription flatMap {
            case (mlrRegNo, total, rows) =>
              Future.successful(Ok(views.html.confirmation.confirmation(mlrRegNo, total, rows)))
          }
        }
      }
  }
}

object ConfirmationController extends ConfirmationController {
  // $COVERAGE-OFF$
  override protected val authConnector = AMLSAuthConnector
  override private[controllers] val subscriptionService = SubmissionService
  override val statusService: StatusService = StatusService
}
