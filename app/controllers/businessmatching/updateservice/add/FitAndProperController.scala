/*
 * Copyright 2018 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.businessmatching.updateservice.add

import cats.data.OptionT
import cats.implicits._
import config.AppConfig
import connectors.DataCacheConnector
import controllers.BaseController
import controllers.businessmatching.updateservice.UpdateServiceHelper
import controllers.businessmatching.updateservice.add.routes._
import forms.{EmptyForm, Form2, InvalidForm, ValidForm}
import javax.inject.{Inject, Singleton}
import models.businessmatching.{MoneyServiceBusiness, TrustAndCompanyServices}
import models.flowmanagement.AddServiceFlowModel
import models.responsiblepeople.ResponsiblePeople
import play.api.mvc.{Request, Result}
import services.StatusService
import services.businessmatching.BusinessMatchingService
import services.flowmanagement.Router
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import utils.{BooleanFormReadWrite, RepeatingSection}
import views.html.businessmatching.updateservice.add._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FitAndProperController @Inject()(
                                        val authConnector: AuthConnector,
                                        implicit val dataCacheConnector: DataCacheConnector,
                                        val statusService: StatusService,
                                        val businessMatchingService: BusinessMatchingService,
                                        val helper: UpdateServiceHelper,
                                        val router: Router[AddServiceFlowModel],
                                        config: AppConfig
                                        ) extends BaseController with RepeatingSection {

  val NAME = "passedFitAndProper"

  implicit val boolWrite = BooleanFormReadWrite.formWrites(NAME)
  implicit val boolRead = BooleanFormReadWrite.formRule(NAME, "error.businessmatching.updateservice.fitandproper")

  def get() = Authorised.async {
    implicit authContext =>
      implicit request =>
        filterRequest {
          Future.successful(Ok(fit_and_proper(EmptyForm, config.showFeesToggle)))
        }
  }

  def post() = Authorised.async{
    implicit authContext =>
      implicit request =>
      filterRequest {
        Form2[Boolean](request.body) match {
          case ValidForm(_, data) => data match {
            case true =>
              updateDataStrict[ResponsiblePeople] { responsiblePeople: Seq[ResponsiblePeople] =>
                responsiblePeople.map(_.hasAlreadyPassedFitAndProper(Some(true)).copy(hasAccepted = true))
              } map { _ => Redirect(NewServiceInformationController.get()) }
            case false => Future.successful(Redirect(WhichFitAndProperController.get()))
          }
          case f: InvalidForm => Future.successful(BadRequest(fit_and_proper(f, config.showFeesToggle)))
        }
      }
  }

  private def filterRequest(fn: Future[Result])
                           (implicit hc: HeaderCarrier, ac: AuthContext, ec: ExecutionContext, request: Request[_]): Future[Result] = {
    (businessMatchingService.getModel flatMap { bm =>
      OptionT.fromOption[Future](bm.activities)
    } flatMap { ba =>
      OptionT.liftF(statusService.isPreSubmission flatMap {
        case false if ba.businessActivities.contains(MoneyServiceBusiness) | ba.businessActivities.contains(TrustAndCompanyServices) => fn
        case _ => Future.successful(NotFound(notFoundView))
      })
    }) getOrElse InternalServerError("Cannot retrieve activities")
  }

}