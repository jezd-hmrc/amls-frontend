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

package views.businessmatching.updateservice.add

import forms.EmptyForm
import models.businessmatching.updateservice.{ResponsiblePeopleFitAndProper, TradingPremisesActivities}
import models.businessmatching._
import models.flowmanagement.AddBusinessTypeFlowModel
import org.scalatest.MustMatchers
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import utils.AmlsViewSpec
import views.Fixture
import views.html.businessmatching.updateservice.add._
import utils.UpdateServicesSummaryFixtures

class update_services_summarySpec extends UpdateServicesSummaryFixtures {
  "The update_services_summary view" must {
    "have the correct title" in new ViewFixture {
      def view = update_services_summary(EmptyForm, AddBusinessTypeFlowModel(), Seq(), Seq())
      doc.title must startWith(Messages("title.cya") + " - " + Messages("summary.updateservice"))
    }

    "have correct heading" in new ViewFixture {
      def view = update_services_summary(EmptyForm, AddBusinessTypeFlowModel(), Seq(), Seq())
      heading.html must be(Messages("title.cya"))
    }

    "have correct subHeading" in new ViewFixture {
      def view = update_services_summary(EmptyForm, AddBusinessTypeFlowModel(), Seq(), Seq())
      subHeading.html must include(Messages("summary.updateservice"))
    }
  }

  "for which business type you wish to register" must {
    "have a question title" in new ViewFixture {
      val addBusinessTypeFlowModel:AddBusinessTypeFlowModel  = AddBusinessTypeFlowModel(activity = Some(AccountancyServices))
      def view = update_services_summary(EmptyForm, addBusinessTypeFlowModel, Seq(), Seq())

      doc.body().text must include(Messages("businessmatching.updateservice.selectactivities.title"))
    }

    "show edit link" in new SimpleFlowModelViewFixture {
      Option(doc.getElementById("selectactivities-edit")).isDefined mustBe true
    }

    "for all business types" must {
      "show AccountancyServices if present" in new ViewFixture {
        val addBusinessTypeFlowModel: AddBusinessTypeFlowModel = AddBusinessTypeFlowModel(activity = Some(AccountancyServices))

        def view = update_services_summary(EmptyForm, addBusinessTypeFlowModel, Seq(), Seq())

        doc.getElementById("activity-name").text mustBe (Messages("businessmatching.registerservices.servicename.lbl.01"))
      }


      "show BillPaymentServices if present" in new ViewFixture {
        val addBusinessTypeFlowModel: AddBusinessTypeFlowModel = AddBusinessTypeFlowModel(activity = Some(BillPaymentServices))

        def view = update_services_summary(EmptyForm, addBusinessTypeFlowModel, Seq(), Seq())

        doc.getElementById("activity-name").text mustBe (Messages("businessmatching.registerservices.servicename.lbl.03"))
      }

      "show EstateAgentBusinessService if present" in new ViewFixture {
        val addBusinessTypeFlowModel: AddBusinessTypeFlowModel = AddBusinessTypeFlowModel(activity = Some(EstateAgentBusinessService))

        def view = update_services_summary(EmptyForm, addBusinessTypeFlowModel, Seq(), Seq())

        doc.getElementById("activity-name").text mustBe (Messages("businessmatching.registerservices.servicename.lbl.04"))
      }

      "show HighValueDealing if present" in new ViewFixture {
        val addBusinessTypeFlowModel: AddBusinessTypeFlowModel = AddBusinessTypeFlowModel(activity = Some(HighValueDealing))

        def view = update_services_summary(EmptyForm, addBusinessTypeFlowModel, Seq(), Seq())

        doc.getElementById("activity-name").text mustBe (Messages("businessmatching.registerservices.servicename.lbl.05"))
      }

      "show MoneyServiceBusiness if present" in new ViewFixture {
        val addBusinessTypeFlowModel: AddBusinessTypeFlowModel = AddBusinessTypeFlowModel(activity = Some(MoneyServiceBusiness))

        def view = update_services_summary(EmptyForm, addBusinessTypeFlowModel, Seq(), Seq())

        doc.getElementById("activity-name").text mustBe (Messages("businessmatching.registerservices.servicename.lbl.06"))
      }

      "show TrustAndCompanyServices if present" in new ViewFixture {
        val addBusinessTypeFlowModel: AddBusinessTypeFlowModel = AddBusinessTypeFlowModel(activity = Some(TrustAndCompanyServices))

        def view = update_services_summary(EmptyForm, addBusinessTypeFlowModel, Seq(), Seq())

        doc.getElementById("activity-name").text mustBe (Messages("businessmatching.registerservices.servicename.lbl.07"))
      }

      "show TelephonePaymentService if present" in new ViewFixture {
        val addBusinessTypeFlowModel: AddBusinessTypeFlowModel = AddBusinessTypeFlowModel(activity = Some(TelephonePaymentService))

        def view = update_services_summary(EmptyForm, addBusinessTypeFlowModel, Seq(), Seq())

        doc.getElementById("activity-name").text mustBe (Messages("businessmatching.registerservices.servicename.lbl.08"))
      }
    }
  }

