import sbt._

object FrontendBuild extends Build with MicroService {

  val appName = "pensions-scheme-frontend"

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}

private object AppDependencies {
  import play.core.PlayVersion
  import play.sbt.PlayImport._

  private val playHealthVersion = "3.14.0-play-25"
  private val logbackJsonLoggerVersion = "3.1.0"
  private val govukTemplateVersion = "5.26.0-play-25"
  private val hmrcTestVersion = "3.8.0-play-25"
  private val scalaTestVersion = "3.0.4"
  private val scalaTestPlusPlayVersion = "2.0.1"
  private val playUiVersion = "7.40.0-play-25"
  private val pegdownVersion = "1.6.0"
  private val mockitoAllVersion = "1.10.19"
  private val httpCachingClientVersion = "8.4.0-play-25"
  private val playReactivemongoVersion = "6.7.0"
  private val playConditionalFormMappingVersion = "0.2.0"
  private val playLanguageVersion = "3.4.0"
  private val bootstrapVersion = "4.12.0"
  private val domainVersion = "5.6.0-play-25"
  private val scalacheckGenRegexp = "0.1.1"
  private val wireMockVersion = "2.18.0"
  private val whitelistVersion = "2.0.0"

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "play-reactivemongo" % playReactivemongoVersion,
    "uk.gov.hmrc" %% "logback-json-logger" % logbackJsonLoggerVersion,
    "uk.gov.hmrc" %% "govuk-template" % govukTemplateVersion,
    "uk.gov.hmrc" %% "play-health" % playHealthVersion,
    "uk.gov.hmrc" %% "play-ui" % playUiVersion,
    "uk.gov.hmrc" %% "http-caching-client" % httpCachingClientVersion,
    "uk.gov.hmrc" %% "play-conditional-form-mapping" % playConditionalFormMappingVersion,
    "uk.gov.hmrc" %% "bootstrap-play-25" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-language" % playLanguageVersion,
    "uk.gov.hmrc" %% "domain" % domainVersion,
    "uk.gov.hmrc" %% "play-whitelist-filter" % whitelistVersion
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test : Seq[ModuleID] = ???
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
        "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusPlayVersion % scope,
        "org.scalacheck" %% "scalacheck" % "1.14.0" % scope,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "org.jsoup" % "jsoup" % "1.11.3" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "org.mockito" % "mockito-all" % mockitoAllVersion % scope,
        "com.github.tomakehurst" % "wiremock" % wireMockVersion % scope,
        "wolfendale" %% "scalacheck-gen-regexp" % scalacheckGenRegexp % scope

      )
    }.test
  }

  def apply() = compile ++ Test()
}
