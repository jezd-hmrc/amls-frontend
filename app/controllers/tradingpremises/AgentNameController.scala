package controllers.tradingpremises

import config.{AMLSAuthConnector, ApplicationConfig}
import connectors.DataCacheConnector
import controllers.BaseController
import forms.{Form2, _}
import models.DateOfChange
import models.status.SubmissionDecisionApproved
import models.tradingpremises._
import org.joda.time.LocalDate
import play.api.i18n.Messages
import services.StatusService
import utils.{DateOfChangeHelper, FeatureToggle, RepeatingSection}

import scala.concurrent.Future


trait AgentNameController extends RepeatingSection with BaseController with DateOfChangeHelper {

  val dataCacheConnector: DataCacheConnector
  val statusService: StatusService

  def get(index: Int, edit: Boolean = false) = Authorised.async {
    implicit authContext => implicit request =>

      getData[TradingPremises](index) map {

        case Some(tp) => {
          val form = tp.agentName match {
            case Some(data) => Form2[AgentName](data)
            case None => EmptyForm
          }
          Ok(views.html.tradingpremises.agent_name(form, index, edit))
        }
        case None => NotFound(notFoundView)
      }
  }

  def post(index: Int, edit: Boolean = false) = Authorised.async {
    implicit authContext => implicit request => {
      Form2[AgentName](request.body) match {
        case f: InvalidForm =>
          Future.successful(BadRequest(views.html.tradingpremises.agent_name(f, index, edit)))
        case ValidForm(_, data) => {
          for {
            tradingPremises <- getData[TradingPremises](index)
            result <- updateDataStrict[TradingPremises](index) { tp =>
              TradingPremises(tp.registeringAgentPremises, tp.yourTradingPremises,
                tp.businessStructure, Some(data), None, None, tp.whatDoesYourBusinessDoAtThisAddress, tp.msbServices, true, tp.lineId, tp.status, tp.endDate)
            }
            status <- statusService.getStatus
          } yield status match {
            case SubmissionDecisionApproved if redirectToDateOfChange(tradingPremises.get, data) =>
              Redirect(routes.AgentNameController.dateOfChange(index))
            case _ => edit match {
              case true => Redirect(routes.SummaryController.getIndividual(index))
              case false => Redirect(routes.WhereAreTradingPremisesController.get(index, edit))
            }
          }
        }.recoverWith {
          case _: IndexOutOfBoundsException => Future.successful(NotFound(notFoundView))
        }
      }
    }
  }

  def dateOfChange(index: Int) = FeatureToggle(ApplicationConfig.release7) {
    Authorised {
      implicit authContext => implicit request =>
        Ok(views.html.date_of_change(Form2[DateOfChange](DateOfChange(LocalDate.now)),
          "summary.tradingpremises", routes.AgentNameController.saveDateOfChange(index)))
    }
  }

  def saveDateOfChange(index: Int) = Authorised.async {
    implicit authContext =>
      implicit request =>
        getData[TradingPremises](index) flatMap { tradingPremises =>
          Form2[DateOfChange](request.body.asFormUrlEncoded.get ++ startDateFormFields(tradingPremises.startDate)) match {
            case form: InvalidForm =>
              Future.successful(BadRequest(views.html.date_of_change(
                form.withMessageFor(DateOfChange.errorPath, tradingPremises.startDateValidationMessage),
                "summary.tradingpremises", routes.AgentNameController.saveDateOfChange(index))))
            case ValidForm(_, dateOfChange) =>
              for {
                _ <- updateDataStrict[TradingPremises](index) { tp =>
                  tp.agentName(tradingPremises.agentName.get.copy(dateOfChange = Some(dateOfChange)))
                }
              } yield Redirect(routes.SummaryController.get())
          }
        }
  }

  private def redirectToDateOfChange(tradingPremises: TradingPremises, name: AgentName) = {
    ApplicationConfig.release7 && !tradingPremises.agentName.contains(name)
  }
}

object AgentNameController extends AgentNameController {
  // $COVERAGE-OFF$
  override val dataCacheConnector = DataCacheConnector
  override val authConnector = AMLSAuthConnector
  override val statusService = StatusService
}
