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

package services

import connectors.EnrolmentStoreConnector
import generators.AmlsReferenceNumberGenerator
import generators.enrolment.ESEnrolmentGenerator
import models.enrolment.{Constants, EnrolmentEntry, EnrolmentIdentifier}
import org.mockito.Matchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import org.scalatest.MustMatchers
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.frontend.auth.connectors.domain.{ConfidenceLevel, CredentialStrength}
import uk.gov.hmrc.play.frontend.auth.{AuthContext, LoggedInUser}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EnrolmentStoreServiceSpec extends PlaySpec with MustMatchers with MockitoSugar with AmlsReferenceNumberGenerator with ESEnrolmentGenerator {

  trait Fixture {
    implicit val hc = HeaderCarrier()

    implicit val authContext = mock[AuthContext]

    //noinspection ScalaStyle
    val userId = numSequence(10).sample.get

    when {
      authContext.user
    } thenReturn LoggedInUser(userId, None, None, None, CredentialStrength.Strong, ConfidenceLevel.L0, "")

    val connector = mock[EnrolmentStoreConnector]
    val service = new EnrolmentStoreService(connector)

    val validMlrEnrolment = EnrolmentEntry(
      Constants.serviceName, "active", "MLR Enrolment", Seq(
        EnrolmentIdentifier(Constants.amlsRefIdentKey, amlsRegistrationNumber)
      )
    )

  }

  "getAmlsRegistrationNumber" when {
    "the user has the correct enrolment" must {
      "parse the response from the EnrolmentStoreConnector" in new Fixture {
        val enrolment = esEnrolmentWith(validMlrEnrolment).sample

        when {
          connector.userEnrolments(eqTo(userId))(any(), any())
        } thenReturn Future.successful(enrolment)

        val result = await(service.getAmlsRegistrationNumber)

        result must contain(amlsRegistrationNumber)
      }
    }

    "the user doesn't have the correct enrolment" must {
      "return None" in new Fixture {
        val enrolment = esEnrolmentGen.sample

        when {
          connector.userEnrolments(eqTo(userId))(any(), any())
        } thenReturn Future.successful(enrolment)

        val result = await(service.getAmlsRegistrationNumber)

        result mustBe None
      }
    }

    "the user doesn't have any enrolments" must {
      "return None" in new Fixture {
        when {
          connector.userEnrolments(eqTo(userId))(any(), any())
        } thenReturn Future.successful(None)

        val result = await(service.getAmlsRegistrationNumber)

        result mustBe None
      }
    }
  }

}