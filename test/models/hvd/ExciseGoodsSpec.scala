package models.hvd

import org.scalatestplus.play.PlaySpec
import jto.validation.{Invalid, Path, Valid}
import jto.validation.ValidationError
import play.api.libs.json.{JsPath, JsSuccess}

class ExciseGoodsSpec extends PlaySpec {

  "ExciseGoods" should {

    "Form Validation:" must {

      "successfully validate given an valid 'yes' option" in {
        val map = Map {
          "exciseGoods" -> Seq("true")
        }

        ExciseGoods.formRule.validate(map) must be(Valid(ExciseGoods(true)))
      }

      "successfully validate given an valid 'no' option" in {
        val map = Map {
          "exciseGoods" -> Seq("false")
        }

        ExciseGoods.formRule.validate(map) must be(Valid(ExciseGoods(false)))
      }

      "fail when neither option has been selected" in {

        ExciseGoods.formRule.validate(Map.empty) must be(Invalid(
          Seq(Path \ "exciseGoods" -> Seq(ValidationError("error.required.hvd.excise.goods")))))

      }

      "successfully write form data" in {

        ExciseGoods.formWrites.writes(ExciseGoods(true)) must be(Map("exciseGoods" -> Seq("true")))

      }
    }

    "Json Validation" must {

      "successfully read and write json data" in {

        ExciseGoods.format.reads(ExciseGoods.format.writes(ExciseGoods(true))) must be(JsSuccess(ExciseGoods(true),
          JsPath \ "exciseGoods"))

      }
    }
  }
}
