package controllers.responsiblepeople

import connectors.DataCacheConnector
import models.Country
import models.responsiblepeople.TimeAtAddress.ZeroToFiveMonths
import models.responsiblepeople._
import models.status._
import org.joda.time.LocalDate
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, WordSpecLike}
import org.scalatestplus.play.OneAppPerSuite
import services.{AuthEnrolmentsService, StatusService}
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.{StatusConstants, AuthorisedFixture}
import play.api.test.Helpers._
import org.mockito.Matchers.{eq => meq, _}
import play.api.i18n.Messages


import scala.concurrent.Future

class RemoveResponsiblePersonControllerSpec extends WordSpecLike
  with MustMatchers with MockitoSugar with ScalaFutures with OneAppPerSuite with PropertyChecks {

  trait Fixture extends AuthorisedFixture {
    self =>

    val controller = new RemoveResponsiblePersonController {
      override val dataCacheConnector = mock[DataCacheConnector]
      override val statusService: StatusService =  mock[StatusService]
      override val authConnector = self.authConnector
      override val authEnrolmentsService: AuthEnrolmentsService = mock[AuthEnrolmentsService]
    }
  }

  "RemoveResponsiblePersonController" when {
    "get is called" when {
      "the submission status is NotCompleted" must {
        "respond with OK when the index is valid" in new Fixture {

          when(controller.statusService.getStatus(any(), any(), any()))
            .thenReturn(Future.successful(NotCompleted))
          when(controller.dataCacheConnector.fetch[Seq[ResponsiblePeople]](any())(any(), any(), any()))
            .thenReturn(Future.successful(Some(Seq(ResponsiblePeople(Some(PersonName("firstName", None, "lastName", None, None)))))))

          val result = controller.get(1, false)(request)

          status(result) must be(OK)

        }
        "respond with NOT_FOUND when the index is out of bounds" in new Fixture {

          when(controller.statusService.getStatus(any(), any(), any()))
            .thenReturn(Future.successful(NotCompleted))
          when(controller.dataCacheConnector.fetch[Seq[ResponsiblePeople]](any())(any(), any(), any()))
            .thenReturn(Future.successful(Some(Seq(ResponsiblePeople()))))

          val result = controller.get(100, false)(request)

          status(result) must be(NOT_FOUND)

        }
      }
      "the submission status is SubmissionDecisionApproved" must {
        "respond with OK when the index is valid" in new Fixture {

          when(controller.statusService.getStatus(any(), any(), any()))
            .thenReturn(Future.successful(SubmissionDecisionApproved))
          when(controller.dataCacheConnector.fetch[Seq[ResponsiblePeople]](any())(any(), any(), any()))
            .thenReturn(Future.successful(Some(Seq(ResponsiblePeople(Some(PersonName("firstName", None, "lastName", None, None)))))))

          val result = controller.get(1, false)(request)

          status(result) must be(OK)

        }
        "respond with NOT_FOUND when the index is out of bounds" in new Fixture {

          when(controller.statusService.getStatus(any(), any(), any()))
            .thenReturn(Future.successful(SubmissionDecisionApproved))
          when(controller.dataCacheConnector.fetch[Seq[ResponsiblePeople]](any())(any(), any(), any()))
            .thenReturn(Future.successful(Some(Seq(ResponsiblePeople()))))

          val result = controller.get(100, false)(request)

          status(result) must be(NOT_FOUND)

        }
      }
    }

    "remove is called" must {
      "respond with SEE_OTHER" when {
        "removing a responsible person from an application with status NotCompleted" in new Fixture {

          val emptyCache = CacheMap("", Map.empty)

          when(controller.dataCacheConnector.fetch[Seq[ResponsiblePeople]](any())(any(), any(), any()))
            .thenReturn(Future.successful(Some(ResponsiblePeopleList)))
          when(controller.dataCacheConnector.save[Seq[ResponsiblePeople]](any(), any())(any(), any(), any()))
            .thenReturn(Future.successful(emptyCache))
          when(controller.statusService.getStatus(any(), any(), any()))
            .thenReturn(Future.successful(NotCompleted))

          val result = controller.remove(1, false, "John Envy Doe")(request)
          status(result) must be(SEE_OTHER)
          redirectLocation(result) must be(Some(controllers.responsiblepeople.routes.CheckYourAnswersController.get().url))

          verify(controller.dataCacheConnector).save[Seq[ResponsiblePeople]](any(), meq(Seq(
            CompleteResponsiblePeople2,
            CompleteResponsiblePeople3
          )))(any(), any(), any())
        }

        "removing a responsible person from an application with status SubmissionReady" in new Fixture {

          val emptyCache = CacheMap("", Map.empty)

          when(controller.dataCacheConnector.fetch[Seq[ResponsiblePeople]](any())(any(), any(), any()))
            .thenReturn(Future.successful(Some(ResponsiblePeopleList)))
          when(controller.dataCacheConnector.save[Seq[ResponsiblePeople]](any(), any())(any(), any(), any()))
            .thenReturn(Future.successful(emptyCache))
          when(controller.statusService.getStatus(any(), any(), any()))
            .thenReturn(Future.successful(SubmissionReady))

          val result = controller.remove(1, false, "John Envy Doe")(request)
          status(result) must be(SEE_OTHER)
          redirectLocation(result) must be(Some(controllers.responsiblepeople.routes.CheckYourAnswersController.get().url))

          verify(controller.dataCacheConnector).save[Seq[ResponsiblePeople]](any(), meq(Seq(
            CompleteResponsiblePeople2,
            CompleteResponsiblePeople3
          )))(any(), any(), any())
        }

        "removing a responsible person from an application with status SubmissionReadyForReview" in new Fixture {

          val emptyCache = CacheMap("", Map.empty)

          when(controller.dataCacheConnector.fetch[Seq[ResponsiblePeople]](any())(any(), any(), any()))
            .thenReturn(Future.successful(Some(ResponsiblePeopleList)))
          when(controller.dataCacheConnector.save[Seq[ResponsiblePeople]](any(), any())(any(), any(), any()))
            .thenReturn(Future.successful(emptyCache))
          when(controller.statusService.getStatus(any(), any(), any()))
            .thenReturn(Future.successful(SubmissionReadyForReview))


          val result = controller.remove(1, false, "John Envy Doe")(request)
          status(result) must be(SEE_OTHER)
          redirectLocation(result) must be(Some(controllers.responsiblepeople.routes.CheckYourAnswersController.get().url))

          verify(controller.dataCacheConnector).save[Seq[ResponsiblePeople]](any(), meq(Seq(
            CompleteResponsiblePeople1.copy(status = Some(StatusConstants.Deleted), hasChanged = true),
            CompleteResponsiblePeople2,
            CompleteResponsiblePeople3
          )))(any(), any(), any())
        }

        "removing a responsible person from an application with status SubmissionDecisionApproved" in new Fixture {

          val emptyCache = CacheMap("", Map.empty)
          val newRequest = request.withFormUrlEncodedBody(
            "endDate.day" -> "1",
            "endDate.month" -> "1",
            "endDate.year" -> "1990"
          )

          when(controller.dataCacheConnector.fetch[Seq[ResponsiblePeople]](any())(any(), any(), any()))
            .thenReturn(Future.successful(Some(ResponsiblePeopleList)))
          when(controller.dataCacheConnector.save[Seq[ResponsiblePeople]](any(), any())(any(), any(), any()))
            .thenReturn(Future.successful(emptyCache))
          when(controller.statusService.getStatus(any(), any(), any()))
            .thenReturn(Future.successful(SubmissionDecisionApproved))


          val result = controller.remove(1, false, "John Envy Doe")(newRequest)
          status(result) must be(SEE_OTHER)
          redirectLocation(result) must be(Some(controllers.responsiblepeople.routes.CheckYourAnswersController.get().url))

          verify(controller.dataCacheConnector).save[Seq[ResponsiblePeople]](any(), meq(Seq(
            CompleteResponsiblePeople1.copy(status = Some(StatusConstants.Deleted), hasChanged = true,
              endDate = Some(ResponsiblePersonEndDate(new LocalDate(1990, 1, 1)))),
            CompleteResponsiblePeople2,
            CompleteResponsiblePeople3
          )))(any(), any(), any())
        }
      }

      "respond with BAD_REQUEST" when {
        "removing a responsible person from an application with no date" in new Fixture {
          val emptyCache = CacheMap("", Map.empty)

          val newRequest = request.withFormUrlEncodedBody(
            "endDate.day" -> "",
            "endDate.month" -> "",
            "endDate.year" -> ""
          )

          when(controller.dataCacheConnector.fetch[Seq[ResponsiblePeople]](any())(any(), any(), any()))
            .thenReturn(Future.successful(Some(ResponsiblePeopleList)))
          when(controller.dataCacheConnector.save[Seq[ResponsiblePeople]](any(), any())(any(), any(), any()))
            .thenReturn(Future.successful(emptyCache))
          when(controller.statusService.getStatus(any(), any(), any()))
            .thenReturn(Future.successful(SubmissionDecisionApproved))

          val result = controller.remove(1, true, "person Name")(newRequest)
          status(result) must be(BAD_REQUEST)
          contentAsString(result) must include(Messages("error.expected.jodadate.format"))

        }

        "removing a trading premises from an application with future date" in new Fixture {
          val emptyCache = CacheMap("", Map.empty)

          val newRequest = request.withFormUrlEncodedBody(
            "endDate.day" -> "15",
            "endDate.month" -> "1",
            "endDate.year" -> "2020"
          )

          when(controller.dataCacheConnector.fetch[Seq[ResponsiblePeople]](any())(any(), any(), any()))
            .thenReturn(Future.successful(Some(ResponsiblePeopleList)))
          when(controller.dataCacheConnector.save[Seq[ResponsiblePeople]](any(), any())(any(), any(), any()))
            .thenReturn(Future.successful(emptyCache))
          when(controller.statusService.getStatus(any(), any(), any()))
            .thenReturn(Future.successful(SubmissionDecisionApproved))

          val result = controller.remove(1, true, "person Name")(newRequest)
          status(result) must be(BAD_REQUEST)
          contentAsString(result) must include(Messages("error.expected.future.date"))

        }
      }

    }
  }

  private val residence = UKResidence("AA3464646")
  private val residenceCountry = Country("United Kingdom", "GB")
  private val residenceNationality = Country("United Kingdom", "GB")
  private val currentPersonAddress = PersonAddressUK("Line 1", "Line 2", None, None, "NE981ZZ")
  private val currentAddress = ResponsiblePersonAddress(currentPersonAddress, ZeroToFiveMonths)
  private val additionalPersonAddress = PersonAddressUK("Line 1", "Line 2", None, None, "NE15GH")
  private val additionalAddress = ResponsiblePersonAddress(additionalPersonAddress, ZeroToFiveMonths)
  //scalastyle:off magic.number
  val previousName = PreviousName(Some("Matt"), Some("Mc"), Some("Fly"), new LocalDate(1990, 2, 24))
  val personName = PersonName("John", Some("Envy"), "Doe", Some(previousName), Some("name"))
  val personResidenceType = PersonResidenceType(residence, residenceCountry, Some(residenceNationality))
  val saRegistered = SaRegisteredYes("0123456789")
  val contactDetails = ContactDetails("07702743555", "test@test.com")
  val addressHistory = ResponsiblePersonAddressHistory(Some(currentAddress), Some(additionalAddress))
  val vatRegistered = VATRegisteredNo
  val training = TrainingYes("test")
  val experienceTraining = ExperienceTrainingYes("Some training")
  val positions = Positions(Set(BeneficialOwner, InternalAccountant),Some(new LocalDate()))

  val CompleteResponsiblePeople1 = ResponsiblePeople(
    Some(personName),
    Some(personResidenceType),
    Some(contactDetails),
    Some(addressHistory),
    Some(positions),
    Some(saRegistered),
    Some(vatRegistered),
    Some(experienceTraining),
    Some(training),
    Some(true),
    false,
    Some(1),
    Some("test")
  )
  val CompleteResponsiblePeople2 = ResponsiblePeople(
    Some(personName),
    Some(personResidenceType),
    Some(contactDetails),
    Some(addressHistory),
    Some(positions),
    Some(saRegistered),
    Some(vatRegistered),
    Some(experienceTraining),
    Some(training),
    Some(true),
    false,
    Some(1),
    Some("test")
  )
  val CompleteResponsiblePeople3 = ResponsiblePeople(
    Some(personName),
    Some(personResidenceType),
    Some(contactDetails),
    Some(addressHistory),
    Some(positions),
    Some(saRegistered),
    Some(vatRegistered),
    Some(experienceTraining),
    Some(training),
    Some(true),
    false,
    Some(1),
    Some("test")
  )

  val ResponsiblePeopleList = Seq(CompleteResponsiblePeople1, CompleteResponsiblePeople2, CompleteResponsiblePeople3)
}