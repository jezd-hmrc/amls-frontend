package models.tradingpremises

import org.scalatestplus.play.PlaySpec
import play.api.data.mapping.{Failure, Path, Success}
import play.api.data.validation.ValidationError

class AgentNameSpec extends PlaySpec {

  "AgentName" must {

    "validate form Read" in {
      val formInput = Map("agentName" -> Seq("sometext"))
      AgentName.formReads.validate(formInput) must be(Success(AgentName("sometext")))
    }

    "throw error when required field is missing" in {
      val formInput = Map("agentName" -> Seq(""))
      AgentName.formReads.validate(formInput) must be(Failure(Seq((Path \ "agentName", Seq(ValidationError("error.required.tp.agent.name"))))))
    }

    "throw error when input exceeds max length" in {
      val formInput = Map("agentName" -> Seq("sometesttexttest"*11))
      AgentName.formReads.validate(formInput) must be(Failure(Seq((Path \ "agentName") -> Seq(ValidationError("error.invalid.tp.agent.name")))))
    }

    "validate form write" in {
      AgentName.formWrites.writes(AgentName("sometext")) must be(Map("agentName" -> Seq("sometext")))
    }


  }
}