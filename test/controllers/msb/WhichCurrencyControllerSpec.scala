package controllers.msb

import connectors.DataCacheConnector
import models.moneyservicebusiness.{MoneyServiceBusiness, WhichCurrencies}
import models.status.{NotCompleted, SubmissionDecisionApproved}
import org.mockito.Mockito._
import org.mockito.Matchers.{eq => eqTo, _}
import org.scalatest.concurrent.{IntegrationPatience, PatienceConfiguration, ScalaFutures}
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import services.StatusService
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import utils.AuthorisedFixture
import play.api.test.Helpers._
import play.api.http.Status.{BAD_REQUEST, SEE_OTHER}
import play.api.http.HeaderNames.LOCATION
import views.html.msb.which_currencies

import scala.concurrent.Future

class WhichCurrencyControllerSpec extends WordSpec
                                    with MockitoSugar
                                    with MustMatchers
                                    with OneAppPerSuite
                                    with PatienceConfiguration
                                    with IntegrationPatience
                                    with ScalaFutures {

  trait Fixture extends AuthorisedFixture {
    self =>

    val cache: DataCacheConnector = mock[DataCacheConnector]

    when(cache.fetch[MoneyServiceBusiness](eqTo(MoneyServiceBusiness.key))(any(), any(), any()))
      .thenReturn(Future.successful(None))

    when(cache.save[MoneyServiceBusiness](any(), any())(any(),any(),any()))
      .thenReturn(Future.successful(CacheMap("TESTID", Map())))

    val controller = new WhichCurrenciesController {

      override def cache: DataCacheConnector = self.cache
      override protected def authConnector: AuthConnector = self.authConnector
      override implicit val statusService: StatusService = mock[StatusService]
    }
  }

  "WhichCurrencyController" when {
    "get is called" should {
      "succeed" in new Fixture {
        when(controller.statusService.getStatus(any(), any(), any()))
          .thenReturn(Future.successful(NotCompleted))

        val resp = controller.get(false).apply(request)
        status(resp) must be(200)
      }
    }

    "redirect to Page not found" when {
      "application is in variation mode" in new Fixture {

        when(controller.statusService.getStatus(any(), any(), any()))
          .thenReturn(Future.successful(SubmissionDecisionApproved))

        val result = controller.get()(request)
        status(result) must be(NOT_FOUND)
      }
    }

    "post is called " when {
      "data is valid" should {
        "redirect to check your answers" in new Fixture {
          val newRequest = request.withFormUrlEncodedBody (
            "currencies[0]" -> "USD",
            "currencies[1]" -> "GBP",
            "currencies[2]" -> "BOB",
            "bankMoneySource" ->"Yes",
            "bankNames" ->"Bank names",
            "wholesalerMoneySource" -> "Yes",
            "wholesalerNames" -> "wholesaler names",
            "customerMoneySource" -> "Yes",
            "usesForeignCurrencies" -> "Yes"
          )

          val result = controller.post(false).apply(newRequest)

          status(result) must be (SEE_OTHER)
          redirectLocation(result) mustEqual Some(routes.SummaryController.get().url)
        }
      }

      "data is invalid" should {
        "return invalid form" in new Fixture {

          val newRequest = request.withFormUrlEncodedBody(
            "bankMoneySource" ->"Yes",
            "bankNames" ->"Bank names",
            "wholesalerMoneySource" -> "Yes",
            "wholesalerNames" -> "wholesaler names",
            "customerMoneySource" -> "Yes"
          )

          whenReady(controller.post(false).apply(newRequest)) {
            resp =>
              resp.header.status must be(BAD_REQUEST)
          }
        }
      }
    }
  }
}
