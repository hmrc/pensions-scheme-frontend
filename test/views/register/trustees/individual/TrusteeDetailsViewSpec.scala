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

package views.register.trustees.individual

import controllers.register.trustees.individual.routes._
import forms.register.PersonDetailsFormProvider
import models.person.PersonDetails
import models.{Index, NormalMode, UpdateMode}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.QuestionViewBehaviours
import views.html.register.trustees.individual.trusteeDetails

class TrusteeDetailsViewSpec extends QuestionViewBehaviours[PersonDetails] {

  val messageKeyPrefix = "trusteeDetails"

  val firstIndex = Index(0)

  override val form = new PersonDetailsFormProvider()()
  val submitUrl = controllers.register.trustees.individual.routes.TrusteeDetailsController.onSubmit(NormalMode, firstIndex, None)
  def createView(): () => HtmlFormat.Appendable = () => trusteeDetails(
    frontendAppConfig, form, NormalMode, firstIndex, None, submitUrl, None)(fakeRequest, messages)
  def createUpdateView: () => HtmlFormat.Appendable = () => trusteeDetails(
    frontendAppConfig, form, UpdateMode, firstIndex, None, submitUrl, Some("srn"))(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    trusteeDetails(frontendAppConfig, form, NormalMode, firstIndex, None, submitUrl, None)(fakeRequest, messages)


  "TrusteeDetails view" must {

    behave like normalPage(createView(), messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"))

    behave like pageWithReturnLink(createView(), getReturnLink)

    behave like pageWithReturnLinkAndSrn(createUpdateView, getReturnLinkWithSrn)

    behave like pageWithTextFields(
      createViewUsingForm,
      messageKeyPrefix,
      TrusteeDetailsController.onSubmit(NormalMode, firstIndex, None).url,
      "firstName", "middleName", "lastName"
    )

    behave like pageWithDateFields(createViewUsingForm, form)
  }
}
