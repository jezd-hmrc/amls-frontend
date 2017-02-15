package models.aboutthebusiness

import jto.validation._
import jto.validation.forms.UrlFormEncoded
import jto.validation.ValidationError
import play.api.libs.json._
import cats.data.Validated.{Invalid, Valid}

sealed trait PreviouslyRegistered

case class PreviouslyRegisteredYes(value: String) extends PreviouslyRegistered

case object PreviouslyRegisteredNo extends PreviouslyRegistered

object PreviouslyRegistered {

  import utils.MappingUtils.Implicits._

  implicit val formRule: Rule[UrlFormEncoded, PreviouslyRegistered] = From[UrlFormEncoded] { __ =>
    import jto.validation.forms.Rules._

    val mlrRegNoRegex = "^([0-9]{8})$".r

    (__ \ "previouslyRegistered").read[Boolean].withMessage("error.required.atb.previously.registered") flatMap {
      case true =>
        (__ \ "prevMLRRegNo").read(pattern(mlrRegNoRegex).withMessage("error.invalid.atb.mlr.number")) map PreviouslyRegisteredYes.apply
      case false => Rule.fromMapping { _ => Valid(PreviouslyRegisteredNo) }
    }
  }

  implicit val formWrites: Write[PreviouslyRegistered, UrlFormEncoded] = Write {
    case PreviouslyRegisteredYes(value) =>
      Map("previouslyRegistered" -> Seq("true"),
        "prevMLRRegNo" -> Seq(value)
      )
    case PreviouslyRegisteredNo => Map("previouslyRegistered" -> Seq("false"))
  }

  implicit val jsonReads: Reads[PreviouslyRegistered] =
    (__ \ "previouslyRegistered").read[Boolean] flatMap {
      case true => (__ \ "prevMLRRegNo").read[String] map PreviouslyRegisteredYes.apply
      case false => Reads(_ => JsSuccess(PreviouslyRegisteredNo))
    }

  implicit val jsonWrites = Writes[PreviouslyRegistered] {
    case PreviouslyRegisteredYes(value) => Json.obj(
      "previouslyRegistered" -> true,
      "prevMLRRegNo" -> value
    )
    case PreviouslyRegisteredNo => Json.obj("previouslyRegistered" -> false)
  }
}
