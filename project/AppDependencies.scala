import sbt._

object AppDependencies {

  import play.sbt.PlayImport._
  private val bootstrapVersion = "8.4.0"
  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"                   %%  "http-caching-client-play-30"           % "11.2.0",
    "uk.gov.hmrc"                   %%  "play-conditional-form-mapping-play-30" % "2.0.0",
    "uk.gov.hmrc"                   %%  "bootstrap-frontend-play-30"    % bootstrapVersion,
    "uk.gov.hmrc"                   %%  "domain-play-30"                % "9.0.0",
    "com.typesafe.play"             %%  "play-json-joda"                % "2.9.4",
    "com.google.inject.extensions"  %   "guice-multibindings"           % "4.2.3",
    "uk.gov.hmrc"                   %%  "play-frontend-hmrc-play-30"    % "8.3.0",
    "org.owasp.encoder"             %   "encoder"                       % "1.2.3"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                  %% "bootstrap-test-play-30" % bootstrapVersion         % Test,
    "org.scalatest"                %% "scalatest"              % "3.2.15"            % Test,
    "org.scalatestplus"            %% "scalacheck-1-17"        % "3.2.15.0"          % Test,
    "org.scalatestplus"            %% "mockito-4-6"            % "3.2.15.0"          % Test,
    "org.scalatestplus.play"       %% "scalatestplus-play"     % "5.1.0"             % Test,
    "org.pegdown"                  %  "pegdown"                % "1.6.0"             % Test,
    "org.jsoup"                    %  "jsoup"                  % "1.15.4"            % Test,
    "io.github.wolfendale"         %% "scalacheck-gen-regexp"  % "1.1.0"             % Test,
    "com.vladsch.flexmark"         %  "flexmark-all"           % "0.64.6"            % Test,
    "com.fasterxml.jackson.module" %% "jackson-module-scala"   % "2.14.2"            % Test
  )

  def apply(): Seq[ModuleID] = compile ++ test
}
