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

package models.changeofficer

import cats.data.Validated.{Invalid, Valid}
import jto.validation.{ValidationError, Path}
import org.scalatest.MustMatchers
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json


class NewOfficerSpec extends PlaySpec with MustMatchers {

  "NewOfficer model" must {
    "successfully convert json in a round trip" in {

      val model = NewOfficer("testName")

      Json.toJson(model).as[NewOfficer] mustBe model

    }

    "successfully write the form" in {
      val model = NewOfficer("testName")

      val form = Map(
        "person" -> Seq("testName")
      )

      NewOfficer.formWrites.writes(model) mustBe form
    }

    "successfully read the form" in {
      val form = Map(
        "person" -> Seq("testName")
      )

      val model = NewOfficer("testName")

      NewOfficer.formReads.validate(form) mustBe Valid(model)
    }

    "fail validation when no option is selected" in {
      NewOfficer.formReads.validate(Map.empty) mustBe Invalid(Seq(
        Path \ "person" -> Seq(ValidationError("changeofficer.newnominatedofficer.validationerror"))
      ))
    }
  }
}