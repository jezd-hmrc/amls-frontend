package models.businessactivities

import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsNull, Json}

class BusinessActivitiesSpec extends PlaySpec with MockitoSugar {

  val DefaultFranchiseName     = "DEFAULT FRANCHISE NAME"
  val DefaultSoftwareName      = "DEFAULT SOFTWARE"
  val DefaultBusinessTurnover  = ExpectedBusinessTurnover.First
  val DefaultAMLSTurnover      = ExpectedAMLSTurnover.First
  val DefaultInvolvedInOtherDetails = "DEFAULT INVOLVED"
  val DefaultInvolvedInOther   = InvolvedInOtherYes(DefaultInvolvedInOtherDetails)
  val DefaultBusinessFranchise = BusinessFranchiseYes(DefaultFranchiseName)
  val DefaultTransactionRecord = TransactionRecordYes(Set(Paper, DigitalSoftware(DefaultSoftwareName)))
  val DefaultCustomersOutsideUK = CustomersOutsideUKYes(Countries("GP"))
  val DefaultNCARegistered      = NCARegistered(true)


  val NewFranchiseName          = "NEW FRANCHISE NAME"
  val NewBusinessFranchise      = BusinessFranchiseYes(NewFranchiseName)
  val NewInvolvedInOtherDetails = "NEW INVOLVED"
  val NewInvoledInOther         = InvolvedInOtherYes(NewInvolvedInOtherDetails)
  val NewBusinessTurnover       = ExpectedBusinessTurnover.Second
  val NewAMLSTurnover           = ExpectedAMLSTurnover.Second
  val NewTransactionRecord      = TransactionRecordNo
  val NewCustomersOutsideUK     = CustomersOutsideUKNo
  val NewNCARegistered          = NCARegistered(false)

  "BusinessActivities" must {

    val completeJson = Json.obj(
      "involvedInOther" -> true,
      "details" -> DefaultInvolvedInOtherDetails,
      "expectedBusinessTurnover" -> "01",
      "expectedAMLSTurnover" -> "01",
      "businessFranchise" -> true,
      "franchiseName" -> DefaultFranchiseName,
      "isRecorded" -> true,
      "transactions" -> Seq("01", "03"),
      "name" -> DefaultSoftwareName,
      "isOutside" -> true,
      "country_1" -> "GP",
      "country_2" -> JsNull,
      "country_3" -> JsNull,
      "country_4" -> JsNull,
      "country_5" -> JsNull,
      "country_6" -> JsNull,
      "country_7" -> JsNull,
      "country_8" -> JsNull,
      "country_9" -> JsNull,
      "country_10" ->JsNull,
      "ncaRegistered" -> true
    )

    val completeModel = BusinessActivities(involvedInOther = Some(DefaultInvolvedInOther),
                                           expectedBusinessTurnover = Some(DefaultBusinessTurnover),
                                           expectedAMLSTurnover = Some(DefaultAMLSTurnover) ,
                                           businessFranchise = Some(DefaultBusinessFranchise),
                                           transactionRecord = Some(DefaultTransactionRecord),
                                           customersOutsideUK = Some(DefaultCustomersOutsideUK),
                                           ncaRegistered = Some(DefaultNCARegistered))

    "Serialise as expected" in {
      Json.toJson(completeModel) must be(completeJson)
    }

    "Deserialise as expected" in {
      completeJson.as[BusinessActivities] must be(completeModel)
    }

  }

  "Partially complete BusinessActivities" must {

    val partialJson = Json.obj(
      "businessFranchise" -> true,
      "franchiseName"     -> DefaultFranchiseName
    )

    val partialModel = BusinessActivities(businessFranchise = Some(DefaultBusinessFranchise))

    "Serialise as expected" in {
      Json.toJson(partialModel) mustBe partialJson
    }

    "Deserialise as expected" in {
      partialJson.as[BusinessActivities] mustBe partialModel
    }

  }

  "None" must {

    val initial: Option[BusinessActivities] = None

    "Merged with InvolvedInOther" in {
      val result = initial.involvedInOther(NewInvoledInOther)
      result must be (BusinessActivities(Some(NewInvoledInOther), None, None, None, None))
    }

    "Merged with ExcpectedBusinessTurnover" in {
      val result = initial.expectedBusinessTurnover(NewBusinessTurnover)
      result must be (BusinessActivities(None, Some(NewBusinessTurnover), None, None, None))
    }

    "Merged with ExpectedAMLSTurnover" in {
      val result = initial.expectedAMLSTurnover(NewAMLSTurnover)
      result must be (BusinessActivities(None, None, Some(NewAMLSTurnover), None, None))
    }

    "Merged with BusinessFranchise" in {
      val result = initial.businessFranchise(NewBusinessFranchise)
      result must be (BusinessActivities(None, None, None, Some(NewBusinessFranchise), None))
    }

    "Merged with TransactionRecord" in {
      val result = initial.transactionRecord(NewTransactionRecord)
      result must be (BusinessActivities(None, None, None, None, Some(NewTransactionRecord)))
    }

    "Merged with CustomersOutsideUK" in {
      val result = initial.customersOutsideUK(NewCustomersOutsideUK)
      result must be (BusinessActivities(None, None, None, None, None, Some(NewCustomersOutsideUK)))
    }

    "Merged with ncaRegistered" in {
      val result = initial.ncaRegistered(NewNCARegistered)
      result must be (BusinessActivities(None, None, None, None, None, None, Some(NewNCARegistered)))
    }

  }

