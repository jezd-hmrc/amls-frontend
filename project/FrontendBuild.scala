import sbt._

object FrontendBuild extends Build with MicroService {
  import sbt.Keys._

  val appName = "amls-frontend"

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}

private object AppDependencies {

  import play.sbt.PlayImport._
  import play.core.PlayVersion

  private val playPartialsVersion = "6.11.0-play-26"
  private val httpCachingClientVersion = "9.0.0-play-26"
  private val playWhitelistFilterVersion = "3.4.0-play-26"
  private val validationVersion = "2.1.0"
  private val flexmarkVersion = "0.19.1"
  private val okHttpVersion = "3.9.1"
  private val jsonEncryptionVersion = "4.5.0-play-26"
  private val playReactivemongoVersion = "7.26.0-play-26"
  private val authVersion = "3.0.0-play-26"
  private val domain = "5.9.0-play-26"

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "domain" % domain,
    "uk.gov.hmrc" %% "play-partials" % playPartialsVersion,
    "uk.gov.hmrc" %% "http-caching-client" % httpCachingClientVersion,
    "uk.gov.hmrc" %% "play-whitelist-filter" % playWhitelistFilterVersion,
    "uk.gov.hmrc" %% "json-encryption" % jsonEncryptionVersion,
    "uk.gov.hmrc" %% "simple-reactivemongo" % playReactivemongoVersion,
    "uk.gov.hmrc" %% "auth-client" % authVersion,
    "uk.gov.hmrc" %% "play-ui" % "8.10.0-play-26",
    "uk.gov.hmrc" %% "bootstrap-play-26" % "1.8.0",
    "uk.gov.hmrc" %% "govuk-template" % "5.55.0-play-26",
    "uk.gov.hmrc" %% "http-verbs" % "10.7.0-play-26",

    "io.github.jto" %% "validation-core"      % validationVersion,
    "io.github.jto" %% "validation-playjson"  % validationVersion,
    "io.github.jto" %% "validation-form"      % validationVersion,

    "com.vladsch.flexmark" % "flexmark-all" % flexmarkVersion,
    "com.beachape" %% "enumeratum-play" % "1.5.10",
    "com.squareup.okhttp3" % "mockwebserver" % okHttpVersion,
    "com.typesafe.play" %% "play-json" % "2.6.13",
    "com.typesafe.play" %% "play-json-joda" % "2.6.13"
  )

  trait ScopeDependencies {
    val scope: String
    val dependencies: Seq[ModuleID]
  }

  private val pegdownVersion = "1.6.0"
  private val jsoupVersion = "1.9.2"

  object Test {
    def apply() = new ScopeDependencies {
      override val scope = "test"
      override lazy val dependencies = Seq(
        "org.scalacheck" %% "scalacheck" % "1.12.5" % scope,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "org.jsoup" % "jsoup" % jsoupVersion % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "org.mockito" % "mockito-all" % "1.10.19" % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % scope
      )
    }.dependencies
  }

  def apply() = compile ++ Test()
}
