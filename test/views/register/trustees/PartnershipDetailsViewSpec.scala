/*
 * Copyright 2022 HM Revenue & Customs
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

import controllers.register.trustees.partnership._
import forms.register.PartnershipDetailsFormProvider
import models.{Index, NormalMode, PartnershipDetails, UpdateMode}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.QuestionViewBehaviours
import views.html.register.trustees.partnership.partnershipDetails

class PartnershipDetailsViewSpec extends QuestionViewBehaviours[PartnershipDetails] {

  val messageKeyPrefix = "partnershipName"

  override val form = new PartnershipDetailsFormProvider()()
  val firstIndex = Index(1)
  val submitUrl = controllers.register.trustees.partnership.routes.PartnershipDetailsController.onPageLoad(NormalMode, firstIndex, None)

  val view: partnershipDetails = app.injector.instanceOf[partnershipDetails]

  def createView(): () => HtmlFormat.Appendable = () => view(
    form, NormalMode, firstIndex, None, submitUrl, None)(fakeRequest, messages)
  def createUpdateView: () => HtmlFormat.Appendable = () => view(
    form, UpdateMode, firstIndex, None, submitUrl, Some("srn"))(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    view(form, NormalMode, firstIndex, None, submitUrl, None)(fakeRequest, messages)


  "PartnershipDetails view" must {

    behave like normalPageWithHeaderCheck(
      createView(),
      messageKeyPrefix,
      messages(s"messages__${messageKeyPrefix}__title"),
      messages(s"messages__${messageKeyPrefix}__title")
    )
    behave like pageWithErrorOutsideLabel(createViewUsingForm, messageKeyPrefix,
      routes.PartnershipDetailsController.onSubmit(NormalMode, firstIndex, None).url, "partnershipName")

    behave like pageWithReturnLink(createView(), getReturnLink)

    behave like pageWithReturnLinkAndSrn(createUpdateView, getReturnLinkWithSrn)
  }
}
