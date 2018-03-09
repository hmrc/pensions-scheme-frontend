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

package views.register.establishers.company.director

import play.api.data.Form
import controllers.register.establishers.company.director.routes
import forms.register.establishers.company.director.DirectorContactDetailsFormProvider
import models.register.establishers.company.director.{DirectorContactDetails}
import models.{Index, NormalMode}
import play.twirl.api.HtmlFormat
import views.behaviours.QuestionViewBehaviours
import views.html.register.establishers.company.director.directorContactDetails

class DirectorContactDetailsViewSpec extends QuestionViewBehaviours[DirectorContactDetails] {

  val messageKeyPrefix = "company_director_contact"
  val establisherIndex = Index(1)
  val directorIndex = Index(1)
  val directorName="test director name"

  override val form = new DirectorContactDetailsFormProvider()()

  def createView: () => HtmlFormat.Appendable = () => directorContactDetails(frontendAppConfig, form, NormalMode, establisherIndex,
    directorIndex,directorName)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) => directorContactDetails(frontendAppConfig,
    form, NormalMode, establisherIndex, directorIndex,directorName)(fakeRequest, messages)


  "CompanyContactDetails view" must {

    behave like normalPage(createView, messageKeyPrefix, messages("messages__company_director_contact__title"))

    behave like pageWithBackLink(createView)

    behave like pageWithTextFields(createViewUsingForm, messageKeyPrefix,
      routes.DirectorContactDetailsController.onSubmit(NormalMode, establisherIndex, directorIndex).url, "emailAddress", "phoneNumber")
  }
}
