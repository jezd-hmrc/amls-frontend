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

package audit

import models.SatisfactionSurvey
import models.governmentgateway.EnrolmentRequest
import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.play.audit.model.DataEvent
import uk.gov.hmrc.play.audit.AuditExtensions._
import uk.gov.hmrc.http.HeaderCarrier
import utils.AuditHelper

object SurveyEvent {
  def apply
  (survey: SatisfactionSurvey)
  (implicit
   hc: HeaderCarrier,
   reqW: Writes[EnrolmentRequest]
  ): DataEvent =
    DataEvent(
      auditSource = AuditHelper.appName,
      auditType = "SurveyCompleted",
      tags = hc.toAuditTags("SatisfactionSurvey", "N/A"),
      detail = hc.toAuditDetails() ++ Map(
        "survey" -> Json.toJson(survey).toString
      )
    )
}
