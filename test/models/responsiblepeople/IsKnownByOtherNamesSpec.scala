package models.responsiblepeople

import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import jto.validation.{Invalid, Path, Valid}
import jto.validation.ValidationError
import play.api.libs.json.{JsPath, JsSuccess, Json}

class IsKnownByOtherNamesSpec extends PlaySpec with MockitoSugar {

  "Validate definitions" must {

    "successfully validate the name" in {
      IsKnownByOtherNames.otherFirstNameType.validate("John") must be(Valid("John"))
    }

    "pass validation if the middle name is not provided" in {
      IsKnownByOtherNames.otherMiddleNameType.validate("") must be(Valid(""))
    }

    "fail validation if the name is more than 35 characters" in {
      IsKnownByOtherNames.otherLastNameType.validate("JohnJohnJohnJohnJohnJohnJohnJohnJohnJohn") must
        be(Invalid(Seq(Path -> Seq(ValidationError("error.invalid.length.lastname")))))
    }

  }

  "Form Rules and Writes" must {

    "successfully validate given all fields" in {
      val urlFormEncoded = Map(
        "isKnownByOtherNames" -> Seq("true"),
        "otherfirstnames" -> Seq("John"),
        "othermiddlenames" -> Seq("Envy"),
        "otherlastnames" -> Seq("Doe")
      )
      IsKnownByOtherNames.formRule.validate(urlFormEncoded) must be(Valid(IsKnownByOtherNamesYes("John", Some("Envy"), "Doe")))
    }

    "successfully validate given the middle name is optional" in {
      val urlFormEncoded = Map(
        "isKnownByOtherNames" -> Seq("true"),
        "otherfirstnames" -> Seq("John"),
        "otherlastnames" -> Seq("Doe")
      )
      IsKnownByOtherNames.formRule.validate(urlFormEncoded) must be(Valid(IsKnownByOtherNamesYes("John", None, "Doe")))
    }


    "fail validation when no option is selected for isKnownByOtherNames" in {

      IsKnownByOtherNames.formRule.validate(Map.empty) must
        be(Invalid(Seq(
          (Path \ "isKnownByOtherNames") -> Seq(ValidationError("error.required.rp.isknownbyothernames"))
        )))
    }

    "fail validation when isKnownByOtherNames is selcted but name not provided" in {

      val urlFormEncoded = Map(
        "isKnownByOtherNames" -> Seq("true")
      )

      IsKnownByOtherNames.formRule.validate(urlFormEncoded) must
        be(Invalid(Seq(
          (Path \ "otherfirstnames") -> Seq(ValidationError("error.required")),
          (Path \ "otherlastnames") -> Seq(ValidationError("error.required"))
        )))
    }

    "fail to validate when first name is missing" in {

      val urlFormEncoded = Map(
        "isKnownByOtherNames" -> Seq("true"),
        "otherlastnames" -> Seq("Doe")
      )

      IsKnownByOtherNames.formRule.validate(urlFormEncoded) must
        be(Invalid(Seq(
          (Path \ "otherfirstnames") -> Seq(ValidationError("error.required"))
        )))
    }

    "fail to validate when last name is missing" in {

      val urlFormEncoded = Map(
        "isKnownByOtherNames" -> Seq("true"),
        "otherfirstnames" -> Seq("John")
      )

      IsKnownByOtherNames.formRule.validate(urlFormEncoded) must
        be(Invalid(Seq(
          (Path \ "otherlastnames") -> Seq(ValidationError("error.required"))
        )))
    }


    "fail to validate when otherfirstnames or otherlastnames are more than the required length" in {

      val urlFormEncoded = Map(
        "isKnownByOtherNames" -> Seq("true"),
        "otherfirstnames" -> Seq("JohnJohnJohnJohnJohnJohnJohnJohnJohn"),
        "otherlastnames" -> Seq("DoeDoeDoeDoeDoeDoeDoeDoeDoeDoeDoeDoe")
      )

      IsKnownByOtherNames.formRule.validate(urlFormEncoded) must
        be(Invalid(Seq(
          (Path \ "otherfirstnames") -> Seq(ValidationError("error.invalid.length.firstname")),
          (Path \ "otherlastnames") -> Seq(ValidationError("error.invalid.length.lastname"))
        )))
    }
  }

  "JSON Read/Write " must {

    "Read the json and return the InKnownByOtherNamesYes domain object successfully" in {

      val json = Json.obj(
        "isKnownByOtherNames" -> true,
        "otherfirstnames" -> "John",
        "othermiddlenames" -> "Envy",
        "otherlastnames" -> "Doe"
      )

      IsKnownByOtherNames.jsonReads.reads(json) must
        be(JsSuccess(IsKnownByOtherNamesYes("John", Some("Envy"), "Doe"), JsPath ))
    }

    "Write the json successfully from the InKnownByOtherNamesYes domain object created" in {

      val isKnownByOtherNamesYes = IsKnownByOtherNamesYes("John", Some("Envy"), "Doe")

      val json = Json.obj(
        "isKnownByOtherNames" -> true,
        "otherfirstnames" -> "John",
        "othermiddlenames" -> "Envy",
        "otherlastnames" -> "Doe"
      )

      IsKnownByOtherNames.jsonWrites.writes(isKnownByOtherNamesYes) must be(json)
    }
  }

}
