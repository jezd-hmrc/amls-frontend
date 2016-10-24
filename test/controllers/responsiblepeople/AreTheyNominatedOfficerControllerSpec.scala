package controllers.responsiblepeople

import connectors.DataCacheConnector
import models.Country
import models.businesscustomer.{Address, ReviewDetails}
import models.businessmatching.{BusinessMatching, BusinessType}
import models.responsiblepeople._
import org.joda.time.LocalDate
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.i18n.Messages
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.http.HeaderCarrier
import utils.AuthorisedFixture


import scala.concurrent.Future

class AreTheyNominatedOfficerControllerSpec extends PlaySpec with OneAppPerSuite with MockitoSugar {

  trait Fixture extends AuthorisedFixture {
    self =>

    val controller = new AreTheyNominatedOfficerController {
      override val dataCacheConnector = mock[DataCacheConnector]
      override val authConnector = self.authConnector
    }

    object DefaultValues {
      val noNominatedOfficerPositions = Positions(Set(BeneficialOwner, InternalAccountant), startDate)
      val hasNominatedOfficerPositions = Positions(Set(BeneficialOwner, InternalAccountant, NominatedOfficer), startDate)
    }

    val noNominatedOfficer = ResponsiblePeople(None, None, None, None, Some(DefaultValues.noNominatedOfficerPositions), None, None, None, None, Some(true), false, Some(1), Some("test"))
    val hasNominatedOfficer = ResponsiblePeople(None, None, None, None, Some(DefaultValues.hasNominatedOfficerPositions), None, None, None, None, Some(true), false, Some(1), Some("test"))
    val withPartnerShip = ResponsiblePeople(None, None, None, None, Some(DefaultValues.hasNominatedOfficerPositions.copy(positions = DefaultValues.hasNominatedOfficerPositions.positions + Partner)), None, None, None, None, Some(true), false, Some(1), Some("test"))
  }

  val emptyCache = CacheMap("", Map.empty)

  val RecordId = 1

  private val startDate: Option[LocalDate] = Some(new LocalDate())
  "AreTheyNominatedOfficerController" must {

    "on get()" must {

      "display nominated officer fields" in new Fixture {
        val mockCacheMap = mock[CacheMap]
        val reviewDtls = ReviewDetails("BusinessName", Some(BusinessType.SoleProprietor),
          Address("line1", "line2", Some("line3"), Some("line4"), Some("NE77 0QQ"), Country("United Kingdom", "GB")), "ghghg")
        val businessMatching = BusinessMatching(Some(reviewDtls))
        when(controller.dataCacheConnector.fetchAll(any[HeaderCarrier], any[AuthContext]))
          .thenReturn(Future.successful(Some(mockCacheMap)))
        when(mockCacheMap.getEntry[Seq[ResponsiblePeople]](any())(any()))
          .thenReturn(Some(Seq(ResponsiblePeople())))
        when(mockCacheMap.getEntry[BusinessMatching](BusinessMatching.key))
          .thenReturn(Some(businessMatching))
        val result = controller.get(RecordId)(request)

        status(result) must be(OK)

        val document = Jsoup.parse(contentAsString(result))
        document.title must include(Messages("responsiblepeople.aretheynominatedofficer.title"))
        document.select("input[value=true]") must not be(null)
        document.select("input[value=false]") must not be(null)

        document.body().html() must include(Messages("responsiblepeople.aretheynominatedofficer.heading"))

      }
    }

    "post" must {
      "submit with valid data as a partnership" in new Fixture {
        val mockCacheMap = mock[CacheMap]
        val newRequest = request.withFormUrlEncodedBody("isNominatedOfficer" -> "true")
        when(mockCacheMap.getEntry[Seq[ResponsiblePeople]](any())(any()))
          .thenReturn(Some(Seq(withPartnerShip)))
        when(controller.dataCacheConnector.fetch[Seq[ResponsiblePeople]](any())
          (any(), any(), any())).thenReturn(Future.successful(Some(Seq(withPartnerShip))))
        when(controller.dataCacheConnector.save[Seq[ResponsiblePeople]](any(), any())(any(), any(), any())).thenReturn(Future.successful(mockCacheMap))

        val result = controller.post(RecordId)(newRequest)
        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(controllers.responsiblepeople.routes.VATRegisteredController.get(RecordId).url))
      }

      "submit with valid data as another type" in new Fixture {
        val mockCacheMap = mock[CacheMap]
        val newRequest = request.withFormUrlEncodedBody("isNominatedOfficer" -> "true")
        when(mockCacheMap.getEntry[Seq[ResponsiblePeople]](any())(any()))
          .thenReturn(Some(Seq(noNominatedOfficer)))
        when(controller.dataCacheConnector.fetch[Seq[ResponsiblePeople]](any())
          (any(), any(), any())).thenReturn(Future.successful(Some(Seq(noNominatedOfficer))))
        when(controller.dataCacheConnector.save[Seq[ResponsiblePeople]](any(), any())(any(), any(), any())).thenReturn(Future.successful(mockCacheMap))

        val result = controller.post(RecordId)(newRequest)
        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(controllers.responsiblepeople.routes.ExperienceTrainingController.get(RecordId).url))
      }

