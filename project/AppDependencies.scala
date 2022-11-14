import sbt._

object AppDependencies {

  import play.sbt.PlayImport._

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"                   %%  "govuk-template"                % "5.78.0-play-28",
    "uk.gov.hmrc"                   %%  "play-ui"                       % "9.11.0-play-28",
    "uk.gov.hmrc"                   %%  "http-caching-client"           % "10.0.0-play-28",
    "uk.gov.hmrc"                   %%  "play-conditional-form-mapping" % "1.12.0-play-28",
    "uk.gov.hmrc"                   %%  "bootstrap-frontend-play-28"    % "7.11.0",
    "uk.gov.hmrc"                   %%  "domain"                        % "8.1.0-play-28",
    "com.typesafe.play"             %%  "play-json-joda"                % "2.9.3",
    "com.google.inject.extensions"  %   "guice-multibindings"           % "4.2.3",
    "uk.gov.hmrc"                   %%  "play-frontend-hmrc"            % "3.32.0-play-28"
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatest"                %% "scalatest"              % "3.2.14"            % Test,
    "org.scalatestplus"            %% "scalacheck-1-17"        % "3.2.14.0"          % Test,
    "org.scalatestplus"            %% "mockito-4-6"            % "3.2.14.0"          % Test,
    "org.scalatestplus.play"       %% "scalatestplus-play"     % "5.1.0"             % Test,
    "org.pegdown"                  %  "pegdown"                % "1.6.0"             % Test,
    "org.jsoup"                    %  "jsoup"                  % "1.15.3"            % Test,
    "com.github.tomakehurst"       %  "wiremock-jre8"          % "2.35.0"            % Test,
    "io.github.wolfendale"         %% "scalacheck-gen-regexp"  % "1.0.0"             % Test,
    "com.vladsch.flexmark"         %  "flexmark-all"           % "0.62.2"            % Test,
    "com.fasterxml.jackson.module" %% "jackson-module-scala"   % "2.14.0"            % Test
  )

  def apply(): Seq[ModuleID] = compile ++ test
}
