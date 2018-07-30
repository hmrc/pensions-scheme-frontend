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

package views.register.establishers.partnership

import controllers.register.establishers.partnership.routes
import forms.register.establishers.partnership.PartnershipDetailsFormProvider
import models.{Index, NormalMode, PartnershipDetails}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.QuestionViewBehaviours
import views.html.register.establishers.partnership.partnershipDetails

class PartnershipDetailsViewSpec extends QuestionViewBehaviours[PartnershipDetails] {

  val messageKeyPrefix = "partnershipDetails"

  override val form = new PartnershipDetailsFormProvider()()
  val firstIndex = Index(1)
  val schemeName = "test scheme name"

  def createView: () => HtmlFormat.Appendable = () => partnershipDetails(frontendAppConfig, form, NormalMode, firstIndex, schemeName)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    partnershipDetails(frontendAppConfig, form, NormalMode, firstIndex, schemeName)(fakeRequest, messages)


  "PartnershipDetails view" must {

    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"))

    behave like pageWithBackLink(createView)

    behave like pageWithTextFields(createViewUsingForm, messageKeyPrefix,
      routes.PartnershipDetailsController.onSubmit(NormalMode, firstIndex).url, "partnershipName")
  }
}
