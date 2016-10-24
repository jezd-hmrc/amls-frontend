package controllers.responsiblepeople

import config.AMLSAuthConnector
import connectors.DataCacheConnector
import controllers.BaseController
import forms.{EmptyForm, Form2, InvalidForm, ValidForm}
import models.responsiblepeople.{ResponsiblePeople, ResponsiblePersonEndDate}
import services.{AuthEnrolmentsService, StatusService}
import utils.{RepeatingSection, StatusConstants}
import models.status.{NotCompleted, SubmissionDecisionApproved, SubmissionReady, SubmissionReadyForReview}
import views.html.responsiblepeople.remove_responsible_person

import scala.concurrent.Future

trait RemoveResponsiblePersonController extends RepeatingSection with BaseController {

  val dataCacheConnector: DataCacheConnector

  private[controllers] def statusService: StatusService

  private[controllers] def authEnrolmentsService: AuthEnrolmentsService

  def get(index: Int, complete: Boolean = false) = Authorised.async {
    implicit authContext => implicit request =>
      for {
        rp <- getData[ResponsiblePeople](index)
        status <- statusService.getStatus
      } yield (rp, status) match {
        case (Some(ResponsiblePeople(Some(personName), _, _, _, _, _, _, _, _, _, _, _, _, _)), SubmissionDecisionApproved) => {
          Ok(views.html.responsiblepeople.remove_responsible_person(EmptyForm, index,
            personName.fullName, complete, true))
        }
        case (Some(ResponsiblePeople(Some(personName), _, _, _, _, _, _, _, _, _, _, _, _, _)),_) => {
          Ok(views.html.responsiblepeople.remove_responsible_person(EmptyForm, index, personName.fullName, complete, false))
        }
        case _ => NotFound(notFoundView)
      }
  }

  def remove(index: Int, complete: Boolean = false, personName: String) = Authorised.async {
    implicit authContext => implicit request =>

      statusService.getStatus flatMap {
        case NotCompleted | SubmissionReady => removeDataStrict[ResponsiblePeople](index) map { _ =>
          Redirect(routes.CheckYourAnswersController.get())
        }
        case SubmissionReadyForReview => for {
          result <- updateDataStrict[ResponsiblePeople](index) { tp =>
            tp.copy(status = Some(StatusConstants.Deleted), hasChanged = true)
          }
        } yield Redirect(routes.CheckYourAnswersController.get())
        case SubmissionDecisionApproved => Form2[ResponsiblePersonEndDate](request.body) match {
          case f: InvalidForm =>
            Future.successful(BadRequest(remove_responsible_person(f, index, personName, complete,  true)))
          case ValidForm(_, data) => {
            for {
              result <- updateDataStrict[ResponsiblePeople](index) { tp =>
                tp.copy(status = Some(StatusConstants.Deleted), endDate = Some(data), hasChanged = true)
              }
            } yield Redirect(routes.CheckYourAnswersController.get())
          }
        }
      }
  }
}

object RemoveResponsiblePersonController extends RemoveResponsiblePersonController {
  // $COVERAGE-OFF$
  override val authConnector = AMLSAuthConnector
  override val dataCacheConnector: DataCacheConnector = DataCacheConnector
  override private[controllers] val statusService: StatusService = StatusService
  override private[controllers] val authEnrolmentsService: AuthEnrolmentsService = AuthEnrolmentsService

}