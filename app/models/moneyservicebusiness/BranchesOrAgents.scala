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

package models.moneyservicebusiness

import jto.validation._
import jto.validation.forms.UrlFormEncoded

case class BranchesOrAgents(hasCountries: Boolean)

object BranchesOrAgents {

  import utils.MappingUtils.Implicits._

  implicit val formRule : Rule[UrlFormEncoded, BranchesOrAgents] = From[UrlFormEncoded] { __ =>
    import jto.validation.forms.Rules._
    (__ \ "hasCountries")
      .read[Boolean]
      .withMessage("error.required.hasCountries.msb.branchesOrAgents") flatMap { BranchesOrAgents.apply }
  }

  implicit def formWrites: Write[BranchesOrAgents, UrlFormEncoded] = Write {
    case BranchesOrAgents(accepted) => Map("hasCountries" -> accepted.toString)
  }
}