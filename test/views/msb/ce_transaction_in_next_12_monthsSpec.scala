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
import models.moneyservicebusiness.CETransactionsInNext12Months
import org.scalatest.MustMatchers
import play.api.i18n.Messages
import utils.AmlsViewSpec
import views.Fixture
import views.html.msb.ce_transaction_in_next_12_months


class ce_transaction_in_next_12_monthsSpec extends AmlsViewSpec with MustMatchers {

  trait ViewFixture extends Fixture {
    lazy val ce_transaction_in_next_12_months = app.injector.instanceOf[ce_transaction_in_next_12_months]
    implicit val requestWithToken = addTokenForView()
  }

  "ce_transaction_in_next_12_months view" must {

    "have the back link button" in new ViewFixture {
      val form2: ValidForm[CETransactionsInNext12Months] = Form2(CETransactionsInNext12Months("1"))
      def view = ce_transaction_in_next_12_months(form2, true)
      doc.getElementsByAttributeValue("class", "link-back") must not be empty
    }

    "have correct title" in new ViewFixture {

      val form2: ValidForm[CETransactionsInNext12Months] = Form2(CETransactionsInNext12Months("1"))

      def view = ce_transaction_in_next_12_months(form2, true)

      doc.title must be(Messages("msb.ce.transactions.expected.in.12.months.title") +
        " - " + Messages("summary.msb") +
        " - " + Messages("title.amls") +
        " - " + Messages("title.gov"))
    }

    "have correct headings" in new ViewFixture {

      val form2: ValidForm[CETransactionsInNext12Months] = Form2(CETransactionsInNext12Months("1"))

      def view = ce_transaction_in_next_12_months(form2, true)

      heading.html must include(Messages("msb.ce.transactions.expected.in.12.months.title"))
      subHeading.html must include(Messages("summary.msb"))

    }

    "show errors in the correct locations" in new ViewFixture {

      val form2: InvalidForm = InvalidForm(Map.empty,
        Seq(
          (Path \ "ceTransaction") -> Seq(ValidationError("not a message Key"))
        ))

      def view = ce_transaction_in_next_12_months(form2, true)

      errorSummary.html() must include("not a message Key")

      doc.getElementById("ceTransaction").parent()
        .getElementsByClass("error-notification").first().html() must include("not a message Key")

    }
  }
}