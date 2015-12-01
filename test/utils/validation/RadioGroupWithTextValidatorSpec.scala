package utils.validation

import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.data.FormError
import utils.validation.RadioGroupWithTextValidator._

class RadioGroupWithTextValidatorSpec extends PlaySpec with MockitoSugar  with OneServerPerSuite {

  "RadioGroupWithTextValidator" must {
    "when mandatory fields filled correctly respond with success[true, false]" in {
      val mapping = mandatoryBooleanWithText("mlrNumber", "prevMlrNumber", "No radio button selected", "blank value", "value not allowed")
      mapping.bind(Map("" -> "01", "mlrNumber" -> "12345078")) mustBe Right(Tuple2(true, false))
    }

    "when mandatory fields filled correctly respond with success[true, false] with empty text" in {
      val mapping = mandatoryBooleanWithText("mlrNumber", "prevMlrNumber", "No radio button selected", "blank value", "value not allowed")
      mapping.bind(Map("" -> "01", "mlrNumber" -> ""))
        .left.getOrElse(Nil).contains(FormError("mlrNumber", "blank value")) mustBe true
    }

    "when mandatory fields filled correctly respond with success[false, true]" in {
      val mapping = mandatoryBooleanWithText("mlrNumber", "prevMlrNumber", "No radio button selected", "blank value", "value not allowed")
      mapping.bind(Map("" -> "02", "mlrNumber" -> "")) mustBe Right(Tuple2(false, true))
    }

    "when mandatory fields filled correctly respond with success[false, false]" in {
      val mapping = mandatoryBooleanWithText("mlrNumber", "prevMlrNumber", "No radio button selected", "blank value", "value not allowed")
      mapping.bind(Map("" -> "03" )) mustBe Right(Tuple2(false, false))
    }

    "when mandatory fields not filled correctly respond with failure - unexpected values in input fields" in {
      val mapping = mandatoryBooleanWithText("mlrNumber", "prevMlrNumber", "No radio button selected", "blank value", "value not allowed")
      val afterBind = mapping.bind(Map("" -> "03", "mlrNumber"->"12345678", "prevMlrNumber"->"12436143651436152346"))

      afterBind.left.getOrElse(Nil).contains(FormError("mlrNumber", "value not allowed")) mustBe true
      afterBind.left.getOrElse(Nil).contains(FormError("prevMlrNumber", "value not allowed")) mustBe true
    }


    "respond with failure for unexpected values  " in {
      val mapping = mandatoryBooleanWithText("mlrNumber", "prevMlrNumber", "No radio button selected", "blank value", "value not allowed")
      mapping.bind(Map("" -> "03", "mlrNumber"->"", "prevMlrNumber"->"")) mustBe Right(Tuple2(false, false))


    }

    "when mandatory fields not filled correctly respond with failure - no mlrnumber" in {
      val mapping = mandatoryBooleanWithText("mlrNumber", "prevMlrNumber", "No radio button selected", "blank value", "value not allowed")
      mapping.bind(Map("" -> "03", "prevMlrNumber"->"1243614365143"))
        .left.getOrElse(Nil).contains(FormError("prevMlrNumber", "value not allowed")) mustBe true
    }

    "when mandatory fields not filled correctly respond with failure - no prevMlrNumber" in {
      val mapping = mandatoryBooleanWithText("mlrNumber", "prevMlrNumber", "No radio button selected", "blank value", "value not allowed")
      mapping.bind(Map("" -> "03", "mlrNumber"->"12345678"))
        .left.getOrElse(Nil).contains(FormError("mlrNumber", "value not allowed")) mustBe true
    }

    "when mandatory fields not filled correctly respond with failure - invalid option selected" in {
      val mapping = mandatoryBooleanWithText("mlrNumber", "prevMlrNumber", "No radio button selected", "blank value", "value not allowed")
      mapping.bind(Map("" -> "04"))
        .left.getOrElse(Nil).contains(FormError("", "No radio button selected")) mustBe true
    }

    "respond with failure when form is empty" in {
      val mapping = mandatoryBooleanWithText("mlrNumber", "prevMlrNumber", "No radio button selected", "blank value", "value not allowed")
      mapping.bind(Map())
        .left.getOrElse(Nil).contains(FormError("", "No radio button selected")) mustBe true
    }

      "respond to unbind" in {
        val mapping = mandatoryBooleanWithText("mlrNumber", "prevMlrNumber", "No radio button selected", "blank value", "value not allowed")
          mapping.unbind( (true, false) ) mustBe Map("" -> "01")
          mapping.unbind( (false, true) ) mustBe Map("" -> "02")
          mapping.unbind( (false, false) ) mustBe Map("" -> "03")
      }

    "respond with failure to unbind" in {
      val mapping = mandatoryBooleanWithText("mlrNumber", "prevMlrNumber", "No radio button selected", "blank value", "value not allowed")
      an[RuntimeException] must be thrownBy{
        mapping.unbind((true, true))
      }
    }

  }
}
