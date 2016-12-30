package views.hvd

import forms.{InvalidForm, ValidForm, Form2}
import models.hvd.PercentageOfCashPaymentOver15000
import models.hvd.PercentageOfCashPaymentOver15000.{Third, Second}
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatestplus.play.OneAppPerSuite
import play.api.data.mapping.Path
import play.api.data.validation.ValidationError
import play.api.i18n.Messages
import views.ViewFixture


class percentageSpec extends WordSpec with MustMatchers with OneAppPerSuite {

  "percentage view" must {
    "have correct title" in new ViewFixture {

      val form2: ValidForm[PercentageOfCashPaymentOver15000] = Form2(Second)

      def view = views.html.hvd.percentage(form2, true)

      doc.title must startWith(Messages("hvd.percentage.title") + " - " + Messages("summary.hvd"))
    }

    "have correct headings" in new ViewFixture {

      val form2: ValidForm[PercentageOfCashPaymentOver15000] = Form2(Third)

      def view = views.html.hvd.percentage(form2, true)

      heading.html must be(Messages("hvd.percentage.title"))
      subHeading.html must include(Messages("summary.hvd"))

    }

    "show errors in the correct locations" in new ViewFixture {

      val form2: InvalidForm = InvalidForm(Map.empty,
        Seq(
          (Path \ "percentage") -> Seq(ValidationError("not a message Key"))
        ))

      def view = views.html.hvd.percentage(form2, true)

      errorSummary.html() must include("not a message Key")

      doc.getElementById("percentage")
        .getElementsByClass("error-notification").first().html() must include("not a message Key")
    }
  }
}