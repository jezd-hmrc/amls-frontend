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

package controllers.businessmatching.updateservice.remove

import cats.data.OptionT
import cats.implicits._
import connectors.DataCacheConnector
import controllers.BaseController
import forms.{EmptyForm, Form2, InvalidForm, ValidForm}
import javax.inject.{Inject, Singleton}
import jto.validation.forms.UrlFormEncoded
import jto.validation.{Path, Rule, RuleLike}
import models.FormTypes
import models.businessmatching.{BusinessActivities, BusinessActivity}
import services.StatusService
import services.businessmatching.BusinessMatchingService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

@Singleton
class RemoveActivitiesController @Inject()(
                                          val authConnector: AuthConnector,
                                          val businessMatchingService: BusinessMatchingService,
                                          val statusService: StatusService,
                                          val dataCacheConnector: DataCacheConnector
                                          ) extends BaseController {

  implicit def formReads(implicit p: Path => RuleLike[UrlFormEncoded, Set[BusinessActivity]]): Rule[UrlFormEncoded, BusinessActivities] =
    FormTypes.businessActivityRule("error.required.bm.remove.service")

  def get = Authorised.async{
    implicit authContext =>
      implicit request =>
        statusService.isPreSubmission flatMap {
          case false => OptionT(getActivities) map { activities =>
            Ok(views.html.businessmatching.updateservice.remove.remove_activities(EmptyForm, activities))
          } getOrElse InternalServerError("Could not retrieve activities")
          case true => Future.successful(NotFound(notFoundView))
        }
  }

  def post = Authorised.async{
    implicit authContext =>
      implicit request =>
        import jto.validation.forms.Rules._
        OptionT(getActivities) map { activities =>
          Form2[BusinessActivities](request.body) match {
            case ValidForm(_, data) =>
              if (data.businessActivities.size < activities.size) {
                Redirect(controllers.businessmatching.updateservice.remove.routes.UpdateServiceDateOfChangeController.get(data.businessActivities map BusinessActivities.getValue mkString "/"))
              } else {
                Redirect(controllers.businessmatching.updateservice.remove.routes.RemoveActivitiesInformationController.get())
              }
            case f: InvalidForm => BadRequest(views.html.businessmatching.updateservice.remove.remove_activities(f, activities))
          }
        } getOrElse InternalServerError("Could not retrieve activities")
  }

  private def getActivities(implicit hc: HeaderCarrier, ac: AuthContext) = businessMatchingService.getModel.value map { bm =>
    for {
      businessMatching <- bm
      businessActivities <- businessMatching.activities
    } yield businessActivities.businessActivities map BusinessActivities.getValue
  }

}