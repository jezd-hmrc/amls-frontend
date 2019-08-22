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

package controllers.businessmatching.updateservice.remove

import cats.data.OptionT
import cats.implicits._
import connectors.DataCacheConnector
import controllers.DefaultBaseController
import javax.inject.{Inject, Singleton}
import models.businessmatching.{BusinessActivity, BusinessMatching}
import uk.gov.hmrc.http.HeaderCarrier
import utils.AuthAction
import views.html.businessmatching.updateservice.remove.unable_to_remove_activity

import scala.concurrent.Future

@Singleton
class UnableToRemoveBusinessTypesController @Inject()(authAction: AuthAction,
                                                      val dataCacheConnector: DataCacheConnector
                                                     ) extends DefaultBaseController {

  def get = authAction.async {
      implicit request =>
      getBusinessActivity(request.credId) map {
        case activity => Ok(unable_to_remove_activity(activity.getMessage(true)))
      } getOrElse (InternalServerError("Get: Unable to show Unable to Remove Activities page"))
  }

  private def getBusinessActivity(credId: String)(implicit hc: HeaderCarrier): OptionT[Future, BusinessActivity] = for {
    model <- OptionT(dataCacheConnector.fetch[BusinessMatching](credId, BusinessMatching.key))
    activities <- OptionT.fromOption[Future](model.activities)
  } yield activities.businessActivities.head

}
