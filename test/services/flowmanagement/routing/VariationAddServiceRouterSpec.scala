/*
 * Copyright 2018 HM Revenue & Customs
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

package services.flowmanagement.routing

import controllers.businessmatching.updateservice.add.{routes => addRoutes}
import models.businessmatching.updateservice.TradingPremisesActivities
import models.businessmatching.{BillPaymentServices, HighValueDealing}
import models.flowmanagement._
import org.scalatestplus.play.PlaySpec
import play.api.mvc.Results.Redirect
import play.api.test.Helpers._
import services.flowmanagement.routings._

class VariationAddServiceRouterSpec extends PlaySpec {

  trait Fixture {
    val routingFile = VariationAddServiceRouter.router
  }

  "getRoute" must {

    "return the 'trading premises' page (TradingPremisesController)" when {
      "given the 'BusinessActivities' model contains a single activity" in new Fixture {
        val model = AddServiceFlowModel(
          activity = Some(HighValueDealing))
        val result = await(routingFile.getRoute(SelectActivitiesPageId, model))

        result mustBe Redirect(addRoutes.TradingPremisesController.get())
      }
    }

    "return the 'Check your answers' page (UpdateServicesSummaryController)" when {
      "given the activity is not done at any trading premises" +
        "and the activity requires further information" in new Fixture {
        val model = AddServiceFlowModel(
          activity = Some(HighValueDealing),
          areNewActivitiesAtTradingPremises = Some(false))

        val result = await(routingFile.getRoute(TradingPremisesPageId, model))

        result mustBe Redirect(addRoutes.UpdateServicesSummaryController.get())
      }
    }

    "return the 'Check your answers' page (UpdateServicesSummaryController)" when {
      "given we've chosen an activity" +
        "and we're in the edit flow" in new Fixture {
        val model = AddServiceFlowModel(
          activity = Some(HighValueDealing),
          areNewActivitiesAtTradingPremises = Some(false))

        val result = await(routingFile.getRoute(SelectActivitiesPageId, model, edit = true))

        result mustBe Redirect(addRoutes.UpdateServicesSummaryController.get())
      }
    }

    "return the 'Check your answers' page (UpdateServicesSummaryController)" when {
      "editing the trading premises yes/no question" +
      "the trading premises have already been selected" in new Fixture {
        val model = AddServiceFlowModel(
          activity = Some(HighValueDealing),
          areNewActivitiesAtTradingPremises = Some(true),
          tradingPremisesActivities = Some(TradingPremisesActivities(Set(0, 1))))

        val result = await(routingFile.getRoute(TradingPremisesPageId, model, edit = true))

        result mustBe Redirect(addRoutes.UpdateServicesSummaryController.get())
      }
    }

    "return the 'which trading premises' page (WhichTradingPremisesController)" when {
      "given the 'NewActivitiesAtTradingPremisesYes' model contains HVD" in new Fixture {
        val model = AddServiceFlowModel(
          activity = Some(HighValueDealing),
          areNewActivitiesAtTradingPremises = Some(true))
        val result = await(routingFile.getRoute(TradingPremisesPageId, model))

        result mustBe Redirect(addRoutes.WhichTradingPremisesController.get())
      }
    }

    "return the 'Check your answers' page (UpdateServicesSummaryController)" when {
      "given a set of trading premises has been chosen" in new Fixture {
        val model = AddServiceFlowModel(
          activity = Some(HighValueDealing),
          areNewActivitiesAtTradingPremises = Some(true),
          tradingPremisesActivities = Some(TradingPremisesActivities(Set(0,1,2)))
        )

        val result = await(routingFile.getRoute(WhichTradingPremisesPageId, model))

        result mustBe Redirect(addRoutes.UpdateServicesSummaryController.get())
      }
    }

    "return the 'Do you want add more activities' page (addMoreActivitiesController)" when {
      "we're on the summary page and the user selects continue" in new Fixture {
        val model = AddServiceFlowModel(
          activity = Some(HighValueDealing),
          areNewActivitiesAtTradingPremises = Some(true))

        val result = await(routingFile.getRoute(UpdateServiceSummaryPageId, model))

        result mustBe Redirect(addRoutes.AddMoreActivitiesController.get())
      }
    }

    "redirect to the 'Registration Progress' page" when {
        "we're on the summary page and the user selects continue " +
        "if all possible activities are added" +
        " and a none of the new ones require more information" ignore new Fixture {
          fail()
//        val model = AddServiceFlowModel(
//          activity = Some(AccountancyServices),
//          addMoreActivities = Some(false))
//
//        val result = await(routingFile.getRoute(UpdateServiceSummaryPageId, model))
//
//        result mustBe Redirect(controllers.routes.RegistrationProgressController.get())
      }
    }

//AccountancyServices extends BusinessActivity
//case object BillPaymentServices extends  BusinessActivity
//case object EstateAgentBusinessService extends BusinessActivity
//case object HighValueDealing extends BusinessActivity
//case object MoneyServiceBusiness extends BusinessActivity
//case object TrustAndCompanyServices extends BusinessActivity
//case object TelephonePaymentService extends BusinessActivity


    "redirect to the 'New Service Information' page" when {
      "we're on the summary page and the user selects continue " +
        "and if all possible activities are added" +
        " and a new one requires more information" ignore new Fixture {
fail()
      }
    }

    "return the 'Activities selection' page (SelectActivitiesController)" when {
      "we're on the 'Do you want at add more activities' page " +
        "and the use wants to add more activities" in new Fixture {
        val model = AddServiceFlowModel(
          activity = Some(HighValueDealing),
          areNewActivitiesAtTradingPremises = Some(true),
          addMoreActivities = Some(true))

        val result = await(routingFile.getRoute(AddMoreAcivitiesPageId, model))

        result mustBe Redirect(addRoutes.SelectActivitiesController.get())
      }
    }

    "return the 'New Service questions' page (NewServiceInformationController)" when {
      "we're on the 'Do you want at add more activities' page " +
      "and the user has added Activities that require more questions" +
        "and the use doesn't want to add more activities" in new Fixture {
        val model = AddServiceFlowModel(
          activity = Some(HighValueDealing),
          areNewActivitiesAtTradingPremises = Some(true),
          addMoreActivities = Some(false))

        val result = await(routingFile.getRoute(AddMoreAcivitiesPageId, model))

        result mustBe Redirect(addRoutes.NewServiceInformationController.get())
      }
    }

    "return the 'Registration progress' page (RegistrationProgressController)" when {
      "we're on the 'Do you want at add more activities' page " +
        "and the user has NOT added Activities that require more questions" +
        "and the use doesn't want to add more activities" in new Fixture {
        val model = AddServiceFlowModel(
          activity = Some(BillPaymentServices),
          addMoreActivities = Some(false))

        val result = await(routingFile.getRoute(AddMoreAcivitiesPageId, model))

        result mustBe Redirect(controllers.routes.RegistrationProgressController.get())
      }
    }

    "return the 'registration progress' page" when {
      "we're on the 'new service information' page" in new Fixture {
        val model = AddServiceFlowModel(
          activity = Some(HighValueDealing),
          areNewActivitiesAtTradingPremises = Some(true))

        val result = await(routingFile.getRoute(NewServiceInformationPageId, model))

        result mustBe Redirect(controllers.routes.RegistrationProgressController.get())
      }
    }

  }

}
