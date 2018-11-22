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

package utils

import models.responsiblepeople._
import org.joda.time.LocalDate
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec

class ControllerHelperSpec  extends PlaySpec with MockitoSugar with ResponsiblePeopleValues {

  def createResponsiblePersonSeq: Option[Seq[ResponsiblePerson]] = {

    val positions = Positions(Set(BeneficialOwner, Partner, NominatedOfficer), Some(new LocalDate()))
    val completeRP = ResponsiblePerson(
      personName = Some(DefaultValues.personName),
      legalName = Some(DefaultValues.legalName),
      legalNameChangeDate = Some(new LocalDate(1990, 2, 24)),
      knownBy = Some(DefaultValues.knownBy),
      personResidenceType = Some(DefaultValues.personResidenceTypeNonUk),
      ukPassport = Some(DefaultValues.ukPassportNo),
      nonUKPassport = Some(DefaultValues.nonUKPassportYes),
      dateOfBirth = Some(DefaultValues.dateOfBirth),
      contactDetails = Some(DefaultValues.contactDetails),
      addressHistory = Some(DefaultValues.addressHistory),
      positions = Some(DefaultValues.positions),
      saRegistered = Some(DefaultValues.saRegistered),
      vatRegistered = Some(DefaultValues.vatRegistered),
      experienceTraining = Some(DefaultValues.experienceTraining),
      training = Some(DefaultValues.training),
      approvalFlags = ApprovalFlags(hasAlreadyPassedFitAndProper = Some(true)),
      hasChanged = false,
      hasAccepted = false,
      lineId = Some(1),
      status = Some(StatusConstants.Unchanged),
      endDate = None,
      soleProprietorOfAnotherBusiness = Some(DefaultValues.soleProprietorOfAnotherBusiness)
    )

    Some(
      Seq(
        completeRP
      )
    )
  }

  "ControllerHelper" must {
    "hasIncompleteResponsiblePerson" must {
      "return false" when {
        "no responsiblePerson is supplied" in {
          val hasIncomplete = ControllerHelper.hasIncompleteResponsiblePerson(None)

          hasIncomplete mustEqual false
        }
      }

      "return false" when {
        "a responsiblePerson seq is supplied" in {
          val rp: Option[Seq[ResponsiblePerson]] = createResponsiblePersonSeq
          val hasIncomplete = ControllerHelper.hasIncompleteResponsiblePerson(rp)

          hasIncomplete mustEqual false
        }
      }

      "return false" when {
        "a responsiblePerson seq has is supplied" in {
          val rp: Option[Seq[ResponsiblePerson]] = createResponsiblePersonSeq
          val hasIncomplete = ControllerHelper.hasIncompleteResponsiblePerson(rp)

          hasIncomplete mustEqual false
        }
      }
    }
  }
}
