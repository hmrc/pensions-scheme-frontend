/*
 * Copyright 2020 HM Revenue & Customs
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

package views.register.establishers.partnership

import controllers.register.establishers.partnership.routes
import forms.register.establishers.partnership.OtherPartnersFormProvider
import models.{Index, NormalMode}
import play.api.data.Form
import views.behaviours.YesNoViewBehaviours
import views.html.register.establishers.partnership.otherPartners

class OtherPartnersViewSpec extends YesNoViewBehaviours {

  val index = Index(1)

  val messageKeyPrefix = "otherPartners"

  val form = new OtherPartnersFormProvider()()
  val submitUrl = controllers.register.establishers.partnership.routes.OtherPartnersController.onSubmit(NormalMode, index, None)
  private def createView() = () =>
    otherPartners(frontendAppConfig, form, NormalMode, index, None, submitUrl, None)(fakeRequest, messages)
  private def createUpdateView() = () =>
    otherPartners(frontendAppConfig, form, NormalMode, index, None, submitUrl, Some("srn"))(fakeRequest, messages)

  private def createViewUsingForm = (form: Form[_]) =>
    otherPartners(frontendAppConfig, form, NormalMode, index, None, submitUrl, None)(fakeRequest, messages)

  "OtherPartners view" must {

    behave like normalPage(
      createView(),
      messageKeyPrefix,
      messages("messages__otherPartners__heading")
    )

    behave like yesNoPageLegendWithH1(createViewUsingForm,
      messageKeyPrefix,
      routes.OtherPartnersController.onSubmit(NormalMode, index, None).url,
      legend= messages("messages__otherPartners__heading"),
      expectedHintKey = Some(messages("messages__otherPartners__lede"))
    )

    behave like pageWithSubmitButton(createView())

    behave like pageWithReturnLink(createView(), getReturnLink)

    behave like pageWithReturnLinkAndSrn(createUpdateView(), getReturnLinkWithSrn)

  }
}
