import com.typesafe.sbt.digest.Import._
import com.typesafe.sbt.web.Import._
import net.ground5hark.sbt.concat.Import._
import play.sbt.PlayImport.PlayKeys
import play.sbt.routes.RoutesKeys
import sbt.Keys._
import sbt._
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings._
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

lazy val appName: String = "pensions-scheme-frontend"

lazy val root = (project in file("."))
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin)
  .settings(scalaSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(publishingSettings: _*)
  .settings(inConfig(Test)(testSettings): _*)
  .settings(majorVersion := 0)
  .settings(scalaVersion := "2.12.11")
  .settings(silencerSettings)
  .settings(
    name := appName,
    RoutesKeys.routesImport ++= Seq(
    "models.Index",
    "models.Mode",
    "models.CheckMode",
    "models.NormalMode",
    "models.UpdateMode",
    "models.CheckUpdateMode",
    "models.register.trustees.TrusteeKind",
    "models.register.establishers.EstablisherKind",
    "models.OptionBinder._"
  ),
    TwirlKeys.templateImports ++= Seq(
      "play.twirl.api.HtmlFormat",
      "play.twirl.api.HtmlFormat._",
      "uk.gov.hmrc.play.views.html.helpers._",
      "uk.gov.hmrc.play.views.html.layouts._"
    ))
  .settings(
    PlayKeys.playDefaultPort := 8200,
    ScoverageKeys.coverageExcludedFiles := "<empty>;Reverse.*;.*filters.*;.*handlers.*;.*components.*;.*models.*;.*repositories.*;" +
      ".*BuildInfo.*;.*javascript.*;.*FrontendAuditConnector.*;.*Routes.*;.*GuiceInjector;.*UserAnswersCacheConnector;" +
      ".*ControllerConfiguration;.*LanguageSwitchController",
    ScoverageKeys.coverageMinimumStmtTotal := 80,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )
  .settings(
    scalacOptions ++= Seq("-Xfatal-warnings", "-feature"),
    libraryDependencies ++= AppDependencies(),
    retrieveManaged := true,
    update / evictionWarningOptions := EvictionWarningOptions.default.withWarnScalaVersionEviction(false)
  )
  .settings(resolvers ++= Seq(
    Resolver.jcenterRepo
  )
  )
  .settings(
    // concatenate js
    Concat.groups := Seq(
      "javascripts/pensionsschemefrontend-app.js" -> group(Seq(
        "javascripts/jquery-3.6.0.min.js",
        "javascripts/autocomplete/location-autocomplete.min.js",
        "javascripts/pensionsschemefrontend.js"
      ))),
    // prevent removal of unused code which generates warning errors due to use of third-party libs
    uglifyCompressOptions := Seq("unused=false", "dead_code=false"),
    pipelineStages := Seq(digest),
    // below line required to force asset pipeline to operate in dev rather than only prod
    Assets / pipelineStages := Seq(concat, uglify),
    // only compress files generated by concat
    uglify / includeFilter := GlobFilter("pensionsschemefrontend-*.js")
  )
  .settings(
    PlayKeys.devSettings ++= Seq(
      "urls.loginContinue" -> "http://localhost:8200/register-pension-scheme"
    )
  )

lazy val testSettings: Seq[Def.Setting[_]] = Seq(
  fork        := true,
  javaOptions ++= Seq(
    "-Dconfig.resource=test.application.conf"
  )
)

lazy val silencerSettings: Seq[Setting[_]] = {
  val silencerVersion = "1.7.0"
  Seq(
    libraryDependencies ++= Seq(compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full)),
    // silence all warnings on autogenerated files
    scalacOptions += "-P:silencer:pathFilters=target/.*",
    // Make sure you only exclude warnings for the project directories, i.e. make builds reproducible
    scalacOptions += s"-P:silencer:sourceRoots=${baseDirectory.value.getCanonicalPath}"
  )
}
