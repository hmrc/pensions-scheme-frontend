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
import forms.register.establishers.individual.UniqueTaxReferenceFormProvider
import views.behaviours.YesNoViewBehaviours
import models.NormalMode
import views.html.register.establishers.individual.uniqueTaxReference

class UniqueTaxReferenceViewSpec extends YesNoViewBehaviours {

  val messageKeyPrefix = "establisher__has_sautr"

  val form = new UniqueTaxReferenceFormProvider()()

  def createView = () => uniqueTaxReference(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  def createViewUsingForm = (form: Form[_]) => uniqueTaxReference(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  "UniqueTaxReference view" must {

    behave like normalPage(createView, messageKeyPrefix, messages("messages__establisher__has_sautr__title"))

    behave like yesNoPage(createView = createViewUsingForm, messageKeyPrefix = messageKeyPrefix,
      expectedFormAction = routes.UniqueTaxReferenceController.onSubmit(NormalMode).url)
  }
}
