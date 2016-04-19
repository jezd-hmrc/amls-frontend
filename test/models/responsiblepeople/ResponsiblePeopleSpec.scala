package models.responsiblepeople

import models.Country
import models.responsiblepeople.TimeAtAddress.ZeroToFiveMonths
import org.joda.time.LocalDate
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json

class ResponsiblePeopleSpec extends PlaySpec with MockitoSugar with ResponsiblePeopleValues {

  "ResponsiblePeople" must {

    "validate complete json" must {

      "Serialise as expected" in {
        Json.toJson(CompleteResponsiblePeople) must be(CompleteJson)
      }

      "Deserialise as expected" in {
        CompleteJson.as[ResponsiblePeople] must be(CompleteResponsiblePeople)
      }
    }

    "implicitly return an existing Model if one present" in {
      val responsiblePeople = ResponsiblePeople.default(Some(CompleteResponsiblePeople))
      responsiblePeople must be(CompleteResponsiblePeople)
    }

    "implicitly return an empty Model if not present" in {
      val responsiblePeople = ResponsiblePeople.default(None)
      responsiblePeople must be(ResponsiblePeople())
    }
  }

  "None" when {

    val EmptyResponsiblePeople: Option[ResponsiblePeople] = None

    "Merged with AddPerson" must {
      "return ResponsiblePeople with correct AddPerson" in {
        val result = EmptyResponsiblePeople.addPerson(NewValues.addPerson)
        result must be (ResponsiblePeople(addPerson = Some(NewValues.addPerson)))
      }
    }

    "Merged with PersonResidenceType" must {
      "return ResponsiblePeople with correct PersonResidenceType" in {
        val result = EmptyResponsiblePeople.personResidenceType(NewValues.personResidenceType)
        result must be (ResponsiblePeople(personResidenceType = Some(NewValues.personResidenceType)))
      }
    }

    "Merged with ContactDetails" must {
      "return ResponsiblePeople with correct ContactDetails" in {
        val result = EmptyResponsiblePeople.contactDetails(NewValues.contactDetails)
        result must be (ResponsiblePeople(contactDetails = Some(NewValues.contactDetails)))
      }
    }

    "Merged with AddressHistory" must {
      "return ResponsiblePeople with correct AddressHistory" in {
        val result = EmptyResponsiblePeople.addressHistory(NewValues.addressHistory)
        result must be (ResponsiblePeople(addressHistory = Some(NewValues.addressHistory)))
      }
    }


    "Merged with Positions" must {
      "return ResponsiblePeople with correct Positions" in {
        val result = EmptyResponsiblePeople.positions(NewValues.positions)
        result must be (ResponsiblePeople(positions = Some(NewValues.positions)))
      }
    }

    "Merged with SaRegistered" must {
      "return ResponsiblePeople with correct SaRegistered" in {
        val result = EmptyResponsiblePeople.saRegistered(NewValues.saRegistered)
        result must be (ResponsiblePeople(saRegistered = Some(NewValues.saRegistered)))
      }
    }

    "Merged with VatRegistered" must {
      "return ResponsiblePeople with correct VatRegistered" in {
        val result = EmptyResponsiblePeople.vatRegistered(NewValues.vatRegistered)
        result must be (ResponsiblePeople(vatRegistered = Some(NewValues.vatRegistered)))
      }
    }

    "Merged with Training" must {
      "return ResponsiblePeople with correct Training" in {
        val result = EmptyResponsiblePeople.training(NewValues.training)
        result must be (ResponsiblePeople(training = Some(NewValues.training)))
      }
    }

  }

  "Successfully validate if the model is complete" when {

    "the model is fully complete" in {
      CompleteResponsiblePeople.isComplete must be(true)
    }

    "the model is not complete" in {
      val initial = ResponsiblePeople()
      initial.isComplete must be(false)
    }

  }

