package models.tradingpremises

import models.FormTypes._
import play.api.data.mapping._
import play.api.data.mapping.forms.Rules._
import play.api.libs.json._
import play.api.data.mapping._
import play.api.data.mapping.forms.UrlFormEncoded
import play.api.libs.json._
import typeclasses.MongoKey
import utils.{JsonMapping, TraversableValidators}

case class AgentName(agentName: String)

object AgentName {

  import utils.MappingUtils.Implicits._

  val maxAgentNameLength = 140

  val agentNameType =  notEmptyStrip compose notEmpty.withMessage("error.required.tp.agent.name") compose
    maxLength(maxAgentNameLength).withMessage("error.invalid.tp.agent.name")

  implicit val mongoKey = new MongoKey[AgentName] {
    override def apply(): String = "agent-name"
  }
  implicit val formats = Json.format[AgentName]

  implicit val formReads: Rule[UrlFormEncoded, AgentName] = From[UrlFormEncoded] { __ =>
    import play.api.data.mapping.forms.Rules._
    (__ \ "agentName").read(agentNameType) fmap AgentName.apply
  }

  implicit val formWrites: Write[AgentName, UrlFormEncoded] = Write {
    case AgentName(crn) => Map("agentName" -> Seq(crn))
  }

}