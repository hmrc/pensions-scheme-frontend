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

package views

import controllers.routes
import forms.InvestmentRegulatedSchemeFormProvider
import models.NormalMode
import play.api.data.Form
import views.behaviours.YesNoViewBehaviours
import views.html.investmentRegulatedScheme

class InvestmentRegulatedSchemeViewSpec extends YesNoViewBehaviours {

  val messageKeyPrefix = "investment_regulated_scheme"

  val form = new InvestmentRegulatedSchemeFormProvider()()

  private def createView() = () =>
    investmentRegulatedScheme(frontendAppConfig, form, NormalMode, Some("schemeName"))(fakeRequest, messages)

  private def createViewUsingForm = (form: Form[_]) =>
    investmentRegulatedScheme(frontendAppConfig, form, NormalMode, None)(fakeRequest, messages)

  "InvestmentRegulated view " must {

    behave like normalPage(createView(), messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__h1","schemeName"))

    behave like yesNoPage(createView = createViewUsingForm, messageKeyPrefix = messageKeyPrefix,
      expectedFormAction = routes.InvestmentRegulatedSchemeController.onSubmit(NormalMode).url)

    behave like pageWithReturnLink(createView(), getReturnLink)
  }
}
