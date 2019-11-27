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

package views.register.establishers.partnership.partner

import controllers.register.establishers.partnership.partner.routes.ConfirmDeletePartnerController
import forms.register.establishers.partnership.partner.ConfirmDeletePartnerFormProvider
import models.NormalMode
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.register.establishers.partnership.partner.confirmDeletePartner

class ConfirmDeletePartnerViewSpec extends YesNoViewBehaviours{

  val messageKeyPrefix = "confirmDeletePartner"

  private val partnerName = "John Doe"
  private val postCall = ConfirmDeletePartnerController.onSubmit(NormalMode, establisherIndex = 0, partnerIndex = 0, None)

  val form = new ConfirmDeletePartnerFormProvider()()

  private def createView() = () =>
    confirmDeletePartner(frontendAppConfig, form, partnerName, postCall, None)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    confirmDeletePartner(frontendAppConfig, form, partnerName, postCall, None)(fakeRequest, messages)

  "ConfirmDeleteDirector view" must {

    behave like normalPage(createView(), messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__heading", partnerName))

    behave like yesNoPageWithLegend(createView = createViewUsingForm, messageKeyPrefix = messageKeyPrefix,
      expectedFormAction = postCall.url)

    behave like pageWithReturnLink(createView(), getReturnLink)

  }
}
