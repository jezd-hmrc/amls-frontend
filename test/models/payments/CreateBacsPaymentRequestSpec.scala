/*
 * Copyright 2020 HM Revenue & Customs
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

package models.payments

import generators.PaymentGenerator
import org.scalatest.MustMatchers
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsSuccess, Json}

class CreateBacsPaymentRequestSpec extends PlaySpec with MustMatchers with PaymentGenerator {

  "CreateBacsPaymentRequest" must {

    val model = createBacsPaymentGen.sample.get

    val json = Json.obj(
      "amlsReference" -> model.amlsReference,
      "paymentReference" -> model.paymentReference,
      "safeId" -> model.safeId,
      "amountInPence" -> model.amountInPence
    )

    "serialize to the correct JSON format" in {
      Json.toJson(model) mustBe json
    }

    "deserialize from json" in {
      Json.fromJson[CreateBacsPaymentRequest](json) mustBe JsSuccess(model)
    }

  }

}
