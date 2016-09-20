package controllers.bankdetails

import config.AMLSAuthConnector
import connectors.DataCacheConnector
import controllers.BaseController
import models.bankdetails.BankDetails
import models.estateagentbusiness.EstateAgentBusiness

import scala.concurrent.Future

trait SummaryController extends BaseController {

  protected def dataCache: DataCacheConnector

  def get(complete: Boolean = false) = Authorised.async {
    implicit authContext => implicit request =>
      dataCache.fetch[Seq[BankDetails]](BankDetails.key) map {
        case Some(data) => Ok(views.html.bankdetails.summary(data, complete, hasBankAccount(data)))
        case _ => Redirect(controllers.routes.RegistrationProgressController.get())
      }
  }

  private def hasBankAccount(bankDetails: Seq[BankDetails]): Boolean = {
    bankDetails.exists(_.bankAccount.isDefined)
  }
}

object SummaryController extends SummaryController {
  // $COVERAGE-OFF$
  override val dataCache = DataCacheConnector
  override val authConnector = AMLSAuthConnector
}
