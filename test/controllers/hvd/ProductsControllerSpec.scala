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

package controllers.hvd

import connectors.DataCacheConnector
import models.hvd.{Alcohol, Hvd, Products, Tobacco}
import models.businessmatching.HighValueDealing
import models.status.{ReadyForRenewal, SubmissionDecisionApproved, SubmissionDecisionRejected}
import org.jsoup.Jsoup
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.i18n.Messages
import play.api.test.FakeApplication
import play.api.test.Helpers._
import services.StatusService
import services.businessmatching.ServiceFlow
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import utils.{AuthorisedFixture, DependencyMocks, GenericTestHelper, ServiceFlowMocks}

import scala.concurrent.Future

class ProductsControllerSpec extends GenericTestHelper with MockitoSugar {

  override lazy val app = FakeApplication(additionalConfiguration = Map("microservice.services.feature-toggle.release7" -> true) )

  trait Fixture extends AuthorisedFixture with DependencyMocks {
    self => val request = addToken(authRequest)

    val controller = new ProductsController(
      mockCacheConnector,
      mockStatusService,
      self.authConnector,
      mockServiceFlow
    )

    mockIsNewActivity(false)
    mockCacheSave[Hvd]
  }

  val emptyCache = CacheMap("", Map.empty)

  "ProductsController" must {

    "load the 'What will your business sell?' page" in new Fixture  {

      mockCacheFetch[Hvd](None)

      val result = controller.get()(request)
      status(result) must be(OK)
      contentAsString(result) must include(Messages("hvd.products.title"))

    }

    "pre-populate the 'What will your business sell?' page" in new Fixture  {

      mockCacheFetch(Some(Hvd(products = Some(Products(Set(Alcohol, Tobacco))))))

      val result = controller.get()(request)
      status(result) must be(OK)

      val document = Jsoup.parse(contentAsString(result))
      document.select("input[value=01]").hasAttr("checked") must be(true)
      document.select("input[value=02]").hasAttr("checked") must be(true)

    }

    "Successfully post the data when the option alcohol is selected" in new Fixture {

      val newRequest = request.withFormUrlEncodedBody(
        "products[0]" -> "01",
        "products[1]" -> "02"
      )

      mockCacheFetch[Hvd](None)
      mockApplicationStatus(SubmissionDecisionRejected)

      val result = controller.post()(newRequest)
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(routes.ExciseGoodsController.get().url))
    }

    "successfully navigate to next page when the option other than alcohol and tobacco selected " in new Fixture {

      val newRequest = request.withFormUrlEncodedBody(
        "products[0]" -> "03",
        "products[1]" -> "04"
      )

      mockCacheFetch[Hvd](None)
      mockApplicationStatus(SubmissionDecisionRejected)

      val result = controller.post()(newRequest)
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(routes.HowWillYouSellGoodsController.get().url))
    }

    "on post with valid data in edit mode" in new Fixture {

      val newRequest = request.withFormUrlEncodedBody(
        "products[0]" -> "01",
        "products[1]" -> "02",
        "products[2]" -> "12",
        "otherDetails" -> "test"
      )

      mockCacheFetch[Hvd](None)
      mockApplicationStatus(SubmissionDecisionRejected)

      val result = controller.post(true)(newRequest)
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(routes.ExciseGoodsController.get(true).url))
    }

    "on post with invalid data" in new Fixture {

      val newRequest = request.withFormUrlEncodedBody(
        "products[0]" -> "01",
        "products[1]" -> "12",
        "otherDetails" -> ""
      )

      mockCacheFetch[Hvd](None)

      val result = controller.post()(newRequest)
      status(result) must be(BAD_REQUEST)

      val document = Jsoup.parse(contentAsString(result))
      document.select("a[href=#otherDetails]").html() must include(Messages("error.required.hvd.business.sell.other.details"))
    }

    "on post with invalid data1" in new Fixture {

      val newRequest = request.withFormUrlEncodedBody(
        "products[0]" -> "01",
        "products[1]" -> "02",
        "products[2]" -> "12",
        "otherDetails" -> "g"*256
      )

      mockCacheFetch[Hvd](None)

      val result = controller.post()(newRequest)
      status(result) must be(BAD_REQUEST)

      val document = Jsoup.parse(contentAsString(result))
      document.select("a[href=#otherDetails]").html() must include(Messages("error.invalid.hvd.business.sell.other.details"))
    }

    "redirect to dateOfChange when a change is made and decision is approved" in new Fixture {
      val newRequest = request.withFormUrlEncodedBody(
        "products[0]" -> "01",
        "products[1]" -> "02",
        "products[2]" -> "12",
        "otherDetails" -> "test"
      )

      mockCacheFetch(Some(Hvd(products = Some(Products(Set(Alcohol, Tobacco))))))
      mockApplicationStatus(SubmissionDecisionApproved)

      val result = controller.post(true)(newRequest)
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(routes.HvdDateOfChangeController.get().url))
    }

    "redirect to dateOfChange when a change is made and decision is ready for renewal" in new Fixture {
      val newRequest = request.withFormUrlEncodedBody(
        "products[0]" -> "01",
        "products[1]" -> "02",
        "products[2]" -> "12",
        "otherDetails" -> "test"
      )

      mockCacheFetch(Some(Hvd(products = Some(Products(Set(Alcohol, Tobacco))))))
      mockApplicationStatus(ReadyForRenewal(None))

      val result = controller.post(true)(newRequest)
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(routes.HvdDateOfChangeController.get().url))
    }
  }

  "Calling POST" when {
    "the submission is approved" when {
      "the sector has just been added" must {
        "redirect to the next page" when {
          "the user selectes 'alcohol' or 'tobacco" in new Fixture {
            val newRequest = request.withFormUrlEncodedBody(
              "products[0]" -> "01",
              "products[1]" -> "02"
            )

            mockIsNewActivity(true, Some(HighValueDealing))
            mockCacheFetch[Hvd](None)
            mockApplicationStatus(SubmissionDecisionApproved)

            val result = controller.post()(newRequest)
            status(result) must be(SEE_OTHER)
            redirectLocation(result) must be(Some(routes.ExciseGoodsController.get().url))
          }

          "the user selects something other than alcohol or tobacco" in new Fixture {
            val newRequest = request.withFormUrlEncodedBody(
              "products[0]" -> "03",
              "products[1]" -> "04"
            )

            mockIsNewActivity(true, Some(HighValueDealing))
            mockCacheFetch[Hvd](None)
            mockApplicationStatus(SubmissionDecisionApproved)

            val result = controller.post()(newRequest)
            status(result) must be(SEE_OTHER)
            redirectLocation(result) must be(Some(routes.HowWillYouSellGoodsController.get().url))
          }
        }
      }
    }
  }

}
