/*
 * Copyright 2017 HM Revenue & Customs
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

package controllers.tcsp

import cats.implicits._
import cats.data.OptionT
import config.AMLSAuthConnector
import connectors.DataCacheConnector
import controllers.BaseController
import models.tcsp.Tcsp
import uk.gov.hmrc.play.frontend.auth.AuthContext
import views.html.tcsp.summary
import uk.gov.hmrc.http.HeaderCarrier

trait SummaryController extends BaseController {

  protected def dataCache: DataCacheConnector

  def get = Authorised.async {
    implicit authContext =>
      implicit request =>
        fetchModel map {
          case Some(data) => Ok(summary(data))
          case _ => Redirect(controllers.routes.RegistrationProgressController.get())
        }
  }

  def post = Authorised.async {
    implicit authContext =>
      implicit request =>
        (for {
          model <- OptionT(fetchModel)
          _ <- OptionT.liftF(dataCache.save[Tcsp](Tcsp.key, model.copy(hasAccepted = true)))
        } yield {
          Redirect(controllers.routes.RegistrationProgressController.get())
        }) getOrElse InternalServerError("Cannot update Tcsp")
  }

  private def fetchModel(implicit authContext: AuthContext, hc: HeaderCarrier) = dataCache.fetch[Tcsp](Tcsp.key)

}

object SummaryController extends SummaryController {
  // $COVERAGE-OFF$
  override val dataCache = DataCacheConnector
  override val authConnector = AMLSAuthConnector
}
