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

package controllers.responsiblepeople

import javax.inject.Inject

import connectors.DataCacheConnector
import controllers.BaseController
import forms.{EmptyForm, Form2, InvalidForm, ValidForm}
import models.responsiblepeople.{NonUKPassport, ResponsiblePeople}
import play.api.i18n.MessagesApi
import play.api.mvc.{AnyContent, Request}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import utils.{ControllerHelper, RepeatingSection}
import views.html.responsiblepeople.person_non_uk_passport

import scala.concurrent.Future

class PersonNonUKPassportController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            val dataCacheConnector: DataCacheConnector,
                                            val authConnector: AuthConnector
                                          ) extends RepeatingSection with BaseController {


  def get(index:Int, edit: Boolean = false, fromDeclaration: Option[String]) = Authorised.async {
    implicit authContext =>
      implicit request =>
        getData[ResponsiblePeople](index) map {
          case Some(ResponsiblePeople(Some(personName),_,_,Some(nonUKPassport),_,_,_,_,_,_,_,_,_,_,_,_,_,_)) =>
            Ok(person_non_uk_passport(Form2[NonUKPassport](nonUKPassport), edit, index, fromDeclaration, personName.titleName))
          case Some(ResponsiblePeople(Some(personName),_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_)) =>
            Ok(person_non_uk_passport(EmptyForm, edit, index, fromDeclaration, personName.titleName))
          case _ => NotFound(notFoundView)
        }
  }

  private def redirectToNextPage(result: Option[CacheMap], index: Int,
                         edit: Boolean, fromDeclaration: Option[String] )(implicit authContext:AuthContext, request: Request[AnyContent]) = {
    (for {
      cache <- result
      rp <- getData[ResponsiblePeople](cache, index)
    } yield rp.dateOfBirth.isDefined && edit match {
      case true => Redirect(routes.DetailedAnswersController.get(index))
      case false => Redirect(routes.DateOfBirthController.get(index, edit, fromDeclaration))
    }).getOrElse(NotFound(notFoundView))
  }


  def post(index: Int, edit: Boolean = false, fromDeclaration: Option[String]) = Authorised.async {
    implicit authContext =>
      implicit request =>
        Form2[NonUKPassport](request.body) match {
          case f: InvalidForm => getData[ResponsiblePeople](index) map { rp =>
            BadRequest(person_non_uk_passport(f, edit, index, fromDeclaration, ControllerHelper.rpTitleName(rp)))
          }
          case ValidForm(_, data) => {
            for {
              result <- fetchAllAndUpdateStrict[ResponsiblePeople](index) { (_, rp) =>
                rp.nonUKPassport(data)
              }
            } yield redirectToNextPage(result, index, edit, fromDeclaration)

          } recoverWith {
            case _: IndexOutOfBoundsException => Future.successful(NotFound(notFoundView))
          }
        }
  }

}
