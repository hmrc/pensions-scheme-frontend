/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package config

import com.google.inject.{Inject, Singleton}
import controllers.routes
import models.ReportTechnicalIssue
import models.requests.DataRequest
import play.api.i18n.Lang
import play.api.mvc.Call
import play.api.{Configuration, Environment, Mode}
import uk.gov.hmrc.domain.{PsaId, PspId}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class FrontendAppConfig @Inject()(runModeConfiguration: Configuration, environment: Environment,
                                  servicesConfig: ServicesConfig) {
  def localFriendlyUrl(uri:String):String = loadConfig("host") + uri

  def featureToggleUrl(toggle:String) : String =
    s"${servicesConfig.baseUrl("pensions-scheme")}${runModeConfiguration.underlying.getString("urls.featureToggle").format(toggle)}"
  lazy val managePensionsSchemeOverviewUrl: Call = Call("GET", loadConfig("urls.manage-pensions-frontend" +
    ".schemesOverview"))

  lazy val administratorOrPractitionerUrl: String = loadConfig("urls.manage-pensions-frontend.administratorOrPractitioner")
  def cannotAccessPageAsAdministratorUrl(continueUrl:String): String =
    loadConfig("urls.manage-pensions-frontend.cannotAccessPageAsAdministrator").format(continueUrl)
  def cannotAccessPageAsPractitionerUrl(continueUrl:String): String =
    loadConfig("urls.manage-pensions-frontend.cannotAccessPageAsPractitioner").format(continueUrl)

  lazy val psaUpdateContactDetailsUrl: String = loadConfig("urls.pension-administrator-frontend.psaUpdateContactDetails")

  lazy val managePensionsSchemeSummaryUrl: String = loadConfig("urls.manage-pensions-frontend.schemesSummary")
  lazy val managePensionsSchemeDetailsPspUrl: String = loadConfig("urls.manage-pensions-frontend.schemesSummaryPsp")
  lazy val youMustContactHMRCUrl: String = loadConfig("urls.manage-pensions-frontend.youMustContactHMRC")
  lazy val managePensionsYourPensionSchemesUrl: String = loadConfig("urls.manage-pensions-frontend.yourPensionSchemes")
  lazy val appName: String = runModeConfiguration.underlying.getString("appName")
  lazy val googleTagManagerIdAvailable: Boolean = runModeConfiguration.underlying.getBoolean(s"google-tag-manager" +
    s".id-available")
  lazy val googleTagManagerId: String = loadConfig(s"google-tag-manager.id")
  lazy val pensionsSchemeUrl = s"${servicesConfig.baseUrl("pensions-scheme")}"
  lazy val pensionsAdministratorUrl = s"${servicesConfig.baseUrl("pension-administrator")}"
  lazy val timeout: String = loadConfig("session._timeoutSeconds")
  lazy val countdown: String = loadConfig("session._CountdownInSeconds")
  lazy val loginUrl: String = loadConfig("urls.login")
  lazy val serviceSignOut: String = loadConfig("urls.logout")
  lazy val registerSchemeUrl: String = pensionsSchemeUrl +
    runModeConfiguration.underlying.getString("urls.registerScheme")
  lazy val registerSchemeAdministratorUrl: String = loadConfig("urls.registerSchemeAdministrator")
  lazy val pensionAdministratorGovUkLink: String = runModeConfiguration.underlying.getString("urls" +
    ".pensionAdministratorGovUkLink")
  lazy val pensionPractitionerGovUkLink: String = runModeConfiguration.underlying.getString("urls" +
    ".pensionPractitionerGovUkLink")
  lazy val govUkLink: String = runModeConfiguration.underlying.getString("urls.govUkLink")
  lazy val appealLink: String = runModeConfiguration.underlying.getString("urls.appealLink")
  lazy val pensionsRegulatorLink: String = runModeConfiguration.underlying.getString("urls.pensionsRegulatorLink")
  lazy val getPSAEmail: String = runModeConfiguration.underlying.getString("urls.get-psa-email")
  lazy val getPSAName: String = runModeConfiguration.underlying.getString("urls.get-psa-name")
  lazy val minimalPsaDetailsUrl: String = pensionsAdministratorUrl + runModeConfiguration.underlying.getString("urls" +
    ".minimalPsaDetails")
  lazy val checkAssociationUrl: String = s"$pensionsSchemeUrl${runModeConfiguration.underlying.getString("urls" +
    ".checkPsaAssociation")}"
  lazy val locationCanonicalList: String = loadConfig("location.canonical.list")
  lazy val addressLookUp = s"${servicesConfig.baseUrl("address-lookup")}"
  lazy val maxDirectors: Int = loadConfig("company.maxDirectors").toInt
  lazy val maxTrustees: Int = loadConfig("maxTrustees").toInt
  lazy val maxPartners: Int = loadConfig("maxPartners").toInt
  lazy val daysDataSaved: Int = loadConfig("daysDataSaved").toInt
  lazy val emailApiUrl: String = s"${servicesConfig.baseUrl("email")}"
  lazy val emailTemplateId: String = loadConfig("email.templateId")
  lazy val emailSendForce: Boolean = runModeConfiguration.getOptional[Boolean]("email.force").getOrElse(false)
  lazy val schemeDetailsUrl: String = s"${servicesConfig.baseUrl("pensions-scheme")}${runModeConfiguration.underlying
    .getString("urls.schemeDetails")}"
  lazy val pspSchemeDetailsUrl: String = s"${servicesConfig.baseUrl("pensions-scheme")}${runModeConfiguration.underlying
    .getString("urls.pspSchemeDetails")}"
  lazy val updateSchemeDetailsUrl: String = s"${servicesConfig.baseUrl("pensions-scheme")}${
    runModeConfiguration
      .underlying.getString("urls.updateSchemeDetails")
  }"

  def schemeDashboardUrl(request: DataRequest[_]): String = schemeDashboardUrl(request.psaId, request.pspId)

  def schemeDashboardUrl(psaId: Option[PsaId], pspId: Option[PspId]): String =
    (psaId, pspId) match {
      case (Some(_), None) => managePensionsSchemeSummaryUrl
      case (None, Some(_)) => managePensionsSchemeDetailsPspUrl
      case _ => managePensionsSchemeSummaryUrl
    }

  lazy val registerUrl: String = runModeConfiguration.underlying.getString("urls.partials.registerScheme")
  lazy val continueUrl = s"${loadConfig("urls.partials.continueSchemeRegistration")}"
  lazy val viewUrl: String = runModeConfiguration.underlying.getString("urls.partials.viewSchemeDetails")
  lazy val deleteSubscriptionUrl: String = runModeConfiguration.underlying.getString("urls.partials.deleteSubscription")
  lazy val deleteSubscriptionRacdacUrl: String = runModeConfiguration.underlying.getString("urls.partials.deleteSubscriptionRacdac")
  lazy val deleteVariationsUrl: String = runModeConfiguration.underlying.getString("urls.partials.deleteVariations")
  lazy val cannotStartRegUrl: String = runModeConfiguration.underlying.getString("urls.partials.cannotStartReg")
  lazy val canBeRegisteredUrl: String = runModeConfiguration.underlying.getString("urls.partials.canBeRegistered")
  lazy val declareAsRACDACUrl: String = runModeConfiguration.underlying.getString("urls.partials.declareAsRACDAC")

  //FEATURES
  lazy val languageTranslationEnabled: Boolean = runModeConfiguration.getOptional[Boolean]("features" +
    ".welsh-translation").getOrElse(true)
  val reportAProblemNonJSUrl: String = getConfigString("contact-frontend.report-problem-url.non-js")
  val betaFeedbackUnauthenticatedUrl: String = getConfigString("contact-frontend.beta-feedback-url.unauthenticated")
  val reportTechnicalIssues = ReportTechnicalIssue(serviceId = "PODS", baseUrl = Some(reportAProblemNonJSUrl))
  def languageMap: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy"))

  def routeToSwitchLanguage: String => Call = (lang: String) => routes.LanguageSwitchController.switchToLanguage(lang)

  protected def mode: Mode = environment.mode

  private def loadConfig(key: String): String = runModeConfiguration.getOptional[String](key).getOrElse(throw new Exception
  (s"Missing configuration key: $key"))


  private def getConfigString(key: String) = servicesConfig.getConfString(key, throw new Exception(s"Could not find " +
    s"config '$key'"))
}
