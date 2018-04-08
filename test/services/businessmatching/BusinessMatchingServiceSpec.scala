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

package services.businessmatching

import generators.businessmatching.BusinessMatchingGenerator
import generators.tradingpremises.TradingPremisesGenerator
import models.ViewResponse
import models.aboutthebusiness.AboutTheBusiness
import models.asp.Asp
import models.businessactivities.BusinessActivities
import models.businessmatching.{BusinessActivities => BMActivities, _}
import models.declaration.AddPerson
import models.declaration.release7.RoleWithinBusinessRelease7
import models.estateagentbusiness.{EstateAgentBusiness => Eab}
import models.hvd.Hvd
import models.moneyservicebusiness.{MoneyServiceBusiness => Msb}
import models.status.{NotCompleted, SubmissionDecisionApproved, SubmissionReadyForReview}
import models.tcsp.Tcsp
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.test.Helpers._
import utils.{DependencyMocks, FutureAssertions, GenericTestHelper}

import scala.concurrent.ExecutionContext.Implicits.global

class BusinessMatchingServiceSpec extends PlaySpec
  with GenericTestHelper
  with MockitoSugar
  with ScalaFutures
  with FutureAssertions
  with TradingPremisesGenerator
  with BusinessMatchingGenerator {

  trait Fixture extends DependencyMocks {
    val service = new BusinessMatchingService(mockStatusService, mockCacheConnector)

    val primaryModel = businessMatchingGen.sample.get

    mockCacheFetch(Some(primaryModel), Some(BusinessMatching.key))
    mockCacheSave[BusinessMatching]
  }

  "getModel" when {
    "called" must {
      "return the primary model" when {
        "in a pre-application status" in new Fixture {
          mockApplicationStatus(NotCompleted)
          service.getModel returnsSome primaryModel
        }

        "the variation model is empty and status is post-preapp" in new Fixture {
          mockApplicationStatus(SubmissionDecisionApproved)
          mockCacheFetch(Some(BusinessMatching()), Some(BusinessMatching.variationKey))

          service.getModel returnsSome primaryModel
        }
      }
    }
  }

  "updateModel" when {
    "called" must {
      "update the original model" when {
        "in pre-application status" in new Fixture {
          mockApplicationStatus(NotCompleted)
          mockCacheSave(primaryModel)

          service.updateModel(primaryModel) returnsSome mockCacheMap
          verify(mockCacheConnector).save[BusinessMatching](eqTo(BusinessMatching.key), any())(any(), any(), any())
        }
      }
    }
  }

  "getAdditionalBusinessActivities" must {
    "return saved activities not found in view response" in new Fixture {
      val api5BusinessMatching = BusinessMatching(
        activities = Some(BMActivities(
          Set(BillPaymentServices)
        ))
      )

      val newBusinessMatching = BusinessMatching(
        activities = Some(BMActivities(
          Set(BillPaymentServices, HighValueDealing)
        ))
      )

      val viewResponse = ViewResponse(
        "",
        businessMatchingSection = api5BusinessMatching,
        aboutTheBusinessSection = AboutTheBusiness(),
        bankDetailsSection = Seq.empty,
        businessActivitiesSection = BusinessActivities(),
        eabSection = None,
        aspSection = None,
        tcspSection = None,
        responsiblePeopleSection = None,
        tradingPremisesSection = None,
        msbSection = None,
        hvdSection = None,
        supervisionSection = None,
        aboutYouSection = AddPerson("", None, "", RoleWithinBusinessRelease7(Set.empty))
      )

      mockCacheFetch(Some(newBusinessMatching), Some(BusinessMatching.key))
      mockCacheFetch[ViewResponse](Some(viewResponse), Some(ViewResponse.key))

      whenReady(service.getAdditionalBusinessActivities.value) { result =>
        result must be(Some(Set(HighValueDealing)))
      }
    }

    "return an empty set if saved business activities are the same as view response" in new Fixture {
      val businessMatching = BusinessMatching(
        activities = Some(BMActivities(
          Set(BillPaymentServices)
        ))
      )

      val viewResponse = ViewResponse(
        "",
        businessMatchingSection = BusinessMatching(
          activities = Some(BMActivities(
            Set(BillPaymentServices)
          ))
        ),
        aboutTheBusinessSection = AboutTheBusiness(),
        bankDetailsSection = Seq.empty,
        businessActivitiesSection = BusinessActivities(),
        eabSection = None,
        aspSection = None,
        tcspSection = None,
        responsiblePeopleSection = None,
        tradingPremisesSection = None,
        msbSection = None,
        hvdSection = None,
        supervisionSection = None,
        aboutYouSection = AddPerson("", None, "", RoleWithinBusinessRelease7(Set.empty))
      )

      mockCacheFetch(Some(businessMatching), Some(BusinessMatching.key))
      mockCacheFetch[ViewResponse](Some(viewResponse), Some(ViewResponse.key))

      whenReady(service.getAdditionalBusinessActivities.value){ result =>
        result must be(Some(Set.empty))
      }

    }

    "return none if all business activities cannot be retrieved" in new Fixture {

      val businessMatching = BusinessMatching(
        activities = Some(BMActivities(
          Set(BillPaymentServices)
        ))
      )

      mockApplicationStatus(SubmissionDecisionApproved)

      mockCacheFetch(Some(businessMatching), Some(BusinessMatching.key))
      mockCacheFetch(Some(businessMatching), Some(BusinessMatching.variationKey))
      mockCacheFetch[ViewResponse](None, Some(ViewResponse.key))

      whenReady(service.getAdditionalBusinessActivities.value){ result =>
        result must be(None)
      }

    }
  }

  "getOriginalBusinessActivities" must {
    "return the activities that are present only in the view response" in new Fixture {
      val existing = BusinessMatching(
        activities = Some(BMActivities(
          Set(BillPaymentServices)
        ))
      )

      val current = BusinessMatching(
        activities = Some(BMActivities(
          Set(BillPaymentServices, HighValueDealing)
        ))
      )

      val viewResponse = ViewResponse(
        "",
        businessMatchingSection = BusinessMatching(
          activities = Some(BMActivities(
            Set(BillPaymentServices)
          ))
        ),
        aboutTheBusinessSection = AboutTheBusiness(),
        bankDetailsSection = Seq.empty,
        businessActivitiesSection = BusinessActivities(),
        eabSection = None,
        aspSection = None,
        tcspSection = None,
        responsiblePeopleSection = None,
        tradingPremisesSection = None,
        msbSection = None,
        hvdSection = None,
        supervisionSection = None,
        aboutYouSection = AddPerson("", None, "", RoleWithinBusinessRelease7(Set.empty))
      )

      mockApplicationStatus(SubmissionDecisionApproved)

      mockCacheFetch(Some(existing), Some(BusinessMatching.key))
      mockCacheFetch(Some(current), Some(BusinessMatching.variationKey))
      mockCacheFetch[ViewResponse](Some(viewResponse), Some(ViewResponse.key))

      whenReady(service.getSubmittedBusinessActivities.value){ result =>
        result must be(Some(Set(BillPaymentServices)))
      }
    }

    "return none if all business activities cannot be retrieved" in new Fixture {

      val businessMatching = BusinessMatching(
        activities = Some(BMActivities(
          Set(BillPaymentServices)
        ))
      )

      mockApplicationStatus(SubmissionDecisionApproved)

      mockCacheFetch(Some(businessMatching), Some(BusinessMatching.key))
      mockCacheFetch(Some(businessMatching), Some(BusinessMatching.variationKey))
      mockCacheFetch[ViewResponse](None, Some(ViewResponse.key))

      whenReady(service.getSubmittedBusinessActivities.value){ result =>
        result must be(None)
      }
    }
  }

  "fitAndProperRequired" must {
    "return true" when {
      "existing activities does not contain msb and tcsp" when {
        "current activities contains msb" in new Fixture {
          val existing = BusinessMatching(
            activities = Some(BMActivities(
              Set(BillPaymentServices)
            ))
          )

          val current = BusinessMatching(
            activities = Some(BMActivities(
              Set(MoneyServiceBusiness)
            ))
          )

          val viewResponse = ViewResponse(
            "",
            businessMatchingSection = existing,
            aboutTheBusinessSection = AboutTheBusiness(),
            bankDetailsSection = Seq.empty,
            businessActivitiesSection = BusinessActivities(),
            eabSection = None,
            aspSection = None,
            tcspSection = None,
            responsiblePeopleSection = None,
            tradingPremisesSection = None,
            msbSection = None,
            hvdSection = None,
            supervisionSection = None,
            aboutYouSection = AddPerson("", None, "", RoleWithinBusinessRelease7(Set.empty))
          )

          mockCacheFetch(Some(current), Some(BusinessMatching.key))
          mockCacheFetch[ViewResponse](Some(viewResponse), Some(ViewResponse.key))

          whenReady(service.fitAndProperRequired.value) { result =>
            result must be(Some(true))
          }
        }

        "current activities contains tcsp" in new Fixture {

          val existing = BusinessMatching(
            activities = Some(BMActivities(
              Set(BillPaymentServices)
            ))
          )
          val current = BusinessMatching(
            activities = Some(BMActivities(
              Set(TrustAndCompanyServices)
            ))
          )

          val viewResponse = ViewResponse(
            "",
            businessMatchingSection = existing,
            aboutTheBusinessSection = AboutTheBusiness(),
            bankDetailsSection = Seq.empty,
            businessActivitiesSection = BusinessActivities(),
            eabSection = None,
            aspSection = None,
            tcspSection = None,
            responsiblePeopleSection = None,
            tradingPremisesSection = None,
            msbSection = None,
            hvdSection = None,
            supervisionSection = None,
            aboutYouSection = AddPerson("", None, "", RoleWithinBusinessRelease7(Set.empty))
          )

          mockCacheFetch(Some(current), Some(BusinessMatching.key))
          mockCacheFetch[ViewResponse](Some(viewResponse), Some(ViewResponse.key))

          whenReady(service.fitAndProperRequired.value) { result =>
            result must be(Some(true))
          }
        }
      }
    }
    "return false" when {
      "existing activities contains msb" in new Fixture {

        val existing = BusinessMatching(
          activities = Some(BMActivities(
            Set(MoneyServiceBusiness)
          ))
        )
        val current = BusinessMatching(
          activities = Some(BMActivities(
            Set(TrustAndCompanyServices)
          ))
        )

        val viewResponse = ViewResponse(
          "",
          businessMatchingSection = existing,
          aboutTheBusinessSection = AboutTheBusiness(),
          bankDetailsSection = Seq.empty,
          businessActivitiesSection = BusinessActivities(),
          eabSection = None,
          aspSection = None,
          tcspSection = None,
          responsiblePeopleSection = None,
          tradingPremisesSection = None,
          msbSection = None,
          hvdSection = None,
          supervisionSection = None,
          aboutYouSection = AddPerson("", None, "", RoleWithinBusinessRelease7(Set.empty))
        )

        mockCacheFetch(Some(current), Some(BusinessMatching.key))
        mockCacheFetch[ViewResponse](Some(viewResponse), Some(ViewResponse.key))

        whenReady(service.fitAndProperRequired.value) { result =>
          result must be(Some(false))
        }
      }
      "existing activities contains tcsp" in new Fixture {

        val existing = BusinessMatching(
          activities = Some(BMActivities(
            Set(TrustAndCompanyServices)
          ))
        )
        val current = BusinessMatching(
          activities = Some(BMActivities(
            Set(BillPaymentServices)
          ))
        )

        val viewResponse = ViewResponse(
          "",
          businessMatchingSection = existing,
          aboutTheBusinessSection = AboutTheBusiness(),
          bankDetailsSection = Seq.empty,
          businessActivitiesSection = BusinessActivities(),
          eabSection = None,
          aspSection = None,
          tcspSection = None,
          responsiblePeopleSection = None,
          tradingPremisesSection = None,
          msbSection = None,
          hvdSection = None,
          supervisionSection = None,
          aboutYouSection = AddPerson("", None, "", RoleWithinBusinessRelease7(Set.empty))
        )

        mockCacheFetch(Some(current), Some(BusinessMatching.key))
        mockCacheFetch[ViewResponse](Some(viewResponse), Some(ViewResponse.key))

        whenReady(service.fitAndProperRequired.value) { result =>
          result must be(Some(false))
        }
      }
    }
  }

  "clear section" must {

    "clear data of Asp given AccountancyServices" in new Fixture {

      val result = service.clearSection(AccountancyServices)

      await(result)

      verify(mockCacheConnector).save[Asp](
        eqTo(Asp.key),
        eqTo(None)
      )(any(),any(),any())

    }
    "clear data of Hvd given HighValueDealing" in new Fixture {

      val result = service.clearSection(HighValueDealing)

      await(result)

      verify(mockCacheConnector).save[Hvd](
        eqTo(Hvd.key),
        eqTo(None)
      )(any(),any(),any())

    }
    "clear data of Msb given MoneyServiceBusiness" in new Fixture {

      val result = service.clearSection(MoneyServiceBusiness)

      await(result)

      verify(mockCacheConnector).save[Msb](
        eqTo(Msb.key),
        eqTo(None)
      )(any(),any(),any())

    }
    "clear data of Tcsp given TrustAndCompanyServices" in new Fixture {

      val result = service.clearSection(TrustAndCompanyServices)

      await(result)

      verify(mockCacheConnector).save[Tcsp](
        eqTo(Tcsp.key),
        eqTo(None)
      )(any(),any(),any())

    }
    "clear data of Eab given EstateAgentBusinessService" in new Fixture {

      val result = service.clearSection(EstateAgentBusinessService)

      await(result)

      verify(mockCacheConnector).save[Eab](
        eqTo(Eab.key),
        eqTo(None)
      )(any(),any(),any())

    }

  }

  "preApplicationComplete" when {
    "called" must {
      "return true" when {
        "in the right status" in new Fixture {
          mockCacheFetch[BusinessMatching](Some(BusinessMatching(preAppComplete = true)))

          val result = await(service.preApplicationComplete)

          result mustBe true
        }
      }
    }
  }

}