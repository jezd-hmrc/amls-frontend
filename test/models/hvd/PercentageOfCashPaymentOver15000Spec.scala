package models.hvd

import org.scalatestplus.play.PlaySpec
import play.api.data.mapping.{Failure, Path, Success}
import play.api.data.validation.ValidationError
import play.api.libs.json.{JsError, JsPath, JsSuccess, Json}

class PercentageOfCashPaymentOver15000Spec extends PlaySpec {

  "PercentageOfCashPaymentOver15000" should {
    "Form Validation" must {

      "successfully validate given an enum value" in {

        PercentageOfCashPaymentOver15000.formRule.validate(Map("percentage" -> Seq("01"))) must
          be(Success(PercentageOfCashPaymentOver15000.First))

        PercentageOfCashPaymentOver15000.formRule.validate(Map("percentage" -> Seq("02"))) must
          be(Success(PercentageOfCashPaymentOver15000.Second))

        PercentageOfCashPaymentOver15000.formRule.validate(Map("percentage" -> Seq("03"))) must
          be(Success(PercentageOfCashPaymentOver15000.Third))

        PercentageOfCashPaymentOver15000.formRule.validate(Map("percentage" -> Seq("04"))) must
          be(Success(PercentageOfCashPaymentOver15000.Fourth))

        PercentageOfCashPaymentOver15000.formRule.validate(Map("percentage" -> Seq("05"))) must
          be(Success(PercentageOfCashPaymentOver15000.Fifth))

      }

      "write correct data from enum value" in {

        PercentageOfCashPaymentOver15000.formWrites.writes(PercentageOfCashPaymentOver15000.First) must
          be(Map("percentage" -> Seq("01")))

        PercentageOfCashPaymentOver15000.formWrites.writes(PercentageOfCashPaymentOver15000.Second) must
          be(Map("percentage" -> Seq("02")))

        PercentageOfCashPaymentOver15000.formWrites.writes(PercentageOfCashPaymentOver15000.Third) must
          be(Map("percentage" -> Seq("03")))

        PercentageOfCashPaymentOver15000.formWrites.writes(PercentageOfCashPaymentOver15000.Fourth) must
          be(Map("percentage" -> Seq("04")))

        PercentageOfCashPaymentOver15000.formWrites.writes(PercentageOfCashPaymentOver15000.Fifth) must
          be(Map("percentage" -> Seq("05")))

      }


      "throw error on invalid data" in {
        PercentageOfCashPaymentOver15000.formRule.validate(Map("percentage" -> Seq("20"))) must
          be(Failure(Seq((Path \ "percentage", Seq(ValidationError("error.invalid"))))))
      }

      "throw error on empty data" in {
        PercentageOfCashPaymentOver15000.formRule.validate(Map.empty) must
          be(Failure(Seq((Path \ "percentage", Seq(ValidationError("error.required.hvd.percentage"))))))
      }
    }

    "JSON validation" must {

      "successfully validate given an enum value" in {

        Json.fromJson[PercentageOfCashPaymentOver15000](Json.obj("percentage" -> "01")) must
          be(JsSuccess(PercentageOfCashPaymentOver15000.First, JsPath \ "percentage"))

        Json.fromJson[PercentageOfCashPaymentOver15000](Json.obj("percentage" -> "02")) must
          be(JsSuccess(PercentageOfCashPaymentOver15000.Second, JsPath \ "percentage"))

        Json.fromJson[PercentageOfCashPaymentOver15000](Json.obj("percentage" -> "03")) must
          be(JsSuccess(PercentageOfCashPaymentOver15000.Third, JsPath \ "percentage"))

        Json.fromJson[PercentageOfCashPaymentOver15000](Json.obj("percentage" -> "04")) must
          be(JsSuccess(PercentageOfCashPaymentOver15000.Fourth, JsPath \ "percentage"))

        Json.fromJson[PercentageOfCashPaymentOver15000](Json.obj("percentage" -> "05")) must
          be(JsSuccess(PercentageOfCashPaymentOver15000.Fifth, JsPath \ "percentage"))

      }

      "write the correct value" in {

        Json.toJson(PercentageOfCashPaymentOver15000.First) must
          be(Json.obj("percentage" -> "01"))

        Json.toJson(PercentageOfCashPaymentOver15000.Second) must
          be(Json.obj("percentage" -> "02"))

        Json.toJson(PercentageOfCashPaymentOver15000.Third) must
          be(Json.obj("percentage" -> "03"))

        Json.toJson(PercentageOfCashPaymentOver15000.Fourth) must
          be(Json.obj("percentage" -> "04"))

        Json.toJson(PercentageOfCashPaymentOver15000.Fifth) must
          be(Json.obj("percentage" -> "05"))

      }

      "throw error for invalid data" in {
        Json.fromJson[PercentageOfCashPaymentOver15000](Json.obj("percentage" -> "20")) must
          be(JsError(JsPath \ "percentage", ValidationError("error.invalid")))
      }
    }
   
  }
}