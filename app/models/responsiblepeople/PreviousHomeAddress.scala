package models.responsiblepeople

import models.Country
import play.api.data.mapping._
import play.api.libs.json._
import play.api.data.mapping.forms.UrlFormEncoded

sealed trait PreviousHomeAddress {

  val TimeAtAddress: TimeAtAddress = this match {
    case uk: PreviousHomeAddressUK => uk.timeAtAddress
    case non: PreviousHomeAddressNonUK => non.timeAtAddress
  }

}

case class PreviousHomeAddressUK(
                                 addressLine1: String,
                                 addressLine2: String,
                                 addressLine3: Option[String],
                                 addressLine4: Option[String],
                                 postCode: String,
                                 timeAtAddress: TimeAtAddress
                               ) extends PreviousHomeAddress

case class PreviousHomeAddressNonUK(
                                     addressLineNonUK1: String,
                                     addressLineNonUK2: String,
                                     addressLineNonUK3: Option[String],
                                     addressLineNonUK4: Option[String],
                                     country: Country,
                                     timeAtAddress: TimeAtAddress
                                   ) extends PreviousHomeAddress

object PreviousHomeAddress {

  implicit val formRule: Rule[UrlFormEncoded, PreviousHomeAddress] = From[UrlFormEncoded] { __ =>

    import play.api.data.mapping.forms.Rules._
    import models.FormTypes._
    import utils.MappingUtils.Implicits._

    (__ \ "isUK").read[Boolean] flatMap {
      case true =>
        (
          (__ \ "addressLine1").read(notEmpty.withMessage("error.required.address.line1") compose validateAddress) ~
            (__ \ "addressLine2").read(notEmpty.withMessage("error.required.address.line2") compose validateAddress) ~
            (__ \ "addressLine3").read(optionR(validateAddress)) ~
            (__ \ "addressLine4").read(optionR(validateAddress)) ~
            (__ \ "postCode").read(postcodeType) ~
            (__ \ "timeAtAddress").read[TimeAtAddress].withMessage("error.required.timeAtAddress")
          ) (PreviousHomeAddressUK.apply _)
      case false =>
        (
          (__ \ "addressLineNonUK1").read(notEmpty.withMessage("error.required.address.line1") compose validateAddress)  ~
            (__ \ "addressLineNonUK2").read(notEmpty.withMessage("error.required.address.line2") compose validateAddress)  ~
            (__ \ "addressLineNonUK3").read(optionR(validateAddress))  ~
            (__ \ "addressLineNonUK4").read(optionR(validateAddress))  ~
            (__ \ "country").read[Country]  ~
            (__ \ "timeAtAddress").read[TimeAtAddress].withMessage("error.required.timeAtAddress")
          ) (PreviousHomeAddressNonUK.apply _)
    }
  }

  implicit val formWrites: Write[PreviousHomeAddress, UrlFormEncoded] = Write {

    case a: PreviousHomeAddressUK =>
      Map(
        "isUK" -> Seq("true"),
        "addressLine1" -> Seq(a.addressLine1),
        "addressLine2" -> Seq(a.addressLine2),
        "addressLine3" -> a.addressLine3.toSeq,
        "addressLine4" -> a.addressLine4.toSeq,
        "postCode" -> Seq(a.postCode),
        "timeAtAddress" -> TimeAtAddress.timeAtAddressFormWrite.writes(a.timeAtAddress)
      )
    case a: PreviousHomeAddressNonUK =>
      Map(
        "isUK" -> Seq("false"),
        "addressLineNonUK1" -> Seq(a.addressLineNonUK1),
        "addressLineNonUK2" -> Seq(a.addressLineNonUK2),
        "addressLineNonUK3" -> a.addressLineNonUK3.toSeq,
        "addressLineNonUK4" -> a.addressLineNonUK4.toSeq,
        "country" -> Seq(a.country.code),
        "timeAtAddress" -> TimeAtAddress.timeAtAddressFormWrite.writes(a.timeAtAddress)
      )
  }

  implicit val jsonReads: Reads[PreviousHomeAddress] = {
    import play.api.libs.functional.syntax._
    import play.api.libs.json.Reads._
    import play.api.libs.json._
    (__ \ "previousAddressPostCode").read[String] andKeep (
      ((__ \ "previousAddressLine1").read[String] and
        (__ \ "previousAddressLine2").read[String] and
        (__ \ "previousAddressLine3").readNullable[String] and
        (__ \ "previousAddressLine4").readNullable[String] and
        (__ \ "previousAddressPostCode").read[String] and
        (__ \ "previousTimeAtAddress").read[TimeAtAddress]) (PreviousHomeAddressUK.apply _) map identity[PreviousHomeAddress]
      ) orElse
      ((__ \ "previousAddressLine1").read[String] and
        (__ \ "previousAddressLine2").read[String] and
        (__ \ "previousAddressLine3").readNullable[String] and
        (__ \ "previousAddressLine4").readNullable[String] and
        (__ \ "previousAddressCountry").read[Country] and
        (__ \ "previousTimeAtAddress").read[TimeAtAddress]) (PreviousHomeAddressNonUK.apply _)
  }

  implicit val jsonWrites: Writes[PreviousHomeAddress] = {
    import play.api.libs.functional.syntax._
    import play.api.libs.json.Writes._
    import play.api.libs.json._
    Writes[PreviousHomeAddress] {
      case a: PreviousHomeAddressUK =>
        (
          (__ \ "previousAddressLine1").write[String] and
            (__ \ "previousAddressLine2").write[String] and
            (__ \ "previousAddressLine3").writeNullable[String] and
            (__ \ "previousAddressLine4").writeNullable[String] and
            (__ \ "previousAddressPostCode").write[String] and
            (__ \ "previousTimeAtAddress").write[TimeAtAddress]
          ) (unlift(PreviousHomeAddressUK.unapply)).writes(a)
      case a: PreviousHomeAddressNonUK =>
        (
          (__ \ "previousAddressLine1").write[String] and
            (__ \ "previousAddressLine2").write[String] and
            (__ \ "previousAddressLine3").writeNullable[String] and
            (__ \ "previousAddressLine4").writeNullable[String] and
            (__ \ "previousAddressCountry").write[Country] and
            (__ \ "previousTimeAtAddress").write[TimeAtAddress]
          ) (unlift(PreviousHomeAddressNonUK.unapply)).writes(a)
    }
  }
}