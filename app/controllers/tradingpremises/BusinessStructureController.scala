package controllers.tradingpremises

import config.AMLSAuthConnector
import connectors.DataCacheConnector
import controllers.BaseController
import forms._
import models.tradingpremises._
import play.api.libs.json.Json
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import utils.RepeatingSection

import scala.concurrent.Future

trait BusinessStructureController extends RepeatingSection with BaseController {

  val dataCacheConnector: DataCacheConnector

  def get(index: Int, edit: Boolean = false) = Authorised.async {
    implicit authContext => implicit request =>
      getData[TradingPremises](index) map {
        response =>
          val form = (for {
            tp <- response
            services <- tp.businessStructure
          } yield Form2[BusinessStructure](services)).getOrElse(EmptyForm)

          Ok(views.html.tradingpremises.business_structure(form, index, edit))
      }
  }

  def redirectToPage(data: BusinessStructure, edit: Boolean, index: Int) = {
    data match {
      case SoleProprietor => Redirect(routes.AgentNameController.get(index, edit))
      case LimitedLiabilityPartnership | IncorporatedBody => Redirect(routes.AgentCompanyNameController.get(index,edit))
      case Partnership => Redirect(routes.AgentPartnershipController.get(index, edit))
      case UnincorporatedBody => edit match {
        case true => Redirect(routes.SummaryController.getIndividual(index))
        case false => Redirect(routes.WhereAreTradingPremisesController.get(index, edit))
      }
    }
  }

  def post(index: Int, edit: Boolean = false) = Authorised.async {
    implicit authContext => implicit request =>
      Form2[BusinessStructure](request.body) match {
        case f: InvalidForm =>
          Future.successful(BadRequest(views.html.tradingpremises.business_structure(f, index, edit)))
        case ValidForm(_, data) =>
          for {
            _ <- updateData[TradingPremises](index) {
              case Some(tp) => Some(resetAgentValues(tp.businessStructure(data), data))
              case _ => Some(TradingPremises(businessStructure = Some(data)))
            }
          } yield redirectToPage(data, edit, index)
      }
  }

  private def resetAgentValues(tp:TradingPremises, data:BusinessStructure):TradingPremises = data match {
    case UnincorporatedBody => tp.copy(agentName=None,agentCompanyName=None,agentPartnership=None)
    case _ => tp.businessStructure(data)
  }

}

object BusinessStructureController extends BusinessStructureController {
  // $COVERAGE-OFF$
  override protected val authConnector: AuthConnector = AMLSAuthConnector
  override val dataCacheConnector: DataCacheConnector = DataCacheConnector
}
