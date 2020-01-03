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

package views.supervision

import forms.EmptyForm
import org.scalatest.MustMatchers
import utils.AmlsViewSpec
import views.Fixture


class penalised_by_professionalSpec extends AmlsViewSpec with MustMatchers  {

  trait ViewFixture extends Fixture {
    implicit val requestWithToken = addTokenForView()
  }

  "what_you_need view" must {

    "have a back link" in new ViewFixture {

      def view = views.html.supervision.penalised_by_professional(EmptyForm, edit = false)

      doc.getElementsByAttributeValue("class", "link-back") must not be empty
    }
  }
}
