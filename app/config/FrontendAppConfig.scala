/*
 * Copyright 2018 HM Revenue & Customs
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
import play.api.{Configuration, Environment}
import play.api.i18n.Lang
import controllers.routes
import play.api.mvc.Call
import uk.gov.hmrc.play.config.ServicesConfig

@Singleton
class FrontendAppConfig @Inject() (override val runModeConfiguration: Configuration, environment: Environment) extends ServicesConfig {

  override protected def mode = environment.mode

  private def loadConfig(key: String) = runModeConfiguration.getString(key).getOrElse(throw new Exception(s"Missing configuration key: $key"))
  private def loadConfigOrDefault(key: String, default: String) = runModeConfiguration.getString(key).getOrElse(default)

  private lazy val contactHost = runModeConfiguration.getString("contact-frontend.host").getOrElse("")
  private val contactFormServiceIdentifier = "pensionsschemefrontend"

  lazy val appName: String = runModeConfiguration.underlying.getString("appName")
  lazy val analyticsToken = loadConfig(s"google-analytics.token")
  lazy val analyticsHost = loadConfig(s"google-analytics.host")
  lazy val reportAProblemPartialUrl = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  lazy val reportAProblemNonJSUrl = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  lazy val betaFeedbackUrl = s"$contactHost/contact/beta-feedback"
  lazy val betaFeedbackUnauthenticatedUrl = s"$contactHost/contact/beta-feedback-unauthenticated"

  lazy val authUrl = baseUrl("auth")
  lazy val pensionsSchemeUrl = baseUrl("pensions-scheme")

  lazy val loginUrl = loadConfig("urls.login")
  lazy val loginContinueUrl = loadConfig("urls.loginContinue")
  lazy val serviceSignOut = loadConfig("urls.logout")
  lazy val registerSchemeUrl: String =  pensionsSchemeUrl +
    runModeConfiguration.underlying.getString("urls.registerScheme")
  lazy val registerSchemeAdministratorUrl: String = loadConfig("urls.registerSchemeAdministrator")
  lazy val listOfSchemesUrl: String = pensionsSchemeUrl +
    runModeConfiguration.underlying.getString("urls.listOfSchemes")
  lazy val pensionSchemeOnlineServiceUrl: String = loadConfig("urls.pensionSchemeOnlineService")
  lazy val pensionAdministratorGovUkLink = runModeConfiguration.underlying.getString("urls.pensionAdministratorGovUkLink")
  lazy val pensionPractitionerGovUkLink = runModeConfiguration.underlying.getString("urls.pensionPractitionerGovUkLink")
  lazy val govUkLink = runModeConfiguration.underlying.getString("urls.govUkLink")
  lazy val languageTranslationEnabled = runModeConfiguration.getBoolean("features.welsh-translation").getOrElse(true)
  def languageMap: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy"))

  def routeToSwitchLanguage: (String => Call) = (lang: String) => routes.LanguageSwitchController.switchToLanguage(lang)
  lazy val locationCanonicalList = loadConfig("location.canonical.list")
  lazy val addressLookUp = baseUrl("address-lookup")
  lazy val maxDirectors: Int = loadConfig("company.maxDirectors").toInt
  lazy val maxTrustees: Int = loadConfig("maxTrustees").toInt
  lazy val maxPartners: Int = loadConfig("maxPartners").toInt
  lazy val daysDataSaved: Int = loadConfig("daysDataSaved").toInt
  lazy val allowPartnerships: Boolean = loadConfigOrDefault("features.allowPartnerships", "false").toBoolean
  lazy val allowMasterTrust: Boolean = loadConfigOrDefault("features.allowMasterTrust", "false").toBoolean
  lazy val completeFlagEnabled: Boolean = runModeConfiguration.getBoolean("features.is-complete").getOrElse(true)
  lazy val emailApiUrl: String = baseUrl("email")
  lazy val emailTemplateId: String = loadConfig("email.templateId")
  lazy val emailSendForce: Boolean = runModeConfiguration.getBoolean("email.force").getOrElse(false)
}
