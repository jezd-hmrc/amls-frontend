package controllers.supervision

import config.AMLSAuthConnector
import controllers.BaseController
import views.html.supervision.what_you_need

import scala.concurrent.Future


trait WhatYouNeedController extends BaseController {

  def get() =
    ResponsiblePeopleToggle {
      Authorised.async {
        implicit authContext => implicit request =>
          Future.successful(Ok(what_you_need()))
      }
    }
}

object WhatYouNeedController extends WhatYouNeedController {
  // $COVERAGE-OFF$
  override val authConnector = AMLSAuthConnector
}
