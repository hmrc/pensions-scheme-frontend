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

package views

import controllers.routes
import forms.InvestmentRegulatedSchemeFormProvider
import models.NormalMode
import play.api.data.Form
import views.behaviours.YesNoViewBehaviours
import views.html.investmentRegulatedScheme

class InvestmentRegulatedSchemeViewSpec extends YesNoViewBehaviours {

  val messageKeyPrefix = "investment_regulated_scheme"
  private val schemeName = "schemeName"

  val form = new InvestmentRegulatedSchemeFormProvider()(schemeName)

  val view: investmentRegulatedScheme = app.injector.instanceOf[investmentRegulatedScheme]

  private def createView() = () =>
    view(form, NormalMode, Some(schemeName))(fakeRequest, messages)

  private def createViewUsingForm = (form: Form[_]) =>
    view(form, NormalMode, Some(schemeName))(fakeRequest, messages)

  "InvestmentRegulated view " must {

    behave like normalPage(createView(), messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__h1","schemeName"))

    behave like yesNoPageExplicitLegend(createView = createViewUsingForm, messageKeyPrefix = messageKeyPrefix,
      legend = messages("messages__investment_regulated_scheme__h1", schemeName),
      expectedFormAction = routes.InvestmentRegulatedSchemeController.onSubmit(NormalMode).url)

    behave like pageWithReturnLink(createView(), getReturnLink)
  }
}
