package models.businessmatching

import play.api.data.mapping.forms.Rules._
import play.api.data.mapping.{Write, From, Rule}
import play.api.data.mapping.forms._
import play.api.libs.json.Json

case class CompanyRegistrationNumber(companyRegistrationNumber: String)


object CompanyRegistrationNumber {

  val registrationNumberLength = 8
  val registrationNumberRegex = "^[A-Z0-9]{8}$".r
  val registrationType = notEmpty compose pattern(registrationNumberRegex)

  implicit val formats = Json.format[CompanyRegistrationNumber]

  implicit val formReads: Rule[UrlFormEncoded, CompanyRegistrationNumber] = From[UrlFormEncoded] { __ =>
    import play.api.data.mapping.forms.Rules._
    (__ \ "companyRegistrationNumber").read(registrationType) fmap CompanyRegistrationNumber.apply
  }

  implicit val formWrites: Write[CompanyRegistrationNumber, UrlFormEncoded] = Write {
    case CompanyRegistrationNumber(registered) => Map("companyRegistrationNumber" -> Seq(registered))
  }

}