/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers.renewal

import cats.implicits._
import connectors.DataCacheConnector
import controllers.actions.SuccessfulAuthAction
import models.businessmatching._
import models.renewal.{BankMoneySource, MoneySources, Renewal, UsesForeignCurrenciesYes, WhichCurrencies, WholesalerMoneySource}
import org.jsoup.Jsoup
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers._
import services.RenewalService
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.{AmlsSpec, AuthorisedFixture}
import views.html.renewal.which_currencies

import scala.concurrent.Future

class WhichCurrenciesControllerSpec extends AmlsSpec with MockitoSugar {

  trait Fixture {
    self =>
    val renewalService = mock[RenewalService]
    val request = addToken(authRequest)
    val dataCacheConnector = mock[DataCacheConnector]
    val cacheMap = mock[CacheMap]
    lazy val view = app.injector.instanceOf[which_currencies]
    lazy val controller = new WhichCurrenciesController(
      SuccessfulAuthAction,
      ds = commonDependencies,
      renewalService,
      dataCacheConnector,
      cc = mockMcc,
      which_currencies = view)

    when {
      renewalService.getRenewal(any())(any(), any())
    } thenReturn Future.successful(Renewal().some)

    when(dataCacheConnector.fetchAll(any())(any()))
      .thenReturn(Future.successful(Some(cacheMap)))
  }

  trait FormSubmissionFixture extends Fixture {
    val validFormRequest = requestWithUrlEncodedBody(
      "currencies[0]" -> "USD",
      "currencies[1]" -> "GBP",
      "currencies[2]" -> "BOB",
      "bankMoneySource" -> "Yes",
      "bankNames" -> "Bank names",
      "wholesalerMoneySource" -> "Yes",
      "wholesalerNames" -> "wholesaler names",
      "customerMoneySource" -> "Yes",
      "usesForeignCurrencies" -> "Yes"
    )

    when {
      renewalService.updateRenewal(any(),any())(any(), any())
    } thenReturn Future.successful(mock[CacheMap])
  }

  trait RoutingFixture extends FormSubmissionFixture {
    val renewal = Renewal()

    val msbServices = Some(
      BusinessMatchingMsbServices(
        Set(
          TransmittingMoney
        )
      )
    )

    val businessActivities = Some(
      BusinessActivities(Set(HighValueDealing,  AccountancyServices))
    )

    val whichCurrencies = WhichCurrencies(
      Seq("USD"),
      Some(UsesForeignCurrenciesYes),
      Some(MoneySources(None,
      None,
      Some(true))))


    val expectedRenewal = renewal.copy(
      whichCurrencies = Some(whichCurrencies), hasChanged = true
    )

    when(cacheMap.getEntry[Renewal](eqTo(Renewal.key))(any()))
      .thenReturn(Some(renewal))

    when(cacheMap.getEntry[BusinessMatching](BusinessMatching.key))
      .thenReturn(Some(BusinessMatching(msbServices = msbServices, activities = businessActivities)))

    when(dataCacheConnector.save[Renewal](any(), eqTo(Renewal.key), eqTo(expectedRenewal))(any(), any()))
      .thenReturn(Future.successful(new CacheMap("", Map.empty)))

    def setupBusinessMatching(activities: Set[BusinessActivity], msbServices: Set[BusinessMatchingMsbService]) = when {
      cacheMap.getEntry[BusinessMatching](BusinessMatching.key)
    } thenReturn Some(BusinessMatching(msbServices = Some(BusinessMatchingMsbServices(msbServices)), activities = Some(BusinessActivities(activities))))
  }

  "Calling the GET action" must {
    "return the correct view" when {
      "edit is false" in new Fixture {
        val result = controller.get()(request)

        status(result) mustBe OK

        val doc = Jsoup.parse(contentAsString(result))
        doc.select(".heading-xlarge").text mustBe Messages("renewal.msb.whichcurrencies.header")
      }

      "edit is true" in new Fixture {
        val result = controller.get(true)(request)

        status(result) mustBe OK

        val doc = Jsoup.parse(contentAsString(result))
        doc.select("form").first.attr("action") mustBe routes.WhichCurrenciesController.post(true).url
      }

      "reads the current value from the renewals model" in new Fixture {
        when {
          renewalService.getRenewal(any())(any(), any())
        } thenReturn Future.successful(Renewal(whichCurrencies = WhichCurrencies(Seq("EUR"), None, MoneySources(None, None, None).some).some).some)

        val result = controller.get(true)(request)
        val doc = Jsoup.parse(contentAsString(result))

        doc.select("select[name=currencies[0]] option[selected]").attr("value") mustBe "EUR"

        verify(renewalService).getRenewal(any())(any(), any())
      }
    }
  }

  "Calling the POST action" when {
    "posting valid data" must {
      "redirect to the summary page" when {
        "editing" in new RoutingFixture {
          setupBusinessMatching(Set(HighValueDealing), Set(TransmittingMoney))

          val result = controller.post(edit = true)(validFormRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe controllers.renewal.routes.SummaryController.get().url.some
        }
      }

      "save the model data into the renewal object" in new RoutingFixture {
        val currentModel = WhichCurrencies(
          Seq("USD", "GBP", "BOB"),
          Some(UsesForeignCurrenciesYes),
          Some(MoneySources(
          Some(BankMoneySource("Bank names")),
          Some(WholesalerMoneySource("wholesaler names")),
          Some(true))))

        val result = await(controller.post()(validFormRequest))
        val captor = ArgumentCaptor.forClass(classOf[Renewal])

        verify(renewalService).updateRenewal(any(), captor.capture())(any(), any())

        captor.getValue.whichCurrencies mustBe Some(WhichCurrencies(
          Seq("USD", "GBP", "BOB")))
      }
    }

    "return a bad request" when {
      "the form fails validation" in new FormSubmissionFixture {
        val newRequest = requestWithUrlEncodedBody("currencies[0]" -> "1dfasdffds")

        val result = controller.post()(newRequest)

        status(result) mustBe BAD_REQUEST
        verify(renewalService, never()).updateRenewal(any(),any())(any(), any())
      }
    }
  }
}
