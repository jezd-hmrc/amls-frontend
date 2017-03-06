package connectors

import javax.inject._

import models.payments.{PaymentRedirectRequest, PaymentServiceRedirect}
import play.api.Logger
import play.api.http.Status
import play.api.mvc.Request
import uk.gov.hmrc.play.config.inject.ServicesConfig
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpPost}
import utils.HttpUtils._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PaymentsConnector @Inject()(http: HttpPost, config: ServicesConfig) {

  val baseUrl = config.baseUrl("payments-frontend")
  lazy val customPaymentId = config.getConfString("payments-frontend.custom-payment-id", "")

  def requestPaymentRedirectUrl(request: PaymentRedirectRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext, httpRequest: Request[_]): Future[Option[PaymentServiceRedirect]] = {

    import utils.Strings._

    val url = s"$baseUrl/pay-online/other-taxes/custom"

    println(httpRequest.headers.get("Cookie").toString in Console.RED)

    val headers = Seq(
      "Custom-Payment" -> customPaymentId,
      "Cookie" -> httpRequest.headers.get("Cookie").getOrElse("")
    )

    http.POST(url, request, headers) map { r =>
      r.status match {
        case Status.CREATED =>

          r.redirectLocation match {
            case Some(location) =>
              Some(PaymentServiceRedirect(location))
            case _ =>
              Logger.warn("[PaymentsConnector] No redirect url was returned")
              None
          }

        case s =>
          Logger.warn(s"[PaymentsConnector] A $s status was returned when trying to retrieve the payment url")
          None
      }
    }
  }
}
