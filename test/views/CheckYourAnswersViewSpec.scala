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
import play.twirl.api.HtmlFormat
import viewmodels.Section
import views.behaviours.{CheckYourAnswersBehaviours, ViewBehaviours}
import views.html.check_your_answers

class CheckYourAnswersViewSpec extends CheckYourAnswersBehaviours with ViewBehaviours {

  private val messageKeyPrefix = "checkYourAnswers"

  private def emptyAnswerSections: Seq[Section] = Nil

  def createView(isHnsIterationTwoEnabled: Boolean): () => HtmlFormat.Appendable = () =>
    check_your_answers(
      frontendAppConfig,
      emptyAnswerSections,
      routes.IndexController.onPageLoad(),
      isHnsIterationTwoEnabled
    )(fakeRequest, messages)

  def createViewWithData: (Seq[Section]) => HtmlFormat.Appendable = (sections) =>
    check_your_answers(
      frontendAppConfig,
      sections,
      routes.IndexController.onPageLoad()
    )(fakeRequest, messages)

  "check_your_answers view with toggle On" must {
    behave like normalPageWithTitle(createView(isHnsIterationTwoEnabled = true),
      messageKeyPrefix, messages("checkYourAnswers.hs.title"), messages("checkYourAnswers.hs.heading"))

    behave like pageWithSubmitButton(createView(isHnsIterationTwoEnabled = true))

    behave like checkYourAnswersPage(createViewWithData)
  }

  "check_your_answers view with toggle Off" must {
    behave like normalPageWithTitle(createView(isHnsIterationTwoEnabled = false),
      messageKeyPrefix, messages("checkYourAnswers.title"), messages("checkYourAnswers.heading"))
  }

}
