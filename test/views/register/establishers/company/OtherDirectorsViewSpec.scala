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

package views.register.establishers.company

import controllers.register.establishers.company.routes
import forms.register.establishers.company.OtherDirectorsFormProvider
import models.{Index, NormalMode}
import play.api.data.Form
import views.behaviours.YesNoViewBehaviours
import views.html.register.establishers.company.otherDirectors

class OtherDirectorsViewSpec extends YesNoViewBehaviours {

  val index = Index(1)

  val messageKeyPrefix = "otherDirectors"

  val form = new OtherDirectorsFormProvider()()

  def createView = () => otherDirectors(frontendAppConfig, form, NormalMode, index)(fakeRequest, messages)

  def createViewUsingForm = (form: Form[_]) => otherDirectors(frontendAppConfig, form, NormalMode, index)(fakeRequest, messages)

  "OtherDirectors view" must {

    behave like normalPage(
      createView,
      messageKeyPrefix,
      messages("messages__otherDirectors__heading")
    )

    behave like pageWithBackLink(createView)

    behave like yesNoPage(createViewUsingForm,
      messageKeyPrefix,
      routes.OtherDirectorsController.onSubmit(NormalMode, index).url,
      expectedHintKey = Some("_lede")
    )

    behave like pageWithSubmitButton(createView)
  }
}
