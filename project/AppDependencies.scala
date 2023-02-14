import sbt._

object AppDependencies {

  import play.sbt.PlayImport._
  private val playVersion = "7.13.0"
  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"                   %%  "http-caching-client"           % "10.0.0-play-28",
    "uk.gov.hmrc"                   %%  "play-conditional-form-mapping" % "1.12.0-play-28",
    "uk.gov.hmrc"                   %%  "bootstrap-frontend-play-28"    % playVersion,
    "uk.gov.hmrc"                   %%  "domain"                        % "8.1.0-play-28",
    "com.typesafe.play"             %%  "play-json-joda"                % "2.9.4",
    "com.google.inject.extensions"  %   "guice-multibindings"           % "4.2.3",
    "uk.gov.hmrc"                   %%  "play-frontend-hmrc"            % "6.4.0-play-28"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                  %% "bootstrap-test-play-28" % playVersion         % Test,
    "org.scalatest"                %% "scalatest"              % "3.2.15"            % Test,
    "org.scalatestplus"            %% "scalacheck-1-17"        % "3.2.14.0"          % Test,
    "org.scalatestplus"            %% "mockito-4-6"            % "3.2.15.0"          % Test,
    "org.scalatestplus.play"       %% "scalatestplus-play"     % "5.1.0"             % Test,
    "org.pegdown"                  %  "pegdown"                % "1.6.0"             % Test,
    "org.jsoup"                    %  "jsoup"                  % "1.15.3"            % Test,
    "com.github.tomakehurst"       %  "wiremock-jre8"          % "2.35.0"            % Test,
    "io.github.wolfendale"         %% "scalacheck-gen-regexp"  % "1.1.0"             % Test,
    "com.vladsch.flexmark"         %  "flexmark-all"           % "0.64.0"            % Test,
    "com.fasterxml.jackson.module" %% "jackson-module-scala"   % "2.14.2"            % Test
  )

  def apply(): Seq[ModuleID] = compile ++ test
}
