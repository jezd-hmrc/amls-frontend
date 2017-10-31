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

package views.responsiblepeople

import forms.{EmptyForm, InvalidForm}
import jto.validation.{Path, ValidationError}
import org.scalatest.MustMatchers
import play.api.i18n.Messages
import utils.GenericTestHelper
import views.Fixture

class legal_name_change_dateSpec extends GenericTestHelper with MustMatchers {

  trait ViewFixture extends Fixture {
    implicit val requestWithToken = addToken(request)
  }

  "legal_name_change_date view" must {
    "have correct title, headings and form fields" in new ViewFixture {
      val form2 = EmptyForm

      def view = views.html.responsiblepeople.legal_name_change_date(form2, true, 1, None)

      doc.title must startWith(Messages("responsiblepeople.legalnamechangedate.title"))
      heading.html must be(Messages("responsiblepeople.legalnamechangedate.heading"))
      subHeading.html must include(Messages("summary.responsiblepeople"))

      doc.getElementsByAttributeValue("name", "previous.date.day") must not be empty
      doc.getElementsByAttributeValue("name", "previous.date.month") must not be empty
      doc.getElementsByAttributeValue("name", "previous.date.year") must not be empty

    }
    "show errors in the correct locations" in new ViewFixture {
      val form2: InvalidForm = InvalidForm(Map.empty,
        Seq(
          (Path \ "previous.date.day") -> Seq(ValidationError("not a message Key")),
          (Path \ "previous.date.month") -> Seq(ValidationError("second not a message Key")),
          (Path \ "previous.date.year") -> Seq(ValidationError("third not a message Key"))
        ))

      def view = views.html.responsiblepeople.legal_name_change_date(form2, true, 1, None)

      errorSummary.html() must include("not a message Key")
      errorSummary.html() must include("second not a message Key")
      errorSummary.html() must include("third not a message Key")
    }
  }
}