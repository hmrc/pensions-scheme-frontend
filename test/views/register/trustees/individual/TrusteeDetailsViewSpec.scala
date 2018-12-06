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

package views.register.trustees.individual

import controllers.register.trustees.individual.routes._
import forms.register.PersonDetailsFormProvider
import models.person.PersonDetails
import models.{Index, NormalMode}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.QuestionViewBehaviours
import views.html.register.trustees.individual.trusteeDetails

class TrusteeDetailsViewSpec extends QuestionViewBehaviours[PersonDetails] {

  val messageKeyPrefix = "trusteeDetails"

  val firstIndex = Index(0)

  override val form = new PersonDetailsFormProvider()()

  def createView(isHubEnabled: Boolean = true): () => HtmlFormat.Appendable = () =>
    trusteeDetails(appConfig(isHubEnabled), form, NormalMode, firstIndex)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    trusteeDetails(frontendAppConfig, form, NormalMode, firstIndex)(fakeRequest, messages)


  "TrusteeDetails view with hub enabled" must {

    behave like normalPage(createView(), messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"))

    behave like pageWithReturnLink(createView(), controllers.register.routes.SchemeTaskListController.onPageLoad().url)

    behave like pageWithTextFields(
      createViewUsingForm,
      messageKeyPrefix,
      TrusteeDetailsController.onSubmit(NormalMode, firstIndex).url,
      "firstName", "middleName", "lastName"
    )

    behave like pageWithDateFields(createViewUsingForm, form)

    "not have a back link" in {
      val doc = asDocument(createView()())
      assertNotRenderedById(doc, "back-link")
    }

  }

  "TrusteeDetails view with hub disabled" must {
    behave like pageWithBackLink(createView(isHubEnabled = false))

    "not have a return link" in {
      val doc = asDocument(createView(isHubEnabled = false)())
      assertNotRenderedById(doc, "return-link")
    }
  }

}
