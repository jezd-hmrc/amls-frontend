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

package controllers.tcsp

import controllers.actions.SuccessfulAuthAction
import models.tcsp.{ServicesOfAnotherTCSPYes, Tcsp}
import org.jsoup.Jsoup
import org.scalatest.mockito.MockitoSugar
import play.api.test.Helpers._
import utils.{AmlsSpec, AuthorisedFixture, DependencyMocks}
import views.html.tcsp.another_tcsp_supervision

class AnotherTCSPSupervisionControllerSpec extends AmlsSpec with MockitoSugar {

  trait Fixture extends DependencyMocks {
    self => val request = addToken(authRequest)
    lazy val view = app.injector.instanceOf[another_tcsp_supervision]
    val controller = new AnotherTCSPSupervisionController(
      SuccessfulAuthAction, ds = commonDependencies,
      dataCacheConnector = mockCacheConnector,
      cc = mockMcc,
      another_tcsp_supervision = view,
      error = errorView
    )
  }

  "AnotherTCSPSupervisionController" when {

    "get is called" must {

      "display another_tcsp_supervision view without pre-filled input" in new Fixture {

        mockCacheFetch[Tcsp](None)

        val result = controller.get()(request)
        status(result) must be(OK)

      }

      "display another_tcsp_supervision view with pre-filled input" in new Fixture {

        val mlrRefNumber = "12345678"

        mockCacheFetch[Tcsp](Some(Tcsp(servicesOfAnotherTCSP = Some(ServicesOfAnotherTCSPYes(mlrRefNumber)))))

        val result = controller.get()(request)
        status(result) must be(OK)

        val content = Jsoup.parse(contentAsString(result))
        content.getElementById("mlrRefNumber").`val`() must be(mlrRefNumber)
      }

    }

    "post is called" must {

      "redirect to SummaryController" when {

        "valid data" when {
          "edit is false" in new Fixture {

            mockCacheFetch[Tcsp](None)
            mockCacheSave[Tcsp]

            val newRequest = requestWithUrlEncodedBody(
              "servicesOfAnotherTCSP" -> "true",
              "mlrRefNumber" -> "12345678"
            )

            val result = controller.post()(newRequest)

            status(result) must be(SEE_OTHER)
            redirectLocation(result) must be(Some(routes.SummaryController.get().url))
          }

          "edit is true" in new Fixture {

            mockCacheFetch[Tcsp](None)
            mockCacheSave[Tcsp]

            val newRequest = requestWithUrlEncodedBody(
              "servicesOfAnotherTCSP" -> "true",
              "mlrRefNumber" -> "12345678"
            )

            val result = controller.post(true)(newRequest)

            status(result) must be(SEE_OTHER)
            redirectLocation(result) must be(Some(routes.SummaryController.get().url))
          }

        }

      }

      "respond with BAD_REQUEST" when {
        "invalid data" in new Fixture {

          val newRequestInvalid = requestWithUrlEncodedBody(
            "servicesOfAnotherTCSP" -> "true",
            "mlrRefNumber" -> "adbg1233"
          )

          val result = controller.post()(newRequestInvalid)
          status(result) must be(BAD_REQUEST)
        }
      }

    }

  }

}
