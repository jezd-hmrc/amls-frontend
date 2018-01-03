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

package controllers.payments

import connectors.PayApiConnector
import generators.{AmlsReferenceNumberGenerator, PaymentGenerator}
import models.confirmation.Currency
import models.payments._
import models.status.SubmissionReadyForReview
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito.{verify, when}
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.i18n.Messages
import play.api.test.Helpers._
import services.{AuthEnrolmentsService, PaymentsService, StatusService, SubmissionResponseService}
import uk.gov.hmrc.play.frontend.auth.AuthContext
import utils.{AuthorisedFixture, GenericTestHelper}

import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.http.HeaderCarrier

class BankDetailsControllerSpec extends PlaySpec with GenericTestHelper with PaymentGenerator{

  trait Fixture extends AuthorisedFixture { self =>

    val request = addToken(authRequest)

    implicit val hc: HeaderCarrier = new HeaderCarrier()
    implicit val ac: AuthContext = mock[AuthContext]
    implicit val ec: ExecutionContext = mock[ExecutionContext]

    val controller = new BankDetailsController(
      authConnector = self.authConnector,
      statusService = mock[StatusService],
      submissionResponseService = mock[SubmissionResponseService]
    )

  }

  "BankDetailsController" when {

    "get is called" must {
      "return OK with view" in new Fixture {

        val submissionSatus = SubmissionReadyForReview

        when {
          controller.statusService.getStatus(any(),any(),any())
        } thenReturn Future.successful(submissionSatus)

        when {
          controller.submissionResponseService.getSubmissionData(eqTo(submissionSatus))(any(),any(),any())
        } thenReturn Future.successful(Some((Some(paymentReferenceNumber), Currency(200), Seq.empty, Right(None))))

        val result = controller.get()(request)

        status(result) must be(OK)
        contentAsString(result) must include(Messages("payments.bankdetails.title"))

      }
    }

  }

}
