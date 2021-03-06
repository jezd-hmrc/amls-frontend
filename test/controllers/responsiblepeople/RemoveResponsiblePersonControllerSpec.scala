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

package controllers.responsiblepeople

import connectors.DataCacheConnector
import controllers.actions.SuccessfulAuthAction
import generators.ResponsiblePersonGenerator
import models.Country
import models.responsiblepeople.TimeAtAddress.ZeroToFiveMonths
import models.responsiblepeople._
import models.status._
import org.joda.time.LocalDate
import org.jsoup.Jsoup
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.{eq => meq, _}
import org.mockito.Mockito._
import org.scalatest.MustMatchers
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.prop.PropertyChecks
import play.api.i18n.Messages
import play.api.test.Helpers.{status, _}
import services.StatusService
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.{AmlsSpec, AuthorisedFixture, StatusConstants}
import views.html.responsiblepeople.remove_responsible_person

import scala.concurrent.Future

class RemoveResponsiblePersonControllerSpec extends AmlsSpec
  with MustMatchers with MockitoSugar with ScalaFutures with PropertyChecks with NinoUtil with ResponsiblePersonGenerator {

  trait Fixture {
    self => val request = addToken(authRequest)
    lazy val view = app.injector.instanceOf[remove_responsible_person]
    val controller = new RemoveResponsiblePersonController (
      dataCacheConnector = mock[DataCacheConnector],
      statusService =  mock[StatusService],
      authAction = SuccessfulAuthAction, ds = commonDependencies, cc = mockMcc,
      remove_responsible_person = view,
      error = errorView)
  }

  "RemoveResponsiblePersonController" when {
    "get is called" when {
      "the submission status is NotCompleted" must {
        "respond with OK when the index is valid" in new Fixture {

          when(controller.statusService.getStatus(any[Option[String]](), any[(String, String)](), any[String]())(any(), any()))
            .thenReturn(Future.successful(NotCompleted))
          when(controller.dataCacheConnector.fetch[Seq[ResponsiblePerson]](any(), any())(any(), any()))
            .thenReturn(Future.successful(Some(Seq(ResponsiblePerson(Some(PersonName("firstName", None, "lastName")))))))

          val result = controller.get(1)(request)

          status(result) must be(OK)

        }
        "respond with NOT_FOUND when the index is out of bounds" in new Fixture {

          when(controller.statusService.getStatus(any[Option[String]](), any[(String, String)](), any[String]())(any(), any()))
            .thenReturn(Future.successful(NotCompleted))
          when(controller.dataCacheConnector.fetch[Seq[ResponsiblePerson]](any(), any())(any(), any()))
            .thenReturn(Future.successful(Some(Seq(ResponsiblePerson()))))

          val result = controller.get(100)(request)

          status(result) must be(NOT_FOUND)

        }
      }

      "the submission status is Renewal amendment" must {
        "respond with OK when the index is valid" in new Fixture {

          val p = mock[ResponsiblePerson]
          when(p.isComplete).thenReturn(true)
          when(p.personName).thenReturn(Some(PersonName("firstName", None, "lastName")))
          when(p.lineId).thenReturn(Some(4444))

          when(controller.statusService.getStatus(any[Option[String]](), any[(String, String)](), any[String]())(any(), any()))
            .thenReturn(Future.successful(RenewalSubmitted(None)))
          when(controller.dataCacheConnector.fetch[Seq[ResponsiblePerson]](any(), any())(any(), any()))
            .thenReturn(Future.successful(Some(Seq(p))))


          val result = controller.get(1)(request)

          status(result) must be(OK)
          val contentString = contentAsString(result)
          val doc =  Jsoup.parse(contentString)
          doc.getElementsMatchingOwnText(Messages("lbl.day")).hasText must be(true)

        }
      }

      "the submission status is Renewal" must {
        "respond with OK when the index is valid" in new Fixture {

          val p = mock[ResponsiblePerson]
          when(p.isComplete).thenReturn(true)
          when(p.personName).thenReturn(Some(PersonName("firstName", None, "lastName")))
          when(p.lineId).thenReturn(Some(4444))

          when(controller.statusService.getStatus(any[Option[String]](), any[(String, String)](), any[String]())(any(), any()))
            .thenReturn(Future.successful(ReadyForRenewal(None)))
          when(controller.dataCacheConnector.fetch[Seq[ResponsiblePerson]](any(), any())(any(), any()))
            .thenReturn(Future.successful(Some(Seq(p))))

          val result = controller.get(1)(request)

          status(result) must be(OK)
          val contentString = contentAsString(result)
          val doc =  Jsoup.parse(contentString)
          doc.getElementsMatchingOwnText(Messages("lbl.day")).hasText must be(true)

        }
      }
      "the submission status is SubmissionDecisionApproved" must {
        "respond with OK when the index is valid" in new Fixture {

          val p = mock[ResponsiblePerson]
          when(p.isComplete).thenReturn(true)
          when(p.personName).thenReturn(Some(PersonName("firstName", None, "lastName")))
          when(p.lineId).thenReturn(Some(4444))

          when(controller.statusService.getStatus(any[Option[String]](), any[(String, String)](), any[String]())(any(), any()))
            .thenReturn(Future.successful(SubmissionDecisionApproved))
          when(controller.dataCacheConnector.fetch[Seq[ResponsiblePerson]](any(), any())(any(), any()))
            .thenReturn(Future.successful(Some(Seq(p))))

          val result = controller.get(1)(request)

          status(result) must be(OK)
          val contentString = contentAsString(result)
          val doc =  Jsoup.parse(contentString)
          doc.getElementsMatchingOwnText(Messages("lbl.day")).hasText must be(true)

        }
        "respond with NOT_FOUND when the index is out of bounds" in new Fixture {

          when(controller.statusService.getStatus(any[Option[String]](), any[(String, String)](), any[String]())(any(), any()))
            .thenReturn(Future.successful(SubmissionDecisionApproved))
          when(controller.dataCacheConnector.fetch[Seq[ResponsiblePerson]](any(), any())(any(), any()))
            .thenReturn(Future.successful(Some(Seq(ResponsiblePerson()))))

          val result = controller.get(100)(request)

          status(result) must be(NOT_FOUND)

        }
        "respond with OK without showing endDate form when RP does not have lineId" in new Fixture{

          val rp = ResponsiblePerson(
            Some(PersonName("firstName", None, "lastName"))
          )

          when(controller.statusService.getStatus(any[Option[String]](), any[(String, String)](), any[String]())(any(), any()))
            .thenReturn(Future.successful(SubmissionDecisionApproved))

          when(controller.dataCacheConnector.fetch[Seq[ResponsiblePerson]](any(), any())(any(), any()))
            .thenReturn(Future.successful(Some(Seq(rp))))

          val result = controller.get(1)(request)

          status(result) must be(OK)

          contentAsString(result) must not include Messages("responsiblepeople.remove.responsible.person.enddate.lbl")
        }
      }
      "the submission status is SubmissionReadyForReview" must {
        "respond with OK without showing endDate form when RP does not have lineId" in new Fixture{

          val rp = ResponsiblePerson(
            Some(PersonName("firstName", None, "lastName"))
          )

          when(controller.statusService.getStatus(any[Option[String]](), any[(String, String)](), any[String]())(any(), any()))
            .thenReturn(Future.successful(SubmissionReadyForReview))

          when(controller.dataCacheConnector.fetch[Seq[ResponsiblePerson]](any(), any())(any(), any()))
            .thenReturn(Future.successful(Some(Seq(rp))))

          val result = controller.get(1)(request)

          status(result) must be(OK)

          contentAsString(result) must not include Messages("responsiblepeople.remove.responsible.person.enddate.lbl")

        }

        "respond with OK without showing endDate form when RP does have lineId" in new Fixture{

          val p = mock[ResponsiblePerson]
          when(p.isComplete).thenReturn(true)
          when(p.personName).thenReturn(Some(PersonName("firstName", None, "lastName")))
          when(p.lineId).thenReturn(Some(4444))

          when(controller.statusService.getStatus(any[Option[String]](), any[(String, String)](), any[String]())(any(), any()))
            .thenReturn(Future.successful(SubmissionReadyForReview))

          when(controller.dataCacheConnector.fetch[Seq[ResponsiblePerson]](any(), any())(any(), any()))
            .thenReturn(Future.successful(Some(Seq(p))))

          val result = controller.get(1)(request)

          status(result) must be(OK)

          contentAsString(result) must not include Messages("responsiblepeople.remove.responsible.person.enddate.lbl")

        }

        "redirect to start of RP flow where RP is not completed and has a lineId" in new Fixture {

          val emptyCache = CacheMap("", Map.empty)

          val p = mock[ResponsiblePerson]
          when(p.isComplete).thenReturn(false)
          when(p.personName).thenReturn(Some(PersonName("firstName", None, "lastName")))
          when(p.lineId).thenReturn(Some(4444))

          when(controller.statusService.getStatus(any[Option[String]](), any[(String, String)](), any[String]())(any(), any()))
            .thenReturn(Future.successful(SubmissionReadyForReview))

          when(controller.dataCacheConnector.fetch[Seq[ResponsiblePerson]](any(), any())(any(), any()))
            .thenReturn(Future.successful(Some(Seq(p))))

          val result = controller.get(1)(request)
          status(result) must be(SEE_OTHER)
          redirectLocation(result) must be(Some(controllers.responsiblepeople.routes.WhatYouNeedController.get(1).url))
        }
      }
    }

    "remove is called" must {
      "respond with SEE_OTHER" when {
        "removing a responsible person from an application with status NotCompleted" in new Fixture {

          val emptyCache = CacheMap("", Map.empty)

          when(controller.dataCacheConnector.fetch[Seq[ResponsiblePerson]](any(), any())(any(), any()))
            .thenReturn(Future.successful(Some(ResponsiblePeopleList)))
          when(controller.dataCacheConnector.save[Seq[ResponsiblePerson]](any(), any(), any())(any(), any()))
            .thenReturn(Future.successful(emptyCache))
          when(controller.statusService.getStatus(any[Option[String]](), any[(String, String)](), any[String]())(any(), any()))
            .thenReturn(Future.successful(NotCompleted))

          val result = controller.remove(1)(request)
          status(result) must be(SEE_OTHER)
          redirectLocation(result) must be(Some(controllers.responsiblepeople.routes.YourResponsiblePeopleController.get().url))

          verify(controller.dataCacheConnector).save[Seq[ResponsiblePerson]](any(), any(), meq(Seq(
            CompleteResponsiblePeople2,
            CompleteResponsiblePeople3
          )))(any(), any())
        }

        "removing a responsible person from an application with status SubmissionReady" in new Fixture {

          val emptyCache = CacheMap("", Map.empty)

          when(controller.dataCacheConnector.fetch[Seq[ResponsiblePerson]](any(), any())(any(), any()))
            .thenReturn(Future.successful(Some(ResponsiblePeopleList)))
          when(controller.dataCacheConnector.save[Seq[ResponsiblePerson]](any(), any(), any())(any(), any()))
            .thenReturn(Future.successful(emptyCache))
          when(controller.statusService.getStatus(any[Option[String]](), any[(String, String)](), any[String]())(any(), any()))
            .thenReturn(Future.successful(SubmissionReady))

          val result = controller.remove(1)(request)
          status(result) must be(SEE_OTHER)
          redirectLocation(result) must be(Some(controllers.responsiblepeople.routes.YourResponsiblePeopleController.get().url))

          verify(controller.dataCacheConnector).save[Seq[ResponsiblePerson]](any(), any(), meq(Seq(
            CompleteResponsiblePeople2,
            CompleteResponsiblePeople3
          )))(any(), any())
        }

        "removing a responsible person from an application with status SubmissionReady and redirect to your answers page" in new Fixture {

          val emptyCache = CacheMap("", Map.empty)

          when(controller.dataCacheConnector.fetch[Seq[ResponsiblePerson]](any(), any())(any(), any()))
            .thenReturn(Future.successful(Some(ResponsiblePeopleList)))
          when(controller.dataCacheConnector.save[Seq[ResponsiblePerson]](any(), any(), any())(any(), any()))
            .thenReturn(Future.successful(emptyCache))
          when(controller.statusService.getStatus(any[Option[String]](), any[(String, String)](), any[String]())(any(), any()))
            .thenReturn(Future.successful(SubmissionReady))

          val result = controller.remove(1)(request)
          status(result) must be(SEE_OTHER)
          redirectLocation(result) must be(Some(controllers.responsiblepeople.routes.YourResponsiblePeopleController.get().url))

          verify(controller.dataCacheConnector).save[Seq[ResponsiblePerson]](any(), any(), meq(Seq(
            CompleteResponsiblePeople2,
            CompleteResponsiblePeople3
          )))(any(), any())
        }

        "removing a responsible person with lineId from an application with status SubmissionReadyForReview" in new Fixture {

          val emptyCache = CacheMap("", Map.empty)

          when(controller.dataCacheConnector.fetch[Seq[ResponsiblePerson]](any(), any())(any(), any()))
                  .thenReturn(Future.successful(Some(Seq(CompleteResponsiblePeople1, CompleteResponsiblePeople2, CompleteResponsiblePeople3))))
          when(controller.dataCacheConnector.save[Seq[ResponsiblePerson]](any(), any(), any())(any(), any()))
            .thenReturn(Future.successful(emptyCache))
          when(controller.statusService.getStatus(any[Option[String]](), any[(String, String)](), any[String]())(any(), any()))
            .thenReturn(Future.successful(SubmissionReadyForReview))


          val result = controller.remove(1)(request)
          status(result) must be(SEE_OTHER)
          redirectLocation(result) must be(Some(controllers.responsiblepeople.routes.YourResponsiblePeopleController.get().url))

          verify(controller.dataCacheConnector).save[Seq[ResponsiblePerson]](any(), any(), meq(Seq(
            CompleteResponsiblePeople1.copy(status = Some(StatusConstants.Deleted), hasChanged = true),
            CompleteResponsiblePeople2,
            CompleteResponsiblePeople3
          )))(any(), any())
        }

        "removing a responsible person without lineId from an application with status SubmissionReadyForReview" in new Fixture {

          val emptyCache = CacheMap("", Map.empty)

          when(controller.dataCacheConnector.fetch[Seq[ResponsiblePerson]](any(), any())(any(), any()))
                  .thenReturn(Future.successful(Some(Seq(CompleteResponsiblePeople1.copy(lineId = None), CompleteResponsiblePeople2, CompleteResponsiblePeople3))))
          when(controller.dataCacheConnector.save[Seq[ResponsiblePerson]](any(), any(), any())(any(), any()))
                  .thenReturn(Future.successful(emptyCache))
          when(controller.statusService.getStatus(any[Option[String]](), any[(String, String)](), any[String]())(any(), any()))
                  .thenReturn(Future.successful(SubmissionReadyForReview))


          val result = controller.remove(1)(request)
          status(result) must be(SEE_OTHER)
          redirectLocation(result) must be(Some(controllers.responsiblepeople.routes.YourResponsiblePeopleController.get().url))

          verify(controller.dataCacheConnector).save[Seq[ResponsiblePerson]](any(), any(), meq(Seq(
            CompleteResponsiblePeople2,
            CompleteResponsiblePeople3
          )))(any(), any())
        }

        "removing a responsible person from an application with status SubmissionDecisionApproved" in new Fixture {

          val emptyCache = CacheMap("", Map.empty)
          val newRequest = requestWithUrlEncodedBody(
            "endDate.day" -> "1",
            "endDate.month" -> "1",
            "endDate.year" -> "2006"
          )

          when(controller.dataCacheConnector.fetch[Seq[ResponsiblePerson]](any(), any())(any(), any()))
            .thenReturn(Future.successful(Some(ResponsiblePeopleList)))
          when(controller.dataCacheConnector.save[Seq[ResponsiblePerson]](any(), any(), any())(any(), any()))
            .thenReturn(Future.successful(emptyCache))
          when(controller.statusService.getStatus(any[Option[String]](), any[(String, String)](), any[String]())(any(), any()))
            .thenReturn(Future.successful(SubmissionDecisionApproved))

          val result = controller.remove(1)(newRequest)
          status(result) must be(SEE_OTHER)
          redirectLocation(result) must be(Some(controllers.responsiblepeople.routes.YourResponsiblePeopleController.get().url))

          verify(controller.dataCacheConnector).save[Seq[ResponsiblePerson]](any(), any(), meq(Seq(
            CompleteResponsiblePeople1.copy(status = Some(StatusConstants.Deleted), hasChanged = true,
              endDate = Some(ResponsiblePersonEndDate(new LocalDate(2006, 1, 1)))),
            CompleteResponsiblePeople2,
            CompleteResponsiblePeople3
          )))(any(), any())
        }

        "removing a new incomplete responsible person from an application with status SubmissionDecisionApproved" in new Fixture {

          val emptyCache = CacheMap("", Map.empty)
          val newRequest = requestWithUrlEncodedBody("" -> "")

          val people = Seq(
            responsiblePersonGen.sample.get.copy(lineId = None, positions = Some(positionsGen.sample.get.copy(startDate = None)))
          )

          when(controller.dataCacheConnector.fetch[Seq[ResponsiblePerson]](any(), any())(any(), any()))
            .thenReturn(Future.successful(Some(people)))

          when(controller.dataCacheConnector.save[Seq[ResponsiblePerson]](any(), any(), any())(any(), any()))
            .thenReturn(Future.successful(emptyCache))

          when(controller.statusService.getStatus(any[Option[String]](), any[(String, String)](), any[String]())(any(), any()))
            .thenReturn(Future.successful(SubmissionDecisionApproved))

          val result = controller.remove(1)(newRequest)

          status(result) must be(SEE_OTHER)
          redirectLocation(result) must be(Some(controllers.responsiblepeople.routes.YourResponsiblePeopleController.get().url))

          val captor = ArgumentCaptor.forClass(classOf[Seq[ResponsiblePerson]])
          verify(controller.dataCacheConnector).save[Seq[ResponsiblePerson]](any(), meq(ResponsiblePerson.key), captor.capture())(any(), any())

          captor.getValue mustBe Seq.empty[ResponsiblePerson]
        }

        "removing a responsible person from an application with no date" in new Fixture {
          val emptyCache = CacheMap("", Map.empty)

          val newRequest = requestWithUrlEncodedBody(
            "endDate.day" -> "",
            "endDate.month" -> "",
            "endDate.year" -> ""
          )

          when(controller.dataCacheConnector.fetch[Seq[ResponsiblePerson]](any(), any())(any(), any()))
            .thenReturn(Future.successful(Some(Seq(CompleteResponsiblePeople1.copy(lineId = None)))))
          when(controller.dataCacheConnector.save[Seq[ResponsiblePerson]](any(), any(), any())(any(), any()))
            .thenReturn(Future.successful(emptyCache))
          when(controller.statusService.getStatus(any[Option[String]](), any[(String, String)](), any[String]())(any(), any()))
            .thenReturn(Future.successful(SubmissionDecisionApproved))

          val result = controller.remove(1)(newRequest)
          status(result) must be(SEE_OTHER)

        }

      }

      "respond with BAD_REQUEST" when {
        "removing a responsible person from an application with no date" in new Fixture {
          val emptyCache = CacheMap("", Map.empty)

          val newRequest = requestWithUrlEncodedBody(
            "endDate.day" -> "",
            "endDate.month" -> "",
            "endDate.year" -> ""
          )

          when(controller.dataCacheConnector.fetch[Seq[ResponsiblePerson]](any(), any())(any(), any()))
            .thenReturn(Future.successful(Some(ResponsiblePeopleList)))
          when(controller.dataCacheConnector.save[Seq[ResponsiblePerson]](any(), any(), any())(any(), any()))
            .thenReturn(Future.successful(emptyCache))
          when(controller.statusService.getStatus(any[Option[String]](), any[(String, String)](), any[String]())(any(), any()))
            .thenReturn(Future.successful(SubmissionDecisionApproved))

          val result = controller.remove(1)(newRequest)
          status(result) must be(BAD_REQUEST)
          contentAsString(result) must include(Messages("error.expected.jodadate.format"))

        }

        "removing a responsible person from an application given a year which is too short" in new Fixture {
          val emptyCache = CacheMap("", Map.empty)

          val newRequest = requestWithUrlEncodedBody(
            "endDate.day" -> "24",
            "endDate.month" -> "2",
            "endDate.year" -> "16"
          )

          when(controller.dataCacheConnector.fetch[Seq[ResponsiblePerson]](any(), any())(any(), any()))
            .thenReturn(Future.successful(Some(ResponsiblePeopleList)))
          when(controller.dataCacheConnector.save[Seq[ResponsiblePerson]](any(), any(), any())(any(), any()))
            .thenReturn(Future.successful(emptyCache))
          when(controller.statusService.getStatus(any[Option[String]](), any[(String, String)](), any[String]())(any(), any()))
            .thenReturn(Future.successful(SubmissionDecisionApproved))

          val result = controller.remove(1)(newRequest)
          status(result) must be(BAD_REQUEST)
          contentAsString(result) must include(Messages("error.expected.jodadate.format"))

        }

        "removing a responsible person from an application given a year which is too long" in new Fixture {
          val emptyCache = CacheMap("", Map.empty)

          val newRequest = requestWithUrlEncodedBody(
            "endDate.day" -> "24",
            "endDate.month" -> "2",
            "endDate.year" -> "10166"
          )

          when(controller.dataCacheConnector.fetch[Seq[ResponsiblePerson]](any(), any())(any(), any()))
            .thenReturn(Future.successful(Some(ResponsiblePeopleList)))
          when(controller.dataCacheConnector.save[Seq[ResponsiblePerson]](any(), any(), any())(any(), any()))
            .thenReturn(Future.successful(emptyCache))
          when(controller.statusService.getStatus(any[Option[String]](), any[(String, String)](), any[String]())(any(), any()))
            .thenReturn(Future.successful(SubmissionDecisionApproved))

          val result = controller.remove(1)(newRequest)
          status(result) must be(BAD_REQUEST)
          contentAsString(result) must include(Messages("error.expected.jodadate.format"))

        }

        "removing a rp from an application with future date" in new Fixture {
          val emptyCache = CacheMap("", Map.empty)

          val newRequest = requestWithUrlEncodedBody(
            "endDate.day" -> "15",
            "endDate.month" -> "1",
            "endDate.year" -> "2030"
          )

          when(controller.dataCacheConnector.fetch[Seq[ResponsiblePerson]](any(), any())(any(), any()))
            .thenReturn(Future.successful(Some(ResponsiblePeopleList)))
          when(controller.dataCacheConnector.save[Seq[ResponsiblePerson]](any(), any(), any())(any(), any()))
            .thenReturn(Future.successful(emptyCache))
          when(controller.statusService.getStatus(any[Option[String]](), any[(String, String)](), any[String]())(any(), any()))
            .thenReturn(Future.successful(SubmissionDecisionApproved))

          val result = controller.remove(1)(newRequest)
          status(result) must be(BAD_REQUEST)
          contentAsString(result) must include(Messages("error.future.date"))

        }

        "removing a responsible person from an application with end date before position start date" in new Fixture {

          val emptyCache = CacheMap("", Map.empty)

          val position = Positions(Set(InternalAccountant), Some(PositionStartDate(new LocalDate(1999, 5, 1))))
          val peopleList = Seq(CompleteResponsiblePeople1.copy(positions = Some(position)))

          val newRequest = requestWithUrlEncodedBody(
            "endDate.day" -> "15",
            "endDate.month" -> "1",
            "endDate.year" -> "1998"
          )

          when(controller.dataCacheConnector.fetch[Seq[ResponsiblePerson]](any(), any())(any(), any()))
            .thenReturn(Future.successful(Some(peopleList)))
          when(controller.dataCacheConnector.save[Seq[ResponsiblePerson]](any(), any(), any())(any(), any()))
            .thenReturn(Future.successful(emptyCache))
          when(controller.statusService.getStatus(any[Option[String]](), any[(String, String)](), any[String]())(any(), any()))
            .thenReturn(Future.successful(SubmissionDecisionApproved))

          val result = controller.remove(1)(newRequest)
          status(result) must be(BAD_REQUEST)
          //contentAsString(result) must include(Messages("error.expected.future.date.after.start"))

        }
      }

    }
  }

  private val residence = UKResidence(Nino(nextNino))
  private val residenceCountry = Country("United Kingdom", "GB")
  private val residenceNationality = Country("United Kingdom", "GB")
  private val currentPersonAddress = PersonAddressUK("Line 1", "Line 2", None, None, "AA111AA")
  private val currentAddress = ResponsiblePersonCurrentAddress(currentPersonAddress, Some(ZeroToFiveMonths))
  private val additionalPersonAddress = PersonAddressUK("Line 1", "Line 2", None, None, "AA11AA")
  private val additionalAddress = ResponsiblePersonAddress(additionalPersonAddress, Some(ZeroToFiveMonths))
  //scalastyle:off magic.number
  val personName = PersonName("firstName", Some("middleName"), "lastName")
  val legalName = PreviousName(Some(true), Some("firstName"), Some("middleName"), Some("lastName"))
  val legalNameChangeDate = new LocalDate(1990, 2, 24)
  val knownBy = KnownBy(Some(true), Some("knownByName"))
  val personResidenceType = PersonResidenceType(residence, Some(residenceCountry), Some(residenceNationality))
  val saRegistered = SaRegisteredYes("0123456789")
  val contactDetails = ContactDetails("07000000000", "test@test.com")
  val addressHistory = ResponsiblePersonAddressHistory(Some(currentAddress), Some(additionalAddress))
  val vatRegistered = VATRegisteredNo
  val training = TrainingYes("test")
  val experienceTraining = ExperienceTrainingYes("Some training")

  //scalastyle:off magic.number
  val positions = Positions(Set(BeneficialOwner, InternalAccountant),Some(PositionStartDate(new LocalDate(2005, 3, 15))))

  val CompleteResponsiblePeople1 = ResponsiblePerson(
    personName = Some(personName),
    legalName = Some(legalName),
    legalNameChangeDate = Some(legalNameChangeDate),
    knownBy = Some(knownBy),
    personResidenceType = Some(personResidenceType),
    ukPassport = None,
    nonUKPassport = None,
    dateOfBirth = None,
    contactDetails = Some(contactDetails),
    addressHistory = Some(addressHistory),
    positions = Some(positions),
    saRegistered = Some(saRegistered),
    vatRegistered = Some(vatRegistered),
    experienceTraining = Some(experienceTraining),
    training = Some(training),
    approvalFlags = ApprovalFlags(hasAlreadyPassedFitAndProper = Some(true)),
    hasChanged = false,
    hasAccepted = false,
    lineId = Some(1),
    status = Some("test")
  )
  val CompleteResponsiblePeople2 = ResponsiblePerson(
    personName = Some(personName),
    legalName = Some(legalName),
    legalNameChangeDate = Some(legalNameChangeDate),
    knownBy = Some(knownBy),
    personResidenceType = Some(personResidenceType),
    ukPassport = None,
    nonUKPassport = None,
    dateOfBirth = None,
    contactDetails = Some(contactDetails),
    addressHistory = Some(addressHistory),
    positions = Some(positions),
    saRegistered = Some(saRegistered),
    vatRegistered = Some(vatRegistered),
    experienceTraining = Some(experienceTraining),
    training = Some(training),
    approvalFlags = ApprovalFlags(hasAlreadyPassedFitAndProper = Some(true)),
    hasChanged = false,
    hasAccepted = false,
    lineId = Some(1),
    status = Some("test")
  )
  val CompleteResponsiblePeople3 = ResponsiblePerson(
    personName = Some(personName),
    legalName = Some(legalName),
    legalNameChangeDate = Some(legalNameChangeDate),
    knownBy = Some(knownBy),
    personResidenceType = Some(personResidenceType),
    ukPassport = None,
    nonUKPassport = None,
    dateOfBirth = None,
    contactDetails = Some(contactDetails),
    addressHistory = Some(addressHistory),
    positions = Some(positions),
    saRegistered = Some(saRegistered),
    vatRegistered = Some(vatRegistered),
    experienceTraining = Some(experienceTraining),
    training = Some(training),
    approvalFlags = ApprovalFlags(hasAlreadyPassedFitAndProper = Some(true)),
    hasChanged = false,
    hasAccepted = false,
    lineId = Some(1),
    status = Some("test")
  )

  val ResponsiblePeopleList = Seq(CompleteResponsiblePeople1, CompleteResponsiblePeople2, CompleteResponsiblePeople3)
}
