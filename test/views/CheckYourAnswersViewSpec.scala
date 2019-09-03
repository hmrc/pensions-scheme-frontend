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
import models.Mode.checkMode
import models.{Link, Mode, NormalMode}
import play.twirl.api.HtmlFormat
import viewmodels.{AnswerRow, AnswerSection, Section}
import views.behaviours.{CheckYourAnswersBehaviours, ViewBehaviours}
import views.html.check_your_answers_old

class CheckYourAnswersViewSpec extends CheckYourAnswersBehaviours with ViewBehaviours {

  private val messageKeyPrefix = "checkYourAnswers"

  private def emptyAnswerSections: Seq[AnswerSection] = Seq(AnswerSection(None, Seq(
    AnswerRow(
      messages("investmentRegulated.checkYourAnswersLabel"),
      Seq("site.yes"),
      answerIsMessageKey = true,
      Some(Link("site.change", routes.InvestmentRegulatedSchemeController.onPageLoad(checkMode(NormalMode)).url,
        Some(messages("messages__visuallyhidden__investmentRegulated"))))
    )
  )))

  private val srn = Some("S123")

  def createView(returnOverview: Boolean = false,
                 mode: Mode = NormalMode,
                 hideEditLinks: Boolean = false,
                 srn: Option[String] = None,
                 hideSaveAndContinueButton: Boolean
                ): () => HtmlFormat.Appendable = () =>
    check_your_answers_old(
      frontendAppConfig,
      emptyAnswerSections,
      routes.IndexController.onPageLoad(),
      None,
      returnOverview,
      mode,
      hideEditLinks,
      srn,
      hideSaveAndContinueButton
    )(fakeRequest, messages)

  def createViewWithData: (Seq[Section], Mode, Boolean) => HtmlFormat.Appendable = (sections, mode, viewOnly) =>
    check_your_answers_old(
      frontendAppConfig,
      sections,
      routes.IndexController.onPageLoad(),
      None,
      mode = mode,
      hideEditLinks = viewOnly,
      hideSaveAndContinueButton = viewOnly
    )(fakeRequest, messages)

  "check_your_answers_old view" must {

    behave like normalPageWithTitle(createView(hideSaveAndContinueButton = false),
      messageKeyPrefix, messages("checkYourAnswers.hs.title"), messages("checkYourAnswers.hs.heading"))

    behave like pageWithReturnChangeLink(createView(hideSaveAndContinueButton = false))

    behave like pageWithoutReturnChangeLink(createView(hideEditLinks = true, hideSaveAndContinueButton = true))

    behave like pageWithReturnLink(createView(hideSaveAndContinueButton = false), controllers.routes.SchemeTaskListController.onPageLoad(NormalMode, None).url)

    behave like pageWithReturnLink(createView(returnOverview = true, hideSaveAndContinueButton = true), frontendAppConfig.managePensionsSchemeOverviewUrl.url)

    behave like pageWithReturnLink(createView(returnOverview = false, UpdateMode, hideEditLinks = false, srn, hideSaveAndContinueButton = true),
      controllers.routes.SchemeTaskListController.onPageLoad(UpdateMode, srn).url)

    behave like pageWithoutSubmitButton(createView(hideEditLinks = true, hideSaveAndContinueButton = true))

    behave like pageWithSubmitButton(createView(hideEditLinks = true, hideSaveAndContinueButton = false))

    behave like checkYourAnswersPage(createViewWithData)
  }
}
