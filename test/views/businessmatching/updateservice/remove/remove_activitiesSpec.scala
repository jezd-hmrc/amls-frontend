/*
 * Copyright 2018 HM Revenue & Customs
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

package views.businessmatching.updateservice.remove

import forms.EmptyForm
import org.scalatest.MustMatchers
import org.scalatest.mock.MockitoSugar
import utils.GenericTestHelper
import views.Fixture

class remove_activitiesSpec extends GenericTestHelper with MockitoSugar with MustMatchers {

  trait ViewFixture extends Fixture {
    implicit val requestWithToken = addToken(request)
    def view = views.html.businessmatching.updateservice.remove.remove_activities(EmptyForm, Set.empty[String])
  }
//
//  "remove_activities view" must {
//
//    "display the correct headings and title" in new ViewFixture {
//
//      def view = views.html.businessmatching.updateservice.remove.remove_activities(EmptyForm, Set.empty)
//
//      doc.title must include(Messages("updateservice.removeactivities.title"))
//      heading.html must include(Messages("updateservice.removeactivities.header"))
//      subHeading.html must include(Messages("summary.updateservice"))
//
//    }
//
//    "show errors in the correct locations" in new ViewFixture {
//
//      val form2: InvalidForm = InvalidForm(Map.empty, Seq(
//        (Path \ "businessActivities") -> Seq(ValidationError("not a message Key"))
//      ))
//
//      def view = views.html.businessmatching.updateservice.remove.remove_activities(form2, Set.empty)
//
//      errorSummary.html must include("not a message Key")
//
//      doc.getElementById("businessActivities")
//        .getElementsByClass("error-notification").first.html must include("not a message Key")
//
//    }
//  }
}