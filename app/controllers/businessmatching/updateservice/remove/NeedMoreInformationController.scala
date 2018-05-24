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
import javax.inject.{Inject, Singleton}
import models.businessmatching.BusinessActivity
import models.flowmanagement.{NeedToUpdatePageId, RemoveBusinessTypeFlowModel}
import services.flowmanagement.Router
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import views.html.businessmatching.updateservice.remove.need_more_information

import scala.concurrent.Future

@Singleton
class NeedMoreInformationController @Inject()(val authConnector: AuthConnector,
                                              implicit val dataCacheConnector: DataCacheConnector,
                                              val router: Router[RemoveBusinessTypeFlowModel]
                                               ) extends BaseController {

  def get() = Authorised.async {
    implicit authContext =>
      implicit request =>
        (for {
          model <- OptionT(dataCacheConnector.fetch[RemoveBusinessTypeFlowModel](RemoveBusinessTypeFlowModel.key))
          activities <- OptionT.fromOption[Future](model.activitiesToRemove) orElse OptionT.some[Future, Set[BusinessActivity]](Set.empty)
        } yield {
          val activityNames = activities map { _.getMessage() }
          Ok(need_more_information(activityNames))
         })getOrElse(InternalServerError("Cannot retrieve information from cache"))
  }

  def post() = Authorised.async {
    implicit authContext =>
      implicit request =>
        (for {
            route <- OptionT.liftF(router.getRoute(NeedToUpdatePageId, new RemoveBusinessTypeFlowModel()))
        } yield route) getOrElse InternalServerError("Post: Cannot retrieve data: Remove : NewServiceInformationController")
  }
}