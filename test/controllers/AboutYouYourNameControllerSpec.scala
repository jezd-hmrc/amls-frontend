package controllers

import java.util.UUID

import builders.{AuthBuilder, SessionBuilder}
import config.AMLSAuthConnector
import connectors.DataCacheConnector
import models.YourName
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.http.SessionKeys

import scala.concurrent.Future

class AboutYouYourNameControllerSpec extends PlaySpec with OneServerPerSuite with MockitoSugar {

  val userId = s"user-${UUID.randomUUID}"
  implicit val request = FakeRequest()
  val yourName: YourName = YourName("FirstName", "middleName", "lastName")
  val mockAuthConnector = mock[AuthConnector]
  val mockDataCacheConnector = mock[DataCacheConnector]

  object MockAboutYouController extends AboutYouYourNameController {
    override protected def authConnector: AuthConnector = mockAuthConnector
    val dataCacheConnector: DataCacheConnector = mockDataCacheConnector
  }

  "AboutYouController" must {
    "use correct service" in {
      AboutYouYourNameController.authConnector must be(AMLSAuthConnector)
    }

    "on load of page " must {
      "Authorised users" must {
        "load the Sample Login page" in {
          getWithAuthorisedUser {
            result =>
              status(result) must be(OK)
          }
        }
      }
    }

    "on submit" must {
      "Authorised users" must {
        "successfully navigate to next page " in {
          submitWithAuthorisedUser { result =>
            status(result) must be(SEE_OTHER)

          }
        }
      }
    }
    def getMockAuthorisedUser() {
      implicit val user = AuthBuilder.createUserAuthContext(userId, "name")
      AuthBuilder.mockAuthorisedUser(userId, mockAuthConnector)
    }

    def getWithAuthorisedUser(test: Future[Result] => Any) {
      getMockAuthorisedUser
      when(mockDataCacheConnector.fetchDataShortLivedCache[YourName](Matchers.any(),
      Matchers.any()) (Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
      val result = MockAboutYouController.onPageLoad.apply(SessionBuilder.buildRequestWithSession(userId))
      test(result)
    }

    def submitWithAuthorisedUser(test: Future[Result] => Any) {
      val sessionId = s"session-${UUID.randomUUID}"
      val session = request.withSession(SessionKeys.sessionId -> sessionId,
        SessionKeys.token -> "RANDOMTOKEN",
        SessionKeys.userId -> userId)
      implicit val user = AuthBuilder.createUserAuthContext(userId, "name")
      AuthBuilder.mockAuthorisedUser(userId, mockAuthConnector)
      when(mockDataCacheConnector.saveDataShortLivedCache[YourName](Matchers.any(),
      Matchers.any(), Matchers.any()) (Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(yourName)))

      val result = MockAboutYouController.onSubmit.apply(session)
      test(result)
    }

  }
}
