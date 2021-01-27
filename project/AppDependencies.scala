import sbt._

object AppDependencies {

  import play.core.PlayVersion
  import play.sbt.PlayImport._

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"                   %%  "logback-json-logger"           % "4.9.0",
    "uk.gov.hmrc"                   %%  "govuk-template"                % "5.61.0-play-27",
    "uk.gov.hmrc"                   %%  "play-health"                   % "3.16.0-play-27",
    "uk.gov.hmrc"                   %%  "play-ui"                       % "8.21.0-play-27",
    "uk.gov.hmrc"                   %%  "http-caching-client"           % "9.2.0-play-27",
    "uk.gov.hmrc"                   %%  "play-conditional-form-mapping" % "1.5.0-play-27",
    "uk.gov.hmrc"                   %%  "bootstrap-frontend-play-27"    % "3.4.0",
    "uk.gov.hmrc"                   %%  "play-language"                 % "4.10.0-play-27",
    "uk.gov.hmrc"                   %%  "domain"                        % "5.10.0-play-27",
    "com.typesafe.play"             %%  "play-json-joda"                % "2.6.10",
    "com.google.inject.extensions"  %   "guice-multibindings"           % "4.2.2"
  )

  val scope: String = "test"
  val test: Seq[ModuleID] = Seq(
    "org.scalatest"           %%  "scalatest"             % "3.0.8"             % scope,
    "org.scalatestplus.play"  %%  "scalatestplus-play"    % "4.0.2"             % scope,
    "org.scalacheck"          %%  "scalacheck"            % "1.14.0"            % scope,
    "org.pegdown"             %   "pegdown"               % "1.6.0"             % scope,
    "org.jsoup"               %   "jsoup"                 % "1.12.1"            % scope,
    "com.typesafe.play"       %%  "play-test"             % PlayVersion.current % scope,
    "org.mockito"             %   "mockito-all"           % "1.10.19"           % scope,
    "com.github.tomakehurst"  %   "wiremock-jre8"         % "2.21.0"            % scope,
    "wolfendale"              %%  "scalacheck-gen-regexp" % "0.1.1"             % scope
  )

  def apply(): Seq[ModuleID] = compile ++ test
}
