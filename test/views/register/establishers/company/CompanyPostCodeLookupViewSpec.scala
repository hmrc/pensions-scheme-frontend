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

import play.api.data.Form
import controllers.register.establishers.company.routes
import views.html.register.establishers.company.companyPostCodeLookup
import forms.register.establishers.company.CompanyPostCodeLookupFormProvider
import models.{Index, NormalMode}
import org.jsoup.Jsoup
import play.twirl.api.HtmlFormat
import views.behaviours.StringViewBehaviours

class CompanyPostCodeLookupViewSpec extends StringViewBehaviours {

  val messageKeyPrefix = "benefits_insurance_addr"

  val form = new CompanyPostCodeLookupFormProvider()()
  val firstIndex = Index(0)
  val schemeName = "test scheme name"

  def createView: () => HtmlFormat.Appendable = () => companyPostCodeLookup(frontendAppConfig, form, NormalMode, firstIndex,
    schemeName)(fakeRequest, messages)

  def createViewUsingForm: Form[String] => HtmlFormat.Appendable = (form: Form[String]) => companyPostCodeLookup(frontendAppConfig, form,
    NormalMode, firstIndex, schemeName)(fakeRequest, messages)

  "Address view" must {
    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"))

    behave like pageWithBackLink(createView)

    behave like stringPage(createViewUsingForm, messageKeyPrefix, routes.CompanyPostCodeLookupController.onSubmit(NormalMode, firstIndex).url,
      Some("messages__common__address_postcode"), expectedHint = Some("messages__common__address_postcode_hint"))

    "have establisher name rendered on the page" in {
      Jsoup.parse(createView().toString()) must haveDynamicText(schemeName)
    }

    "have link for enter address manually" in {
      Jsoup.parse(createView().toString()).select("a[id=manual-address-link]") must haveLink(
        routes.CompanyPostCodeLookupController.onPageLoad(NormalMode, firstIndex).url)
    }
  }
}
