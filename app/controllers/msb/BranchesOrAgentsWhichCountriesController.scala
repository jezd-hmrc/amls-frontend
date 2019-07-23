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

package controllers.msb

import connectors.DataCacheConnector
import controllers.BaseController
import forms.{EmptyForm, Form2, InvalidForm, ValidForm}
import javax.inject.Inject
import models.moneyservicebusiness.{BranchesOrAgentsHasCountries, BranchesOrAgentsWhichCountries, BranchesOrAgents, MoneyServiceBusiness}
import services.AutoCompleteService
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

class BranchesOrAgentsWhichCountriesController @Inject()(val dataCacheConnector: DataCacheConnector,
                                                         val authConnector: AuthConnector,
                                                         val autoCompleteService: AutoCompleteService
                                           ) extends BaseController {

  def get(edit: Boolean = false) = Authorised.async {
    implicit authContext => implicit request =>
      dataCacheConnector.fetch[MoneyServiceBusiness](MoneyServiceBusiness.key) map {
        response =>
          val form = (for {
            msb <- response
            boa <- msb.branchesOrAgents
            branches <- boa.branches
          } yield Form2[BranchesOrAgentsWhichCountries](branches)).getOrElse(EmptyForm)

          Ok(views.html.msb.branches_or_agents_which_countries(form, edit, autoCompleteService.getCountries))
      }
  }

  def post(edit: Boolean = false) = Authorised.async {
    implicit authContext => implicit request =>
      Form2[BranchesOrAgentsWhichCountries](request.body) match {
        case f: InvalidForm =>
          Future.successful(BadRequest(views.html.msb.branches_or_agents_which_countries(removeEmptyFields(f), edit, autoCompleteService.getCountries)))
        case ValidForm(_, data) =>
          for {
            msb <- dataCacheConnector.fetch[MoneyServiceBusiness](MoneyServiceBusiness.key)
            _ <- dataCacheConnector.save[MoneyServiceBusiness](MoneyServiceBusiness.key,
              msb.branchesOrAgents(BranchesOrAgents.update(msb.branchesOrAgents.getOrElse(BranchesOrAgents(BranchesOrAgentsHasCountries(false), None)), data)))
          } yield edit match {
            case false =>
              Redirect(routes.IdentifyLinkedTransactionsController.get())
            case true =>
              Redirect(routes.SummaryController.get())
          }
      }
  }

  def removeEmptyFields(f: InvalidForm): InvalidForm = {
    val csrfToken = f.data.head
    val fieldsWithData:Map[String, Seq[String]] = f.data
      .filter(field => field._1.contains("countries"))
      .filter(field => field._2.exists(s => s.nonEmpty))
      .zipWithIndex
      .map((tuple: ((String, Seq[String]), Int)) => (tuple._1._1.replaceFirst("[\\d]", s"${tuple._2 / 2}"), tuple._1._2))
    f.copy(data = fieldsWithData + csrfToken)
  }
}