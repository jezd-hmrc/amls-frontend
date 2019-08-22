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

package services

import cats.data.OptionT
import cats.data.Validated.Valid
import cats.implicits._
import connectors.DataCacheConnector
import javax.inject.{Inject, Singleton}
import models.businessmatching._
import models.registrationprogress.{Completed, NotStarted, Section, Started}
import models.renewal.Renewal.ValidationRules._
import models.renewal._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.frontend.auth.AuthContext
import utils.MappingUtils._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RenewalService @Inject()(dataCache: DataCacheConnector) {

  @deprecated("To be removed when auth upgrade is implemented")
  def getSection(implicit authContext: AuthContext, headerCarrier: HeaderCarrier, ec: ExecutionContext) = {

    val notStarted = Section(Renewal.sectionKey, NotStarted, hasChanged = false, controllers.renewal.routes.WhatYouNeedController.get())

    this.getRenewal flatMap {
      case Some(model) =>
        isRenewalComplete(model) flatMap { x =>
          if (x) {
            Future.successful(Section(Renewal.sectionKey, Completed, model.hasChanged, controllers.renewal.routes.SummaryController.get()))
          } else {
            model match {
              case Renewal(None, None, None, None, _, _, _, _, _, _, _, _, _, _, _, _, _) => Future.successful(notStarted)
              case _ => Future.successful(Section(Renewal.sectionKey, Started, model.hasChanged, controllers.renewal.routes.WhatYouNeedController.get()))
            }
          }
        }
      case _ => Future.successful(notStarted)
    }
  }

  def getSection(credId: String)(implicit headerCarrier: HeaderCarrier, ec: ExecutionContext) = {

    val notStarted = Section(Renewal.sectionKey, NotStarted, hasChanged = false, controllers.renewal.routes.WhatYouNeedController.get())

    this.getRenewal(credId).flatMap {
      case Some(model) =>
        isRenewalComplete(model, credId) flatMap { x =>
          if (x) {
            Future.successful(Section(Renewal.sectionKey, Completed, model.hasChanged, controllers.renewal.routes.SummaryController.get()))
          } else {
            model match {
              case Renewal(None, None, None, None, _, _, _, _, _, _, _, _, _, _, _, _, _) => Future.successful(notStarted)
              case _ => Future.successful(Section(Renewal.sectionKey, Started, model.hasChanged, controllers.renewal.routes.WhatYouNeedController.get()))
            }
          }
        }
      case _ => Future.successful(notStarted)
    }
  }

  @deprecated("To be removed when auth upgrade is implemented")
  def getRenewal(implicit authContext: AuthContext, headerCarrier: HeaderCarrier, ec: ExecutionContext) =
    dataCache.fetch[Renewal](Renewal.key)

  def getRenewal(cacheId: String)(implicit headerCarrier: HeaderCarrier, ec: ExecutionContext) =
    dataCache.fetch[Renewal](cacheId, Renewal.key)

  @deprecated("To be removed when auth upgrade is implemented")
  def updateRenewal(renewal: Renewal)(implicit authContext: AuthContext, headerCarrier: HeaderCarrier, ec: ExecutionContext) =
    dataCache.save[Renewal](Renewal.key, renewal)

  def updateRenewal(credId: String, renewal: Renewal)(implicit headerCarrier: HeaderCarrier, ec: ExecutionContext) =
    dataCache.save[Renewal](credId, Renewal.key, renewal)

  @deprecated("To be removed when auth upgrade is implemented")
  def isRenewalComplete(renewal: Renewal)(implicit authContext: AuthContext, headerCarrier: HeaderCarrier, ec: ExecutionContext) = {

    val isComplete = for {
      cache <- OptionT(dataCache.fetchAll)
      businessMatching <- OptionT.fromOption[Future](cache.getEntry[BusinessMatching](BusinessMatching.key))
      activities <- OptionT.fromOption[Future](businessMatching.activities)
    } yield {

      activities.businessActivities collect {
        case MoneyServiceBusiness => checkCompletionOfMsb(renewal, businessMatching.msbServices)
        case HighValueDealing => checkCompletionOfHvd(renewal)
        case AccountancyServices => checkCompletionOfAsp(renewal)
      } match {
        case s if s.nonEmpty => s.forall(identity)

        case _ => standardRule.validate(renewal) match {
          case Valid(_) => true
          case _ => false
        }
      }
    }

    isComplete.getOrElse(false)
  }

  def isRenewalComplete(renewal: Renewal, credId: String)(implicit headerCarrier: HeaderCarrier, ec: ExecutionContext) = {

    val isComplete = for {
      cache <- OptionT(dataCache.fetchAll(credId))
      businessMatching <- OptionT.fromOption[Future](cache.getEntry[BusinessMatching](BusinessMatching.key))
      activities <- OptionT.fromOption[Future](businessMatching.activities)
    } yield {

      activities.businessActivities collect {
        case MoneyServiceBusiness => checkCompletionOfMsb(renewal, businessMatching.msbServices)
        case HighValueDealing => checkCompletionOfHvd(renewal)
        case AccountancyServices => checkCompletionOfAsp(renewal)
      } match {
        case s if s.nonEmpty => s.forall(identity)

        case _ => standardRule.validate(renewal) match {
          case Valid(_) => true
          case _ => false
        }
      }
    }

    isComplete.getOrElse(false)
  }

  private def checkCompletionOfMsb(renewal: Renewal, msbServices: Option[BusinessMatchingMsbServices]) = {

    val validationRule = compileOpt {
      Seq(
        Some(msbRule),
        if (msbServices.exists(_.msbServices.contains(TransmittingMoney))) Some(moneyTransmitterRule) else None,
        if (msbServices.exists(_.msbServices.contains(CurrencyExchange))) Some(currencyExchangeRule) else None,
        if (msbServices.exists(_.msbServices.contains(ForeignExchange))) Some(foreignExchangeRule) else None,
        Some(standardRule)
      )
    }

    // Validate the renewal object using the composed chain of validation rules
    validationRule.validate(renewal) match {
      case Valid(_) => true
      case r => false
    }
  }

  private def checkCompletionOfAsp(renewal: Renewal) = {

    val validationRule = compileOpt {
      Seq(
        Some(aspRule),
        Some(standardRule)
      )
    }

    // Validate the renewal object using the composed chain of validation rules
    validationRule.validate(renewal) match {
      case Valid(_) => true
      case r => false
    }
  }

  private def checkCompletionOfHvd(renewal: Renewal) = {

    val validationRule = compileOpt {
      Seq(
        Some(hvdRule),
        Some(standardRule)
      )
    }

    // Validate the renewal object using the composed chain of validation rules
    validationRule.validate(renewal) match {
      case Valid(_) => true
      case r => false
    }
  }

  def canSubmit(renewalSection: Section, variationSections: Seq[Section]) = {
    variationSections.forall(_.status == Completed) &&
            !renewalSection.status.equals(Started) &&
            (variationSections :+ renewalSection).exists(_.hasChanged)
  }

}
