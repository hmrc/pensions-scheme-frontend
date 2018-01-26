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
import forms.register.establishers.individual.PreviousAddressFormProvider
import models.NormalMode
import views.behaviours.StringViewBehaviours
import views.html.register.establishers.individual.previousAddress

class PreviousAddressViewSpec extends StringViewBehaviours {

  val messageKeyPrefix = "previousAddress"

  val form = new PreviousAddressFormProvider()()

  def createView = () => previousAddress(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  def createViewUsingForm = (form: Form[String]) => previousAddress(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  "PreviousAddress view" must {
    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__heading"))

    behave like pageWithBackLink(createView)

    behave like stringPage(createViewUsingForm, messageKeyPrefix, controllers.register.establishers.individual.routes.PreviousAddressController.onSubmit(NormalMode).url)
  }
}
