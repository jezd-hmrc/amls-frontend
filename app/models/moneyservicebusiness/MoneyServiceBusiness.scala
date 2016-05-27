package models.moneyservicebusiness

import models.registrationprogress.{Completed, NotStarted, Section, Started}
import typeclasses.MongoKey
import uk.gov.hmrc.http.cache.client.CacheMap
import play.api.libs.json._
import utils.JsonMapping

case class MoneyServiceBusiness(msbServices : Option[MsbServices] = None) {

  def msbServices(services: MsbServices): MoneyServiceBusiness =
    this.copy(msbServices = Some(services))

  def isComplete = msbServices.nonEmpty
}

object MoneyServiceBusiness extends JsonMapping {

  implicit def default(value : Option[MoneyServiceBusiness]) :  MoneyServiceBusiness = {
    value.getOrElse(MoneyServiceBusiness())
  }

  val key = "money-service-business"

  implicit val mongoKey = new MongoKey[MoneyServiceBusiness] {
    def apply() = "money-service-business"
  }

  def section(implicit cache: CacheMap): Section = {
    val messageKey = "msb"
    val notStarted = Section(messageKey, NotStarted, controllers.msb.routes.WhatYouNeedController.get())
//    cache.getEntry[MoneyServiceBusiness](key).fold(notStarted) {
//      model =>
//        if (model.isComplete) {
//          Section(messageKey, Completed, controllers.msb.routes.WhatYouNeedController.get())
//        } else {
//          Section(messageKey, Started, controllers.msb.routes.WhatYouNeedController.get())
//        }
//    }
    notStarted
  }

//  implicit val jsonReads : Reads[MoneyServiceBusiness] = Reads[MoneyServiceBusiness] { jsVal =>
//    Json.fromJson[MsbServices](jsVal).map(x => MoneyServiceBusiness(Some(x)))
//  }
//
//  implicit val jsonWrites : Writes[MoneyServiceBusiness] = Writes { msb:MoneyServiceBusiness =>
//    Json.toJson(msb.msbServices)
//  }
}
