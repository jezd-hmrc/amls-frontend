/*
 * Copyright 2018 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package audit

import audit.Utils._
import cats.Functor
import cats.implicits._
import models.aboutthebusiness._
import models.responsiblepeople.{PersonAddress, PersonAddressNonUK, PersonAddressUK}
import models.tradingpremises.{Address => TradingPremisesAddress}
import play.api.libs.json._
import play.api.mvc.Request
import uk.gov.hmrc.play.audit.AuditExtensions._
import uk.gov.hmrc.play.audit.model.DataEvent
import uk.gov.hmrc.play.config.AppName
import uk.gov.hmrc.http.HeaderCarrier

case class AuditAddress(addressLine1: String, addressLine2: String, addressLine3: Option[String], country: String, postCode: Option[String])

object AuditAddress {
  implicit val format = Json.format[AuditAddress]
}

object AddressCreatedEvent {
  def apply(address: AuditAddress)(implicit hc: HeaderCarrier, request: Request[_]) = DataEvent(
    auditSource = AppName.appName,
    auditType = "manualAddressSubmitted",
    tags = hc.toAuditTags("manualAddressSubmitted", request.path),
    detail = hc.toAuditDetails() ++ toMap(address)
  )
}

case class AddressModifiedEvent(currentAddress: AuditAddress, oldAddress: Option[AuditAddress])

object AddressModifiedEvent {

  implicit val writes = Writes[AddressModifiedEvent] { event =>
    import play.api.libs.json._

    val currentAddressObj = Json.obj(
      "addressLine1" -> event.currentAddress.addressLine1,
      "addressLine2" -> event.currentAddress.addressLine2,
      "country" -> event.currentAddress.country) ++?
      ("addressLine3" -> event.currentAddress.addressLine3) ++?
      ("postCode" -> event.currentAddress.postCode)

    event.oldAddress.fold(currentAddressObj){ old =>
      currentAddressObj ++ Json.obj(
        "originalLine1" -> old.addressLine1,
        "originalLine2" -> old.addressLine2,
        "originalCountry" -> old.country) ++?
        ("originalLine3" -> old.addressLine3) ++?
        ("originalPostCode" -> old.postCode)
    }

  }
}

object AddressConversions {

  implicit def toDataEvent(event: AddressModifiedEvent)(implicit hc: HeaderCarrier, request: Request[_]): DataEvent = DataEvent(
    auditSource = AppName.appName,
    auditType = "addressModified",
    tags = hc.toAuditTags("addressModified", request.path),
    detail = hc.toAuditDetails() ++ toMap(event)
  )

  implicit def convert(address: PersonAddress): AuditAddress = address match {
    case a: PersonAddressUK => convert(a)
    case a: PersonAddressNonUK => convert(a)
  }

  implicit def convert(address: PersonAddressUK): AuditAddress =
    AuditAddress(address.addressLine1, address.addressLine2, address.addressLine3, "GB", address.postCode.some)

  implicit def convert(address: PersonAddressNonUK): AuditAddress =
    AuditAddress(address.addressLineNonUK1, address.addressLineNonUK2, address.addressLineNonUK3, address.country.name, None)

  implicit def convert(address: TradingPremisesAddress): AuditAddress =
    AuditAddress(address.addressLine1, address.addressLine2, address.addressLine3, "GB", address.postcode.some)

  implicit def convert(address: RegisteredOffice): AuditAddress = address match {
    case a: RegisteredOfficeUK => convert(a)
    case a: RegisteredOfficeNonUK => convert(a)
  }

  implicit def convert(address: RegisteredOfficeUK): AuditAddress =
    AuditAddress(address.addressLine1, address.addressLine2, address.addressLine3, "GB", address.postCode.some)

  implicit def convert(address: RegisteredOfficeNonUK): AuditAddress =
    AuditAddress(address.addressLine1, address.addressLine2, address.addressLine3, address.country.name, None)

  implicit def convert(address: CorrespondenceAddress): AuditAddress = address match {
    case a: UKCorrespondenceAddress => convert(a)
    case a: NonUKCorrespondenceAddress => convert(a)
  }

  implicit def convert(address: UKCorrespondenceAddress): AuditAddress =
    AuditAddress(address.addressLine1, address.addressLine2, address.addressLine3, "GB", address.postCode.some)

  implicit def convert(address: NonUKCorrespondenceAddress): AuditAddress =
    AuditAddress(address.addressLineNonUK1, address.addressLineNonUK2, address.addressLineNonUK3, address.country.name, None)

  implicit def convertOptionalAddress[A](address: Option[A])(implicit f: A => AuditAddress): Option[AuditAddress] = Functor[Option].lift(f)(address)

}
