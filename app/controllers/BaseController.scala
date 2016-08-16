package controllers

import config.ApplicationConfig
import controllers.auth.AmlsRegime
import play.api.i18n.Messages
import play.api.mvc.Request
import uk.gov.hmrc.play.frontend.auth.Actions
import uk.gov.hmrc.play.frontend.controller.FrontendController
import utils.{FeatureToggle, JsonMapping}

trait BaseController extends FrontendController with Actions {

  protected val Authorised = AuthorisedFor(AmlsRegime, pageVisibility = GGConfidence)
  protected val ResponsiblePeopleToggle = FeatureToggle(ApplicationConfig.responsiblePeopleToggle)
  protected val StatusToggle = FeatureToggle(ApplicationConfig.statusToggle)
  protected val HvdToggle = FeatureToggle(ApplicationConfig.hvdToggle)

  def notFoundView(implicit request: Request[_]) = {
    views.html.error(Messages("error.not-found.title"),
      Messages("error.not-found.heading"),
      Messages("error.not-found.message"))
  }

}