  "Merge with existing model" when {

    "Merged with add AddPerson" must {
      "return ResponsiblePeople with correct AddPerson" in {
        val result = CompleteResponsiblePeople.addPerson(NewValues.addPerson)
        result must be (ResponsiblePeople(
          Some(NewValues.addPerson),
          Some(DefaultValues.personResidenceType),
          Some(DefaultValues.contactDetails),
          Some(DefaultValues.addressHistory),
          Some(DefaultValues.positions),
          Some(DefaultValues.saRegistered),
          Some(DefaultValues.vatRegistered),
          Some(DefaultValues.training)))
      }
    }

    "Merged with PersonResidenceType" must {
      "return ResponsiblePeople with correct PersonResidenceType" in {
        val result = CompleteResponsiblePeople.personResidenceType(NewValues.personResidenceType)
        result must be (ResponsiblePeople(
          Some(DefaultValues.addPerson),
          Some(NewValues.personResidenceType),
          Some(DefaultValues.contactDetails),
          Some(DefaultValues.addressHistory),
          Some(DefaultValues.positions),
          Some(DefaultValues.saRegistered),
          Some(DefaultValues.vatRegistered),
          Some(DefaultValues.training)))
      }
    }

    "Merged with ContactDetails" must {
      "return ResponsiblePeople with correct ContactDetails" in {
        val result = CompleteResponsiblePeople.contactDetails(NewValues.contactDetails)
        result must be (ResponsiblePeople(
          Some(DefaultValues.addPerson),
          Some(DefaultValues.personResidenceType),
          Some(NewValues.contactDetails),
          Some(DefaultValues.addressHistory),
          Some(DefaultValues.positions),
          Some(DefaultValues.saRegistered),
          Some(DefaultValues.vatRegistered),
          Some(DefaultValues.training)))
      }
    }

    "Merged with AddressHistory" must {
      "return ResponsiblePeople with correct AddressHistory" in {
        val result = CompleteResponsiblePeople.addressHistory(NewValues.addressHistory)
        result must be (ResponsiblePeople(
          Some(DefaultValues.addPerson),
          Some(DefaultValues.personResidenceType),
          Some(DefaultValues.contactDetails),
          Some(NewValues.addressHistory),
          Some(DefaultValues.positions),
          Some(DefaultValues.saRegistered),
          Some(DefaultValues.vatRegistered),
          Some(DefaultValues.training)))
      }
    }

    "Merged with Positions" must {
      "return ResponsiblePeople with correct Positions" in {
        val result = CompleteResponsiblePeople.positions(NewValues.positions)
        result must be (ResponsiblePeople(
          Some(DefaultValues.addPerson),
          Some(DefaultValues.personResidenceType),
          Some(DefaultValues.contactDetails),
          Some(DefaultValues.addressHistory),
          Some(NewValues.positions),
          Some(DefaultValues.saRegistered),
          Some(DefaultValues.vatRegistered),
          Some(DefaultValues.training)))
      }
    }

    "Merged with SaRegistered" must {
      "return ResponsiblePeople with correct SaRegistered" in {
        val result = CompleteResponsiblePeople.saRegistered(NewValues.saRegistered)
        result must be (ResponsiblePeople(
          Some(DefaultValues.addPerson),
          Some(DefaultValues.personResidenceType),
          Some(DefaultValues.contactDetails),
          Some(DefaultValues.addressHistory),
          Some(DefaultValues.positions),
          Some(NewValues.saRegistered),
          Some(DefaultValues.vatRegistered),
          Some(DefaultValues.training)))
      }
    }

    "Merged with VatRegistered" must {
      "return ResponsiblePeople with correct VatRegistered" in {
        val result = CompleteResponsiblePeople.vatRegistered(NewValues.vatRegistered)
        result must be (ResponsiblePeople(
          Some(DefaultValues.addPerson),
          Some(DefaultValues.personResidenceType),
          Some(DefaultValues.contactDetails),
          Some(DefaultValues.addressHistory),
          Some(DefaultValues.positions),
          Some(DefaultValues.saRegistered),
          Some(NewValues.vatRegistered),
          Some(DefaultValues.training)))
      }
    }

    "Merged with Training" must {
      "return ResponsiblePeople with correct Training" in {
        val result = CompleteResponsiblePeople.training(NewValues.training)
        result must be (ResponsiblePeople(
          Some(DefaultValues.addPerson),
          Some(DefaultValues.personResidenceType),
          Some(DefaultValues.contactDetails),
          Some(DefaultValues.addressHistory),
          Some(DefaultValues.positions),
          Some(DefaultValues.saRegistered),
          Some(DefaultValues.vatRegistered),
          Some(NewValues.training)))
      }
    }
  }
}

trait ResponsiblePeopleValues {

  import DefaultValues._

  object DefaultValues {

    private val residence = UKResidence("AA3464646")
    private val residenceCountry = Country("United Kingdom", "GB")
    private val residenceNationality = Country("United Kingdom", "GB")
    private val currentPersonAddress = PersonAddressUK("Line 1", "Line 2", None, None, "NE981ZZ")
    private val currentAddress = ResponsiblePersonAddress(currentPersonAddress, ZeroToFiveMonths)
    private val additionalPersonAddress = PersonAddressUK("Line 1", "Line 2", None, None, "NE15GH")
    private val additionalAddress = ResponsiblePersonAddress(additionalPersonAddress, ZeroToFiveMonths)

