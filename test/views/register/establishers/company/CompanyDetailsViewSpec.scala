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

package views.register.establishers.company

import config.FrontendAppConfig
import controllers.register.establishers.company.routes
import forms.CompanyDetailsFormProvider
import models.{CompanyDetails, Index, NormalMode}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.QuestionViewBehaviours
import views.html.register.establishers.company.companyDetails

class CompanyDetailsViewSpec extends QuestionViewBehaviours[CompanyDetails] {

  val messageKeyPrefix = "common__company_details"

  override val form = new CompanyDetailsFormProvider()()
  val firstIndex = Index(1)

  def createView(isHubEnabled: Boolean = false): () => HtmlFormat.Appendable = () =>
    companyDetails(appConfig(isHubEnabled), form, NormalMode, firstIndex)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    companyDetails(frontendAppConfig, form, NormalMode, firstIndex)(fakeRequest, messages)

  "CompanyDetails view" must {

    behave like normalPage(createView(), messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"))

    behave like pageWithBackLink(createView())

    "not have a return link" in {
      val doc = asDocument(createView()())
      assertNotRenderedById(doc, "return-link")
    }

    behave like pageWithTextFields(createViewUsingForm, messageKeyPrefix,
      routes.CompanyDetailsController.onSubmit(NormalMode, firstIndex).url, "companyName", "vatNumber", "payeNumber")
  }

  "CompanyDetails view with hub enabled" must {
    behave like pageWithReturnLink(createView(isHubEnabled = true), controllers.register.routes.SchemeTaskListController.onPageLoad().url)

    "not have a back link" in {
      val doc = asDocument(createView(isHubEnabled = true)())
      assertNotRenderedById(doc, "back-link")
    }
  }
}
