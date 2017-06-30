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

package controllers.changeofficer

import cats.data.OptionT
import connectors.DataCacheConnector
import models.responsiblepeople.{NominatedOfficer, ResponsiblePeople}
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

object Helpers {

  def getNominatedOfficerName()(implicit authContext: AuthContext,
                                headerCarrier: HeaderCarrier,
                                dataCacheConnector: DataCacheConnector,
                                f: cats.Monad[Future]) = {
    for {
      people <- OptionT(dataCacheConnector.fetch[Seq[ResponsiblePeople]](ResponsiblePeople.key))
      nominatedOfficer <- OptionT.fromOption[Future](getOfficer(people))
      name <- OptionT.fromOption[Future](nominatedOfficer.personName)
    } yield {
      name.fullName
    }
  }

  def getOfficer(people: Seq[ResponsiblePeople]) = {
    people.find(_.positions.fold(false)(p => p.positions.contains(NominatedOfficer)))
  }
}