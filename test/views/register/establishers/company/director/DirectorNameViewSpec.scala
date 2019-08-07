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

package views.register.establishers.company.director

import controllers.register.establishers.company.director.routes
import forms.register.{PersonDetailsFormProvider, PersonNameFormProvider}
import models.person.{PersonDetails, PersonName}
import models.{Index, NormalMode}
import org.joda.time.LocalDate
import play.api.data.{Form, FormError}
import play.twirl.api.HtmlFormat
import views.behaviours.QuestionViewBehaviours
import views.html.register.establishers.company.director.directorName

class DirectorNameViewSpec extends QuestionViewBehaviours[PersonName] {

  val messageKeyPrefix = "directorName"

  val establisherIndex = Index(1)
  val directorIndex = Index(1)
  private val postCall = routes.DirectorNameController.onSubmit _

  override val form = new PersonNameFormProvider()("messages__error__director")

  def createView(): () => HtmlFormat.Appendable = () =>
    directorName(frontendAppConfig, form, NormalMode, establisherIndex, directorIndex, None,
      postCall(NormalMode, establisherIndex, directorIndex, None), None)(fakeRequest, messages)
  def createUpdateView(): () => HtmlFormat.Appendable = () =>
    directorName(frontendAppConfig, form, NormalMode, establisherIndex, directorIndex, None,
      postCall(NormalMode, establisherIndex, directorIndex, None), Some("srn"))(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    directorName(frontendAppConfig, form, NormalMode, establisherIndex, directorIndex, None,
      postCall(NormalMode, establisherIndex, directorIndex, None), None)(fakeRequest, messages)

  val validData: Map[String, String] = Map(
    "firstName" -> "testFirstName",
    "lastName" -> "testLastName"
  )

  "DirectorName view" must {

    behave like normalPage(createView(), messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"))

    behave like pageWithTextFields(createViewUsingForm, messageKeyPrefix,
      controllers.register.establishers.company.director.routes.DirectorNameController.onSubmit(NormalMode, establisherIndex, directorIndex, None).url,
      "firstName", "lastName")
    
    behave like pageWithReturnLink(createView(), getReturnLink)

    behave like pageWithReturnLinkAndSrn(createUpdateView(), getReturnLinkWithSrn)
  }
}
