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

package controllers.declaration

import com.google.inject.Inject
import connectors.{AmlsConnector, DataCacheConnector}
import controllers.BaseController
import forms.{EmptyForm, Form2, InvalidForm, ValidForm}
import jto.validation.{Path, ValidationError}
import models.declaration._
import models.declaration.release7.RoleWithinBusinessRelease7
import models.responsiblepeople.{PositionWithinBusiness, ResponsiblePerson}
import models.status._
import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent, Request, Result}
import services.{RenewalService, StatusService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import views.html.declaration.{who_is_registering_this_registration, who_is_registering_this_renewal, who_is_registering_this_update}

import scala.concurrent.Future

class WhoIsRegisteringController @Inject () (
                                            val authConnector: AuthConnector,
                                            val dataCacheConnector: DataCacheConnector,
                                            val statusService: StatusService,
                                            val renewalService: RenewalService,
                                            val amlsConnector: AmlsConnector
                                            ) extends BaseController {

  def get = Authorised.async {
    implicit authContext => implicit request =>
      dataCacheConnector.fetchAll flatMap {
        optionalCache =>
          (for {
            cache <- optionalCache
            responsiblePeople <- cache.getEntry[Seq[ResponsiblePerson]](ResponsiblePerson.key)
          } yield whoIsRegisteringView(Ok, EmptyForm, ResponsiblePerson.filter(responsiblePeople))
          ) getOrElse whoIsRegisteringView(Ok, EmptyForm, Seq.empty)
      }
  }

  def post: Action[AnyContent] = Authorised.async {
    implicit authContext => implicit request => {
      Form2[WhoIsRegistering](request.body) match {
        case f: InvalidForm =>
          dataCacheConnector.fetch[Seq[ResponsiblePerson]](ResponsiblePerson.key) flatMap {
            case Some(data) =>
              whoIsRegisteringViewWithError(BadRequest, f, ResponsiblePerson.filter(data))
            case None => whoIsRegisteringViewWithError(BadRequest, f, Seq.empty)
          }
        case ValidForm(_, data) =>
          dataCacheConnector.fetchAll flatMap {
            optionalCache =>
              (for {
                cache <- optionalCache
                responsiblePeople <- cache.getEntry[Seq[ResponsiblePerson]](ResponsiblePerson.key)
              } yield {
                data.person match {
                  case "-1" =>
                    redirectToAddPersonPage
                  case _ =>
                    getAddPerson(data, ResponsiblePerson.filter(responsiblePeople)) map { addPerson =>
                      dataCacheConnector.save[AddPerson](AddPerson.key, addPerson) flatMap {
                        _ => redirectToDeclarationPage
                      }
                    } getOrElse Future.successful(NotFound(notFoundView))
                }
              }) getOrElse redirectToDeclarationPage
          }
      }
    }
  }

  def whoIsRegisteringViewWithError(status: Status, form: InvalidForm, rp: Seq[ResponsiblePerson])
                                  (implicit auth: AuthContext, request: Request[AnyContent]): Future[Result] =
    statusService.getStatus flatMap {
      case a@(SubmissionReadyForReview | SubmissionDecisionApproved | ReadyForRenewal(_)) =>
        renewalService.getRenewal map {
          case Some(x) =>
            val updatedForm = updateFormErrors(form, a, renewal = true)
            status(who_is_registering_this_renewal(updatedForm, rp))
          case _ =>
            val updatedForm = updateFormErrors(form, a, renewal = false)
            status(who_is_registering_this_update(updatedForm, rp))
        }
      case b@RenewalSubmitted(_) =>
        val updatedForm = updateFormErrors(form, b, renewal = true)
        Future.successful(status(who_is_registering_this_update(updatedForm, rp)))
      case _ =>
        Future.successful(status(who_is_registering_this_registration(form, rp)))
    }

  def updateFormErrors(f: InvalidForm, status: SubmissionStatus, renewal: Boolean): InvalidForm = {
    val common = "error.required.declaration.who.is.declaring.this"
    status match {
      case SubmissionReadyForReview | SubmissionDecisionApproved | ReadyForRenewal(_) =>
        if (renewal) {
          f.copy(errors = Seq((Path("person"), Seq(ValidationError(Seq(Messages(s"$common.renewal")))))))
        } else {
          f.copy(errors = Seq((Path("person"), Seq(ValidationError(Seq(Messages(s"$common.update")))))))
        }
      case RenewalSubmitted(_) =>
        f.copy(errors = Seq((Path("person"), Seq(ValidationError(Seq(Messages(s"$common.update")))))))
      case _ =>
        f.copy(errors = Seq((Path("person"), Seq(ValidationError(Seq(Messages("error.required.declaration.who.is.registering")))))))
    }
  }

  private def whoIsRegisteringView(status: Status, form: Form2[WhoIsRegistering], rp: Seq[ResponsiblePerson])
                                  (implicit auth: AuthContext, request: Request[AnyContent]): Future[Result] =
    statusService.getStatus flatMap {
      case SubmissionReadyForReview | SubmissionDecisionApproved | ReadyForRenewal(_) =>
        renewalService.getRenewal map {
          case Some(_) =>
            status(who_is_registering_this_renewal(form, rp))
          case _ =>
            status(who_is_registering_this_update(form, rp))
        }
      case RenewalSubmitted(_) =>
        Future.successful(status(who_is_registering_this_update(form, rp)))
      case _ =>
        Future.successful(status(who_is_registering_this_registration(form, rp)))
    }

  private def redirectToDeclarationPage(implicit hc: HeaderCarrier, auth: AuthContext): Future[Result] =
    statusService.getStatus map {
      case SubmissionReadyForReview | SubmissionDecisionApproved => Redirect(routes.DeclarationController.getWithAmendment())
      case _ => Redirect(routes.DeclarationController.get())
    }

  private def redirectToAddPersonPage(implicit hc: HeaderCarrier, auth: AuthContext): Future[Result] =
    statusService.getStatus map {
      case SubmissionReadyForReview | SubmissionDecisionApproved => Redirect(routes.AddPersonController.getWithAmendment())
      case _ => Redirect(routes.AddPersonController.get())
    }

  private def getAddPerson(whoIsRegistering: WhoIsRegistering, responsiblePeople: Seq[ResponsiblePerson]): Option[AddPerson] = {
    for {
      selectedIndex <- whoIsRegistering.indexValue
      selectedPerson <- responsiblePeople.zipWithIndex.collect {
        case (person, i) if i == selectedIndex => person
      }.headOption
      personName <- selectedPerson.personName
    } yield {
      AddPerson(
        personName.firstName,
        personName.middleName,
        personName.lastName,
        selectedPerson.positions.fold[Set[PositionWithinBusiness]](Set.empty)(x => x.positions)
      )
    }
  }

  private implicit def getPosition(positions: Set[PositionWithinBusiness]): RoleWithinBusinessRelease7 = {
    import models.responsiblepeople._

    RoleWithinBusinessRelease7(
      positions.map {
        case BeneficialOwner => models.declaration.release7.BeneficialShareholder
        case Director => models.declaration.release7.Director
        case InternalAccountant => models.declaration.release7.InternalAccountant
        case NominatedOfficer => models.declaration.release7.NominatedOfficer
        case Partner => models.declaration.release7.Partner
        case SoleProprietor => models.declaration.release7.SoleProprietor
        case DesignatedMember => models.declaration.release7.DesignatedMember
        case Other(d) => models.declaration.release7.Other(d)
      }
    )
  }
}
