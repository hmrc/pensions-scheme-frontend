import com.typesafe.sbt.digest.Import._
import com.typesafe.sbt.web.Import._
import net.ground5hark.sbt.concat.Import._
import play.sbt.PlayImport.PlayKeys
import play.sbt.routes.RoutesKeys
import sbt.Keys._
import sbt._
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings._
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

lazy val appName: String = "pensions-scheme-frontend"

lazy val root = (project in file("."))
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(scalaSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(inConfig(Test)(testSettings): _*)
  .settings(majorVersion := 0)
  .settings(scalaVersion := "2.13.12")
  .settings(
    scalacOptions += "-Wconf:cat=unused-imports&src=html/.*:s",
    scalacOptions += "-Wconf:src=routes/.*:s"
  )
  .settings(
    name := appName,
    RoutesKeys.routesImport ++= Seq(
    "models.Index",
    "models.SchemeReferenceNumber",
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
      "play.twirl.api.HtmlFormat._"
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
    libraryDependencies ++= AppDependencies(),
    scalacOptions ++= Seq("-feature"),
    scalacOptions ++= Seq("-Xmaxerrs", "10000"),
    scalacOptions ++= Seq("-Xmaxwarns", "1"),
    //    libraryDependencies ++= AppDependencies(),
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
      "javascripts/application.js" -> group(Seq(
        "javascripts/autocomplete/location-autocomplete.min.js",
        "javascripts/pensionsschemefrontend.js"
      ))
    ),
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

