import sbt._

object AppDependencies {

  import play.core.PlayVersion
  import play.sbt.PlayImport._

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"                   %%  "logback-json-logger"           % "5.1.0",
    "uk.gov.hmrc"                   %%  "govuk-template"                % "5.72.0-play-28",
    "uk.gov.hmrc"                   %%  "play-health"                   % "3.16.0-play-27",
    "uk.gov.hmrc"                   %%  "play-ui"                       % "9.7.0-play-28",
    "uk.gov.hmrc"                   %%  "http-caching-client"           % "9.5.0-play-28",
    "uk.gov.hmrc"                   %%  "play-conditional-form-mapping" % "1.9.0-play-28",
    "uk.gov.hmrc"                   %%  "bootstrap-frontend-play-28"    % "5.16.0",
    "uk.gov.hmrc"                   %%  "play-language"                 % "5.1.0-play-28",
    "uk.gov.hmrc"                   %%  "domain"                        % "6.2.0-play-28",
    "com.typesafe.play"             %%  "play-json-joda"                % "2.6.10",
    "com.google.inject.extensions"  %   "guice-multibindings"           % "4.2.2"
  )

  val scope: String = "test"
  val test: Seq[ModuleID] = Seq(
    "org.scalatest"           %%  "scalatest"             % "3.0.8"             % scope,
    "org.scalatestplus.play"  %%  "scalatestplus-play"    % "5.1.0"             % scope,
    "org.scalacheck"          %%  "scalacheck"            % "1.14.0"            % scope,
    "org.pegdown"             %   "pegdown"               % "1.6.0"             % scope,
    "org.jsoup"               %   "jsoup"                 % "1.12.1"            % scope,
    "com.typesafe.play"       %%  "play-test"             % PlayVersion.current % scope,
    "org.mockito"             %% "mockito-scala"              % "1.16.42"     % "test",
    "org.mockito"             %% "mockito-scala-scalatest"    % "1.16.42"     % "test",
    "com.github.tomakehurst"  %   "wiremock-jre8"         % "2.31.0"            % scope,
    "wolfendale"              %%  "scalacheck-gen-regexp" % "0.1.1"             % scope
  )

  def apply(): Seq[ModuleID] = compile ++ test
}
