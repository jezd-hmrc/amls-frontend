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

package controllers.supervision

import cats.data.OptionT
import cats.implicits._
import config.AMLSAuthConnector
import connectors.DataCacheConnector
import controllers.BaseController
import forms.EmptyForm
import models.supervision.Supervision
import views.html.supervision.summary

trait SummaryController extends BaseController {

  protected def dataCache: DataCacheConnector

  def get() = Authorised.async {
    implicit authContext => implicit request =>
      dataCache.fetch[Supervision](Supervision.key) map {
        case Some(data) =>
          Ok(summary(EmptyForm, data))
        case _ =>
          Redirect(controllers.routes.RegistrationProgressController.get())
      }
  }

  def post = Authorised.async {
    implicit authContext => implicit request => (for {
      supervision <- OptionT(dataCache.fetch[Supervision](Supervision.key))
      _ <- OptionT.liftF(dataCache.save[Supervision](Supervision.key, supervision.copy(hasAccepted = true)))
    } yield Redirect(controllers.routes.RegistrationProgressController.get)) getOrElse InternalServerError("Could not update supervision")
  }
}

object SummaryController extends SummaryController {
  // $COVERAGE-OFF$
  override val dataCache = DataCacheConnector
  override val authConnector = AMLSAuthConnector
}
