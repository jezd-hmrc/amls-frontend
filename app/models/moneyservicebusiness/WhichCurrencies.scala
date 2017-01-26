package models.moneyservicebusiness

import config.ApplicationConfig
import models._
import play.api.data.mapping.GenericRules._
import play.api.data.mapping._
import play.api.data.mapping.forms.UrlFormEncoded
import play.api.libs.functional.Monoid
import play.api.libs.json._
import utils.MappingUtils.Implicits._
import utils.{GenericValidators, TraversableValidators}

case class WhichCurrencies(currencies: Seq[String],
                           usesForeignCurrencies: Option[Boolean],
                           bankMoneySource: Option[BankMoneySource],
                           wholesalerMoneySource: Option[WholesalerMoneySource],
                           customerMoneySource: Option[Boolean])


object WhichCurrencies {

  type MoneySource = (Option[BankMoneySource], Option[WholesalerMoneySource], Option[Boolean])

  val emptyToNone: String => Option[String] = { x =>
    x.trim() match {
      case "" => None
      case s => Some(s)
    }
  }

  private def nameType(fieldName: String) = {
    minLength(1).withMessage(s"error.invalid.msb.wc.$fieldName") compose
      maxLength(140).withMessage(s"error.invalid.msb.wc.$fieldName.too-long")
  }

  private val currencyListType = TraversableValidators.seqToOptionSeq(emptyToNone) compose
    TraversableValidators.flattenR[String] compose
    TraversableValidators.minLengthR[Seq[String]](1) compose
    GenericRules.traversableR(GenericValidators.inList(currencies))

  private val validateMoneySources: ValidationRule[MoneySource] = Rule[MoneySource, MoneySource] {
      case x@(Some(_), _, _) => Success(x)
      case x@(_, Some(_), _) => Success(x)
      case x@(_, _, Some(true)) => Success(x)
      case _ => Failure(Seq((Path \ "WhoWillSupply") -> Seq(ValidationError("error.invalid.msb.wc.moneySources"))))
    }

  private implicit def rule[A]
  (implicit
   a: Path => RuleLike[A, Seq[String]],
   b: Path => RuleLike[A, Option[String]],
   d: Path => RuleLike[A, String],
   c: Path => RuleLike[A, Boolean]
  ): Rule[A, WhichCurrencies] = From[A] { __ =>

    val currencies = (__ \ "currencies").read(currencyListType).withMessage("error.invalid.msb.wc.currencies")

    println("*** " + ApplicationConfig.release7)

    val usesForeignCurrencies = ApplicationConfig.release7 match {
      case true =>
        (__ \ "usesForeignCurrencies").read[String] withMessage "error.required.msb.wc.foreignCurrencies" fmap {
          case "Yes" => Some(true)
          case _ => Some(false)
        }
      case _ => Rule[A, Option[Boolean]](_ => Success(None))
    }

    val bankMoneySource: Rule[A, Option[BankMoneySource]] =
      (__ \ "bankMoneySource").read[Option[String]] flatMap {
        case Some("Yes") => (__ \ "bankNames")
          .read(nameType("bankNames"))
          .fmap(names => Some(BankMoneySource(names)))
        case _ => Rule[A, Option[BankMoneySource]](_ => Success(None))
      }

    val wholesalerMoneySource: Rule[A, Option[WholesalerMoneySource]] =
      (__ \ "wholesalerMoneySource").read[Option[String]] flatMap {
        case Some("Yes") => (__ \ "wholesalerNames")
          .read(nameType("wholesalerNames"))
          .fmap(names => Some(WholesalerMoneySource(names)))
        case _ => Rule[A, Option[WholesalerMoneySource]](_ => Success(None))
      }

    val customerMoneySource = (__ \ "customerMoneySource").read[Option[String]] fmap {
      case Some("Yes") => Some(true)
      case _ => None
    }

    def build(foreignCurrencyFlag: Option[Boolean]) =
      (currencies ~ ((bankMoneySource ~ wholesalerMoneySource ~ customerMoneySource).tupled compose validateMoneySources))
        .apply { (a: Traversable[String], b: MoneySource) =>
          (a, b) match {
            case (c, (bms, wms, cms)) => WhichCurrencies(c.toSeq, foreignCurrencyFlag, bms, wms, cms)
          }
        }

    ApplicationConfig.release7 match {
      case true =>
        usesForeignCurrencies flatMap {
          case flag@Some(true) => build(flag)
          case flag =>
            currencies compose Rule.fromMapping[Traversable[String], WhichCurrencies] { c =>
              Success(WhichCurrencies(c.toSeq, flag, None, None, None))
            }
        }
      case _ => build(None)
    }



  }

