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
      buildView(EmptyForm, edit, Ok)
    }
  }

  private def buildView(form :Form2[_],  edit: Boolean, status: Status)(implicit authContext:AuthContext, request:Request[_]): Future[Result] = {

    dataCacheConnector.fetchAll map { x =>
      (for {
        allData <- x
        businessMatchingData <- allData.getEntry[BusinessMatching](BusinessMatching.key)
        tradingPremisesData <- allData.getEntry[TradingPremises](TradingPremises.key) orElse Some(TradingPremises())
      } yield businessMatchingData match {
        case BusinessMatching(Some(BusinessActivities(activityList))) if (activityList.size == 1) => {
          dataCacheConnector.saveDataShortLivedCache(TradingPremises.key,
            tradingPremisesData.whatDoesYourBusinessDoAtThisAddress(WhatDoesYourBusinessDo(activityList))
          ) map ( _ => SeeOther(controllers.tradingpremises.routes.SummaryController.get.url) )
        }
        case BusinessMatching(Some(businessActivities)) =>
          Future.successful(status(views.html.what_does_your_business_do(form, businessActivities, edit)))
      }) getOrElse Future.successful(NotFound)
    } flatMap (identity)
  }

  def post(edit: Boolean = false) = Authorised.async {
    implicit authContext => implicit request => {
      Form2[WhatDoesYourBusinessDo](request.body) match {
        case f: InvalidForm => buildView(f, edit, BadRequest)
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
