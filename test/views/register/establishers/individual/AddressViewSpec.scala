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
import forms.register.establishers.individual.AddressFormProvider
import models.NormalMode
import views.behaviours.StringViewBehaviours
import views.html.register.establishers.individual.address

class AddressViewSpec extends StringViewBehaviours {

  val messageKeyPrefix = "address"

  val form = new AddressFormProvider()()

  def createView = () => address(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  def createViewUsingForm = (form: Form[String]) => address(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  "Address view" must {
    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"))

    behave like pageWithBackLink(createView)

    behave like stringPage(createViewUsingForm, messageKeyPrefix, routes.AddressController.onSubmit(NormalMode).url)
  }
}
