import sbt._

object AppDependencies {

  import play.sbt.PlayImport._
  private val bootstrapVersion = "9.5.0"
  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"                   %%  "play-conditional-form-mapping-play-30" % "3.2.0",
    "uk.gov.hmrc"                   %%  "bootstrap-frontend-play-30"            % bootstrapVersion,
    "com.typesafe.play"             %%  "play-json-joda"                        % "2.10.6",
    "com.google.inject.extensions"  %   "guice-multibindings"                   % "4.2.3",
    "uk.gov.hmrc"                   %%  "play-frontend-hmrc-play-30"            % "10.13.0",
    "uk.gov.hmrc"                   %%  "domain-play-30"                        % "10.0.0",
    "org.owasp.encoder"             %   "encoder"                               % "1.2.3"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                  %% "bootstrap-test-play-30" % bootstrapVersion    % Test,
    "org.scalatest"                %% "scalatest"              % "3.2.19"            % Test,
    "org.scalatestplus"            %% "scalacheck-1-17"        % "3.2.18.0"          % Test,
    "org.scalatestplus"            %% "mockito-4-6"            % "3.2.15.0"          % Test,
    "org.scalatestplus.play"       %% "scalatestplus-play"     % "7.0.1"             % Test,
    "org.pegdown"                  %  "pegdown"                % "1.6.0"             % Test,
    "org.jsoup"                    %  "jsoup"                  % "1.18.1"            % Test,
    "io.github.wolfendale"         %% "scalacheck-gen-regexp"  % "1.1.0"             % Test,
    "com.vladsch.flexmark"         %  "flexmark-all"           % "0.64.8"            % Test,
    "com.fasterxml.jackson.module" %% "jackson-module-scala"   % "2.17.2"            % Test
  )

  def apply(): Seq[ModuleID] = compile ++ test
}
