/*
 * Copyright 2019 HM Revenue & Customs
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

package controllers.asp

import controllers.{AmlsBaseController, CommonPlayDependencies}
import javax.inject.Inject
import utils.AuthAction
import views.html.asp._

import scala.concurrent.Future

class WhatYouNeedController @Inject()(authAction: AuthAction, val ds: CommonPlayDependencies
                                     ) extends AmlsBaseController(ds) {
  def get = authAction.async {
      implicit request =>
        Future.successful(Ok(what_you_need()))
  }
}
