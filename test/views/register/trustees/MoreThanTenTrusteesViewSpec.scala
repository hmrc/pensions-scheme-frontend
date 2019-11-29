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

package views.register.trustees

import controllers.register.trustees.routes
import forms.register.trustees.MoreThanTenTrusteesFormProvider
import models.{NormalMode, UpdateMode}
import play.api.data.Form
import views.behaviours.YesNoViewBehaviours
import views.html.register.trustees.moreThanTenTrustees

class MoreThanTenTrusteesViewSpec extends YesNoViewBehaviours {

  val messageKeyPrefix = "moreThanTenTrustees"
  val form = new MoreThanTenTrusteesFormProvider()()
  val submitUrl = controllers.register.trustees.routes.MoreThanTenTrusteesController.onSubmit(NormalMode, None)
  private def createView() = () => moreThanTenTrustees(
    frontendAppConfig, form, NormalMode, None, submitUrl, None)(fakeRequest, messages)
  private def createUpdateView = () => moreThanTenTrustees(
    frontendAppConfig, form, UpdateMode, None, submitUrl, Some("srn"))(fakeRequest, messages)

  private def createViewUsingForm = (form: Form[_]) => moreThanTenTrustees(
    frontendAppConfig, form, NormalMode, None, submitUrl, None)(fakeRequest, messages)

  "MoreThanTenTrustees view" must {

    behave like normalPage(createView(), messageKeyPrefix, messages("messages__moreThanTenTrustees__heading"))

    behave like yesNoPageWithMandatoryHint(createViewUsingForm, messageKeyPrefix, routes.MoreThanTenTrusteesController.onSubmit(NormalMode, None).url,
      expectedHint = messages("messages__moreThanTenTrustees__hint"))

    behave like pageWithReturnLink(createView(), getReturnLink)

    behave like pageWithReturnLinkAndSrn(createUpdateView, getReturnLinkWithSrn)
  }
}
