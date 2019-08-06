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

import controllers.register.trustees.individual.routes
import forms.register.PersonNameFormProvider
import models.person.PersonName
import models.{Index, NormalMode}
import org.joda.time.LocalDate
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.QuestionViewBehaviours
import views.html.register.trustees.individual.trusteeName

class TrusteeNameViewSpec extends QuestionViewBehaviours[PersonName] {

  val messageKeyPrefix = "trusteeName"

  val trusteeIndex = Index(1)
  private val postCall = routes.TrusteeNameController.onSubmit _

  override val form = new PersonNameFormProvider()()

  def createView(): () => HtmlFormat.Appendable = () =>
    trusteeName(frontendAppConfig, form, NormalMode, trusteeIndex, None,
      postCall(NormalMode, trusteeIndex, None), None)(fakeRequest, messages)
  def createUpdateView(): () => HtmlFormat.Appendable = () =>
    trusteeName(frontendAppConfig, form, NormalMode, trusteeIndex, None,
      postCall(NormalMode, trusteeIndex, None), Some("srn"))(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    trusteeName(frontendAppConfig, form, NormalMode, trusteeIndex, None,
      postCall(NormalMode, trusteeIndex, None), None)(fakeRequest, messages)

  private val day = LocalDate.now().getDayOfMonth
  private val year = LocalDate.now().getYear
  private val month = LocalDate.now().getMonthOfYear

  val validData: Map[String, String] = Map(
    "firstName" -> "testFirstName",
    "lastName" -> "testLastName"
  )

  "TrusteeName view" must {

    behave like normalPage(createView(), messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"))

    behave like pageWithTextFields(createViewUsingForm, messageKeyPrefix,
      controllers.register.trustees.individual.routes.TrusteeNameController.onSubmit(NormalMode, trusteeIndex, None).url,
      "firstName", "lastName")
    
    behave like pageWithReturnLink(createView(), getReturnLink)

    behave like pageWithReturnLinkAndSrn(createUpdateView(), getReturnLinkWithSrn)
  }
}
