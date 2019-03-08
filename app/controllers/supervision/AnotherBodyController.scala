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

package controllers.supervision

import connectors.DataCacheConnector
import controllers.BaseController
import forms.{EmptyForm, Form2, InvalidForm, ValidForm}
import javax.inject.Inject
import models.supervision.{AnotherBody, AnotherBodyNo, AnotherBodyYes, Supervision}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import views.html.supervision.another_body

import scala.concurrent.Future

class AnotherBodyController @Inject() (val dataCacheConnector: DataCacheConnector,
                                       val authConnector: AuthConnector
                                      ) extends BaseController {

  def get(edit: Boolean = false) = Authorised.async {
    implicit authContext => implicit request =>
      dataCacheConnector.fetch[Supervision](Supervision.key) map {
        response =>
          val form: Form2[AnotherBody] = (for {
            supervision <- response
            anotherBody <- supervision.anotherBody
          } yield Form2[AnotherBody](anotherBody)).getOrElse(EmptyForm)
          Ok(another_body(form, edit))
      }
  }

  def post(edit : Boolean = false) = Authorised.async {
    implicit authContext => implicit request =>
      Form2[AnotherBody](request.body) match {
        case f: InvalidForm =>
          Future.successful(BadRequest(another_body(f, edit)))
        case ValidForm(_, data: AnotherBody) =>
          for {
            supervision <- dataCacheConnector.fetch[Supervision](Supervision.key)
            cache <- dataCacheConnector.save[Supervision](Supervision.key,
              updateData(supervision, data))
          } yield {
            redirectTo(edit, cache)
          }
      }
  }

  private def updateData(supervision: Supervision, data: AnotherBody): Supervision = {
    def updatedAnotherBody = (supervision.anotherBody, data) match {
      case (_, d) if d.equals(AnotherBodyNo) => AnotherBodyNo
      case (Some(ab), d:AnotherBodyYes) if ab.equals(AnotherBodyNo) => AnotherBodyYes(d.supervisorName, None, None, None)
      case (Some(ab), d:AnotherBodyYes) => ab.asInstanceOf[AnotherBodyYes].supervisorName(d.supervisorName)
      case (None, d:AnotherBodyYes) => AnotherBodyYes(d.supervisorName, None, None, None)

    }
    supervision.anotherBody(updatedAnotherBody)
  }

  private def redirectTo(edit: Boolean, cache: CacheMap)(implicit authContext: AuthContext, headerCarrier: HeaderCarrier) = {

    import utils.ControllerHelper.{anotherBodyComplete, isAnotherBodyComplete, isAnotherBodyYes}

    val anotherBody = anotherBodyComplete(cache)

    if (isAnotherBodyYes(anotherBody)) {
      (edit, isAnotherBodyComplete(anotherBody)) match {
        case (true, false) => Redirect(routes.SupervisionStartController.get())
        case (false, false) => Redirect(routes.SupervisionStartController.get())
        case (_, true) => Redirect(routes.SummaryController.get())
      }
    } else {
      (edit, isAnotherBodyComplete(anotherBody)) match {
        case (true, true) => Redirect(routes.SummaryController.get())
        case (false, true) => Redirect(routes.ProfessionalBodyMemberController.get())
      }
    }
  }


}
