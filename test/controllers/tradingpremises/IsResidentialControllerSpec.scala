package controllers.tradingpremises

import connectors.DataCacheConnector
import models.tradingpremises._
import org.joda.time.LocalDate
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers.{any, eq => meq}
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.{AuthorisedFixture, GenericTestHelper}

import scala.concurrent.Future

class IsResidentialControllerSpec extends GenericTestHelper with ScalaFutures with MockitoSugar {

  trait Fixture extends AuthorisedFixture {
    self => val request = addToken(authRequest)

    val cache: DataCacheConnector = mock[DataCacheConnector]

    val controller = new IsResidentialController(messagesApi, self.authConnector, self.cache)
  }

  "IsResidentialController" must {
    val ytpModel = YourTradingPremises("foo", Address("1","2",None,None,"AA1 1BB",None), Some(true), Some(new LocalDate(2010, 10, 10)), None)
    val ytp = Some(ytpModel)
    val emptyCache = CacheMap("", Map.empty)
    "GET:" must {

      val pageTitle = Messages("tradingpremises.isResidential.title", "firstname lastname") + " - " +
        Messages("summary.tradingpremises") + " - " +
        Messages("title.amls") + " - " + Messages("title.gov")

      "successfully load is residential page with empty form" in new Fixture {

        when(controller.dataCacheConnector.fetch[Seq[TradingPremises]](any())(any(), any(), any()))
          .thenReturn(Future.successful(Some(Seq(TradingPremises(yourTradingPremises =  Some(ytpModel.copy(isResidential = None)))))))

        val result = controller.get(1, false)(request)
        status(result) must be(OK)
        val document = Jsoup.parse(contentAsString(result))
        document.title mustBe pageTitle

      }

      "redirect  to not found page when YourTradingPremises is None" in new Fixture {

        when(controller.dataCacheConnector.fetch[Seq[TradingPremises]](any())(any(), any(), any()))
          .thenReturn(Future.successful(None))

        val result = controller.get(1, false)(request)
        status(result) must be(NOT_FOUND)
      }

      "successfully load is residential page with pre - populated data form" in new Fixture {
        when(controller.dataCacheConnector.fetch[Seq[TradingPremises]](any())(any(), any(), any()))
          .thenReturn(Future.successful(Some(Seq(TradingPremises(yourTradingPremises = ytp)))))

        val result = controller.get(1, false)(request)
        status(result) must be(OK)
        val document = Jsoup.parse(contentAsString(result))
        document.title mustBe pageTitle
        document.select("input[value=true]").hasAttr("checked") must be(true)
      }
    }

    "POST:" must {
      "successfully redirect to next page on valid input" in new Fixture {
        val postRequest = request.withFormUrlEncodedBody(
          "isResidential" -> "true"
        )
        when(controller.dataCacheConnector.fetch[Seq[TradingPremises]](any())(any(), any(), any()))
          .thenReturn(Future.successful(Some(Seq(TradingPremises(yourTradingPremises = ytp)))))

        when(controller.dataCacheConnector.save[TradingPremises](any(), any())(any(), any(), any()))
          .thenReturn(Future.successful(emptyCache))

        val result = controller.post(1, false)(postRequest)
        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(routes.WhatDoesYourBusinessDoController.get(1, false).url))
      }

      "successfully redirect to next page on valid input in edit mode" in new Fixture {
        val postRequest = request.withFormUrlEncodedBody(
          "isResidential" -> "false"
        )
        val updatedYtp = Some(YourTradingPremises("foo",
          Address("1","2",None,None,"AA1 1BB",None), Some(false), Some(new LocalDate(2010, 10, 10)), None))

        val updatedTp = TradingPremises(yourTradingPremises = updatedYtp)
        val tp = TradingPremises(yourTradingPremises = ytp)

        when(controller.dataCacheConnector.fetch[Seq[TradingPremises]](any())(any(), any(), any()))
          .thenReturn(Future.successful(Some(Seq(TradingPremises(yourTradingPremises = ytp)))))

        when(controller.dataCacheConnector.save[Seq[TradingPremises]](any(), meq(Seq(updatedTp)))(any(), any(), any()))
          .thenReturn(Future.successful(emptyCache))

        val result = controller.post(1, true)(postRequest)
        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(routes.SummaryController.getIndividual(1).url))

      }

      "throw error on missing required field" in new Fixture {
        val postRequest = request.withFormUrlEncodedBody(
          "isResidential" -> ""
        )

        val result = controller.post(1, false)(postRequest)
        status(result) must be(BAD_REQUEST)
        val document: Document = Jsoup.parse(contentAsString(result))
        document.getElementsByClass("error-notification").html() must include(Messages("tradingpremises.yourtradingpremises.isresidential.required"))
      }
    }

  }
}