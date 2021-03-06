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
import utils.AmlsViewSpec
import views.Fixture
import views.html.responsiblepeople.person_non_uk_passport

class person_non_uk_passportSpec extends AmlsViewSpec with MustMatchers {

  trait ViewFixture extends Fixture {
    lazy val person_non_uk_passport = app.injector.instanceOf[person_non_uk_passport]
    implicit val requestWithToken = addTokenForView()
  }

  "person_uk_passport view" must {
    "have correct title, headings and form fields" in new ViewFixture {
      val form2 = EmptyForm

      val name = "firstName lastName"

      def view = person_non_uk_passport(form2, true, 1, None, name)

      doc.getElementsByAttributeValue("class", "link-back") must not be empty

      doc.title must startWith(Messages("responsiblepeople.non.uk.passport.title"))
      heading.html must be(Messages("responsiblepeople.non.uk.passport.heading", name))
      subHeading.html must include(Messages("summary.responsiblepeople"))

      doc.getElementsByAttributeValue("name", "nonUKPassport") must not be empty
      doc.getElementsByAttributeValue("name", "nonUKPassportNumber") must not be empty

    }
    "show errors in the correct locations" in new ViewFixture {
      val form2: InvalidForm = InvalidForm(Map.empty,
        Seq(
          (Path \ "nonUKPassport") -> Seq(ValidationError("not a message Key")),
          (Path \ "nonUKPassportNumber") -> Seq(ValidationError("second not a message Key"))
        ))

      def view = person_non_uk_passport(form2, true, 1, None, "firstName lastName")

      errorSummary.html() must include("not a message Key")
      errorSummary.html() must include("second not a message Key")

      doc.getElementsByAttributeValue("name", "nonUKPassport") must not be empty
      doc.getElementsByAttributeValue("name", "nonUKPassportNumber") must not be empty

    }
  }

}
