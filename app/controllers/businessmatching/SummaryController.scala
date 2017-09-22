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

package controllers.businessmatching

import config.{AMLSAuthConnector, ApplicationConfig}
import connectors.DataCacheConnector
import controllers.BaseController
import models.businessmatching.BusinessMatching
import views.html.businessmatching._
import _root_.services.StatusService
import cats.implicits._
import cats.data.OptionT
import models.status.{NotCompleted, SubmissionReady, SubmissionReadyForReview, SubmissionStatus}

trait SummaryController extends BaseController {

  protected def dataCache: DataCacheConnector

  protected def statusService: StatusService

  def get() = Authorised.async {
    implicit authContext => implicit request =>
        def isPreApprovedStatus(status: SubmissionStatus) = Set(NotCompleted, SubmissionReady).contains(status)

        val okResult = for {
          bm <- OptionT(dataCache.fetch[BusinessMatching](BusinessMatching.key))
          status <- OptionT.liftF(statusService.getStatus)
        } yield Ok(summary(bm, isPreApprovedStatus(status) || ApplicationConfig.businessMatchingVariationToggle))

        okResult getOrElse Redirect(controllers.routes.RegistrationProgressController.get())
  }

  def post() = Authorised.async {
    implicit authContext => implicit request =>
      val result = for {
        bm <- OptionT(dataCache.fetch[BusinessMatching](BusinessMatching.key))
        _ <- OptionT.liftF(dataCache.save[BusinessMatching](BusinessMatching.key, bm.copy(hasAccepted = true)))
      } yield Redirect(controllers.routes.RegistrationProgressController.get())

      result getOrElse InternalServerError("Unable to update business matching")
  }
}

object SummaryController extends SummaryController {
  // $COVERAGE-OFF$
  override val dataCache = DataCacheConnector
  override val authConnector = AMLSAuthConnector
  override val statusService = StatusService
}
