package views.aboutthebusiness

import forms.{InvalidForm, ValidForm, Form2}
import models.aboutthebusiness.{RegisteredOfficeUK, RegisteredOffice}
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatestplus.play.OneAppPerSuite
import play.api.data.mapping.Path
import play.api.data.validation.ValidationError
import play.api.i18n.Messages
import views.ViewFixture


class registered_officeSpec extends WordSpec with MustMatchers with OneAppPerSuite {

  "registered_office view" must {
    "have correct title" in new ViewFixture {

      val form2: ValidForm[RegisteredOffice] = Form2(RegisteredOfficeUK("line1","line2",None,None,"AB12CD"))

      def view = views.html.aboutthebusiness.registered_office(form2, true)

      doc.title must startWith(Messages("aboutthebusiness.registeredoffice.title") + " - " + Messages("summary.aboutbusiness"))
    }

    "have correct headings" in new ViewFixture {

      val form2: ValidForm[RegisteredOffice] = Form2(RegisteredOfficeUK("line1","line2",None,None,"AB12CD"))

      def view = views.html.aboutthebusiness.registered_office(form2, true)

      heading.html must be(Messages("aboutthebusiness.registeredoffice.title"))
      subHeading.html must include(Messages("summary.aboutbusiness"))

    }

    "show errors in the correct locations" in new ViewFixture {

      val form2: InvalidForm = InvalidForm(Map.empty,
        Seq(
          (Path \ "isUK") -> Seq(ValidationError("not a message Key")),
          (Path \ "postCode-fieldset") -> Seq(ValidationError("second not a message Key")),
          (Path \ "country-fieldset") -> Seq(ValidationError("third not a message Key"))
        ))

      def view = views.html.aboutthebusiness.registered_office(form2, true)

      errorSummary.html() must include("not a message Key")
      errorSummary.html() must include("second not a message Key")
      errorSummary.html() must include("third not a message Key")

      doc.getElementById("isUK")
        .getElementsByClass("error-notification").first().html() must include("not a message Key")

      doc.getElementById("postCode-fieldset")
        .getElementsByClass("error-notification").first().html() must include("second not a message Key")

      doc.getElementById("country-fieldset")
        .getElementsByClass("error-notification").first().html() must include("third not a message Key")

    }
  }
}