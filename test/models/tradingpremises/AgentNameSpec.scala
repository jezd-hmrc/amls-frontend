package models.tradingpremises

import models.DateOfChange
import org.joda.time.LocalDate
import org.scalatestplus.play.PlaySpec
import jto.validation.{Invalid, Path, Valid}
import jto.validation.ValidationError
import play.api.libs.json.{JsPath, JsSuccess}

class AgentNameSpec extends PlaySpec {

  "AgentName" must {

    "validate form Read" in {
      val formInput = Map(
        "agentName" -> Seq("sometext")
      )

      AgentName.formReads.validate(formInput) must be(Valid(AgentName("sometext", None)))
    }

    "validate form Read with agent dob" in {

      val formInput = Map(
        "agentName" -> Seq("sometext"),
        "agentDateOfBirth.day" -> Seq("15"),
        "agentDateOfBirth.month" -> Seq("2"),
        "agentDateOfBirth.year" -> Seq("1956")
      )

      AgentName.formReads.validate(formInput) must be(Valid(AgentName("sometext", None, Some(new LocalDate("1956-02-15")))))

    }

    "throw error when required field is missing" in {
      val formInput = Map("agentName" -> Seq(""))
      AgentName.formReads.validate(formInput) must be(Invalid(Seq((Path \ "agentName", Seq(ValidationError("error.required.tp.agent.name"))))))
    }

    "throw error when input exceeds max length" in {
      val formInput = Map("agentName" -> Seq("sometesttexttest"*11))
      AgentName.formReads.validate(formInput) must be(Invalid(Seq((Path \ "agentName") -> Seq(ValidationError("error.invalid.tp.agent.name")))))
    }

    "validate form write" in {
      AgentName.formWrites.writes(AgentName("sometext")) must be(Map("agentName" -> Seq("sometext")))
    }

  }

  "Json Validation" must {
    "Successfully read/write Json data" in {
      AgentName.format.reads(AgentName.format.writes(
        AgentName("test", Some(DateOfChange(new LocalDate(2017,1,1)))))) must be(
        JsSuccess(
          AgentName("test", Some(DateOfChange(new LocalDate(2017,1,1))))))
    }

    "Succesfully read/write Json data with agent dob" in {

      AgentName.format.reads(AgentName.format.writes(
        AgentName("test", Some(DateOfChange(new LocalDate(2017,1,1))), Some(new LocalDate(2015,10,10))))) must be(
        JsSuccess(
          AgentName("test", Some(DateOfChange(new LocalDate(2017,1,1))),Some(new LocalDate(2015,10,10)))))

    }

  }
}
