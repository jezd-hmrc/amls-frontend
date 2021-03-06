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

package views.msb

import forms.{Form2, InvalidForm, ValidForm}
import jto.validation.{Path, ValidationError}
import models.moneyservicebusiness.ExpectedThroughput
import org.scalatest.MustMatchers
import play.api.i18n.Messages
import utils.AmlsViewSpec
import views.Fixture
import views.html.msb.expected_throughput


class expected_throughputSpec extends AmlsViewSpec with MustMatchers {

  trait ViewFixture extends Fixture {
    lazy val expected_throughput = app.injector.instanceOf[expected_throughput]
    implicit val requestWithToken = addTokenForView()
  }

  "expected_throughput view" must {

    "have the back link button" in new ViewFixture {
      val form2: ValidForm[ExpectedThroughput] = Form2(ExpectedThroughput.First)
      def view = expected_throughput(form2, true)
      doc.getElementsByAttributeValue("class", "link-back") must not be empty
    }

    "have correct title" in new ViewFixture {

      val form2: ValidForm[ExpectedThroughput] = Form2(ExpectedThroughput.First)

      def view = expected_throughput(form2, true)

      doc.title must be(Messages("msb.throughput.title") +
        " - " + Messages("summary.msb") +
        " - " + Messages("title.amls") +
        " - " + Messages("title.gov"))
    }

    "have correct headings" in new ViewFixture {

      val form2: ValidForm[ExpectedThroughput] = Form2(ExpectedThroughput.First)

      def view = expected_throughput(form2, true)

      heading.html must be(Messages("msb.throughput.title"))
      subHeading.html must include(Messages("summary.msb"))

    }

    "show errors in the correct locations" in new ViewFixture {

      val form2: InvalidForm = InvalidForm(Map.empty,
        Seq(
          (Path \ "throughput") -> Seq(ValidationError("not a message Key"))
        ))

      def view = expected_throughput(form2, true)

      errorSummary.html() must include("not a message Key")

      doc.getElementById("throughput")
        .getElementsByClass("error-notification").first().html() must include("not a message Key")

    }
  }
}