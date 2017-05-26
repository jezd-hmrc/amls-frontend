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

package controllers

import connectors.{AmlsConnector, DataCacheConnector}
import models.WithdrawSubscriptionResponse
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import play.api.test.Helpers._
import services.AuthEnrolmentsService
import utils.{AuthorisedFixture, GenericTestHelper}
import cats.implicits._
import models.businesscustomer.ReviewDetails
import models.businessmatching.BusinessMatching

import scala.concurrent.Future

class WithdrawApplicationControllerSpec extends GenericTestHelper {

  trait TestFixture extends AuthorisedFixture {
    self =>

    val request = addToken(authRequest)
    val amlsConnector = mock[AmlsConnector]
    val authService = mock[AuthEnrolmentsService]
    val cacheConnector = mock[DataCacheConnector]

    lazy val controller = new WithdrawApplicationController(authConnector, amlsConnector, authService, cacheConnector)

    val amlsRegistrationNumber = "XA1234567890L"
    val businessName = "Test Business"
    val reviewDetails = mock[ReviewDetails]
    when(reviewDetails.businessName) thenReturn businessName

    when {
      authService.amlsRegistrationNumber(any(), any(), any())
    } thenReturn Future.successful(amlsRegistrationNumber.some)

    when {
      amlsConnector.withdraw(eqTo(amlsRegistrationNumber))(any(), any(), any())
    } thenReturn Future.successful(WithdrawSubscriptionResponse("processing date"))

    when {
      cacheConnector.fetch[BusinessMatching](eqTo(BusinessMatching.key))(any(), any(), any())
    } thenReturn Future.successful(BusinessMatching(reviewDetails.some).some)
  }

  "The WithdrawApplication controller" when {
    "the get method is called" must {
      "show the 'withdraw your application' page" in new TestFixture {
        val result = controller.get()(request)
        status(result) mustBe OK
      }

      "show the business name" in new TestFixture {
        val result = controller.get()(request)
        contentAsString(result) must include(businessName)
      }
    }

    "call the middle tier to initiate the withdrawal process" when {
      "the post method is called" in new TestFixture {
        val result = controller.post()(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe controllers.routes.LandingController.get().url.some

        verify(amlsConnector).withdraw(eqTo(amlsRegistrationNumber))(any(), any(), any())
      }
    }

  }

}
