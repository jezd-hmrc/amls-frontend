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

package modules

import com.google.inject.AbstractModule
import config.{AMLSAuditConnector, WSHttp}
import connectors._
import services._
import uk.gov.hmrc.http.HttpPost
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

class Module extends AbstractModule {

  type HmrcAuthConnector = uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

  override def configure() = {
    bind(classOf[HttpPost]).toInstance(WSHttp)
    bind(classOf[WSHttp]).toInstance(WSHttp)
    bind(classOf[KeystoreConnector]).toInstance(KeystoreConnector)
    bind(classOf[DataCacheConnector]).toInstance(DataCacheConnector)
    bind(classOf[HmrcAuthConnector]).to(classOf[config.FrontendAuthConnector])
    bind(classOf[AmlsNotificationConnector]).toInstance(AmlsNotificationConnector)
    bind(classOf[StatusService]).toInstance(StatusService)
    bind(classOf[AmlsConnector]).toInstance(AmlsConnector)
    bind(classOf[AuditConnector]).toInstance(AMLSAuditConnector)
    bind(classOf[GovernmentGatewayService]).toInstance(GovernmentGatewayService)
    bind(classOf[FeeConnector]).toInstance(FeeConnector)
    bind(classOf[LandingService]).toInstance(LandingService)
  }
}
