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

package services.flowmanagement.flowrouters

import javax.inject.{Inject, Singleton}
import models.flowmanagement._
import play.api.mvc.Result
import play.api.mvc.Results.InternalServerError
import services.StatusService
import services.businessmatching.BusinessMatchingService
import services.flowmanagement.Router
import services.flowmanagement.pagerouters._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.frontend.auth.AuthContext

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DavesVariationAddServiceRouter @Inject()(val statusService: StatusService,
                                               val businessMatchingService: BusinessMatchingService,
                                               val addMoreActivitiesPageRouter: AddMoreActivitiesPageRouter,
                                               val fitAndProperPageRoutes: FitAndProperPageRouter,
                                               val newServicesInformationPageRouter: NewServicesInformationPageRouter,
                                               val selectActivitiesPageRouter: SelectActivitiesPageRouter,
                                               val tradingPremisesPageRouter: TradingPremisesPageRouter,
                                               val updateServicesSummaryPageRoutes: UpdateServicesSummaryPageRouter,
                                               val whichFitAndProperPageRoutes: WhichFitAndProperPageRouter,
                                               val whichTradingPremisesPageRouter: WhichTradingPremisesPageRouter
                                              ) extends Router[AddServiceFlowModel] {


  override def getRoute(pageId: PageId, model: AddServiceFlowModel, edit: Boolean = false)
                       (implicit  ac: AuthContext, hc: HeaderCarrier, ec: ExecutionContext): Future[Result] = {
    pageId match {
      case AddMoreAcivitiesPageId => addMoreActivitiesPageRouter.getPageRoute(model, edit)
      case FitAndProperPageId => fitAndProperPageRoutes.getPageRoute(model, edit)
      case NewServiceInformationPageId => newServicesInformationPageRouter.getPageRoute(model, edit)
      case SelectActivitiesPageId => selectActivitiesPageRouter.getPageRoute(model, edit)
      case TradingPremisesPageId => tradingPremisesPageRouter.getPageRoute(model, edit)
      case UpdateServiceSummaryPageId => updateServicesSummaryPageRoutes.getPageRoute(model, edit)
      case WhichFitAndProperPageId => whichFitAndProperPageRoutes.getPageRoute(model, edit)
      case WhichTradingPremisesPageId => whichTradingPremisesPageRouter.getPageRoute(model, edit)
    }
  }
  //scalastyle:off cyclomatic.complexity
  // scalastyle:off method.length

      //    case SelectActivitiesPageId if edit && model.areNewActivitiesAtTradingPremises.isDefined =>
      //      Future.successful(Redirect(addRoutes.UpdateServicesSummaryController.get()))
      //
      //    case SelectActivitiesPageId => {
      //      model.activity match {
      //        case Some(TrustAndCompanyServices) => Future.successful(Redirect(addRoutes.FitAndProperController.get()))
      //        //case Some(MoneyServiceBusiness) => Future.successful(Redirect(addRoutes.TradingPremisesController.get()))
      //        case _ => Future.successful(Redirect(addRoutes.TradingPremisesController.get()))
      //      }
      //    }

      //    case FitAndProperPageId if edit && model.responsiblePeople.isDefined =>
      //      Future.successful(Redirect(addRoutes.UpdateServicesSummaryController.get()))
      //
      //    case FitAndProperPageId =>
      //      (model.fitAndProper, edit) match {
      //        case (Some(true), _) => Future.successful(Redirect(addRoutes.WhichFitAndProperController.get()))
      //        case (Some(false), true) => Future.successful(Redirect(addRoutes.UpdateServicesSummaryController.get()))
      //        case (Some(false), false) => Future.successful(Redirect(addRoutes.TradingPremisesController.get()))
      //      }

      //    case WhichFitAndProperPageId =>
      //      edit match {
      //        case true => Future.successful(Redirect(addRoutes.UpdateServicesSummaryController.get()))
      //        case false => Future.successful(Redirect(addRoutes.TradingPremisesController.get()))
      //      }
      //
      //    case TradingPremisesPageId if edit && model.tradingPremisesActivities.isDefined =>
      //      Future.successful(Redirect(addRoutes.UpdateServicesSummaryController.get()))
      //
      //    case TradingPremisesPageId =>
      //      model.areNewActivitiesAtTradingPremises match {
      //        case Some(true) =>
      //          Future.successful(Redirect(addRoutes.WhichTradingPremisesController.get()))
      //        case _ =>
      //          Future.successful(Redirect(addRoutes.UpdateServicesSummaryController.get()))
      //      }
      //
      //    case WhichTradingPremisesPageId =>
      //      Future.successful(Redirect(addRoutes.UpdateServicesSummaryController.get()))

      //    case UpdateServiceSummaryPageId =>
      //      businessMatchingService.getRemainingBusinessActivities map {
      //        case set if set.nonEmpty =>
      //          Redirect(addRoutes.AddMoreActivitiesController.get())
      //        case _ if model.informationRequired =>
      //          Redirect(addRoutes.NewServiceInformationController.get())
      //        case _ =>
      //          Redirect(controllers.routes.RegistrationProgressController.get())
      //      } getOrElse error(pageId)

      //    case AddMoreAcivitiesPageId =>
      //      model.addMoreActivities match {
      //        case Some(true) =>
      //          Future.successful(Redirect(addRoutes.SelectActivitiesController.get()))
      //
      //        case _ =>
      //          businessMatchingService.getAdditionalBusinessActivities map { activities =>
      //            if (!activities.forall {
      //              case BillPaymentServices | TelephonePaymentService => true
      //              case _ => false
      //            }) {
      //              Redirect(addRoutes.NewServiceInformationController.get())
      //            } else {
      //              Redirect(controllers.routes.RegistrationProgressController.get())
      //            }
      //          } getOrElse InternalServerError("Unable to get additional business activities")
      //      }
      //
      //    case NewServiceInformationPageId =>
      //      Future.successful(Redirect(controllers.routes.RegistrationProgressController.get()))
//    }
//  }

  private def error(pageId: PageId) = InternalServerError(s"Failed to get route from $pageId")
}
