/*
 * Copyright 2017 HM Revenue & Customs
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

package controllers.businessmatching.updateservice

import javax.inject.Inject

import cats.data.OptionT
import cats.implicits._
import connectors.DataCacheConnector
import controllers.BaseController
import forms.{EmptyForm, Form2, InvalidForm, ValidForm}
import models.DateOfChange
import models.businessmatching.updateservice.{UpdateService, TradingPremisesActivities => TradingPremisesForm}
import models.businessmatching.{BusinessActivities, BusinessActivity}
import models.tradingpremises.{TradingPremises, WhatDoesYourBusinessDo}
import services.businessmatching.BusinessMatchingService
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.http.HeaderCarrier
import utils.{RepeatingSection, StatusConstants}
import views.html.businessmatching.updateservice.which_current_trading_premises

class WhichCurrentTradingPremisesController @Inject()(val authConnector: AuthConnector,
                                                      val dataCacheConnector: DataCacheConnector,
                                                       businessMatchingService: BusinessMatchingService) extends BaseController with RepeatingSection {

  private val failure = InternalServerError("Could not get form data")

  def get() = Authorised.async {
    implicit authContext => implicit request =>
      {
        for {
          (tp, _, act) <- formData
        } yield Ok(which_current_trading_premises(EmptyForm, tp, BusinessActivities.getValue(act)))
      } getOrElse failure
  }

  def post() = Authorised.async {
    implicit authContext => implicit request =>
      Form2[TradingPremisesForm](request.body) match {
        case f: InvalidForm => {
          for {
            (tradingPremises, _, act) <- formData
          } yield BadRequest(which_current_trading_premises(f, tradingPremises, BusinessActivities.getValue(act)))
        } getOrElse failure

        case ValidForm(_, data) => {
          for {
            (tp, _, act) <- formData
            _ <- OptionT.liftF(dataCacheConnector.save[Seq[TradingPremises]](TradingPremises.key, fixActivities(tp.map(_._1), data.index, act)))
          } yield Redirect(controllers.routes.RegistrationProgressController.get())
        } getOrElse failure
      }
  }

  private def fixActivities(tp: Seq[TradingPremises], selected: Set[Int], activity: BusinessActivity): Seq[TradingPremises] = tp.zipWithIndex.collect {
    case (m, i) if !selected.contains(i) && m.whatDoesYourBusinessDoAtThisAddress.isDefined =>
      updateActivities(m, m.whatDoesYourBusinessDoAtThisAddress.get.activities - activity)
    case (m, i) if m.whatDoesYourBusinessDoAtThisAddress.isDefined =>
      updateActivities(m, m.whatDoesYourBusinessDoAtThisAddress.get.activities + activity)
    case (m, _) => m
  }

  private def updateActivities(tp: TradingPremises, activities: Set[BusinessActivity]) =
    tp.whatDoesYourBusinessDoAtThisAddress(WhatDoesYourBusinessDo(activities, tp.whatDoesYourBusinessDoAtThisAddress.fold(none[DateOfChange])(_.dateOfChange)))

  private def formData(implicit hc: HeaderCarrier, ac: AuthContext) = for {
    tp <- getTradingPremises
    activities <- businessMatchingService.getSubmittedBusinessActivities
    } yield (tp, activities, activities.head)

  private def getTradingPremises(implicit hc: HeaderCarrier, ac: AuthContext) =
    OptionT.liftF(getData[TradingPremises].map{ tradingpremises =>
      tradingpremises.zipWithIndex.filterNot{ case (tp, _) =>
        tp.status.contains(StatusConstants.Deleted) | !tp.isComplete
      }
    })

}