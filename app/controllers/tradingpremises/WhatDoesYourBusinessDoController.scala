package controllers.tradingpremises

import config.AMLSAuthConnector
import connectors.DataCacheConnector
import controllers.BaseController
import forms.{EmptyForm, Form2, InvalidForm, ValidForm}
import models.businessmatching._
import models.tradingpremises.{TradingPremises, WhatDoesYourBusinessDo}
import play.api.mvc.Result
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.http.HeaderCarrier
import utils.RepeatingSection
import views.html.tradingpremises._

import scala.concurrent.Future

trait WhatDoesYourBusinessDoController extends RepeatingSection with BaseController {

  val dataCacheConnector: DataCacheConnector

  private def data(index: Int, edit: Boolean)(implicit ac: AuthContext, hc: HeaderCarrier)
  : Future[Either[Result, (CacheMap, Set[BusinessActivity])]] = {
    dataCacheConnector.fetchAll map {
      cache =>
        type Tupe = (CacheMap, Set[BusinessActivity])
        (for {
          c <- cache
          bm <- c.getEntry[BusinessMatching](BusinessMatching.key)
          activities <- bm.activities flatMap {
            _.businessActivities match {
              case set if set.isEmpty => None
              case set => Some(set)
            }
          }
        } yield (c, activities))
          .fold[Either[Result, Tupe]] {
          // TODO: Need to think about what we should do in case of this error
          Left(Redirect(routes.WhereAreTradingPremisesController.get(index, edit)))
        } {
          t => Right(t)
        }
    }
  }

  def get(index: Int, edit: Boolean = false) = Authorised.async {
    implicit authContext => implicit request =>
      data(index, edit) flatMap {
        case Right((c, activities)) =>
          if (activities.size == 1) {
            updateData[TradingPremises](c, index) {
              case Some(tp) =>
                Some(tp.whatDoesYourBusinessDoAtThisAddress(WhatDoesYourBusinessDo(activities)))
              case _ =>
                Some(TradingPremises(
                  whatDoesYourBusinessDoAtThisAddress = Some(WhatDoesYourBusinessDo(activities))
                ))
            } map {
              _ => Redirect(routes.SummaryController.get())
            }
          } else {
            val ba = BusinessActivities(activities)
            Future.successful {
              getData[TradingPremises](c, index) match {
                case Some(TradingPremises(_, _, Some(wdbd),_)) =>
                  Ok(what_does_your_business_do(Form2[WhatDoesYourBusinessDo](wdbd), ba, edit, index))
                case _ =>
                  Ok(what_does_your_business_do(EmptyForm, ba, edit, index))
              }
            }
          }
        case Left(result) => Future.successful(result)
      }
  }

  // scalastyle:off cyclomatic.complexity
  def post(index: Int, edit: Boolean = false) = Authorised.async {
    implicit authContext => implicit request =>
      data(index, edit) flatMap {
        case Right((c, activities)) =>
          Form2[WhatDoesYourBusinessDo](request.body) match {
            case f: InvalidForm =>
              val ba = BusinessActivities(activities)
              Future.successful {
                BadRequest(what_does_your_business_do(f, ba, edit, index))
              }
            case ValidForm(_, data) =>
              updateData[TradingPremises](c, index) {
                case Some(TradingPremises(ytp, ya, _,_)) =>
                  Some(TradingPremises(ytp, ya, Some(data)))
                case _ =>
                  Some(TradingPremises(None, None, Some(data)))
              } map {
                _ => edit match {
                  case true => Redirect(routes.SummaryController.getIndividual(index))
                  case false => {
                    data.activities.contains(MoneyServiceBusiness) match {
                      case true =>  Redirect(routes.MSBServicesController.get(index))
                      case false => Redirect(routes.SummaryController.get())
                    }
                  }
                }
              }
          }
        case Left(result) => Future.successful(result)
      }
  }
  // scalastyle:on cyclomatic.complexity
}

object WhatDoesYourBusinessDoController extends WhatDoesYourBusinessDoController {
  // $COVERAGE-OFF$
  override val authConnector = AMLSAuthConnector
  override val dataCacheConnector = DataCacheConnector
}
