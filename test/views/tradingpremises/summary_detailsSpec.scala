/*
 * Copyright 2019 HM Revenue & Customs
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

package views.tradingpremises

import models.businessmatching.{BillPaymentServices, EstateAgentBusinessService, MoneyServiceBusiness}
import models.tradingpremises._
import org.joda.time.LocalDate
import org.jsoup.nodes.Element
import org.scalatest.prop.TableDrivenPropertyChecks
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import utils.{DateHelper, AmlsSpec}
import views.{Fixture, HtmlAssertions}

import scala.collection.JavaConversions._

sealed trait TestHelper extends AmlsSpec {

  val ytp = YourTradingPremises(
    "foo",
    Address(
      "1",
      "2",
      None,
      None,
      "asdfasdf"
    ),
    Some(true),
    Some(new LocalDate(1990, 2, 24))
  )
  val businessStructure = SoleProprietor
  val agentName = AgentName("test")
  val agentCompanyName = AgentCompanyDetails("test", Some("12345678"))
  val agentPartnership = AgentPartnership("test")
  val wdbd = WhatDoesYourBusinessDo(
    Set(
      BillPaymentServices,
      EstateAgentBusinessService,
      MoneyServiceBusiness)
  )
  val msbServices = TradingPremisesMsbServices(Set(TransmittingMoney, CurrencyExchange))
  val tradingPremises = TradingPremises(
    Some(RegisteringAgentPremises(true)),
    Some(ytp),
    Some(businessStructure),
    Some(agentName),
    Some(agentCompanyName),
    Some(agentPartnership),
    Some(wdbd),
    Some(msbServices),
    false,
    Some(123456),
    Some("Added"),
    Some(ActivityEndDate(new LocalDate(1999, 1, 1)))
  )

  trait ViewFixture extends Fixture {
    implicit val requestWithToken = addToken(FakeRequest())
  }
}

class summary_detailsSpec extends TestHelper with HtmlAssertions with TableDrivenPropertyChecks {

  "The summary details page" must {
    val sectionChecks = Table[String, Element => Boolean](
      ("title key", "check"),
      ("tradingpremises.summary.address", checkElementTextIncludes(_, "Trading address 1 2 asdfasdf")),
      ("tradingpremises.startDate.title", checkElementTextIncludes(_, DateHelper.formatDate(new LocalDate(1990, 2, 24)))),
      ("tradingpremises.isResidential.title", checkElementTextIncludes(_, "lbl.yes")),
      ("tradingpremises.whatdoesyourbusinessdo.title", checkElementTextOnlyIncludes(_, "Bill payment service provider", "Estate agency business", "Money service business")),
      ("tradingpremises.msb.services.title", checkElementTextIncludes(_, "Transmitting money","Currency exchange")),
      ("tradingpremises.agent.premises.title", checkElementTextIncludes(_, "lbl.yes")),
      ("tradingpremises.businessStructure.title", checkElementTextIncludes(_, "businessType.lbl.01")),
      ("tradingpremises.agentname.title", checkElementTextIncludes(_, "test")),
      ("tradingpremises.agentpartnership.title", checkElementTextIncludes(_, "test")),
      ("tradingpremises.youragent.company.name", checkElementTextIncludes(_, "test"))
    )
    "load summary details page when it is an msb" in new ViewFixture {

      val isMsb = true
      def view = views.html.tradingpremises.summary_details(tradingPremises, isMsb, 1, false, false)

      forAll(sectionChecks) { (key, check) => {
        val hTwos = doc.select("span.bold")

        val hTwo = hTwos.toList.find(e => e.text() == Messages(key))

        hTwo must not be (None)
        val section = hTwo.get.parents().select("div").first()
        check(section) must be(true)
      }}
    }

    "load summary details page when it is not an msb" in new ViewFixture {

      val isNotMsb = false
      def view = views.html.tradingpremises.summary_details(tradingPremises, isNotMsb, 1, false, false)

      html mustNot include(Messages("tradingpremises.summary.who-uses"))
    }

    "show the edit link for business services if the business sector has multiple business services" in new ViewFixture {

      val isMsb = true
      val testData = WhatDoesYourBusinessDo(Set(MoneyServiceBusiness))

      def view = views.html.tradingpremises.summary_details(tradingPremises, isMsb, 1, false, false)

      val hTwo = doc.select("div.cya-summary-list__row").toList.find(e => e.text().contains(Messages("tradingpremises.whatdoesyourbusinessdo.title")))
      val servicesSection = hTwo.get.parent.toString

      servicesSection must include(Messages("button.edit"))

    }
  }
}