  "if adding msb" must {
    "which services does your business provide" must {
      "have a question title" in new MSBAllViewFixture {
        doc.body().text must include(Messages("businessmatching.updateservice.msb.services.title"))
      }

      "show edit link" in new MSBAllViewFixture {
        Option(doc.getElementById("msbservices-edit")).isDefined mustBe true
      }

      "for all msb services" must {
        "show msb service TransmittingMoney" in new MSBAllViewFixture {
          doc.getElementById("msb-service").text must include(Messages("businessmatching.services.list.lbl.01"))
        }

        "show msb service CurrencyExchange" in new MSBAllViewFixture {
          doc.getElementById("msb-service").text must include(Messages("businessmatching.services.list.lbl.02"))
        }

        "show msb service ChequeCashingNotScrapMetal" in new MSBAllViewFixture {
          doc.getElementById("msb-service").text must include(Messages("businessmatching.services.list.lbl.03"))
        }

        "show msb service ChequeCashingScrapMetal" in new MSBAllViewFixture {
          doc.getElementById("msb-service").text must include(Messages("businessmatching.services.list.lbl.04"))
        }

        "show msb service ForeignExchange" in new MSBAllViewFixture {
          doc.getElementById("msb-service").text must include(Messages("businessmatching.services.list.lbl.05"))
        }
      }

      "for single msb" must {
        "show msb service TransmittingMoney" in new MSBSingleViewFixture {
          doc.getElementById("msb-service").text must include(Messages("businessmatching.services.list.lbl.01"))
        }

        "show msb service CurrencyExchange" in new MSBSingleViewFixture {
          doc.getElementById("msb-service").text must not include(Messages("businessmatching.services.list.lbl.02"))
        }

        "show msb service ChequeCashingNotScrapMetal" in new MSBSingleViewFixture {
          doc.getElementById("msb-service").text must not include(Messages("businessmatching.services.list.lbl.03"))
        }

        "show msb service ChequeCashingScrapMetal" in new MSBSingleViewFixture {
          doc.getElementById("msb-service").text must not include(Messages("businessmatching.services.list.lbl.04"))
        }

        "show msb service ForeignExchange" in new MSBSingleViewFixture {
          doc.getElementById("msb-service").text must not include(Messages("businessmatching.services.list.lbl.05"))
        }
      }
    }

    "if adding TransmittingMoney as an MSB subsector, for does your business have a PSR number" must {
      "have a question title" in new MSBAllViewFixture {
        doc.body().text must include(Messages("businessmatching.psr.number.title"))
      }

      "show edit link" in new MSBAllViewFixture {
        Option(doc.getElementById("psr-edit")).isDefined mustBe true
      }

      "have a PSR number" in new MSBAllViewFixture {
        doc.getElementById("psr").text mustBe Messages("businessmatching.psr.number.lbl") + ": 111111"
      }

      "have answered no to do I have a PSR number " in new MSBNoPSRViewFixture {
        doc.getElementById("psr").text mustBe Messages("lbl.no")
      }

      "not have transmitting money and not have a PSR section" in new MSBViewFixture {
        Option(doc.getElementById("psr")).isDefined mustBe false
      }
    }
  }

  "if not msb" in new SimpleFlowModelViewFixture {
    doc.body().text must not include(Messages("businessmatching.services.title"))
  }

  "The page have a submit button with correct text" in new MSBAllViewFixture {
    doc.getElementById("updatesummary-submit").text mustBe Messages("button.checkyouranswers.acceptandcomplete")
  }
}
