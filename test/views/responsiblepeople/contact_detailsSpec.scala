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

import forms.{Form2, InvalidForm, ValidForm}
import models.responsiblepeople.ContactDetails
import org.scalatest.MustMatchers
import utils.AmlsViewSpec
import jto.validation.Path
import jto.validation.ValidationError
import play.api.i18n.Messages
import views.Fixture
import views.html.responsiblepeople.contact_details

class contact_detailsSpec extends AmlsViewSpec with MustMatchers {

  trait ViewFixture extends Fixture {
    lazy val contact_details = app.injector.instanceOf[contact_details]
    implicit val requestWithToken = addTokenForView()
  }

  "contact_details view" must {

    "have a back link" in new ViewFixture {
      val form2: ValidForm[ContactDetails] = Form2(ContactDetails("0987654", "email.com"))
      def view = contact_details(form2, true, 1, None, "firstName lastName")
      doc.getElementsByAttributeValue("class", "link-back") must not be empty
    }

    "have correct title" in new ViewFixture {

      val form2: ValidForm[ContactDetails] = Form2(ContactDetails("0987654", "email.com"))

      def view = contact_details(form2, true, 1, None, "firstName lastName")

      doc.title must startWith(Messages("responsiblepeople.contact_details.title"))
    }

    "have correct headings" in new ViewFixture {

      val form2: ValidForm[ContactDetails] = Form2(ContactDetails("0987654", "email.com"))

      def view = contact_details(form2, true, 1, None, "firstName lastName")

      heading.html must be(Messages("responsiblepeople.contact_details.heading", "firstName lastName"))
      subHeading.html must include(Messages("summary.responsiblepeople"))

    }

    "show errors in the correct locations" in new ViewFixture {

      val form2: InvalidForm = InvalidForm(Map.empty,
        Seq(
          (Path \ "phoneNumber") -> Seq(ValidationError("not a message Key")),
          (Path \ "emailAddress") -> Seq(ValidationError("second not a message Key"))
        ))

      def view = contact_details(form2, true, 1, None, "firstName lastName")

      errorSummary.html() must include("not a message Key")
      errorSummary.html() must include("second not a message Key")

      doc.getElementById("phoneNumber").parent()
        .getElementsByClass("error-notification").first().html() must include("not a message Key")

      doc.getElementById("emailAddress").parent()
        .getElementsByClass("error-notification").first().html() must include("second not a message Key")

    }
    "show the person name in intro text" in new ViewFixture {
      val form2: InvalidForm = InvalidForm(Map.empty,
        Seq(
          (Path \ "phoneNumber") -> Seq(ValidationError("not a message Key")),
          (Path \ "emailAddress") -> Seq(ValidationError("second not a message Key"))
        ))


      def view = contact_details(form2, true, 1, None, "firstName lastName")

      doc.body().text() must include(Messages("responsiblepeople.contact_details.lbl","firstName lastName"))

    }
  }
}