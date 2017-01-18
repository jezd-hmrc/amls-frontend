package models.responsiblepeople

import play.api.libs.json.Json

case class ResponsiblePersonAddressHistory(currentAddress: Option[ResponsiblePersonCurrentAddress] = None,
                                           additionalAddress: Option[ResponsiblePersonAddress] = None,
                                           additionalExtraAddress: Option[ResponsiblePersonAddress] = None) {

  def currentAddress(add: ResponsiblePersonCurrentAddress): ResponsiblePersonAddressHistory =
    this.copy(currentAddress = Some(add))

  def additionalAddress(add: ResponsiblePersonAddress): ResponsiblePersonAddressHistory =
    this.copy(additionalAddress = Some(add))

  def additionalExtraAddress(add: ResponsiblePersonAddress): ResponsiblePersonAddressHistory =
    this.copy(additionalExtraAddress = Some(add))

  def removeAdditionalExtraAddress = this.copy(additionalExtraAddress = None)

  def isComplete: Boolean = currentAddress.isDefined

}

object ResponsiblePersonAddressHistory {

  implicit val format = Json.format[ResponsiblePersonAddressHistory]

}
