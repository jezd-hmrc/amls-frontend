package models.moneyservicebusiness

import models.registrationprogress.{Completed, NotStarted, Section}
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json
import typeclasses.MongoKey
import uk.gov.hmrc.http.cache.client.CacheMap
import org.mockito.Mockito._
import play.api.mvc.Call
//
//class MoneyServiceBusinessSpec extends PlaySpec with MockitoSugar with MoneyServiceBusinessTestData{
//  "MoneyServiceBusiness" should {
//    "have an implicit conversion from Option which" when {
//      "called with None" should {
//        "return a default version of MoneyServiceBusiness" in {
//          val res:MoneyServiceBusiness = None
//          res must be (emptyModel)
//        }
//      }
//
//      "called with a concrete value" should {
//        "return the value passed in extracted from the option" in {
//          val res:MoneyServiceBusiness = Some(completeModel)
//          res must be (completeModel)
//        }
//      }
//    }
//
//    "Provide an implicit mongo-key" in {
//      def x(implicit mongoKey: MongoKey[MoneyServiceBusiness]) = mongoKey()
//
//      x must be("money-service-business")
//    }
//
//    "have a section function that" when {
//      implicit val cacheMap = mock[CacheMap]
//
//      "model is empty" should {
//        "return a NotStarted Section" in {
//          when(cacheMap.getEntry[MoneyServiceBusiness]("money-service-business")) thenReturn None
//          MoneyServiceBusiness.section must be (Section("money_service_business", NotStarted, controllers.msb.routes.WhatYouNeedController.get()))
//        }
//      }
//
//      "model is complete" should {
//        "return a Completed Section" in {
//          when(cacheMap.getEntry[MoneyServiceBusiness]("money-service-business")) thenReturn Some(completeModel)
//          MoneyServiceBusiness.section must be (Section("money_service_business", Completed, controllers.msb.routes.WhatYouNeedController.get()))
//        }
//      }
//    }
//
//    "have an isComplete function that" must {
//
//      "correctly show if the model is complete" in  {
//        completeModel.isComplete must be (true)
//      }
//
//      "correctly show if the model is incomplete" in {
//        emptyModel.isComplete must be (false)
//      }
//    }
//
//    "Serialise to expected Json" when {
//      "model is complete" in {Json.toJson(completeModel) must be (completeJson)}
//    }
//
//    "Deserialise from Json as expected" when {
//      "model is complete" in {completeJson.as[MoneyServiceBusiness] must be (completeModel)}
//    }
//  }
//}
//
//trait MoneyServiceBusinessTestData {
//  val completeModel = MoneyServiceBusiness(Some(MsbServices(Set(ChequeCashingScrapMetal, ChequeCashingNotScrapMetal))))
//  val emptyModel = MoneyServiceBusiness(None)
//
//  val completeJson = Json.obj(
//    "msbServices" -> Json.arr("04", "03")
//  )
//
//  val emptyJson = Json.obj("msbServices" -> Json.arr())
//}
