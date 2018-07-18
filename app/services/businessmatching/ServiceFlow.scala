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

package services.businessmatching

import javax.inject.Inject

import cats.data.OptionT
import models.businessmatching.BusinessActivity
import uk.gov.hmrc.http.HeaderCarrier
import models.businessmatching._
import cats.implicits._
import connectors.DataCacheConnector

import models.hvd.Hvd
import models.tcsp.Tcsp
import models.estateagentbusiness.EstateAgentBusiness
import models.asp.Asp
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.HeaderCarrier
import models.moneyservicebusiness.{MoneyServiceBusiness => MSBModel}
import uk.gov.hmrc.play.frontend.auth.AuthContext
import scala.concurrent.{ExecutionContext, Future}
import models.businessmatching.updateservice.UpdateService

case class NextService(url: String, activity: BusinessActivity)

class ServiceFlow @Inject()(businessMatchingService: BusinessMatchingService, cacheConnector: DataCacheConnector) {

  private val activityToUrl = Map[BusinessActivity, String](
    MoneyServiceBusiness -> controllers.msb.routes.WhatYouNeedController.get().url,
    HighValueDealing -> controllers.hvd.routes.WhatYouNeedController.get().url,
    TrustAndCompanyServices -> controllers.tcsp.routes.WhatYouNeedController.get().url,
    EstateAgentBusinessService -> controllers.estateagentbusiness.routes.WhatYouNeedController.get().url,
    AccountancyServices -> controllers.asp.routes.WhatYouNeedController.get().url
  )

  private val activityToData = Map[BusinessActivity, CacheMap => Boolean](
    MoneyServiceBusiness -> { c =>

      val (tm, cx) = c.getEntry[BusinessMatching](BusinessMatching.key) map { b =>
        b.msbServices.fold((false, false))(s => (s.msbServices.contains(TransmittingMoney), s.msbServices.contains(CurrencyExchange)))
      } getOrElse (false, false)

      c.getEntry[MSBModel](MSBModel.key).fold(false)(_.isComplete(tm, cx))
    },
    HighValueDealing -> { c => c.getEntry[Hvd](Hvd.key).fold(false)(_.isComplete) },
    TrustAndCompanyServices -> { c => c.getEntry[Tcsp](Tcsp.key).fold(false)(_.isComplete) },
    EstateAgentBusinessService -> { c => c.getEntry[EstateAgentBusiness](EstateAgentBusiness.key).fold(false)(_.isComplete) },
    AccountancyServices -> { c => c.getEntry[Asp](Asp.key).fold(false)(_.isComplete) }
  )

  @Deprecated
  def next(implicit hc: HeaderCarrier, ec: ExecutionContext, ac: AuthContext) = {
    def redirectUrl(activities: Set[BusinessActivity], cacheMap: CacheMap) = OptionT.fromOption[Future](
      activities collectFirst {
        case act if activityToData.contains(act) && !activityToData(act)(cacheMap) => (activityToUrl(act), act)
      }
    )

    for {
      cacheMap <- OptionT(cacheConnector.fetchAll)
      activities <- businessMatchingService.getAdditionalBusinessActivities
      (url, activity) <- redirectUrl(activities, cacheMap)
    } yield NextService(url, activity)
  }

  def isNewActivity(activity: BusinessActivity)(implicit ac: AuthContext, hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] =
    businessMatchingService.getAdditionalBusinessActivities map {_.contains(activity)} getOrElse false

  @Deprecated
  def inNewServiceFlow(activity: BusinessActivity)
                      (implicit ac: AuthContext, hc: HeaderCarrier, ec: ExecutionContext) = Future.successful(false)

  @Deprecated
  def setInServiceFlowFlag(value: Boolean)(implicit ac: AuthContext, hc: HeaderCarrier, ec: ExecutionContext): Future[CacheMap] = {
    val modelToSave: Option[UpdateService] => UpdateService =
      m => m.fold(UpdateService(inNewServiceFlow = value))(_.copy(inNewServiceFlow = value))

    cacheConnector.fetch[UpdateService](UpdateService.key) flatMap { maybeModel =>
      cacheConnector.save(UpdateService.key, modelToSave(maybeModel))
    }
  }
}
