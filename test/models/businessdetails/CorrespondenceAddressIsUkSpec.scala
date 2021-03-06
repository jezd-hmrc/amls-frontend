/*
 * Copyright 2020 HM Revenue & Customs
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

package models.businessdetails

import jto.validation.{Invalid, Path, ValidationError}
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec

class CorrespondenceAddressIsUkSpec extends PlaySpec with MockitoSugar {

  "CorrespondenceAddressIsUk" must {
      "throw error when mandatory fields are missing" in {
        CorrespondenceAddressIsUk.formRule.validate(Map.empty) must be
        Invalid(Seq(
          (Path \ "isUK") -> Seq(ValidationError("error.required.uk.or.overseas"))
        ))
      }
    }
}
