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

package controllers.tradingpremises

import connectors.DataCacheConnector
import controllers.{AmlsBaseController, CommonPlayDependencies}
import forms.{Form2, _}
import javax.inject.{Inject, Singleton}
import models.DateOfChange
import models.status.SubmissionDecisionApproved
import models.tradingpremises._
import org.joda.time.LocalDate
import play.api.libs.json.Format
import play.api.mvc.MessagesControllerComponents
import services.StatusService
import typeclasses.MongoKey
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.{AuthAction, DateOfChangeHelper, RepeatingSection}
import views.html.date_of_change
import views.html.tradingpremises.agent_name

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class AgentNameController @Inject()(
                                     val dataCacheConnector: DataCacheConnector,
                                     val authAction: AuthAction,
                                     val ds: CommonPlayDependencies,
                                     val statusService: StatusService,
                                     val cc: MessagesControllerComponents,
                                     agent_name: agent_name,
                                     date_of_change: date_of_change,
                                     implicit val error: views.html.error) extends AmlsBaseController(ds, cc) with RepeatingSection with DateOfChangeHelper with FormHelpers {

  def get(index: Int, edit: Boolean = false) = authAction.async {
      implicit request =>

        getData[TradingPremises](request.credId, index) map {

          case Some(tp) => {
            val form = tp.agentName match {
              case Some(data) => Form2[AgentName](data)
              case None => EmptyForm
            }
            Ok(agent_name(form, index, edit))
          }
          case None => NotFound(notFoundView)
        }
  }

  def post(index: Int, edit: Boolean = false) = authAction.async {
      implicit request => {
        Form2[AgentName](request.body) match {
          case f: InvalidForm =>
            Future.successful(BadRequest(agent_name(f, index, edit)))
          case ValidForm(_, data) => {
            for {
              result <- fetchAllAndUpdateStrict[TradingPremises](request.credId, index) { (_, tp) =>
                TradingPremises(
                  registeringAgentPremises = tp.registeringAgentPremises,
                  yourTradingPremises = tp.yourTradingPremises,
                  businessStructure = tp.businessStructure,
                  agentName = Some(data),
                  agentCompanyDetails = None, agentPartnership = None,
                  whatDoesYourBusinessDoAtThisAddress = tp.whatDoesYourBusinessDoAtThisAddress,
                  msbServices = tp.msbServices,
                  hasChanged = true,
                  lineId = tp.lineId,
                  status = tp.status,
                  endDate = tp.endDate) }
              status <- statusService.getStatus(request.amlsRefNumber, request.accountTypeId, request.credId)
            } yield {
              val nextPage = status match {
                case SubmissionDecisionApproved if redirectToAgentNameDateOfChange(getTradingPremises(result, index), data) =>
                  Redirect(routes.AgentNameController.dateOfChange(index))
                case _ => if (edit) {
                  Redirect(routes.DetailedAnswersController.get(index))
                } else {
                  TPControllerHelper.redirectToNextPage(result, index, edit)
                }
              }
              nextPage
            }
          }.recoverWith {
            case _: IndexOutOfBoundsException => Future.successful(NotFound(notFoundView))
          }

          case _ => Future.successful(NotFound(notFoundView))
        }
      }

  }

  def getTradingPremises(result: Option[CacheMap], index: Int)(implicit hc: HeaderCarrier,
                                                               formats: Format[TradingPremises],
                                                               key: MongoKey[TradingPremises]) =
    result flatMap { cache => getData(cache, index) }

  def dateOfChange(index: Int) = authAction.async {
    implicit request =>
          Future(Ok(date_of_change(EmptyForm,
            "summary.tradingpremises", routes.AgentNameController.saveDateOfChange(index))))
  }

  def saveDateOfChange(index: Int) = authAction.async {
      implicit request =>
        getData[TradingPremises](request.credId, index) flatMap { tradingPremises =>
          Form2[DateOfChange](request.body.asFormUrlEncoded.get ++ startDateFormFields(tradingPremises.startDate)) match {
            case form: InvalidForm =>
              Future.successful(BadRequest(date_of_change(
                form.withMessageFor(DateOfChange.errorPath, tradingPremises.startDateValidationMessage),
                "summary.tradingpremises", routes.AgentNameController.saveDateOfChange(index))))
            case ValidForm(_, dateOfChange) =>
              for {
                _ <- updateDataStrict[TradingPremises](request.credId, index) { tp =>
                  tp.agentName(tradingPremises.agentName.get.copy(dateOfChange = Some(dateOfChange)))
                }
              } yield Redirect(routes.DetailedAnswersController.get(index))
          }
        }
  }

  private def redirectToAgentNameDateOfChange(tradingPremises: TradingPremises, agent: AgentName) = {
    def isAgentNameAndDobChanged = tradingPremises.agentName match {
      case Some(AgentName(agentName, _, agentDateOfBirth)) if isAgentDataUnchanged(agentName, agentDateOfBirth, agent) => false
      case _ => true
    }

    isAgentNameAndDobChanged && tradingPremises.lineId.isDefined
  }

  private def isAgentDataUnchanged(name: String, dob: Option[LocalDate], agent: AgentName) = {
    name == agent.agentName && dob == agent.agentDateOfBirth
  }
}
