package controllers.tradingpremises

import config.AMLSAuthConnector
import connectors.DataCacheConnector
import controllers.BaseController
import forms._
import models.tradingpremises._
import play.api.mvc.{AnyContent, Request}
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.{ControllerHelper, RepeatingSection}

import scala.concurrent.Future


trait AgentCompanyNameController extends RepeatingSection with BaseController {

  val dataCacheConnector: DataCacheConnector

  def get(index: Int, edit: Boolean = false) = Authorised.async {
    implicit authContext => implicit request =>

      getData[TradingPremises](index) map {

        case Some(tp) => {
          val form = tp.agentCompanyDetails match {
            case Some(data) => Form2[AgentCompanyDetails](data)
            case None => EmptyForm
          }
          Ok(views.html.tradingpremises.agent_company_name(form, index, edit))
        }
        case None => NotFound(notFoundView)
      }
  }

  def post(index: Int ,edit: Boolean = false) = Authorised.async {
    implicit authContext => implicit request => {
      Form2[AgentCompanyName](request.body) match {
        case f: InvalidForm =>
          Future.successful(BadRequest(views.html.tradingpremises.agent_company_name(f, index,edit)))
        case ValidForm(_, data) => {
          for {
            result <- fetchAllAndUpdateStrict[TradingPremises](index) { (_, tp) =>
              TradingPremises(tp.registeringAgentPremises,
                tp.yourTradingPremises,
                tp.businessStructure, None, Some(data) , None, tp.whatDoesYourBusinessDoAtThisAddress,
                tp.msbServices, true, tp.lineId, tp.status, tp.endDate)
            }
          } yield edit match {
            case true => Redirect(routes.SummaryController.getIndividual(index))
            case false => ControllerHelper.redirectToNextPage(result, index, edit)
          }
        }.recoverWith {
          case _: IndexOutOfBoundsException => Future.successful(NotFound(notFoundView))
        }
      }
    }
  }
}

object AgentCompanyNameController extends AgentCompanyNameController {
  // $COVERAGE-OFF$
  override val dataCacheConnector = DataCacheConnector
  override val authConnector = AMLSAuthConnector
}
