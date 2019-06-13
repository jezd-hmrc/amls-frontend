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

class BranchesOrAgentsWhichCountriesSpec extends PlaySpec with MustMatchers{

  "MsbServices" must {

    val rule = implicitly[Rule[UrlFormEncoded, BranchesOrAgentsCountries]]
    val write = implicitly[Write[BranchesOrAgentsCountries, UrlFormEncoded]]


    "round trip through forms correctly" in {
      val model: BranchesOrAgentsCountries = BranchesOrAgentsCountries(Seq(Country("United Kingdom", "GB")))
      rule.validate(write.writes(model)) mustBe Valid(model)
    }

    "successfully validate when  there is at least 1 country selected" in {
      val form: UrlFormEncoded = Map( "countries" -> Seq("GB") )
      val model: BranchesOrAgentsCountries = BranchesOrAgentsCountries( Seq(Country("United Kingdom", "GB")))
      rule.validate(form) mustBe Valid(model)
    }

    "fail to validate when hasCountries is true and there are no countries selected" in {

      val form: UrlFormEncoded = Map(
        "hasCountries" -> Seq("true"),
        "countries" -> Seq.empty
      )

      rule.validate(form) mustBe Invalid(
        Seq((Path) -> Seq(ValidationError("error.invalid.countries.msb.branchesOrAgents")))
      )
    }

    "fail to validate when hasCountries is true and there are more than 10 countries" in {

      val form: UrlFormEncoded = Map(
        "countries[]" -> Seq.fill(11)("GB")
      )

      rule.validate(form) mustBe Invalid(
        Seq((Path) -> Seq(ValidationError("error.maxLength", 10)))
      )
    }

    "successfully validate when there are empty values in the seq" in {

      val form: UrlFormEncoded = Map(
        "countries[]" -> Seq("GB", "", "US", "")
      )

      rule.validate(form) mustBe Valid(BranchesOrAgentsCountries(Seq(
        Country("United Kingdom", "GB"),
        Country("United States of America", "US")
      )))
    }

    "test" in {

      val form: UrlFormEncoded = Map(
        "countries[0]" -> Seq("GB"),
        "countries[1]" -> Seq("")
      )

      rule.validate(form) mustBe Valid(BranchesOrAgentsCountries(Seq(
        Country("United Kingdom", "GB")
      )))
    }
  }

  "BranchesOrAgents form writes" when {


    "the list of countries has entries" must {
      "populate the countries list" in {
        BranchesOrAgentsCountries.formWrite.writes(BranchesOrAgentsCountries(Seq(Country("TESTCOUNTRY1", "TC1"), Country("TESTCOUNTRY2", "TC2")))) must be (Map(
        "countries[0]" -> Seq("TC1"),
        "countries[1]" -> Seq("TC2")
      ))}
    }
  }
}

