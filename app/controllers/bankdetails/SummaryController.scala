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

package controllers.bankdetails

import cats.data.OptionT
import cats.implicits._
import config.AMLSAuthConnector
import connectors.DataCacheConnector
import controllers.BaseController
import forms.EmptyForm
import models.bankdetails.{BankAccount, BankDetails}
import models.status.{NotCompleted, SubmissionReady}
import services.StatusService
import uk.gov.hmrc.play.http.HeaderCarrier
import utils.StatusConstants

import scala.concurrent.Future

trait SummaryController extends BaseController {

  protected def dataCache: DataCacheConnector
  val statusService: StatusService

  private def updateBankDetails(bankDetails: Option[Seq[BankDetails]]) : Future[Option[Seq[BankDetails]]] = {
    bankDetails match {
      case Some(bdSeq) => {
        val updatedList = bdSeq.map { bank =>
          bank.copy(hasAccepted = true)
        }
        Future.successful(Some(updatedList))
      }
      case _ => Future.successful(bankDetails)
    }
  }

  def get(complete: Boolean = false) = Authorised.async {
    implicit authContext => implicit request =>
      for {
        bankDetails <- dataCache.fetch[Seq[BankDetails]](BankDetails.key)
        status <- statusService.getStatus
      } yield bankDetails match {
        case Some(data) => {
          val canEdit = status match {
            case NotCompleted | SubmissionReady => true
            case _ => false
          }
          val bankDetails = data.filterNot(_.status.contains(StatusConstants.Deleted))
          Ok(views.html.bankdetails.summary(EmptyForm, data, complete, hasBankAccount(bankDetails), canEdit, status))
        }
        case _ => Redirect(controllers.routes.RegistrationProgressController.get())
      }
  }

  def post = Authorised.async {
    implicit authContext => implicit request =>
      (for {
        bd <- dataCache.fetch[Seq[BankDetails]](BankDetails.key)
        bdnew <- updateBankDetails(bd)
        _ <- dataCache.save[Seq[BankDetails]](BankDetails.key, bdnew.getOrElse(Seq.empty))
      } yield Redirect(controllers.routes.RegistrationProgressController.get())) recoverWith {
        case _: Throwable => Future.successful(InternalServerError("Unable to save data and get redirect link"))
      }
  }

  private def hasBankAccount(bankDetails: Seq[BankDetails]): Boolean = {
    bankDetails.exists(_.bankAccount.isDefined)
  }
}

object SummaryController extends SummaryController {
  // $COVERAGE-OFF$
  override val dataCache = DataCacheConnector
  override val authConnector = AMLSAuthConnector
  override val statusService = StatusService
}
