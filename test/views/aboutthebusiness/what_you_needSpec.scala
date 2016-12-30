package views.aboutthebusiness

import org.scalatest.{MustMatchers, WordSpec}
import org.scalatestplus.play.OneAppPerSuite
import play.api.i18n.Messages
import views.ViewFixture


class what_you_needSpec extends WordSpec with MustMatchers with OneAppPerSuite{

  "What you need View" must {
    "Have the correct title" in new ViewFixture {
      def view = views.html.aboutthebusiness.what_you_need()

      doc.title must startWith(Messages("title.wyn"))
    }

    "Have the correct Headings" in new ViewFixture{
      def view = views.html.aboutthebusiness.what_you_need()

      heading.html must be (Messages("title.wyn"))
      subHeading.html must include (Messages("summary.aboutbusiness"))
    }

    "contain the expected content elements" in new ViewFixture{
      def view = views.html.aboutthebusiness.what_you_need()

      html must include(Messages("aboutthebusiness.whatyouneed.line_1"))
      html must include(Messages("aboutthebusiness.whatyouneed.line_2"))
      html must include(Messages("aboutthebusiness.whatyouneed.line_3"))
      html must include(Messages("aboutthebusiness.whatyouneed.line_4"))
      html must include(Messages("aboutthebusiness.whatyouneed.line_5"))
      html must include(Messages("aboutthebusiness.whatyouneed.line_6"))
      html must include(Messages("aboutthebusiness.whatyouneed.line_7"))
      html must include(Messages("aboutthebusiness.whatyouneed.line_8"))
    }
  }
}