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

package services.flowmanagement.pagerouters

import cats.data.OptionT
import cats.implicits._
import controllers.businessmatching.updateservice.add.{routes => addRoutes}
import javax.inject.{Inject, Singleton}
import models.businessmatching.{BillPaymentServices, TelephonePaymentService}
import models.flowmanagement.{AddServiceFlowModel, PageId, UpdateServiceSummaryPageId}
import play.api.mvc.Result
import play.api.mvc.Results.{InternalServerError, Redirect}
import services.StatusService
import services.businessmatching.BusinessMatchingService
import services.flowmanagement.PageRouter
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.frontend.auth.AuthContext

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateServicesSummaryPageRouter @Inject()(val statusService: StatusService,
                                                val businessMatchingService: BusinessMatchingService) extends PageRouter[AddServiceFlowModel] {

  override def getPageRoute(model: AddServiceFlowModel, edit: Boolean = false)
                           (implicit ac: AuthContext,
                            hc: HeaderCarrier,
                            ec: ExecutionContext

                           ): Future[Result] = {

    businessMatchingService.getRemainingBusinessActivities flatMap {
      case set if set.nonEmpty =>
        OptionT.some(Redirect(addRoutes.AddMoreActivitiesController.get()))
      case _ =>
        newServiceInformationRedirect
    } getOrElse error(UpdateServiceSummaryPageId)

  }

  private def error(pageId: PageId) = InternalServerError(s"Failed to get route from $pageId")

  private def newServiceInformationRedirect(implicit ac: AuthContext, hc: HeaderCarrier, ec: ExecutionContext) =
    businessMatchingService.getAdditionalBusinessActivities map { activities =>
      if (!activities.forall {
        case BillPaymentServices | TelephonePaymentService => true
        case _ => false
      }) {
        Redirect(addRoutes.NewServiceInformationController.get())
      } else {
        Redirect(controllers.routes.RegistrationProgressController.get())
      }
    }
}



