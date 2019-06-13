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
import models.Country
import org.scalatest.MustMatchers
import org.scalatestplus.play.PlaySpec
import play.api.libs.json._

class BranchesOrAgentsGroupSpec extends PlaySpec with MustMatchers{

  "MsbServices" must {

    val rule = implicitly[Rule[UrlFormEncoded, BranchesOrAgents]]
    val write = implicitly[Write[BranchesOrAgents, UrlFormEncoded]]

    "round trip through Json correctly" in {
      val model: BranchesOrAgentsGroup = BranchesOrAgentsGroup(BranchesOrAgents(true), Some(BranchesOrAgentsCountries(Seq(Country("United Kingdom", "GB")))))
      Json.fromJson[BranchesOrAgentsGroup](Json.toJson(model)) mustBe JsSuccess(model)
    }
  }

  "BranchesOrAgents form writes" when {

    "the list of countries is empty" must {
      "set hasCountries to false" in {

        BranchesOrAgentsGroup.update(
          BranchesOrAgentsGroup(BranchesOrAgents(true), None),
          BranchesOrAgentsCountries(Seq.empty)) mustBe BranchesOrAgentsGroup(BranchesOrAgents(false), None)
      }
    }

    "the list of countries has entries" must {
      "set hasCountries to true and populate the countries list" in {

        BranchesOrAgentsGroup.update(
          BranchesOrAgentsGroup(BranchesOrAgents(false), None),
          BranchesOrAgentsCountries(Seq(Country(name = "sadasd", code = "asdasd")))) mustBe BranchesOrAgentsGroup(
          BranchesOrAgents(true),
          Some(BranchesOrAgentsCountries(Seq(Country(name = "sadasd", code = "asdasd")))))
      }
    }
  }
}

