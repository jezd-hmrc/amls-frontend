package controllers.hvd

import connectors.DataCacheConnector
import models.hvd.{CashPaymentNo, CashPaymentYes, Hvd}
import org.joda.time.LocalDate
import org.jsoup.Jsoup
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.i18n.Messages
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.AuthorisedFixture

import scala.concurrent.Future


class CashPaymentControllerSpec extends PlaySpec with OneAppPerSuite with MockitoSugar {

  trait Fixture extends AuthorisedFixture {
    self =>
    val controller = new CashPaymentController {
      override val dataCacheConnector = mock[DataCacheConnector]
      override val authConnector = self.authConnector
    }
  }

  val emptyCache = CacheMap("", Map.empty)

  "CashPaymentController" must {

    "Get Option:" must {

      "load the Cash Payment page" in new Fixture {

        when(controller.dataCacheConnector.fetch[Hvd](any())(any(), any(), any()))
          .thenReturn(Future.successful(None))

        val result = controller.get()(request)
        status(result) must be(OK)

        val htmlValue = Jsoup.parse(contentAsString(result))
        htmlValue.title mustBe Messages("hvd.cash.payment.title")
      }

      "load Yes when Cash payment from save4later returns True" in new Fixture {
        // scalastyle:off magic.number
        val cashPayment = Some(CashPaymentYes(new LocalDate(1990, 2, 24)))
        val activities = Hvd(cashPayment = cashPayment)

        when(controller.dataCacheConnector.fetch[Hvd](any())(any(), any(), any()))
          .thenReturn(Future.successful(Some(activities)))

        val result = controller.get()(request)
        status(result) must be(OK)

        val htmlValue = Jsoup.parse(contentAsString(result))
        htmlValue.getElementById("acceptedAnyPayment-true").attr("checked") mustBe "checked"
        htmlValue.getElementById("acceptedAnyPayment-true").attr("checked") mustBe "checked"

      }

      "load No when cashPayment from save4later returns No" in new Fixture {

        val cashPayment = Some(CashPaymentNo)
        val activities = Hvd(cashPayment = cashPayment)

        when(controller.dataCacheConnector.fetch[Hvd](any())(any(), any(), any()))
          .thenReturn(Future.successful(Some(activities)))

        val result = controller.get()(request)
        status(result) must be(OK)

        val htmlValue = Jsoup.parse(contentAsString(result))
        htmlValue.getElementById("acceptedAnyPayment-false").attr("checked") mustBe "checked"

      }
    }

    "Post" must {

      "successfully redirect to the page on selection of 'Yes' when edit mode is on" in new Fixture {

        val newRequest = request.withFormUrlEncodedBody("acceptedAnyPayment" -> "true",
          "paymentDate.day" -> "12",
          "paymentDate.month" -> "5",
          "paymentDate.year" -> "1999"
        )

        when(controller.dataCacheConnector.fetch[Hvd](any())(any(), any(), any()))
          .thenReturn(Future.successful(None))

        when(controller.dataCacheConnector.save[Hvd](any(), any())(any(), any(), any()))
          .thenReturn(Future.successful(emptyCache))

        val result = controller.post(true)(newRequest)
        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(controllers.hvd.routes.SummaryController.get().url))
      }

      "successfully redirect to the page on selection of 'Yes' when edit mode is off" in new Fixture {

        val newRequest = request.withFormUrlEncodedBody("acceptedAnyPayment" -> "true",
          "paymentDate.day" -> "12",
          "paymentDate.month" -> "5",
          "paymentDate.year" -> "1999"
        )

        when(controller.dataCacheConnector.fetch[Hvd](any())(any(), any(), any()))
          .thenReturn(Future.successful(None))

        when(controller.dataCacheConnector.save[Hvd](any(), any())(any(), any(), any()))
          .thenReturn(Future.successful(emptyCache))

        val result = controller.post()(newRequest)
        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(controllers.hvd.routes.LinkedCashPaymentsController.get().url))
      }

      "successfully redirect to the page on selection of 'No' when edit mode is off" in new Fixture {
        val newRequest = request.withFormUrlEncodedBody("acceptedAnyPayment" -> "false")

        when(controller.dataCacheConnector.fetch[Hvd](any())(any(), any(), any()))
          .thenReturn(Future.successful(None))

        when(controller.dataCacheConnector.save[Hvd](any(), any())(any(), any(), any()))
          .thenReturn(Future.successful(emptyCache))

        val result = controller.post()(newRequest)
        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(controllers.hvd.routes.LinkedCashPaymentsController.get().url))
      }

    }

    "successfully redirect to the page on selection of Option 'No' when edit mode is on" in new Fixture {
      val newRequest = request.withFormUrlEncodedBody(
        "acceptedAnyPayment" -> "false"
      )
      when(controller.dataCacheConnector.fetch[Hvd](any())(any(), any(), any()))
        .thenReturn(Future.successful(None))

      when(controller.dataCacheConnector.save[Hvd](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(emptyCache))

      val result = controller.post(true)(newRequest)
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(controllers.hvd.routes.SummaryController.get().url))
    }


    "on post invalid data show error" in new Fixture {

      val newRequest = request.withFormUrlEncodedBody()
      when(controller.dataCacheConnector.fetch[Hvd](any())(any(), any(), any()))
        .thenReturn(Future.successful(None))

      val result = controller.post()(newRequest)
      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include(Messages("error.required.hvd.accepted.cash.payment"))

    }

    "on post with invalid data show error" in new Fixture {
      val newRequest = request.withFormUrlEncodedBody(
        "acceptedAnyPayment" -> "true",
        "paymentDate.day" -> "",
        "paymentDate.month" -> "5",
        "paymentDate.year" -> "1999"
      )
      when(controller.dataCacheConnector.fetch[Hvd](any())(any(), any(), any()))
        .thenReturn(Future.successful(None))

      val result = controller.post()(newRequest)
      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include(Messages("error.expected.jodadate.format"))

    }
  }
}