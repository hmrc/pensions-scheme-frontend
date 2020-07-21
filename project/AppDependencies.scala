import sbt._

object AppDependencies {

  import play.core.PlayVersion
  import play.sbt.PlayImport._

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc" %% "simple-reactivemongo" % "7.23.0-play-26",
    "uk.gov.hmrc" %% "logback-json-logger" % "4.0.0",
    "uk.gov.hmrc" %% "govuk-template" % "5.36.0-play-26",
    "uk.gov.hmrc" %% "play-health" % "3.14.0-play-26",
    "uk.gov.hmrc" %% "play-ui" % "8.8.0-play-26",
    "uk.gov.hmrc" %% "http-caching-client" % "9.0.0-play-26",
    "uk.gov.hmrc" %% "play-conditional-form-mapping" % "1.2.0-play-26",
    "uk.gov.hmrc" %% "bootstrap-play-26" % "1.14.0",
    "uk.gov.hmrc" %% "play-language" % "4.2.0-play-26",
    "uk.gov.hmrc" %% "domain" % "5.9.0-play-26",
    "uk.gov.hmrc" %% "play-whitelist-filter" % "3.1.0-play-26",
    "com.typesafe.play" %% "play-json-joda" % "2.6.10",
    "com.google.inject.extensions" % "guice-multibindings" % "4.2.2"
  )

  val scope: String = "test"
  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-play-26" % "1.14.0" % Test classifier "tests",
    "uk.gov.hmrc" %% "hmrctest" % "3.9.0-play-26" % scope,
    "org.scalatest" %% "scalatest" % "3.0.8" % scope,
    "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % scope,
    "org.scalacheck" %% "scalacheck" % "1.14.0" % scope,
    "org.pegdown" % "pegdown" % "1.6.0" % scope,
    "org.jsoup" % "jsoup" % "1.12.1" % scope,
    "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
    "org.mockito" % "mockito-all" % "1.10.19" % scope,
    "com.github.tomakehurst" % "wiremock-jre8" % "2.21.0" % scope,
    "wolfendale" %% "scalacheck-gen-regexp" % "0.1.1" % scope
  )

  def apply(): Seq[ModuleID] = compile ++ test
}
