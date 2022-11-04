import sbt._

object AppDependencies {

  import play.sbt.PlayImport._

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"                   %%  "logback-json-logger"           % "5.2.0",
    "uk.gov.hmrc"                   %%  "govuk-template"                % "5.78.0-play-28",
    "uk.gov.hmrc"                   %%  "play-ui"                       % "9.11.0-play-28",
    "uk.gov.hmrc"                   %%  "http-caching-client"           % "10.0.0-play-28",
    "uk.gov.hmrc"                   %%  "play-conditional-form-mapping" % "1.12.0-play-28",
    "uk.gov.hmrc"                   %%  "bootstrap-frontend-play-28"    % "7.8.0",
    "uk.gov.hmrc"                   %%  "domain"                        % "8.1.0-play-28",
    "com.typesafe.play"             %%  "play-json-joda"                % "2.9.3",
    "com.google.inject.extensions"  %   "guice-multibindings"           % "4.2.3",
    "uk.gov.hmrc"                   %%  "play-frontend-hmrc"            % "3.32.0-play-28"
  )

  val scope: String = "test"
  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28" % "7.8.0"             % Test,
    "org.scalatestplus"       %% "scalatestplus-scalacheck" % "3.1.0.0-RC2"     % "test",
    "org.scalatestplus.play"  %%  "scalatestplus-play"    % "5.1.0"             % scope,
    "org.scalacheck"          %%  "scalacheck"            % "1.17.0"            % scope,
    "org.pegdown"             %   "pegdown"               % "1.6.0"             % scope,
    "org.jsoup"               %   "jsoup"                 % "1.15.3"            % scope,
    "org.scalatestplus"       %% "mockito-3-4"            % "3.2.10.0"          % Test,
    "com.github.tomakehurst"  %   "wiremock-jre8"         % "2.34.0"            % scope,
    "io.github.wolfendale"    %% "scalacheck-gen-regexp"   % "1.0.0"            % scope,
    "com.vladsch.flexmark"    % "flexmark-all"            % "0.62.2"           % Test,
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.13.4"        % "test"
  )

  def apply(): Seq[ModuleID] = compile ++ test
}
