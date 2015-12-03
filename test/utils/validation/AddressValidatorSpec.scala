package utils.validation

import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.data.FormError
import uk.gov.hmrc.play.test.WithFakeApplication
import utils.validation.AddressValidator._

class AddressValidatorSpec extends PlaySpec with MockitoSugar with OneServerPerSuite  {

  private val maxLengthAddressLines = 40
  private val ukISOCountryCode = "GB"

  "address" should {
    "respond suitably to all mandatory lines being blank" in {
      val allMandatoryBlank = Map(
        "addr1key"->"",
        "addr2key"->"",
        "postcodekey"->"CA3 9SD",
        "countrycodekey"->ukISOCountryCode
      )
      address("addr2key","addr3key","addr4key","postcodekey", "countrycodekey",
        "mandatory-blank", "all-mandatory-blank", "invalid-line","blank-postcode","invalid-postcode",
        maxLengthAddressLines, ukISOCountryCode).bind(allMandatoryBlank)
        .left.getOrElse(Nil).contains(FormError("", "all-mandatory-blank")) mustBe true
    }

    "respond suitably to any but not all mandatory lines being blank" in {
      val anyMandatoryBlank = Map(
        "addr1key"->"a",
        "addr2key"->"",
        "postcodekey"->"CA3 9SD",
        "countrycodekey"->ukISOCountryCode
      )
      val mapping = address("addr2key","addr3key","addr4key","postcodekey", "countrycodekey",
        "mandatory-blank", "all-mandatory-blank", "invalid-line","blank-postcode","invalid-postcode",
        maxLengthAddressLines, ukISOCountryCode)
        .binder.bind("addr1key", anyMandatoryBlank).left.getOrElse(Nil)
        .contains(FormError("addr2key", "mandatory-blank")) mustBe true
    }

    "respond suitably to invalid lines" in {
      val invalidLine2 = Map(
        "addr1key"->"addr1",
        "addr2key"->"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
        "addr3key"->"addr3",
        "addr4key"->"addr4",
        "postcodekey"->"pcode",
        "countrycodekey"->ukISOCountryCode
      )
      address("addr2key","addr3key","addr4key","postcodekey", "countrycodekey",
        "mandatory-blank", "all-mandatory-blank", "invalid-line","blank-postcode","invalid-postcode",
        maxLengthAddressLines, ukISOCountryCode).bind(invalidLine2)
        .left.getOrElse(Nil).contains(FormError("addr2key", "invalid-line")) mustBe true
    }

    "respond suitably to blank postcode" in {
      val blankPostcode = Map(
        "addr1key"->"addr1",
        "addr2key"->"addr2",
        "addr3key"->"addr3",
        "addr4key"->"addr4",
        "postcodekey"->"",
        "countrycodekey"->ukISOCountryCode
      )
      address("addr2key","addr3key","addr4key","postcodekey", "countrycodekey",
        "mandatory-blank", "all-mandatory-blank", "invalid-line","blank-postcode","invalid-postcode",
        maxLengthAddressLines, ukISOCountryCode).bind(blankPostcode)
        .left.getOrElse(Nil).contains(FormError("postcodekey", "blank-postcode")) mustBe true
    }

    "respond suitably to invalid postcode" in {
      val invalidPostcode = Map(
        "addr1key"->"addr1",
        "addr2key"->"addr2",
        "addr3key"->"addr3",
        "addr4key"->"addr4",
        "postcodekey"->"CC!",
        "countrycodekey"->ukISOCountryCode
      )
      address("addr2key","addr3key","addr4key","postcodekey", "countrycodekey",
        "mandatory-blank", "all-mandatory-blank", "invalid-line","blank-postcode","invalid-postcode",
        maxLengthAddressLines, ukISOCountryCode).bind(invalidPostcode)
        .left.getOrElse(Nil).contains(FormError("postcodekey", "invalid-postcode")) mustBe true
    }

    "respond suitably when unbound" in {
      address("addr2key","addr3key","addr4key","postcodekey", "countrycodekey",
        "mandatory-blank", "all-mandatory-blank", "invalid-line","blank-postcode","invalid-postcode",
        maxLengthAddressLines, ukISOCountryCode)
        .binder.unbind("addr2key", "hello") mustBe
          Map( "addr2key" -> "hello")
    }

  }

}
