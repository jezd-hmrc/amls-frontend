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
import connectors.DataCacheConnector
import controllers.BaseController
import controllers.businessmatching.updateservice.AddBusinessTypeHelper
import forms.EmptyForm
import javax.inject.{Inject, Singleton}
import models.flowmanagement.{AddBusinessTypeFlowModel, NoPSRPageId}
import services.flowmanagement.Router
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import views.html.businessmatching.updateservice.add.cannot_add_services

import scala.concurrent.Future

@Singleton
class NoPsrController @Inject()(
                                 val authConnector: AuthConnector,
                                 implicit val dataCacheConnector: DataCacheConnector,
                                 val helper: AddBusinessTypeHelper,
                                 val router: Router[AddBusinessTypeFlowModel]
                               ) extends BaseController {

  def get = Authorised.async {
    implicit authContext =>
      implicit request =>
        Future.successful(Ok(cannot_add_services(EmptyForm)))
  }

  def post() = Authorised.async {
    implicit authContext =>
      implicit request =>
        (for {
          _ <- helper.clearFlowModel()
          route <- OptionT.liftF(router.getRoute(NoPSRPageId, AddBusinessTypeFlowModel()))
        } yield route) getOrElse InternalServerError("Post: Cannot retrieve data: NoPsrController")
  }
}