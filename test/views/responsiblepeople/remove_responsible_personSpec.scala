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
import models.responsiblepeople.{ExperienceTraining, ExperienceTrainingYes}
import org.scalatest.MustMatchers
import utils.AmlsViewSpec
import jto.validation.Path
import jto.validation.ValidationError
import play.api.i18n.Messages
import views.Fixture
import views.html.responsiblepeople.remove_responsible_person

class remove_responsible_personSpec extends AmlsViewSpec with MustMatchers  {

  trait ViewFixture extends Fixture {
    lazy val remove_responsible_person = app.injector.instanceOf[remove_responsible_person]
    implicit val requestWithToken = addTokenForView()
  }

  "remove_responsible_person view" must {

    "have a back link" in new ViewFixture {
      val form2: ValidForm[ExperienceTraining] = Form2(ExperienceTrainingYes("info"))

      def view = remove_responsible_person(form2, 1, "Gary", false)
      doc.getElementsByAttributeValue("class", "link-back") must not be empty
    }

    "have correct title" in new ViewFixture {

      val form2: ValidForm[ExperienceTraining] = Form2(ExperienceTrainingYes("info"))

      def view = remove_responsible_person(form2, 1, "Gary", false)

      doc.title() must startWith(Messages("responsiblepeople.remove.responsible.person.title") + " - " + Messages("summary.responsiblepeople"))

    }

    "show none named person heading" in new ViewFixture {

      val form2: ValidForm[ExperienceTraining] = Form2(ExperienceTrainingYes("info"))

      def view = remove_responsible_person(form2, 1, "Gary", showDateField = false)

      heading.html() must be(Messages("responsiblepeople.remove.responsible.person.title"))
    }

    "show named person heading" in new ViewFixture {

      val form2: ValidForm[ExperienceTraining] = Form2(ExperienceTrainingYes("info"))

      def view = remove_responsible_person(form2, 1, "Gary", showDateField = true)

      heading.html() must be(Messages("responsiblepeople.remove.named.responsible.person", "Gary"))
      doc.getElementsByAttributeValue("id", "endDate") must not be empty
    }

    "show errors in correct places when validation fails" in new ViewFixture {

      val messageKey1 = "definitely not a message key"
      val endDateField = "endDate"

      val form2: InvalidForm = InvalidForm(
        Map("thing" -> Seq("thing")),
        Seq((Path \ endDateField, Seq(ValidationError(messageKey1))))
      )

      def view = remove_responsible_person(form2, 1, "Gary", true)

      errorSummary.html() must include(messageKey1)

      doc.getElementById(endDateField).html() must include(messageKey1)
    }
  }
}
