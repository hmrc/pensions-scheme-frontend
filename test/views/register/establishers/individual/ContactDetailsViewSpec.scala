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

package views.register.establishers.individual

import play.api.data.Form
import controllers.register.establishers.individual.routes
import forms.register.establishers.individual.ContactDetailsFormProvider
import models.register.establishers.individual.ContactDetails
import models.{Index, NormalMode}
import play.twirl.api.HtmlFormat
import views.behaviours.QuestionViewBehaviours
import views.html.register.establishers.individual.contactDetails

class ContactDetailsViewSpec extends QuestionViewBehaviours[ContactDetails] {

  val messageKeyPrefix = "establisher_individual_contact_details"
  val index = Index(1)
  val establisherName = "test name"
  override val form = new ContactDetailsFormProvider()()

  def createView: () => HtmlFormat.Appendable = () => contactDetails(frontendAppConfig, form, NormalMode,
    index, establisherName)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) => contactDetails(frontendAppConfig,
    form, NormalMode, index, establisherName)(fakeRequest, messages)


  "ContactDetails view" must {

    behave like normalPage(createView, messageKeyPrefix, messages("messages__establisher_individual_contact_details__title"))

    behave like pageWithTextFields(createViewUsingForm, messageKeyPrefix, routes.ContactDetailsController.onSubmit(NormalMode, index).url,
      "emailAddress", "phoneNumber")
  }
}