      "submit with valid data as another type and edit mode" in new Fixture {
        val mockCacheMap = mock[CacheMap]
        val newRequest = request.withFormUrlEncodedBody("isNominatedOfficer" -> "true")
        when(mockCacheMap.getEntry[Seq[ResponsiblePeople]](any())(any()))
          .thenReturn(Some(Seq(hasNominatedOfficer)))
        when(controller.dataCacheConnector.fetch[Seq[ResponsiblePeople]](any())
          (any(), any(), any())).thenReturn(Future.successful(Some(Seq(hasNominatedOfficer))))
        when(controller.dataCacheConnector.save[Seq[ResponsiblePeople]](any(), any())(any(), any(), any())).thenReturn(Future.successful(mockCacheMap))

        val result = controller.post(RecordId,true)(newRequest)
        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(controllers.responsiblepeople.routes.DetailedAnswersController.get(RecordId).url))
      }

      "fail submission on empty string" in new Fixture {

        val newRequest = request.withFormUrlEncodedBody("isNominatedOfficer" -> "")

        when(controller.dataCacheConnector.fetch[Seq[ResponsiblePeople]](any())
          (any(), any(), any())).thenReturn(Future.successful(None))

        val result = controller.post(RecordId)(newRequest)
        status(result) must be(BAD_REQUEST)
        val document: Document = Jsoup.parse(contentAsString(result))
        document.select("a[href=#isNominatedOfficer]").html() must include(Messages("error.required.rp.nominated_officer"))

      }

      "return not found when no rps" in new Fixture {

        val newRequest = request.withFormUrlEncodedBody("isNominatedOfficer" -> "true")

        when(controller.dataCacheConnector.fetch[Seq[ResponsiblePeople]](any())
          (any(), any(), any())).thenReturn(Future.successful(None))

        val result = controller.post(RecordId)(newRequest)
        status(result) must be(NOT_FOUND)

      }

      "return not found when index out of bounds" in new Fixture {

        val newRequest = request.withFormUrlEncodedBody("isNominatedOfficer" -> "true")

        when(controller.dataCacheConnector.fetch[Seq[ResponsiblePeople]](any())
          (any(), any(), any())).thenReturn(Future.failed(new IndexOutOfBoundsException))

        val result = controller.post(RecordId)(newRequest)
        status(result) must be(NOT_FOUND)

      }

      "return not found" in new Fixture {
        val mockCacheMap = mock[CacheMap]
        val newRequest = request.withFormUrlEncodedBody("isNominatedOfficer" -> "true")
        when(mockCacheMap.getEntry[Seq[ResponsiblePeople]](any())(any()))
          .thenReturn(Some(Seq(withPartnerShip)))
        when(controller.dataCacheConnector.fetch[Seq[ResponsiblePeople]](any())
          (any(), any(), any())).thenReturn(Future.successful(Some(Seq(withPartnerShip))))
        when(controller.dataCacheConnector.save[Seq[ResponsiblePeople]](any(), any())(any(), any(), any())).thenReturn(Future.successful(mockCacheMap))

        val result = controller.post(0)(newRequest)
        status(result) must be(NOT_FOUND)
       
      }
    }
  }


}