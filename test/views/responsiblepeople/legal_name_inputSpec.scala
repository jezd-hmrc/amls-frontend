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

package views.responsiblepeople

import forms.{EmptyForm, InvalidForm}
import jto.validation.{Path, ValidationError}
import org.scalatest.MustMatchers
import play.api.i18n.Messages
import utils.{AmlsSpec, AmlsViewSpec}
import views.Fixture
import views.html.responsiblepeople.legal_name_input

class legal_name_inputSpec extends AmlsViewSpec with MustMatchers {

  trait ViewFixture extends Fixture {
    lazy val legal_name_input = app.injector.instanceOf[legal_name_input]
    implicit val requestWithToken = addTokenForView()

    val name = "firstName lastName"
  }

  "legal_name view" must {
    "have correct title, headings and form fields" in new ViewFixture {
      val form2 = EmptyForm

      def view = legal_name_input(form2, true, 1, None, name)

      doc.getElementsByAttributeValue("class", "link-back") must not be empty

      doc.title must startWith(Messages("responsiblepeople.legalNameInput.title"))
      heading.html must be(Messages("responsiblepeople.legalNameInput.heading", name))
      subHeading.html must include(Messages("summary.responsiblepeople"))
      doc.html must include(Messages("responsiblepeople.legalnamechangedate.hint"))

      doc.getElementsByAttributeValue("name", "firstName") must not be empty
      doc.getElementsByAttributeValue("name", "middleName") must not be empty
      doc.getElementsByAttributeValue("name", "lastName") must not be empty
    }
    "show errors in the correct locations" in new ViewFixture {
      val form2: InvalidForm = InvalidForm(Map.empty,
        Seq(
          (Path \ "firstName") -> Seq(ValidationError("second not a message Key")),
          (Path \ "middleName") -> Seq(ValidationError("third not a message Key")),
          (Path \ "lastName") -> Seq(ValidationError("fourth not a message Key"))
        ))

      def view = legal_name_input(form2, true, 1, None, name)

      errorSummary.html() must include("second not a message Key")
      errorSummary.html() must include("third not a message Key")
      errorSummary.html() must include("fourth not a message Key")
    }
  }
}
