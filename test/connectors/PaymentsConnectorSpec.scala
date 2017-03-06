package connectors

import models.payments.{PaymentRedirectRequest, PaymentServiceRedirect}
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Cookie
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpPost, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PaymentsConnectorSpec extends PlaySpec with MockitoSugar {

  implicit val hc = HeaderCarrier()

  trait TestFixture {

    val http = mock[HttpPost]

    val app = new GuiceApplicationBuilder()
      .disable[com.kenshoo.play.metrics.PlayModule]
      .overrides(bind[HttpPost].to(http))
      .build()

    lazy val connector = app.injector.instanceOf[PaymentsConnector]

    implicit val request = FakeRequest().withCookies(Cookie("test", "hello"))

    def createResponse(status: Int = CREATED, redirectLocation: String = "http://localhost:9050/pay-online/card-selection") = {
      when {
        http.POST[PaymentRedirectRequest, HttpResponse](any(), any(), any())(any(), any(), any())
      } thenReturn Future.successful(HttpResponse(CREATED, responseHeaders = Map("Location" -> Seq(redirectLocation))))
    }

  }

  "The payments connector" must {

    "Return the payments redirect url" when {

      "given valid payment details" in new TestFixture {

        createResponse()

        val model = PaymentRedirectRequest("reference_number", 150, "http://google.co.uk")

        val result = await(connector.requestPaymentRedirectUrl(model))

        result mustBe Some(PaymentServiceRedirect("http://localhost:9050/pay-online/card-selection"))

        val headerCaptor = ArgumentCaptor.forClass(classOf[Seq[(String, String)]])
        verify(http).POST(any(), any(), headerCaptor.capture())(any(), any(), any())

        headerCaptor.getValue must contain("Cookie" -> "test=hello")

      }
    }

  }

}
