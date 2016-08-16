package controllers.tradingpremises

import config.AMLSAuthConnector
import connectors.DataCacheConnector
import controllers.BaseController
import forms._
import models.tradingpremises._
import utils.RepeatingSection

import scala.concurrent.Future


 trait AgentCompanyNameController extends RepeatingSection with BaseController {

    val dataCacheConnector: DataCacheConnector

    def get(index: Int, edit: Boolean = false) = Authorised.async {
      implicit authContext => implicit request =>

        getData[TradingPremises](index) map {

          case Some(tp) => {
            val form = tp.agentCompanyName match {
              case Some(data) => Form2[AgentCompanyName](data)
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
            result <- updateDataStrict[TradingPremises](index) {
              case Some(tp) =>
                Some(TradingPremises(tp.registeringAgentPremises,
                  tp.yourTradingPremises, tp.businessStructure,tp.agentName,Some(data),tp.agentPartnership, tp.whatDoesYourBusinessDoAtThisAddress, tp.msbServices))
            }
          } yield Redirect(routes.WhereAreTradingPremisesController.get(index,edit))
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