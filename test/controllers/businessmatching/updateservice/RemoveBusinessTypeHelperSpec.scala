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

package controllers.businessmatching.updateservice

import models.businessmatching.{BillPaymentServices, BusinessActivities => BMBusinessActivities, _}
import models.flowmanagement.RemoveBusinessTypeFlowModel
import models.hvd.Hvd
import models.responsiblepeople.{ApprovalFlags, ResponsiblePerson}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import utils._

import scala.concurrent.ExecutionContext.Implicits.global

class RemoveBusinessTypeHelperSpec extends AmlsSpec with FutureAssertions with MockitoSugar with ScalaFutures {

  val MSBOnlyModel = RemoveBusinessTypeFlowModel(activitiesToRemove = Some(Set(MoneyServiceBusiness)))

  trait Fixture extends AuthorisedFixture with DependencyMocks {
    self =>

    val helper = new RemoveBusinessTypeHelper(
      self.authConnector,
      mockCacheConnector
    )
  }

  "RemoveBusinessTypeHelper" must {
    "have removeSectionData method which" when {
      "called with model" must {
        "return updated cache map" in new Fixture {
          val activitiesToRemove = RemoveBusinessTypeFlowModel(activitiesToRemove = Some(Set(HighValueDealing)))

          val testBusinessMatching = BusinessMatching(activities = Some(BMBusinessActivities(Set(HighValueDealing, BillPaymentServices))),
            businessAppliedForPSRNumber = Some(BusinessAppliedForPSRNumberNo),
            hasAccepted = true,
            hasChanged = true)

          mockCacheFetch[BusinessMatching](
            Some(testBusinessMatching),
            Some(BusinessMatching.key))

          val expectedResult = BusinessMatching(activities = Some(BMBusinessActivities(Set(HighValueDealing))),
            businessAppliedForPSRNumber = Some(BusinessAppliedForPSRNumberNo),
            hasAccepted = true,
            hasChanged = true)

          mockCacheRemoveByKey[Hvd]

          helper.removeSectionData(activitiesToRemove).returnsSome(Seq(mockCacheMap))
        }
      }
    }
  }

  "removing Responsible People types" when {
    "there is more than one business type" when {
      "the buisness is TCSP and they answered yes to F&P then do not remove the responsible people approval" in new Fixture {

        val model = RemoveBusinessTypeFlowModel(activitiesToRemove = Some(Set(TrustAndCompanyServices, BillPaymentServices)))

        val startResultRP = Seq(ResponsiblePerson(
          approvalFlags = ApprovalFlags(hasAlreadyPassedFitAndProper = Some(true), hasAlreadyPaidApprovalCheck = Some(true)),
          hasAccepted = true,
          hasChanged = true))

        val startResultMatching = BusinessMatching(activities = Some(BMBusinessActivities(Set(HighValueDealing, BillPaymentServices))),
          businessAppliedForPSRNumber = Some(BusinessAppliedForPSRNumberNo),
          hasAccepted = true,
          hasChanged = true)

        mockCacheFetch[BusinessMatching](
          Some(startResultMatching),
          Some(BusinessMatching.key))

        mockCacheUpdate(Some(BusinessMatching.key), startResultMatching)

        mockCacheFetch[Seq[ResponsiblePerson]](
          Some(startResultRP),
          Some(ResponsiblePerson.key))

        val expectedResultRP = Seq(ResponsiblePerson(
          approvalFlags = ApprovalFlags(hasAlreadyPassedFitAndProper = Some(true), hasAlreadyPaidApprovalCheck = Some(true)),
          hasAccepted = true,
          hasChanged = true))


        mockCacheUpdate(Some(ResponsiblePerson.key), expectedResultRP)

        helper.removeFitAndProper(model).returnsSome(expectedResultRP)
      }

      "the buisness is TCSP and they answered no to F&P then do remove the responsible people approval" in new Fixture {

        val model = RemoveBusinessTypeFlowModel(activitiesToRemove = Some(Set(TrustAndCompanyServices, BillPaymentServices)))

        val startResultRP = Seq(ResponsiblePerson(
          approvalFlags = ApprovalFlags(hasAlreadyPassedFitAndProper = Some(false), hasAlreadyPaidApprovalCheck = Some(true)),
          hasAccepted = true,
          hasChanged = true))

        val startResultMatching = BusinessMatching(activities = Some(BMBusinessActivities(Set(HighValueDealing, BillPaymentServices))),
          businessAppliedForPSRNumber = Some(BusinessAppliedForPSRNumberNo),
          hasAccepted = true,
          hasChanged = true)

        mockCacheFetch[BusinessMatching](
          Some(startResultMatching),
          Some(BusinessMatching.key))

        mockCacheUpdate(Some(BusinessMatching.key), startResultMatching)

        mockCacheFetch[Seq[ResponsiblePerson]](
          Some(startResultRP),
          Some(ResponsiblePerson.key))

        val expectedResultRP = Seq(ResponsiblePerson(
          approvalFlags = ApprovalFlags(hasAlreadyPassedFitAndProper = Some(false), hasAlreadyPaidApprovalCheck = None),
          hasAccepted = true,
          hasChanged = true))

        mockCacheUpdate(Some(ResponsiblePerson.key), expectedResultRP)

        helper.removeFitAndProper(model).returnsSome(expectedResultRP)
      }
    }
  }
}

