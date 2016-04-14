package controllers.responsiblepeople

import config.AMLSAuthConnector
import connectors.DataCacheConnector
import controllers.BaseController
import forms._
import models.responsiblepeople._
import utils.RepeatingSection
import views.html.responsiblepeople.position_within_business

import scala.concurrent.Future

trait PositionWithinBusinessController extends RepeatingSection with BaseController {

  val dataCacheConnector: DataCacheConnector

  def get(index: Int, edit: Boolean = false) =
    ResponsiblePeopleToggle {
      Authorised.async {
        implicit authContext => implicit request =>
          getData[ResponsiblePeople](index) map {
            response =>
              val form: Form2[Positions] = (for {
                responsiblePeople <- response
                positions <- responsiblePeople.positions
              } yield Form2[Positions](positions)).getOrElse(EmptyForm)
              Ok(position_within_business(form, edit, index))
          }
      }
  }

  def post(index: Int, edit: Boolean = false) =
    ResponsiblePeopleToggle {
      Authorised.async {
        import play.api.data.mapping.forms.Rules._
        implicit authContext => implicit request =>
          Form2[Positions](request.body) match {
            case f: InvalidForm =>
              Future.successful(BadRequest(position_within_business(f, edit, index)))
            case ValidForm(_, data) =>
              for {
                _ <- updateData[ResponsiblePeople](index) {
                  case Some(res) => Some(res.positions(data))
                  case _ => Some(ResponsiblePeople(positions = Some(data)))
                }
              } yield (data.positions.exists(personalTax), edit) match {
                case (true, false) => Redirect(routes.VATRegisteredController.get(index))
                case (false, false) => Redirect(routes.SummaryController.get()) //TODO: Experience page.
                case (_, true)  => Redirect(routes.SummaryController.get())
              }
          }
      }
    }

  private def personalTax(x: PositionWithinBusiness) = x == Partner || x == SoleProprietor

}

object PositionWithinBusinessController extends PositionWithinBusinessController {
  // $COVERAGE-OFF$
  override val authConnector = AMLSAuthConnector
  override val dataCacheConnector = DataCacheConnector
}
