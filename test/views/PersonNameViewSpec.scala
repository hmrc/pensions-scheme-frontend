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

package views

import controllers.register.establishers.company.director.routes
import forms.register.PersonNameFormProvider
import models.person.PersonName
import models.{Index, Mode, NormalMode, UpdateMode}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.behaviours.QuestionViewBehaviours
import views.html.personName

class PersonNameViewSpec extends QuestionViewBehaviours[PersonName] {

  val messageKeyPrefix = "directorName"

  val establisherIndex = Index(1)
  val directorIndex = Index(1)
  private def viewmodel(mode: Mode = NormalMode, srn: Option[String] = None) = CommonFormWithHintViewModel(
    routes.DirectorNameController.onSubmit(NormalMode, establisherIndex, directorIndex, None),
    title = Message("messages__directorName__title"),
    heading = Message("messages__directorName__heading"),
    srn = srn
  )

  override val form = new PersonNameFormProvider()("messages__error__director")

  val view: personName = app.injector.instanceOf[personName]

  def createView(): () => HtmlFormat.Appendable = () =>
    view(form, viewmodel(), None)(fakeRequest, messages)

  def createUpdateView(): () => HtmlFormat.Appendable = () =>
    view(form, viewmodel(UpdateMode, Some("srn")), Some("srn"))(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    view(form, viewmodel(), None)(fakeRequest, messages)

  val validData: Map[String, String] = Map(
    "firstName" -> "testFirstName",
    "lastName" -> "testLastName"
  )

  "DirectorName view" must {

    behave like normalPage(createView(), messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"))

    behave like pageWithErrorOutsideLabel(createViewUsingForm, messageKeyPrefix,
      controllers.register.establishers.company.director.routes.DirectorNameController.onSubmit(NormalMode, establisherIndex, directorIndex, None).url,
      "firstName", "lastName")
    
    behave like pageWithReturnLink(createView(), getReturnLink)

    behave like pageWithReturnLinkAndSrn(createUpdateView(), getReturnLinkWithSrn)
  }
}
