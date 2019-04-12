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

package controllers.tcsp

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import play.api.test.Helpers.{BAD_REQUEST, OK, SEE_OTHER, contentAsString, redirectLocation, status}
import connectors.DataCacheConnector
import models.tcsp._
import org.mockito.Matchers.any
import org.mockito.Mockito.{verify, when}
import play.api.inject.bind
import play.api.inject.guice.GuiceInjectorBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import utils.{AmlsSpec, AuthorisedFixture}
import org.mockito.Matchers.{eq => eqTo, _}

import scala.concurrent.Future


class OnlyOffTheShelfCompsSoldControllerSpec extends AmlsSpec with MockitoSugar with ScalaFutures {
  trait TestFixture extends AuthorisedFixture { self =>
    val request = addToken(self.authRequest)

    val cache = mock[DataCacheConnector]

    val injector = new GuiceInjectorBuilder()
      .overrides(bind[AuthConnector].to(self.authConnector))
      .overrides(bind[DataCacheConnector].to(self.cache))
      .build()

    lazy val controller = injector.instanceOf[OnlyOffTheShelfCompsSoldController]

    val tcsp = Tcsp(
      Some(TcspTypes(Set(
        NomineeShareholdersProvider,
        TrusteeProvider))),
      None,
      None,
      Some(ProvidedServices(Set(PhonecallHandling, Other("other service")))),
      Some(true),
      None,
      hasAccepted = true
    )

    when(cache.fetch[Tcsp](any())(any(), any(), any()))
      .thenReturn(Future.successful(Some(tcsp)))

    when(cache.save[Tcsp](any(), any())(any(), any(), any()))
      .thenReturn(Future.successful(new CacheMap("", Map.empty)))
  }

  "The OnlyOffTheShelfCompsSoldController" when {
    "get is called" must {

      "respond with BAD_REQUEST" when {
        "given invalid data" in new TestFixture {

          val newRequest = request.withFormUrlEncodedBody(
            "onlyOffTheShelfCompsSold" -> "invalid"
          )

          val result = controller.post(true)(newRequest)
          status(result) must be(BAD_REQUEST)
        }
      }

      "respond with OK and include onlyOffTheShelfCompsSold" in new TestFixture {

        val result = controller.get()(request)

        status(result) mustBe OK
        contentAsString(result) must include("onlyOffTheShelfCompsSold")
      }
    }

    "post is called" must {
      "respond with BAD_REQUEST" in new TestFixture {
        val result = controller.post()(request)
        status(result) mustBe BAD_REQUEST
      }

      "respond with SUCCESS" when {

        "where CompanyFormationAgent" which {
          "redirects to ComplexCorpStructureCreationController" in new TestFixture {

            val companyFormationAgentTcsp = Tcsp(
              Some(TcspTypes(Set(
                NomineeShareholdersProvider,
                CompanyFormationAgent))),
              None,
              None,
              Some(ProvidedServices(Set(PhonecallHandling, Other("other service")))),
              Some(true),
              None,
              hasAccepted = true
            )

            val expected = Tcsp(
              tcspTypes = Some(TcspTypes(Set(
                NomineeShareholdersProvider,
                CompanyFormationAgent))),
              onlyOffTheShelfCompsSold = Some(OnlyOffTheShelfCompsSoldYes),
              complexCorpStructureCreation = None,
              providedServices = Some(ProvidedServices(Set(PhonecallHandling, Other("other service")))),
              doesServicesOfAnotherTCSP = Some(true),
              servicesOfAnotherTCSP = None,
              hasAccepted = false,
              hasChanged = true
            )

            when(cache.fetch[Tcsp](any())(any(), any(), any()))
              .thenReturn(Future.successful(Some(companyFormationAgentTcsp)))

            val result = controller.post()(request.withFormUrlEncodedBody("onlyOffTheShelfCompsSold" -> "true"))

            status(result) mustBe SEE_OTHER
            verify(controller.dataCacheConnector).save[Tcsp](any(), eqTo(expected))(any(), any(), any())
            redirectLocation(result) mustBe Some(controllers.tcsp.routes.ComplexCorpStructureCreationController.get().url)
          }
        }

        "where not CompanyFormationAgent" which {
          "edit is 'true'" which {
            "redirects to SummaryController" in new TestFixture {

              val expected = Tcsp(
                tcspTypes = Some(TcspTypes(Set(
                  NomineeShareholdersProvider,
                  TrusteeProvider))),
                onlyOffTheShelfCompsSold = Some(OnlyOffTheShelfCompsSoldYes),
                complexCorpStructureCreation = None,
                providedServices = Some(ProvidedServices(Set(PhonecallHandling, Other("other service")))),
                doesServicesOfAnotherTCSP = Some(true),
                servicesOfAnotherTCSP = None,
                hasAccepted = false,
                hasChanged = true
              )

              val result = controller.post(true)(request.withFormUrlEncodedBody("onlyOffTheShelfCompsSold" -> "true"))

              status(result) mustBe SEE_OTHER
              verify(controller.dataCacheConnector).save[Tcsp](any(), eqTo(expected))(any(), any(), any())
              redirectLocation(result) mustBe Some(controllers.tcsp.routes.SummaryController.get().url)
            }
          }

          "edit is 'false'" which {
            "redirects to SummaryController" in new TestFixture {

              val expected = Tcsp(
                tcspTypes = Some(TcspTypes(Set(
                  NomineeShareholdersProvider,
                  TrusteeProvider))),
                onlyOffTheShelfCompsSold = Some(OnlyOffTheShelfCompsSoldNo),
                complexCorpStructureCreation = None,
                providedServices = Some(ProvidedServices(Set(PhonecallHandling, Other("other service")))),
                doesServicesOfAnotherTCSP = Some(true),
                servicesOfAnotherTCSP = None,
                hasAccepted = false,
                hasChanged = true
              )

              val result = controller.post(false)(request.withFormUrlEncodedBody("onlyOffTheShelfCompsSold" -> "false"))
              status(result) mustBe SEE_OTHER
              verify(controller.dataCacheConnector).save[Tcsp](any(), eqTo(expected))(any(), any(), any())
              redirectLocation(result) mustBe Some(controllers.tcsp.routes.SummaryController.get().url)
            }
          }
        }
      }
    }
  }
}