package controllers.businessactivities

import config.AMLSAuthConnector
import connectors.DataCacheConnector
import controllers.BaseController
import forms.{ValidForm, InvalidForm, EmptyForm, Form2}
import models.bankdetails.{BankAccountType, BankDetails}
import models.businessactivities.{RiskAssessmentPolicy, BusinessActivities}
import utils.RepeatingSection
import utils.RepeatingSection
import scala.concurrent.Future

trait RiskAssessmentController extends RepeatingSection with BaseController {

  val dataCacheConnector: DataCacheConnector

  def get(edit : Boolean = false) = Authorised.async {
    implicit authContext => implicit request =>
      dataCacheConnector.fetchDataShortLivedCache[BusinessActivities](BusinessActivities.key) map {
        case Some(BusinessActivities(_, _, _, _, _, _, Some(data))) =>
          Ok(views.html.risk_assessment_policy(Form2[RiskAssessmentPolicy](data), edit))
        case _ =>
          Ok(views.html.risk_assessment_policy(EmptyForm, edit))
      }
  }


  def post(edit : Boolean = false) = Authorised.async {
    import play.api.data.mapping.forms.Rules._
    implicit authContext => implicit request =>
      Form2[RiskAssessmentPolicy](request.body) match {
        case f: InvalidForm =>
          Future.successful(BadRequest(views.html.risk_assessment_policy(f, edit)))
        case ValidForm(_, data) => {
          for {
            businessActivity <-
            dataCacheConnector.fetchDataShortLivedCache[BusinessActivities](BusinessActivities.key)
            _ <- dataCacheConnector.saveDataShortLivedCache[BusinessActivities](BusinessActivities.key,
              businessActivity.riskAssessmentspolicy(data)
            )
          } yield edit match {
            case true => Redirect(routes.WhatYouNeedController.get())
            case false => Redirect(routes.BusinessFranchiseController.get())
          }
        }
      }
  }
}

object RiskAssessmentController extends RiskAssessmentController {
  override val authConnector = AMLSAuthConnector
  override val dataCacheConnector = DataCacheConnector
}
