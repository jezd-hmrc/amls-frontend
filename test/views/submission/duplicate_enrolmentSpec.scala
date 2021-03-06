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

package views.submission

import org.scalatest.MustMatchers
import play.api.i18n.Messages
import utils.AmlsViewSpec
import views.Fixture
import views.html.submission.duplicate_enrolment

class duplicate_enrolmentSpec extends AmlsViewSpec with MustMatchers {

  trait ViewFixture extends Fixture {
    lazy val duplicate_enrolment = app.injector.instanceOf[duplicate_enrolment]
    implicit val requestWithToken = addTokenForView()

    def view = duplicate_enrolment()
  }

  "The duplicate enrolment view" must {
    "display the correct title and headings" in new ViewFixture {

      doc.title mustBe s"${Messages("error.submission.problem.title")} - ${Messages("title.amls")} - ${Messages("title.gov")}"
      doc.select("h1").text() mustBe Messages("error.submission.problem.title")

      validateParagraphizedContent("error.submission.duplicate_enrolment.content")
    }
  }

}
