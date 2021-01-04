/*
 * Copyright 2021 HM Revenue & Customs
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
import forms.register.PartnershipDetailsFormProvider
import models.{Index, NormalMode, PartnershipDetails}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.QuestionViewBehaviours
import views.html.register.establishers.partnership.partnershipDetails

class PartnershipDetailsViewSpec extends QuestionViewBehaviours[PartnershipDetails] {

  private val messageKeyPrefix = "partnershipName"

  override val form = new PartnershipDetailsFormProvider()()
  private val firstIndex = Index(1)

  private val submitUrl = controllers.register.establishers.partnership.routes.PartnershipDetailsController.onSubmit(NormalMode, firstIndex, None)

  val view: partnershipDetails = app.injector.instanceOf[partnershipDetails]

  private def createView(): () => HtmlFormat.Appendable = () =>
    view(form, NormalMode, firstIndex, None, submitUrl, None)(fakeRequest, messages)
  private def createUpdateView(): () => HtmlFormat.Appendable = () =>
    view(form, NormalMode, firstIndex, None, submitUrl, Some("srn"))(fakeRequest, messages)

  private def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    view(form, NormalMode, firstIndex, None, submitUrl, None)(fakeRequest, messages)

  "PartnershipDetails view" must {

    behave like normalPage(createView(), messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"))

    behave like pageWithErrorOutsideLabel(createViewUsingForm, messageKeyPrefix,
      routes.PartnershipDetailsController.onSubmit(NormalMode, firstIndex, None).url, "partnershipName")

    behave like pageWithReturnLink(createView(), getReturnLink)

    behave like pageWithReturnLinkAndSrn(createUpdateView(), getReturnLinkWithSrn)
  }
}
