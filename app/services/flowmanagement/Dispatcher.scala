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

package services.flowmanagement

import models.flowmanagement._
import play.api.mvc.Result
import play.libs.F
import services.flowmanagement.routings.{VariationAddServiceRouting, VariationRemoveServiceRouting}

import scala.concurrent.java8.FuturesConvertersImpl.P
import scala.reflect.internal.annotations

trait Route[A] {
  def go(a: A, pageId: PageId): Result
}

object Route {

  implicit val stringRoute: Route[UpdateServiceFlowModel] =
    new Route[UpdateServiceFlowModel] {
      override def go(a: UpdateServiceFlowModel): Result =
        ???
    }
}


class Dispatcher() {
  def getRoute[T](pageId: PageId, model: T, flow: Flow)(implicit route: Route[T]): Result = flow match {
    case VariationAddServiceFlow => route.go(model, pageId)//VariationAddServiceRouting.getRoute(pageId, model)
    case VariationRemoveServiceFlow => VariationRemoveServiceRouting.getRoute(pageId.asInstanceOf[RemoveServiceFlowPageId], model.asInstanceOf[UpdateServiceFlowModel])
  }
}
