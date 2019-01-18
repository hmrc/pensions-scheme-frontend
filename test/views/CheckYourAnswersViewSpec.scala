/*
 * Copyright 2019 HM Revenue & Customs
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

package views

import config.FrontendAppConfig
import controllers.routes
import play.api.inject.guice.GuiceApplicationBuilder
import play.twirl.api.HtmlFormat
import viewmodels.Section
import views.behaviours.{CheckYourAnswersBehaviours, ViewBehaviours}
import views.html.check_your_answers

class CheckYourAnswersViewSpec extends CheckYourAnswersBehaviours with ViewBehaviours {

  private val messageKeyPrefix = "checkYourAnswers"

  private def emptyAnswerSections: Seq[Section] = Nil

  private lazy val frontendAppConfigWithHubEnabled = appConfig(isHubEnabled=true)

  private lazy val frontendAppConfigWithoutHubEnabled = appConfig(isHubEnabled=false)
  
  def createView(appConfig : FrontendAppConfig, returnOverview : Boolean = false): () => HtmlFormat.Appendable = () =>
    check_your_answers(
      appConfig,
      emptyAnswerSections,
      routes.IndexController.onPageLoad(),
      None,
      returnOverview
    )(fakeRequest, messages)

  def createViewWithData: (Seq[Section]) => HtmlFormat.Appendable = (sections) =>
    check_your_answers(
      frontendAppConfig,
      sections,
      routes.IndexController.onPageLoad(),
      None
    )(fakeRequest, messages)

  "check_your_answers view with toggle On" must {

    behave like normalPageWithTitle(createView(appConfig = frontendAppConfigWithHubEnabled),
      messageKeyPrefix, messages("checkYourAnswers.hs.title"), messages("checkYourAnswers.hs.heading"))

    behave like pageWithSubmitButton(createView(appConfig = frontendAppConfigWithHubEnabled))

    behave like pageWithReturnLink(
      createView(appConfig = frontendAppConfigWithHubEnabled), controllers.routes.SchemeTaskListController.onPageLoad().url)

    behave like pageWithReturnLink(
      createView(frontendAppConfigWithHubEnabled, returnOverview = true), frontendAppConfig.managePensionsSchemeOverviewUrl.url)

    behave like checkYourAnswersPage(createViewWithData)
  }

  "check_your_answers view with toggle Off" must {

    behave like pageWithReturnLink(
      createView(frontendAppConfigWithoutHubEnabled), controllers.register.routes.SchemeTaskListController.onPageLoad().url)

    behave like normalPageWithTitle(createView(frontendAppConfigWithoutHubEnabled),
      messageKeyPrefix, messages("checkYourAnswers.title"), messages("checkYourAnswers.heading"))
  }

}
