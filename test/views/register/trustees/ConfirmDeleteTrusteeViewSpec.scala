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

import forms.register.trustees.ConfirmDeleteTrusteeFormProvider
import models.register.trustees.TrusteeKind.Company
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.ViewSpecBase
import views.behaviours.YesNoViewBehaviours
import views.html.register.trustees.confirmDeleteTrustee

class ConfirmDeleteTrusteeViewSpec extends YesNoViewBehaviours {

  import ConfirmDeleteTrusteeViewSpec._

  override val form: Form[Boolean] = formLocal

  "ConfirmDeleteTrustee view with hub enabled" must {

    behave like normalPage(createView(), messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__heading", trusteeName))

    behave like yesNoPage(createView = createViewUsingForm, messageKeyPrefix = messageKeyPrefix,
      expectedFormAction = postCall.url)

    behave like pageWithReturnLink(createView(), getReturnLink)

  }
}

object ConfirmDeleteTrusteeViewSpec extends ViewSpecBase {

  private val messageKeyPrefix = "confirmDeleteTrustee"
  private val trusteeName = "test-trustee-name"

  val formLocal = new ConfirmDeleteTrusteeFormProvider()()

  val postCall = controllers.register.trustees.routes.ConfirmDeleteTrusteeController.onSubmit(0, Company)

  private def createView() =
    () => confirmDeleteTrustee(
      frontendAppConfig,
      formLocal,
      trusteeName,
      postCall
    )(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    confirmDeleteTrustee(
      frontendAppConfig,
      form,
      trusteeName,
      postCall
    )(fakeRequest, messages)

}
