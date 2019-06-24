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

package views.register.establishers.company

import forms.register.establishers.company.HasCompanyNumberFormProvider
import models.{Index, NormalMode}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.register.establishers.company.hasCompanyNumber

class HasCompanyNumberViewSpec extends YesNoViewBehaviours {
  val schemeName = Some("Scheme x")
  val messageKeyPrefix = "hasCompanyNumber"
  val srn = Some("srn")

  val form = new HasCompanyNumberFormProvider().apply("ABC")
  private val postCall = controllers.register.establishers.company.routes.HasCompanyNumberController.onSubmit(NormalMode, None, Index(0))

  def createView: () => HtmlFormat.Appendable = () =>
    hasCompanyNumber(frontendAppConfig, form, "ABC", schemeName, postCall, srn)(fakeRequest, messages)

  def createUpdateView: () => HtmlFormat.Appendable = () =>
    hasCompanyNumber(frontendAppConfig, form, "ABC", schemeName, postCall, srn)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    hasCompanyNumber(frontendAppConfig, form, "ABC", schemeName, postCall, srn)(fakeRequest, messages)

  "HasCompanyNumber view" must {

    behave like normalPage(createView, messageKeyPrefix, pageHeader = messages(s"messages__${messageKeyPrefix}__h1", "ABC"),
      expectedGuidanceKeys = "_p1")

    behave like yesNoPage(
      createView = createViewUsingForm,
      messageKeyPrefix = messageKeyPrefix,
      expectedFormAction = postCall.url
    )

    behave like pageWithSubmitButton(createView)

    behave like pageWithReturnLinkAndSrn(createUpdateView, getReturnLinkWithSrn)

  }
}
