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
import models.businessmatching.{AccountancyServices, BusinessActivities, BusinessMatching, MoneyServiceBusiness}
import models.responsiblepeople.{ExperienceTraining, ExperienceTrainingYes}
import org.scalatest.MustMatchers
import utils.AmlsViewSpec
import jto.validation.Path
import jto.validation.ValidationError
import play.api.i18n.Messages
import views.Fixture
import views.html.responsiblepeople.experience_training

class experience_trainingSpec extends AmlsViewSpec with MustMatchers  {

  trait ViewFixture extends Fixture {
    lazy val experience_training = app.injector.instanceOf[experience_training]
    implicit val requestWithToken = addTokenForView()
    val businessMatching = BusinessMatching(activities = Some(BusinessActivities(Set(AccountancyServices))))
  }

  "experience_training view" must {

    "have a back link" in new ViewFixture {
      val form2: ValidForm[ExperienceTraining] = Form2(ExperienceTrainingYes("info"))
      def view: _root_.play.twirl.api.HtmlFormat.Appendable =
        experience_training(form2, businessMatching, false, 0, None, "FirstName LastName")
      doc.getElementsByAttributeValue("class", "link-back") must not be empty
    }

    "have correct title" in new ViewFixture {

      val businessTypes = Some(List("AccountancyServices"))
      val form2: ValidForm[ExperienceTraining] = Form2(ExperienceTrainingYes("info"))

      def view: _root_.play.twirl.api.HtmlFormat.Appendable =
        experience_training(form2, businessMatching, false, 0, None, "FirstName LastName")

      doc.title must be(Messages("responsiblepeople.experiencetraining.title") +
        " - " + Messages("summary.responsiblepeople") +
        " - " + Messages("title.amls") +
        " - " + Messages("title.gov"))
    }

    "have correct heading" in new ViewFixture {

      val form2: ValidForm[ExperienceTraining] = Form2(ExperienceTrainingYes("info"))

      def view: _root_.play.twirl.api.HtmlFormat.Appendable =
        experience_training(form2, businessMatching, false, 0, None, "FirstName LastName")

      heading.html() must be(Messages("responsiblepeople.experiencetraining.heading", "FirstName LastName", "an accountancy service provider"))
    }

    "show errors in correct places when validation fails" in new ViewFixture {
      val messageKey1 = "definitely not a message key"
      val messageKey2 = "also not a message key"

      val form2: InvalidForm = InvalidForm(Map("thing" -> Seq("thing")),
        Seq((Path \ "experienceTraining", Seq(ValidationError(messageKey1))),
          (Path \ "experienceInformation", Seq(ValidationError(messageKey2)))))

      def view: _root_.play.twirl.api.HtmlFormat.Appendable =
        experience_training(form2, businessMatching, false, 0, None, "FirstName LastName")

      errorSummary.html() must include(messageKey1)
      errorSummary.html() must include(messageKey2)
    }
  }
}
