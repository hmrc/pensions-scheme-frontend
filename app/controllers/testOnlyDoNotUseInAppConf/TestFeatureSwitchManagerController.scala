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

package controllers.testOnlyDoNotUseInAppConf

import com.google.inject.Inject
import config.FeatureSwitchManagementService
import connectors.{PensionAdministratorFeatureSwitchConnectorImpl, PensionsSchemeFeatureSwitchConnectorImpl}
import play.api.Logger
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.testOnlyDoNotUseInAppConf.testFeatureSwitchManagerSuccess

import scala.concurrent.ExecutionContext

class TestFeatureSwitchManagerController @Inject()(
                                                    fs: FeatureSwitchManagementService,
                                                    schemeFeatureSwitchConnector: PensionsSchemeFeatureSwitchConnectorImpl,
                                                    adminFeatureSwitchConnector: PensionAdministratorFeatureSwitchConnectorImpl,
                                                    val controllerComponents: MessagesControllerComponents,
                                                    val view: testFeatureSwitchManagerSuccess
                                                  )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController {

  def toggleOn(featureSwitch: String): Action[AnyContent] = Action.async {
    implicit request =>
      val frontEndToggledOn = fs.change(featureSwitch, newValue = true)
      if (frontEndToggledOn) {
        Logger.debug(s"[Pensions-Scheme-frontend][ToggleOnSuccess] - $featureSwitch")
      } else {
        Logger.debug(s"[Pensions-Scheme-frontend][ToggleOnFailed] - $featureSwitch")
      }
      for {
        _ <- schemeFeatureSwitchConnector.toggleOn(featureSwitch)
        _ <- adminFeatureSwitchConnector.toggleOn(featureSwitch)
        schemeCurrentValue <- schemeFeatureSwitchConnector.get(featureSwitch)
        adminCurrentValue <- adminFeatureSwitchConnector.get(featureSwitch)
      } yield {
        getView(featureSwitch, newToggleState = true, Option(fs.get(featureSwitch)), schemeCurrentValue,
          adminCurrentValue)
      }
  }

  def toggleOff(featureSwitch: String): Action[AnyContent] = Action.async {
    implicit request =>
      val frontEndToggledOff = fs.change(featureSwitch, newValue = false)
      if (frontEndToggledOff) {
        Logger.debug(s"[Pensions-Scheme-frontend][ToggleOffSuccess] - $featureSwitch")
      } else {
        Logger.debug(s"[Pensions-Scheme-frontend][ToggleOffFailed] - $featureSwitch")
      }
      for {
        _ <- schemeFeatureSwitchConnector.toggleOff(featureSwitch)
        _ <- adminFeatureSwitchConnector.toggleOff(featureSwitch)
        schemeCurrentValue <- schemeFeatureSwitchConnector.get(featureSwitch)
        adminCurrentValue <- adminFeatureSwitchConnector.get(featureSwitch)
      } yield {
        getView(featureSwitch, newToggleState = false, Option(fs.get(featureSwitch)), schemeCurrentValue,
          adminCurrentValue)
      }
  }

  private def getView(featureSwitch: String,
                      newToggleState: Boolean,
                      frontEndState: Option[Boolean],
                      schemeState: Option[Boolean],
                      adminState: Option[Boolean])(implicit request: Request[_]) = {
    if (frontEndState.contains(newToggleState) && schemeState.contains(newToggleState) && adminState.contains
    (newToggleState)) {
      Ok(view(
        """Request to set feature switch successful.""",
        s"""Current values of feature switch "$featureSwitch":-""",
        successMessage(featureSwitch, newToggleState),
        switches(featureSwitch, frontEndState, schemeState, adminState)))
    } else {
      ExpectationFailed(view(
        """Request to set feature switch unsuccessful.""",
        s"""Current values of feature switch "$featureSwitch":-""",
        failureMessage(featureSwitch, newToggleState),
        switches(featureSwitch, frontEndState, schemeState, adminState)))
    }
  }

  private def switches(featureSwitch: String,
                       frontendState: Option[Boolean],
                       schemeState: Option[Boolean],
                       adminState: Option[Boolean]): Map[String, String] = Map(
    "pensions-scheme-frontend" -> onOrOff(frontendState),
    "pensions-scheme" -> onOrOff(schemeState),
    "pension-administrator" -> onOrOff(adminState)
  )

  private def onOrOff(state: Option[Boolean]): String = state match {
    case None => "unknown"
    case Some(true) => "true"
    case _ => "false"
  }

  private def successMessage(featureSwitch: String, newToggleState: Boolean) =
    s"""Feature switch "$featureSwitch" successfully set to ${onOrOff(Option(newToggleState))} in all services listed
       | below.""".stripMargin

  private def failureMessage(featureSwitch: String, newToggleState: Boolean) =
    s"""Unable to set feature switch "$featureSwitch" to ${onOrOff(Option(newToggleState))} in all services listed
       |below.""".stripMargin

  def reset(featureSwitch: String): Action[AnyContent] = Action.async { implicit request =>
    fs.reset(featureSwitch)
    for {
      _ <- schemeFeatureSwitchConnector.reset(featureSwitch)
      _ <- adminFeatureSwitchConnector.reset(featureSwitch)
      schemeCurrentValue <- schemeFeatureSwitchConnector.get(featureSwitch)
      adminCurrentValue <- adminFeatureSwitchConnector.get(featureSwitch)
    } yield {
      getResetView(featureSwitch, Option(fs.get(featureSwitch)), schemeCurrentValue, adminCurrentValue)
    }
  }

  private def getResetView(featureSwitch: String,
                           frontEndState: Option[Boolean],
                           schemeState: Option[Boolean],
                           adminState: Option[Boolean])(implicit request: Request[_]) = {

    Ok(view(
      """Request to reset feature switch successful.""",
      s"""Current values of feature switch "$featureSwitch":-""",
      s"""$featureSwitch has been reset""",
      switches(featureSwitch, frontEndState, schemeState, adminState))
    )
  }
}
