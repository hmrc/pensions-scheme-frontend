/*
 * Copyright 2020 HM Revenue & Customs
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
import play.api.i18n.Lang
import play.api.mvc.Call
import play.api.{Configuration, Environment, Mode}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class FrontendAppConfig @Inject()(runModeConfiguration: Configuration, environment: Environment, servicesConfig: ServicesConfig){

  protected def mode: Mode = environment.mode

  private def loadConfig(key: String) = runModeConfiguration.getOptional[String](key).getOrElse(throw new Exception(s"Missing configuration key: $key"))

  private lazy val contactHost = runModeConfiguration.getOptional[String]("contact-frontend.host").getOrElse("")
  private val contactFormServiceIdentifier = "pensionsschemefrontend"
  lazy val managePensionsSchemeOverviewUrl : Call = Call("GET",loadConfig("urls.manage-pensions-frontend.schemesOverview"))
  lazy val managePensionsSchemeSummaryUrl: String = loadConfig("urls.manage-pensions-frontend.schemesSummary")
  lazy val managePensionsYourPensionSchemesUrl: String = loadConfig("urls.manage-pensions-frontend.yourPensionSchemes")
  lazy val appName: String = runModeConfiguration.underlying.getString("appName")
  lazy val googleTagManagerIdAvailable: Boolean = runModeConfiguration.underlying.getBoolean(s"google-tag-manager.id-available")
  lazy val googleTagManagerId: String = loadConfig(s"google-tag-manager.id")
  lazy val reportAProblemPartialUrl = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  lazy val reportAProblemNonJSUrl = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  lazy val betaFeedbackUnauthenticatedUrl = s"$contactHost/contact/beta-feedback-unauthenticated"

  lazy val pensionsSchemeUrl = s"${servicesConfig.baseUrl("pensions-scheme")}"
  lazy val pensionsAdministratorUrl = s"${servicesConfig.baseUrl("pension-administrator")}"

  lazy val loginUrl: String = loadConfig("urls.login")
  lazy val serviceSignOut: String = loadConfig("urls.logout")
  lazy val registerSchemeUrl: String = pensionsSchemeUrl +
    runModeConfiguration.underlying.getString("urls.registerScheme")
  lazy val registerSchemeAdministratorUrl: String = loadConfig("urls.registerSchemeAdministrator")
  lazy val pensionAdministratorGovUkLink: String = runModeConfiguration.underlying.getString("urls.pensionAdministratorGovUkLink")
  lazy val pensionPractitionerGovUkLink: String = runModeConfiguration.underlying.getString("urls.pensionPractitionerGovUkLink")
  lazy val govUkLink: String = runModeConfiguration.underlying.getString("urls.govUkLink")
  lazy val appealLink: String = runModeConfiguration.underlying.getString("urls.appealLink")
  lazy val pensionsRegulatorLink: String = runModeConfiguration.underlying.getString("urls.pensionsRegulatorLink")
  lazy val getPSAEmail: String = runModeConfiguration.underlying.getString("urls.get-psa-email")
  lazy val getPSAName: String = runModeConfiguration.underlying.getString("urls.get-psa-name")
  lazy val minimalPsaDetailsUrl: String = pensionsAdministratorUrl + runModeConfiguration.underlying.getString("urls.minimalPsaDetails")
  lazy val checkAssociationUrl: String = s"$pensionsSchemeUrl${runModeConfiguration.underlying.getString("urls.checkPsaAssociation")}"

  def languageMap: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy"))

  def routeToSwitchLanguage: String => Call = (lang: String) => routes.LanguageSwitchController.switchToLanguage(lang)

  lazy val locationCanonicalList: String = loadConfig("location.canonical.list")
  lazy val addressLookUp = s"${servicesConfig.baseUrl("address-lookup")}"
  lazy val maxDirectors: Int = loadConfig("company.maxDirectors").toInt
  lazy val maxTrustees: Int = loadConfig("maxTrustees").toInt
  lazy val maxPartners: Int = loadConfig("maxPartners").toInt
  lazy val daysDataSaved: Int = loadConfig("daysDataSaved").toInt
  lazy val emailApiUrl: String = s"${servicesConfig.baseUrl("email")}"
  lazy val emailTemplateId: String = loadConfig("email.templateId")
  lazy val emailSendForce: Boolean = runModeConfiguration.getOptional[Boolean]("email.force").getOrElse(false)
  lazy val schemeDetailsUrl: String = s"${servicesConfig.baseUrl("pensions-scheme")}${runModeConfiguration.underlying.getString("urls.schemeDetails")}"
  lazy val updateSchemeDetailsUrl: String = s"${servicesConfig.baseUrl("pensions-scheme")}${runModeConfiguration
    .underlying.getString("urls.updateSchemeDetails")}"

  //FEATURES
  lazy val languageTranslationEnabled:Boolean = runModeConfiguration.getOptional[Boolean]("features.welsh-translation").getOrElse(true)
}
