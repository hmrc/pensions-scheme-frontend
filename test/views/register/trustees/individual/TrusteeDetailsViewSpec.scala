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

import forms.register.IndividualDetailsFormProvider
import models.{Index, NormalMode}
import models.person.PersonDetails
import play.api.data.Form
import views.behaviours.QuestionViewBehaviours
import controllers.register.trustees.individual.routes._
import views.html.register.trustees.individual.trusteeDetails

class TrusteeDetailsViewSpec extends QuestionViewBehaviours[PersonDetails] {

  val messageKeyPrefix = "trusteeDetails"

  val firstIndex = Index(0)

  override val form = new IndividualDetailsFormProvider()()

  def createView = () => trusteeDetails(frontendAppConfig, form, NormalMode, firstIndex)(fakeRequest, messages)

  def createViewUsingForm = (form: Form[_]) => trusteeDetails(frontendAppConfig, form, NormalMode, firstIndex)(fakeRequest, messages)


  "TrusteeDetails view" must {

    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"))

    behave like pageWithBackLink(createView)

    behave like pageWithTextFields(
      createViewUsingForm,
      messageKeyPrefix,
      TrusteeDetailsController.onSubmit(NormalMode, firstIndex).url,
      "firstName", "middleName", "lastName"
    )

    behave like pageWithDateFields(createViewUsingForm, form)

  }
}
