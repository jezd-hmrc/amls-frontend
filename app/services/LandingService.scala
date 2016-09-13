package services

import connectors.{DESConnector, DataCacheConnector, KeystoreConnector}
import models.aboutthebusiness.AboutTheBusiness
import models.businesscustomer.ReviewDetails
import models.businessmatching.BusinessMatching
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait LandingService {

  private[services] def cacheConnector: DataCacheConnector
  private[services] def keyStore: KeystoreConnector
  private[controllers] def desConnector: DESConnector

  @deprecated("fetch the cacheMap itself instead", "")
  def hasSavedForm
  (implicit
   hc: HeaderCarrier,
   ec: ExecutionContext,
   ac: AuthContext
  ): Future[Boolean] =
    cacheConnector.fetchAll map {
      case Some(_) => true
      case None => false
    }

  def cacheMap
  (implicit
   hc: HeaderCarrier,
   ec: ExecutionContext,
   ac: AuthContext
  ): Future[Option[CacheMap]] =
    cacheConnector.fetchAll

  def refreshCache = {
    desConnector.status(amlsRefNumber) map {
      response => response.formBundleStatus match {
        case "Pending" => SubmissionReadyForReview
        case "Approved" => SubmissionDecisionApproved
        case "Rejected" => SubmissionDecisionRejected
      }

  }

  def reviewDetails
  (implicit
   hc: HeaderCarrier,
   ec: ExecutionContext
  ): Future[Option[ReviewDetails]] =
    keyStore.optionalReviewDetails

  /* TODO: Consider if there's a good way to stop
   * this from just overwriting whatever is in Business Matching,
   * shouldn't be a problem as this should only happen when someone
   * first comes into the Application from Business Customer FE
   */
  def updateReviewDetails
  (reviewDetails: ReviewDetails)
  (implicit
   hc: HeaderCarrier,
   ec: ExecutionContext,
   ac: AuthContext
  ): Future[CacheMap] = {
    val bm = BusinessMatching(reviewDetails = Some(reviewDetails))
    val atb = AboutTheBusiness(registeredOffice = Some(reviewDetails.businessAddress))
    cacheConnector.save[BusinessMatching](BusinessMatching.key, bm) flatMap {
      _ => cacheConnector.save[AboutTheBusiness](AboutTheBusiness.key, atb)
    }
  }
}

object LandingService extends LandingService {
  override private[services] def cacheConnector = DataCacheConnector
  override private[services] def keyStore = KeystoreConnector
}
