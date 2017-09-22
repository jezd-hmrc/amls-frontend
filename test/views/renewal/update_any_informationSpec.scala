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

package views.renewal

import forms.{EmptyForm, Form2, InvalidForm}
import jto.validation.{Path, ValidationError}
import models.renewal.UpdateAnyInformation
import org.scalatest.MustMatchers
import play.api.i18n.Messages
import utils.GenericTestHelper
import views.Fixture

class update_any_informationSpec extends GenericTestHelper with MustMatchers  {

  trait ViewFixture extends Fixture {
    implicit val requestWithToken = addToken(request)
  }

  "vupdate_any_information view" must {
    "have correct title" in new ViewFixture {

      val form2: Form2[UpdateAnyInformation] = EmptyForm

      def view = views.html.renewal.update_any_information(form2)

      doc.title must startWith(Messages("renewal.updateanyinformation.title") + " - " + Messages("summary.renewal"))
    }

    "have correct headings" in new ViewFixture {

      val form2: Form2[UpdateAnyInformation] = EmptyForm

      def view = views.html.renewal.update_any_information(form2)

      heading.html must be(Messages("renewal.updateanyinformation.title"))
      subHeading.html must include(Messages("summary.renewal"))

    }

    "show errors in the correct locations" in new ViewFixture {

      val form2 = InvalidForm(Map.empty,
        Seq(
          (Path \ "updateAnyInformation") -> Seq(ValidationError("not a message Key"))
        ))

      def view = views.html.renewal.update_any_information(form2)

      errorSummary.html() must include("not a message Key")

      doc.getElementById("updateAnyInformation")
        .getElementsByClass("error-notification").first().html() must include("not a message Key")
    }

  }
}
