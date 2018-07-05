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

package connectors

import config.{AppConfig, ApplicationConfig, WSHttp}
import javax.inject.Inject
import uk.gov.hmrc.http.{HeaderCarrier, HttpDelete, HttpGet}
import uk.gov.hmrc.play.frontend.auth.AuthContext

import scala.concurrent.ExecutionContext

class TestOnlyStubConnector @Inject()(http: HttpDelete) {
  lazy val baseUrl = ApplicationConfig.testOnlyStubsUrl

  def clearState()(implicit hc: HeaderCarrier, ac: AuthContext, ex: ExecutionContext) = {
    val requestUrl = s"$baseUrl/clearstate"
    http.DELETE(requestUrl)
  }
}