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

package views.racdac

import forms.racdac.ContractOrPolicyNumberFormProvider
import models.{CheckMode, NormalMode}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.QuestionViewBehaviours
import views.html.racdac.contractOrPolicyNumber

class ContractOrPolicyNumberViewSpec extends QuestionViewBehaviours[String] {
  private val psaName = "A PSA"
  private val schemeName = "scheme"
  val messageKeyPrefix = "racdac_contract_or_policy_number"
  private val racDACName = "RAC DAC NAME"

  private val formProvider = new ContractOrPolicyNumberFormProvider()
  override val form = formProvider(racDACName)

  val view: contractOrPolicyNumber = app.injector.instanceOf[contractOrPolicyNumber]

  def createView: () => HtmlFormat.Appendable = () => view(form, NormalMode, psaName, schemeName)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    view(form, NormalMode, psaName, schemeName)(fakeRequest, messages)

  def createViewInCheckMode: () => HtmlFormat.Appendable = () =>
    view(form, CheckMode, psaName, schemeName)(fakeRequest, messages)

  "ContractOrPolicyNumber view" must {

    behave like normalPageWithDynamicTitleAndHeader(createView, messageKeyPrefix, schemeName)

    behave like pageWithErrorOutsideLabel(createViewUsingForm, messageKeyPrefix, controllers.racdac.routes.RACDACNameController.onSubmit(NormalMode).url,
      "value")

    behave like pageWithReturnLink(createView, frontendAppConfig.managePensionsSchemeOverviewUrl.url)
  }
}
