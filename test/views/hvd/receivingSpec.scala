package views.hvd

import forms.{InvalidForm, ValidForm, Form2}
import models.hvd.{PaymentMethods, ReceiveCashPayments}
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatestplus.play.OneAppPerSuite
import play.api.data.mapping.Path
import play.api.data.validation.ValidationError
import play.api.i18n.Messages
import views.ViewFixture


class receivingSpec extends WordSpec with MustMatchers with OneAppPerSuite {

  "receiving view" must {
    "have correct title" in new ViewFixture {

      val form2: ValidForm[ReceiveCashPayments] = Form2(ReceiveCashPayments(Some(PaymentMethods(true, true, None))))

      def view = views.html.hvd.receiving(form2, true)

      doc.title must startWith (Messages("hvd.receiving.title") + " - " + Messages("summary.hvd"))
    }

    "have correct headings" in new ViewFixture {

      val form2: ValidForm[ReceiveCashPayments] = Form2(ReceiveCashPayments(Some(PaymentMethods(true, true, None))))

      def view = views.html.hvd.receiving(form2, true)

      heading.html must be (Messages("hvd.receiving.title"))
      subHeading.html must include (Messages("summary.hvd"))

    }

    "show errors in the correct locations" in new ViewFixture {

      val form2: InvalidForm = InvalidForm(Map.empty,
        Seq(
          (Path \ "receivePayments") -> Seq(ValidationError("not a message Key")),
          (Path \ "paymentMethods") -> Seq(ValidationError("second not a message Key")),
          (Path \ "paymentMethods" \ "details") -> Seq(ValidationError("third not a message Key"))
        ))

      def view = views.html.hvd.receiving(form2, true)

      errorSummary.html() must include("not a message Key")
      errorSummary.html() must include("second not a message Key")
      errorSummary.html() must include("third not a message Key")

      doc.getElementById("receivePayments").html() must include("not a message Key")
      doc.getElementById("paymentMethods").html() must include("second not a message Key")
      doc.getElementById("paymentMethods-details-fieldset").html() must include("third not a message Key")
    }
  }
}