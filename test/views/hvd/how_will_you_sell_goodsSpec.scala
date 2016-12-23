package views.hvd

import forms.{InvalidForm, ValidForm, Form2}
import models.hvd.{Wholesale, Retail, HowWillYouSellGoods}
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatestplus.play.OneAppPerSuite
import play.api.data.mapping.Path
import play.api.data.validation.ValidationError
import play.api.i18n.Messages
import views.ViewFixture


class how_will_you_sell_goodsSpec extends WordSpec with MustMatchers with OneAppPerSuite {

  "how_will_you_sell_goods view" must {
    "have correct title" in new ViewFixture {

      val form2: ValidForm[HowWillYouSellGoods] = Form2(HowWillYouSellGoods(Seq(Retail)))

      def view = views.html.hvd.how_will_you_sell_goods(form2, true)

      doc.title must startWith(Messages("hvd.how-will-you-sell-goods.title") + " - " + Messages("summary.hvd"))
    }

    "have correct headings" in new ViewFixture {

      val form2: ValidForm[HowWillYouSellGoods] = Form2(HowWillYouSellGoods(Seq(Wholesale)))

      def view = views.html.hvd.how_will_you_sell_goods(form2, true)

      heading.html must be(Messages(Messages("hvd.how-will-you-sell-goods.title")))
      subHeading.html must include(Messages(Messages("summary.hvd")))

    }

    "show errors in the correct locations" in new ViewFixture {

      val form2: InvalidForm = InvalidForm(Map.empty,
        Seq(
          (Path \ "salesChannels") -> Seq(ValidationError("not a message Key"))
        ))

      def view = views.html.hvd.how_will_you_sell_goods(form2, true)

      errorSummary.html() must include("not a message Key")

      doc.getElementById("salesChannels")
        .getElementsByClass("error-notification").first().html() must include("not a message Key")
    }
  }
}