    val addPerson = AddPerson("John", Some("Envy"), "Doe", IsKnownByOtherNamesNo)
    val personResidenceType = PersonResidenceType(residence, residenceCountry, residenceNationality)
    val saRegistered = SaRegisteredYes("0123456789")
    val contactDetails = ContactDetails("07702743555", "test@test.com")
    val addressHistory = ResponsiblePersonAddressHistory(Some(currentAddress), Some(additionalAddress))
    val vatRegistered = VATRegisteredNo
    val training = TrainingYes("test")
    val positions = Positions(Set(BeneficialOwner, InternalAccountant))
  }

  object NewValues {

    private val residenceYear = 1990
    private val residenceMonth = 2
    private val residenceDay = 24
    private val residenceDate = new LocalDate(residenceYear, residenceMonth, residenceDay)
    private val residence = NonUKResidence(residenceDate, UKPassport("123464646"))
    private val residenceCountry = Country("United Kingdom", "GB")
    private val residenceNationality = Country("United Kingdom", "GB")
    private val newPersonAddress = PersonAddressNonUK("Line 1", "Line 2", None, None, Country("Spain", "ES"))
    private val newAdditionalPersonAddress = PersonAddressNonUK("Line 1", "Line 2", None, None, Country("France", "FR"))
    private val currentAddress = ResponsiblePersonAddress(newPersonAddress, ZeroToFiveMonths)
    private val additionalAddress = ResponsiblePersonAddress(newAdditionalPersonAddress, ZeroToFiveMonths)

    val addPerson = AddPerson("first", Some("middle"), "last", IsKnownByOtherNamesNo)
    val contactDetails = ContactDetails("07702743444", "new@test.com")
    val addressHistory = ResponsiblePersonAddressHistory(Some(currentAddress), Some(additionalAddress))
    val personResidenceType = PersonResidenceType(residence, residenceCountry, residenceNationality)
    val saRegistered = SaRegisteredNo
    val vatRegistered = VATRegisteredYes("12345678")
    val positions = Positions(Set(Director, SoleProprietor))
    val training = TrainingNo
  }

  val CompleteResponsiblePeople = ResponsiblePeople(
    Some(DefaultValues.addPerson),
    Some(DefaultValues.personResidenceType),
    Some(DefaultValues.contactDetails),
    Some(DefaultValues.addressHistory),
    Some(DefaultValues.positions),
    Some(DefaultValues.saRegistered),
    Some(DefaultValues.vatRegistered),
    Some(DefaultValues.training)
  )


  val CompleteJson = Json.obj(
    "addPerson" -> Json.obj(
      "firstName" -> "John",
      "middleName" -> "Envy",
      "lastName" -> "Doe",
      "isKnownByOtherNames" -> false
    ),
    "personResidenceType" -> Json.obj(
      "nino" -> "AA3464646",
      "countryOfBirth" -> "GB",
      "nationality" -> "GB"
    ),
    "contactDetails" -> Json.obj(
      "phoneNumber" -> "07702743555",
      "emailAddress" -> "test@test.com"
    ),
    "addressHistory" -> Json.obj(
      "currentAddress" -> Json.obj(
        "personAddress" -> Json.obj(
          "personAddressLine1" -> "Line 1",
          "personAddressLine2" -> "Line 2",
          "personAddressPostCode" -> "NE981ZZ"
        ),
        "timeAtAddress" -> Json.obj(
          "timeAtAddress" -> "01"
        )
      ),
      "additionalAddress" -> Json.obj(
        "personAddress" -> Json.obj(
          "personAddressLine1" -> "Line 1",
          "personAddressLine2" -> "Line 2",
          "personAddressPostCode" -> "NE15GH"
        ),
        "timeAtAddress" -> Json.obj(
          "timeAtAddress" -> "01"
        )
      )
    ),
    "positions" -> Json.obj(
      "positions" -> Seq("01", "03")
    ),
    "saRegistered" -> Json.obj(
      "saRegistered" -> true,
      "utrNumber" -> "0123456789"
    ),
    "vatRegistered" -> Json.obj(
      "registeredForVAT" -> false
    ),
    "training" -> Json.obj(
      "training" -> true,
      "information" -> "test"
    )
  )

  /** Make sure Responsible People model is complete */
  assert(CompleteResponsiblePeople.isComplete)

}
