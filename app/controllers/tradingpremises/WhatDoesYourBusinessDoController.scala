package controllers.tradingpremises

import config.AMLSAuthConnector
import connectors.DataCacheConnector
import controllers.BaseController
import forms.{ValidForm, InvalidForm, EmptyForm, Form2}
import models.businessmatching._
import models.estateagentbusiness.EstateAgentBusiness
import models.tradingpremises.{YourAgent, WhatDoesYourBusinessDo, TradingPremises}
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.play.frontend.auth.AuthContext

import scala.concurrent.Future

trait WhatDoesYourBusinessDoController extends BaseController {
  val dataCacheConnector: DataCacheConnector

  def get(edit: Boolean = false) = Authorised.async {
    implicit authContext => implicit request => {
      buildView(edit, Ok)
    }
  }

  private def buildView(edit: Boolean, status: Status)(implicit authContext:AuthContext, request:Request[_]) = {
    dataCacheConnector.fetchDataShortLivedCache[BusinessMatching](BusinessMatching.key).flatMap {
      case Some(BusinessMatching(Some(BusinessActivities(activityList)))) if activityList.size==1 => {
        dataCacheConnector.fetchDataShortLivedCache[TradingPremises](TradingPremises.key)
          .map {
            case Some(existingData) => existingData.whatDoesYourBusinessDoAtThisAddress(WhatDoesYourBusinessDo(activityList))
            case _ => TradingPremises(None, None, Some(WhatDoesYourBusinessDo(activityList)))
          }
          .map{ tp =>
            dataCacheConnector.saveDataShortLivedCache(TradingPremises.key, tp)
            SeeOther(controllers.tradingpremises.routes.SummaryController.get.url)
          }
      }
      case Some(BusinessMatching(Some(activityList))) => Future.successful(status(views.html.what_does_your_business_do(EmptyForm, activityList, edit)))
      case _ => Future.successful(NotFound)
    }
  }

  def post(edit: Boolean = false) = Authorised.async {
    implicit authContext => implicit request => {
      Form2[WhatDoesYourBusinessDo](request.body) match {
        case f: InvalidForm => buildView(edit, BadRequest)
        case ValidForm(_, data) =>
          for {
            tradingPremises <- dataCacheConnector.fetchDataShortLivedCache[TradingPremises](TradingPremises.key)
            _ <- dataCacheConnector.saveDataShortLivedCache[TradingPremises](TradingPremises.key, tradingPremises.whatDoesYourBusinessDoAtThisAddress(data))
          } yield  Redirect(controllers.tradingpremises.routes.SummaryController.get())
      }
    }
  }
}

object WhatDoesYourBusinessDoController extends WhatDoesYourBusinessDoController {
  override val authConnector = AMLSAuthConnector
  override val dataCacheConnector = DataCacheConnector
}
