package models.businessmatching

import models.businessmatching.BusinessType._
import play.api.data.mapping.forms.UrlFormEncoded
import play.api.data.mapping._

sealed trait BusinessType {
  override def toString: String =
    this match {
      case SoleProprietor => "SOP"
      case LimitedCompany => "LTD"
      case Partnership => "OBP"
      case LPrLLP => "LP"
      case UnincorporatedBody => "UIB"
    }
}

object BusinessType {

  import play.api.data.mapping.forms.Rules._

  val CORPORATE_BODY = "Corporate Body"
  val UNINCORPORATED_BODY = "Unincorporated Body"
  val LLP = "LLP"
  val PARTNERSHIP = "Partnership"

  case object SoleProprietor extends BusinessType
  case object LimitedCompany extends BusinessType
  case object Partnership extends BusinessType
  case object LPrLLP extends BusinessType
  case object UnincorporatedBody extends BusinessType

  implicit val formR: Rule[UrlFormEncoded, BusinessType] =
    From[UrlFormEncoded] { __ =>
      (__ \ "businessType").read[String] flatMap {
        case "01" => Rule(_ => Success(LimitedCompany))
        case "02" => Rule(_ => Success(SoleProprietor))
        case "03" => Rule(_ => Success(Partnership))
        case "04" => Rule(_ => Success(LPrLLP))
        case "05" => Rule(_ => Success(UnincorporatedBody))
        case _ =>
          Rule { _ =>
            Failure(Seq(Path \ "businessType" -> Seq(ValidationError("error.invalid"))))
          }
      }
    }

  implicit val formW: Write[BusinessType, UrlFormEncoded] =
    Write[BusinessType, UrlFormEncoded] {
      case LimitedCompany =>
        Map("businessType" -> Seq("01"))
      case SoleProprietor =>
        Map("businessType" -> Seq("02"))
      case Partnership =>
        Map("businessType" -> Seq("03"))
      case LPrLLP =>
        Map("businessType" -> Seq("04"))
      case UnincorporatedBody =>
        Map("businessType" -> Seq("05"))
    }
}
