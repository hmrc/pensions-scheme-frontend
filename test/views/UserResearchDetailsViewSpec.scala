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

package views

import forms.UserResearchDetailsFormProvider
import models.UserResearchDetails
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.QuestionViewBehaviours
import views.html.userResearchDetails

class UserResearchDetailsViewSpec extends QuestionViewBehaviours[UserResearchDetails] {

  val messageKeyPrefix = "userResearchDetails"

  override val form = new UserResearchDetailsFormProvider()()

  val view: userResearchDetails = app.injector.instanceOf[userResearchDetails]

  def createView: () => HtmlFormat.Appendable = () => view(form)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) => view(form)(fakeRequest, messages)

  "UserResearchContactDetails view" must {

    behave like normalPage(
      createView,
      messageKeyPrefix,
      messages(s"messages__${messageKeyPrefix}__heading"),
      s"_p1",
      s"_p2",
      s"_p3"
    )

    behave like pageWithBackLink(createView)

    behave like pageWithErrorOutsideLabel(
      createViewUsingForm,
      messageKeyPrefix,
      controllers.routes.UserResearchDetailsController.onSubmit().url,
      "name",
      "email"
    )
  }
}