  "BusinessActivities" must {


    val initial = BusinessActivities(involvedInOther = Some(DefaultInvolvedInOther),
                            expectedBusinessTurnover = Some(DefaultBusinessTurnover),
                                expectedAMLSTurnover = Some(DefaultAMLSTurnover) ,
                                   businessFranchise = Some(DefaultBusinessFranchise),
                                   transactionRecord = Some(DefaultTransactionRecord),
                                  customersOutsideUK = Some(DefaultCustomersOutsideUK),
                                       ncaRegistered = Some(DefaultNCARegistered))


    "involvedInOther" must {
      "return BusinessActivities object with correct involvedInOther set" in {
        val result = initial.involvedInOther(NewInvoledInOther)
        result must be(BusinessActivities(involvedInOther = Some(NewInvoledInOther),
          expectedBusinessTurnover = Some(DefaultBusinessTurnover),
          expectedAMLSTurnover = Some(DefaultAMLSTurnover),
          businessFranchise = Some(DefaultBusinessFranchise),
          transactionRecord = Some(DefaultTransactionRecord),
          customersOutsideUK = Some(DefaultCustomersOutsideUK),
          ncaRegistered = Some(DefaultNCARegistered)))
      }
    }

    "expectedBusinessTurnover" must {
      "return BusinessActivities object with correct expectedBusinessTurnover set" in {
        val result = initial.expectedBusinessTurnover(NewBusinessTurnover)
        result must be (BusinessActivities(
          involvedInOther = Some(DefaultInvolvedInOther),
          expectedBusinessTurnover = Some(NewBusinessTurnover),
          expectedAMLSTurnover = Some(DefaultAMLSTurnover),
          businessFranchise = Some(DefaultBusinessFranchise),
          transactionRecord = Some(DefaultTransactionRecord),
          customersOutsideUK = Some(DefaultCustomersOutsideUK),
          ncaRegistered = Some(DefaultNCARegistered)))
      }
    }


    "expectedAMLSTurnover" must {
      "return BusinessActivities object with correct expectedAMLSTurnover set" in {
        val result = initial.expectedAMLSTurnover(NewAMLSTurnover)
        result must be(BusinessActivities(
          involvedInOther = Some(DefaultInvolvedInOther),
          expectedBusinessTurnover = Some(DefaultBusinessTurnover),
          expectedAMLSTurnover = Some(NewAMLSTurnover),
          businessFranchise = Some(DefaultBusinessFranchise),
          transactionRecord = Some(DefaultTransactionRecord),
          customersOutsideUK = Some(DefaultCustomersOutsideUK),
          ncaRegistered = Some(DefaultNCARegistered)))

      }
    }

    "businessFranchise" must {
      "return BusinessActivities object with correct businessFranchise set" in {
        val result = initial.businessFranchise(NewBusinessFranchise)
        result must be(BusinessActivities(
          involvedInOther = Some(DefaultInvolvedInOther),
          expectedBusinessTurnover = Some(DefaultBusinessTurnover),
          expectedAMLSTurnover = Some(DefaultAMLSTurnover),
          businessFranchise = Some(NewBusinessFranchise),
          transactionRecord = Some(DefaultTransactionRecord),
          customersOutsideUK = Some(DefaultCustomersOutsideUK),
          ncaRegistered = Some(DefaultNCARegistered)))
      }
    }

    "transactionRecord" must {
      "return BusinessActivities object with correct transactionRecord set" in {
        val result = initial.transactionRecord(NewTransactionRecord)
        result must be (BusinessActivities(involvedInOther = Some(DefaultInvolvedInOther),
          expectedBusinessTurnover = Some(DefaultBusinessTurnover),
          expectedAMLSTurnover = Some(DefaultAMLSTurnover),
          businessFranchise = Some(DefaultBusinessFranchise),
          transactionRecord = Some(NewTransactionRecord),
          customersOutsideUK = Some(DefaultCustomersOutsideUK),
          ncaRegistered = Some(DefaultNCARegistered)))

      }
    }

    "customersOutsideUK" must {
      "return BusinessActivities object with correct customersOutsideUK set" in {
        val result = initial.customersOutsideUK(NewCustomersOutsideUK)
        result must be (BusinessActivities(involvedInOther = Some(DefaultInvolvedInOther),
          expectedBusinessTurnover = Some(DefaultBusinessTurnover),
          expectedAMLSTurnover = Some(DefaultAMLSTurnover),
          businessFranchise = Some(DefaultBusinessFranchise),
          transactionRecord = Some(DefaultTransactionRecord),
          customersOutsideUK = Some(NewCustomersOutsideUK),
          ncaRegistered = Some(DefaultNCARegistered)))
      }
    }

    "ncaRegistered" must {
      "return BusinessActivities object with correct ncaRegistered set" in {
        val result = initial.ncaRegistered(NewNCARegistered)
        result must be (BusinessActivities(involvedInOther = Some(DefaultInvolvedInOther),
          expectedBusinessTurnover = Some(DefaultBusinessTurnover),
          expectedAMLSTurnover = Some(DefaultAMLSTurnover),
          businessFranchise = Some(DefaultBusinessFranchise),
          transactionRecord = Some(DefaultTransactionRecord),
          customersOutsideUK = Some(DefaultCustomersOutsideUK),
          ncaRegistered = Some(NewNCARegistered)))
      }
    }
  }
}
