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

import controllers.routes
import models.{Mode, NormalMode, UpdateMode}
import play.twirl.api.HtmlFormat
import viewmodels.Section
import views.behaviours.{CheckYourAnswersBehaviours, ViewBehaviours}
import views.html.check_your_answers

class CheckYourAnswersViewSpec extends CheckYourAnswersBehaviours with ViewBehaviours {

  private val messageKeyPrefix = "checkYourAnswers"

  private def emptyAnswerSections: Seq[Section] = Nil
  private val srn = "S123"

  def createView(returnOverview: Boolean = false, mode: Mode = NormalMode, srn: Option[String] = None): () => HtmlFormat.Appendable = () =>
    check_your_answers(
      frontendAppConfig,
      emptyAnswerSections,
      routes.IndexController.onPageLoad(),
      None,
      returnOverview,
      mode,
      srn
    )(fakeRequest, messages)

  def createViewWithData: (Seq[Section], Mode) => HtmlFormat.Appendable = (sections, mode) =>
    check_your_answers(
      frontendAppConfig,
      sections,
      routes.IndexController.onPageLoad(),
      None,
      mode = mode
    )(fakeRequest, messages)

  "check_your_answers view" must {

    behave like normalPageWithTitle(createView(), messageKeyPrefix, messages("checkYourAnswers.hs.title"), messages("checkYourAnswers.hs.heading"))

    behave like pageWithReturnLink(createView(), (controllers.routes.SchemeTaskListController.onPageLoad().url))

    behave like pageWithReturnLink(createView(returnOverview = true), frontendAppConfig.managePensionsSchemeOverviewUrl.url)

    behave like pageWithReturnLink(createView(returnOverview = false, UpdateMode, Some(srn)), controllers.routes.PSASchemeDetailsController.onPageLoad(srn).url)

    behave like checkYourAnswersPage(createViewWithData)
  }
}
