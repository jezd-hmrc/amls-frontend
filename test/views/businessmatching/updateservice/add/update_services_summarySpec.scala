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

package views.businessmatching.updateservice.add

import forms.EmptyForm
import models.flowmanagement.AddServiceFlowModel
import org.scalatest.MustMatchers
import play.api.i18n.Messages
import utils.GenericTestHelper
import views.Fixture


class update_services_summarySpec  extends GenericTestHelper with MustMatchers {

  trait ViewFixture extends Fixture {
    implicit val requestWithToken = addToken(request)
    def view = views.html.businessmatching.updateservice.add.update_services_summary(EmptyForm, AddServiceFlowModel())
  }

  "The update_services_summary view" must {

    "have the correct title" in new ViewFixture {
      doc.title must startWith(Messages("title.cya") + " - " + Messages("summary.updateservice"))
    }

    "have correct heading" in new ViewFixture {
      heading.html must be(Messages("title.cya"))
    }

    "have correct subHeading" in new ViewFixture {
      subHeading.html must include(Messages("summary.updateservice"))
    }

    "show the correct content" in new ViewFixture {

    }

    "not show the return link" in new ViewFixture {
      doc.body().text() must not include Messages("link.return.registration.progress")
    }
  }
}