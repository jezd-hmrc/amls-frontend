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

package controllers.businessmatching.updateservice.remove

import cats.data.OptionT
import cats.implicits._
import controllers.businessmatching.updateservice.RemoveBusinessTypeHelper
import models.DateOfChange
import models.businessmatching._
import models.businessmatching.updateservice.ServiceChangeRegister
import models.flowmanagement.{RemoveBusinessTypeFlowModel, WhatBusinessTypesToRemovePageId, WhatDateRemovedPageId}
import org.joda.time.LocalDate
import org.jsoup.Jsoup
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import play.api.i18n.Messages
import play.api.test.Helpers._
import services.businessmatching.BusinessMatchingService
import utils.{AmlsSpec, AuthorisedFixture, DependencyMocks}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class RemoveBusinessTypesControllerSpec extends AmlsSpec {

  trait Fixture extends AuthorisedFixture with DependencyMocks {
    self =>

    val request = addToken(authRequest)
    val mockBusinessMatchingService = mock[BusinessMatchingService]

    val mockRemoveBusinessTypeHelper = mock[RemoveBusinessTypeHelper]


    val controller = new RemoveBusinessTypesController(
      authConnector = self.authConnector,
      dataCacheConnector = mockCacheConnector,
      businessMatchingService = mockBusinessMatchingService,
      removeBusinessTypeHelper = mockRemoveBusinessTypeHelper,
      router = createRouter[RemoveBusinessTypeFlowModel]
    )

    when {
      controller.businessMatchingService.getModel(any(), any(), any())
    } thenReturn OptionT.some[Future, BusinessMatching](BusinessMatching(
      activities = Some(BusinessActivities(Set(BillPaymentServices)))
    ))

    when {
      controller.businessMatchingService.getSubmittedBusinessActivities(any(), any(), any())
    } thenReturn OptionT.some[Future, Set[BusinessActivity]](Set(BillPaymentServices))

    mockCacheFetch[RemoveBusinessTypeFlowModel](Some(RemoveBusinessTypeFlowModel(Some(Set(BillPaymentServices)))), Some(RemoveBusinessTypeFlowModel.key))
  }

  "RemoveActivitiesController" when {

    "get is called" must {
      "return OK with remove_activities view" in new Fixture {

        val result = controller.get()(request)
        status(result) must be(OK)
        Jsoup.parse(contentAsString(result)).title() must include(Messages("businessmatching.updateservice.removeactivities.title"))
      }
    }

    "post" must {
      "return a bad request when no data has been posted" in new Fixture {

        val result = controller.post()(request.withFormUrlEncodedBody())

        status(result) mustBe BAD_REQUEST
      }

      "return the next page in the flow when valid data has been posted" in new Fixture {
        mockCacheFetch(Some(RemoveBusinessTypeFlowModel()), Some(RemoveBusinessTypeFlowModel.key))

        when(mockRemoveBusinessTypeHelper.dateOfChangeApplicable(any())(any(), any(), any())).thenReturn(OptionT.some[Future, Boolean](true))

        mockCacheSave(RemoveBusinessTypeFlowModel(Some(Set(HighValueDealing))))

        val result = controller.post()(request.withFormUrlEncodedBody(
          "businessActivities[]" -> "04"
        ))

        status(result) mustBe SEE_OTHER
      }

      "save the list of business activites to the data cache" in new Fixture {
        val today = LocalDate.now

        val flowModel = RemoveBusinessTypeFlowModel(dateOfChange = Some(DateOfChange(today)))

        mockCacheFetch(Some(flowModel), Some(RemoveBusinessTypeFlowModel.key))

        when(mockRemoveBusinessTypeHelper.dateOfChangeApplicable(any())(any(), any(), any())).thenReturn(OptionT.some[Future, Boolean](true))

        mockCacheSave[RemoveBusinessTypeFlowModel]

        val result = await(controller.post()(request.withFormUrlEncodedBody(
          "businessActivities[]" -> "04"
        )))

        controller.router.verify(WhatBusinessTypesToRemovePageId, flowModel.copy(activitiesToRemove = Some(Set(HighValueDealing))))
      }

      "wipe the date of change if its not required" in new Fixture {

        mockCacheFetch(Some(RemoveBusinessTypeFlowModel(dateOfChange = Some(DateOfChange(LocalDate.now)))), Some(RemoveBusinessTypeFlowModel.key))

        when(mockRemoveBusinessTypeHelper.dateOfChangeApplicable(any())(any(), any(), any())).thenReturn(OptionT.some[Future, Boolean](false))

        mockCacheSave[RemoveBusinessTypeFlowModel]

        val result = await(controller.post()(request.withFormUrlEncodedBody(
          "businessActivities[]" -> "04"
        )))

        controller.router.verify(WhatBusinessTypesToRemovePageId, RemoveBusinessTypeFlowModel(activitiesToRemove = Some(Set(HighValueDealing))))
      }
    }
  }
}
