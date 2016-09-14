package services

import config.ApplicationConfig
import connectors.{DESConnector, DataCacheConnector, GovernmentGatewayConnector}
import models.asp.Asp
import models.hvd.Hvd
import models.moneyservicebusiness.MoneyServiceBusiness
import models.responsiblepeople.ResponsiblePeople
import models.supervision.Supervision
import models.tcsp.Tcsp
import models.{SubscriptionRequest, SubscriptionResponse}
import models.aboutthebusiness.AboutTheBusiness
import models.bankdetails.BankDetails
import models.businessactivities.BusinessActivities
import models.businessmatching.{BusinessMatching, BusinessType}
import models.confirmation.{BreakdownRow, Currency}
import models.declaration.AddPerson
import models.estateagentbusiness.EstateAgentBusiness
import models.tradingpremises.TradingPremises
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse, NotFoundException}

import scala.concurrent.{ExecutionContext, Future}

trait SubscriptionService extends DataCacheService {

  private[services] def cacheConnector: DataCacheConnector
  private[services] def desConnector: DESConnector
  private[services] def ggService: GovernmentGatewayService

  private object Submission {
    val message = "confirmation.submission"
    val quantity = 1
    val feePer = ApplicationConfig.regFee
  }

  private object Premises {
    val message = "confirmation.tradingpremises"
    val feePer = ApplicationConfig.premisesFee
  }

  private object People {
    val message = "confirmation.responsiblepeople"
    val feePer = ApplicationConfig.peopleFee
  }

  private object UnpaidPeople {
    val message = "confirmation.unpaidpeople"
    val feePer = 0
  }

  private def safeId(cache: CacheMap): Future[String] = {
    (for {
      bm <- cache.getEntry[BusinessMatching](BusinessMatching.key)
      rd <- bm.reviewDetails
    } yield rd.safeId) match {
      case Some(a) =>
        Future.successful(a)
      case _ =>
        // TODO: Better exception
        Future.failed(new Exception(""))
    }
  }

  private def businessType(cache: CacheMap): Option[BusinessType] =
    for {
      bm <- cache.getEntry[BusinessMatching](BusinessMatching.key)
      rd <- bm.reviewDetails
      bt <- rd.businessType
    } yield bt

  private def subscribe
  (cache: CacheMap, safeId: String)
  (implicit
   ac: AuthContext,
   hc: HeaderCarrier,
   ec: ExecutionContext
  ): Future[SubscriptionResponse] = {
    val request = SubscriptionRequest(
      businessMatchingSection = cache.getEntry[BusinessMatching](BusinessMatching.key),
      eabSection = cache.getEntry[EstateAgentBusiness](EstateAgentBusiness.key),
      tradingPremisesSection = cache.getEntry[Seq[TradingPremises]](TradingPremises.key),
      aboutTheBusinessSection = cache.getEntry[AboutTheBusiness](AboutTheBusiness.key),
      bankDetailsSection = cache.getEntry[Seq[BankDetails]](BankDetails.key),
      aboutYouSection = cache.getEntry[AddPerson](AddPerson.key),
      businessActivitiesSection = cache.getEntry[BusinessActivities](BusinessActivities.key),
      responsiblePeopleSection = cache.getEntry[Seq[ResponsiblePeople]](ResponsiblePeople.key),
      tcspSection =  cache.getEntry[Tcsp](Tcsp.key),
      aspSection = cache.getEntry[Asp](Asp.key),
      msbSection = cache.getEntry[MoneyServiceBusiness](MoneyServiceBusiness.key),
      hvdSection = cache.getEntry[Hvd](Hvd.key),
      supervisionSection = cache.getEntry[Supervision](Supervision.key)
    )

    desConnector.subscribe(request, safeId)
  }

  def subscribe
  (implicit
   ec: ExecutionContext,
   hc: HeaderCarrier,
   ac: AuthContext
  ): Future[SubscriptionResponse] = {
    for {
      cache <- getCache
      safeId <- safeId(cache)
      subscription <- subscribe(cache, safeId)
      _ <- cacheConnector.save[SubscriptionResponse](SubscriptionResponse.key, subscription)
      _ <- ggService.enrol(
        safeId = safeId,
        mlrRefNo = subscription.amlsRefNo
      )
    } yield subscription
  }

  private def subscriptionQuantity(subscription: SubscriptionResponse): Int =
    if (subscription.registrationFee == 0) 0 else 1

  private def responsiblePeopleRows(people: Seq[ResponsiblePeople], subscription: SubscriptionResponse): Seq[BreakdownRow] = {
    people.partition(_.hasAlreadyPassedFitAndProper.getOrElse(false)) match {
      case (b, a) =>
        Seq(BreakdownRow(People.message, a.size, People.feePer, Currency.fromBD(subscription.fpFee.getOrElse(0)))) ++
          (if (b.nonEmpty) {
            Seq(BreakdownRow(UnpaidPeople.message, b.size, UnpaidPeople.feePer, Currency.fromBD(UnpaidPeople.feePer)))
          } else {
            Seq.empty
          })
    }
  }

  def getSubscription
  (implicit
   ec: ExecutionContext,
   hc: HeaderCarrier,
   ac: AuthContext
  ): Future[(String, Currency, Seq[BreakdownRow])] =
    cacheConnector.fetchAll flatMap {
      option =>
        getSubscriptionData(option)
    }

  def getSubscriptionData(cacheMap:Option[CacheMap]): Future[(String, Currency, Seq[BreakdownRow])] = {
    (for {
      cache <- cacheMap
      subscription <- cache.getEntry[SubscriptionResponse](SubscriptionResponse.key)
      premises <- cache.getEntry[Seq[TradingPremises]](TradingPremises.key)
      people <- cache.getEntry[Seq[ResponsiblePeople]](ResponsiblePeople.key)
    } yield {
      val subQuantity = subscriptionQuantity(subscription)
      val mlrRegNo = subscription.amlsRefNo
      val total = subscription.totalFees
      val rows = Seq(
        BreakdownRow(Submission.message, subQuantity, Submission.feePer, subQuantity * Submission.feePer)
      ) ++ responsiblePeopleRows(people, subscription) ++
        Seq(BreakdownRow(Premises.message, premises.size, Premises.feePer, subscription.premiseFee))
      Future.successful((mlrRegNo, Currency.fromBD(total), rows))
      // TODO
    }) getOrElse Future.failed(new Exception("TODO"))
  }
}

object SubscriptionService extends SubscriptionService {

  object MockGGService extends GovernmentGatewayService {

    import play.api.http.Status.OK

    override private[services] def ggConnector: GovernmentGatewayConnector = GovernmentGatewayConnector

    override def enrol
    (mlrRefNo: String, safeId: String)
    (implicit
     hc: HeaderCarrier,
     ec: ExecutionContext
    ): Future[HttpResponse] = Future.successful(HttpResponse(OK))
  }

  override private[services] val cacheConnector = DataCacheConnector
  override private[services] val desConnector = DESConnector
  override private[services] val ggService = {
    if (ApplicationConfig.enrolmentToggle) {
      GovernmentGatewayService
    } else {
      MockGGService
    }
  }
}
