package controllers.aboutthebusiness

import connectors.DataCacheConnector
import models.{BCAddress, BusinessCustomerDetails, RegisteredOffice, TelephoningBusiness}
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.BusinessCustomerService
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future


class RegisteredOfficeControllerSpec extends PlaySpec with OneServerPerSuite with MockitoSugar with ScalaFutures {
  private implicit val authContext = mock[AuthContext]
  private val mockAuthConnector = mock[AuthConnector]
  private val mockDataCacheConnector = mock[DataCacheConnector]
  private val mockBusinessCustomerService = mock[BusinessCustomerService]

  object MockRegisteredOfficeController extends RegisteredOfficeController {
    def authConnector = mockAuthConnector
    override def dataCacheConnector : DataCacheConnector = mockDataCacheConnector
  }

  "On Page load" must {
    implicit val fakeGetRequest = FakeRequest()

    "the blank Registered Office page if nothing in cache" in {
      when(mockDataCacheConnector.fetchDataShortLivedCache[RegisteredOffice](Matchers.any())
        (Matchers.any(),Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
      val futureResult = MockRegisteredOfficeController.get
      status(futureResult) must be(OK)
      contentAsString(futureResult) must include(Messages("title.registeredOffice"))
    }


    "the Registered Office details from the Cache" in {
      val registeredOffice = RegisteredOffice(true, false)
      val businessCustomerDetails = BusinessCustomerDetails("businessName", Some("businessType"),
        BCAddress("line_1", "line_2",Some(""),Some(""),Some("CA3 9ST"),"UK"),
        "sapNumber", "safeId", Some("agentReferenceNumber"), Some("firstName"), Some("lastName"))

      when(mockBusinessCustomerService.getReviewBusinessDetails[BusinessCustomerDetails]).
        thenReturn(Future.successful(businessCustomerDetails))
      when(mockDataCacheConnector.fetchDataShortLivedCache[RegisteredOffice](Matchers.any())

        (Matchers.any(),Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(registeredOffice)))
      val futureResult = MockRegisteredOfficeController.get
      status(futureResult) must be(OK)
      contentAsString(futureResult) must include(businessCustomerDetails.businessName)
      contentAsString(futureResult) must include(businessCustomerDetails.businessAddress.line_1)
      contentAsString(futureResult) must include(businessCustomerDetails.businessAddress.postcode.getOrElse(""))

    }


  }

}
