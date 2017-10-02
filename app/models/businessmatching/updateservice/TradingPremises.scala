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

package models.businessmatching.updateservice

import jto.validation.forms.UrlFormEncoded
import jto.validation.{From, Path, Rule, RuleLike, Write}

case class TradingPremises(index: Set[String])

object TradingPremises {

  import utils.MappingUtils.Implicits._

  implicit def formReads(implicit p: Path => RuleLike[UrlFormEncoded, Set[String]]): Rule[UrlFormEncoded, TradingPremises] = From[UrlFormEncoded] { __ =>
      (__ \ "tradingPremises")
        .read[Set[String]]
        .withMessage("error.businessmatching.updateservice.tradingpremisesnewactivities")
        .flatMap(TradingPremises.apply)
    }

  implicit def formWrites(implicit w: Write[String, String]) = Write[TradingPremises, UrlFormEncoded] { data =>
    Map("tradingPremises[]" -> data.index.toSeq.map(w.writes))
  }

}