  private implicit def write[A]
  (implicit
   m: Monoid[A],
   a: Path => WriteLike[Seq[String], A],
   b: Path => WriteLike[String, A],
   c: Path => WriteLike[Option[String], A]
  ): Write[WhichCurrencies, A] = To[A] { __ =>
    (
      (__ \ "currencies").write[Seq[String]] ~
        (__ \ "bankMoneySource").write[Option[String]] ~
        (__ \ "bankNames").write[Option[String]] ~
        (__ \ "wholesalerMoneySource").write[Option[String]] ~
        (__ \ "wholesalerNames").write[Option[String]] ~
        (__ \ "customerMoneySource").write[Option[String]] ~
        (__ \ "usesForeignCurrencies").write[Option[String]]
      ).apply(wc => (wc.currencies,
      wc.bankMoneySource.map(_ => "Yes"),
      wc.bankMoneySource.map(bms => bms.bankNames),
      wc.wholesalerMoneySource.map(_ => "Yes"),
      wc.wholesalerMoneySource.map(bms => bms.wholesalerNames),
      wc.customerMoneySource.map(_ => "Yes"),
      wc.usesForeignCurrencies match {
        case Some(true) => Some("Yes")
        case _ => Some("No")
      }
      ))
  }

  implicit val formR: Rule[UrlFormEncoded, WhichCurrencies] = {
    import play.api.data.mapping.forms.Rules._
    implicitly
  }

  implicit val formW: Write[WhichCurrencies, UrlFormEncoded] = {
    import play.api.data.mapping.forms.Writes._
    implicitly
  }

  implicit val bmsReader: Reads[Option[BankMoneySource]] = {

    import play.api.libs.functional.syntax._
    import play.api.libs.json._

    ((__ \ "bankMoneySource").readNullable[String] and
      (__ \ "bankNames").readNullable[String])((a, b) => (a, b) match {
      case (Some("Yes"), Some(names)) => Some(BankMoneySource(names))
      case _ => None
    })

  }

  implicit val bmsWriter = new Writes[Option[BankMoneySource]] {
    override def writes(o: Option[BankMoneySource]): JsValue = o match {
      case Some(x) => Json.obj("bankMoneySource" -> "Yes",
        "bankNames" -> x.bankNames)
      case _ => Json.obj()
    }
  }

  implicit val wsReader: Reads[Option[WholesalerMoneySource]] = {
    import play.api.libs.functional.syntax._
    import play.api.libs.json._

    ((__ \ "wholesalerMoneySource").readNullable[String] and
      (__ \ "wholesalerNames").readNullable[String])((a, b) => (a, b) match {
      case (Some("Yes"), Some(names)) => Some(WholesalerMoneySource(names))
      case _ => None
    })
  }

  implicit val wsWriter = new Writes[Option[WholesalerMoneySource]] {
    override def writes(o: Option[WholesalerMoneySource]): JsValue = o match {
      case Some(x) => Json.obj("wholesalerMoneySource" -> "Yes",
        "wholesalerNames" -> x.wholesalerNames)
      case _ => Json.obj()
    }
  }

  val cmsReader: Reads[Boolean] = {
    __.read[String] map {
      case "Yes" => true
      case _ => false
    }
  }

  val cmsWriter = new Writes[Boolean] {
    override def writes(o: Boolean): JsValue = o match {
      case true => JsString("Yes")
      case _ => JsNull
    }
  }

  implicit val jsonR: Reads[WhichCurrencies] = {
    import play.api.libs.functional.syntax._
    import play.api.libs.json._

    (
      (__ \ "currencies").read[Seq[String]] and
        (__ \ "usesForeignCurrencies").readNullable[Boolean] and
        __.read[Option[BankMoneySource]] and
        __.read[Option[WholesalerMoneySource]] and
        (__ \ "customerMoneySource").readNullable(cmsReader)
      )(WhichCurrencies.apply _)

  }

  implicit val jsonW: Writes[WhichCurrencies] = {
    import play.api.libs.functional.syntax._
    import play.api.libs.json._

    (
      (__ \ "currencies").write[Seq[String]] and
        (__ \ "usesForeignCurrencies").writeNullable[Boolean] and
        __.write[Option[BankMoneySource]] and
        __.write[Option[WholesalerMoneySource]] and
        (__ \ "customerMoneySource").writeNullable(cmsWriter)

      )(x => (x.currencies, x.usesForeignCurrencies, x.bankMoneySource, x.wholesalerMoneySource, x.customerMoneySource))

  }
}

//object WhichCurrencies {
//
//  private object Cache extends WhichCurrencies0
//
//  implicit val formW: Write[WhichCurrencies, UrlFormEncoded] = Cache.formW
//  implicit val formR: Rule[UrlFormEncoded, WhichCurrencies] = Cache.formR
//  implicit val jsonR: Reads[WhichCurrencies] = Cache.jsonR
//  implicit val jsonW: Writes[WhichCurrencies] = Cache.jsonW
//}
