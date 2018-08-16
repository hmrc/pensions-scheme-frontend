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

import controllers.register.establishers.company.routes
import forms.address.PostCodeLookupFormProvider
import models.{Index, NormalMode}
import org.jsoup.Jsoup
import play.api.data.Form
import views.behaviours.StringViewBehaviours
import views.html.register.establishers.company.companyPreviousAddressPostcodeLookup

class CompanyPreviousAddressPostcodeLookupViewSpec extends StringViewBehaviours {

  val messageKeyPrefix = "companyPreviousAddressPostcodeLookup"
  val index = Index(0)
  val companyName = "test company name"
  val form = new PostCodeLookupFormProvider()()

  def createView = () => companyPreviousAddressPostcodeLookup(frontendAppConfig, form, NormalMode, index, companyName)(fakeRequest, messages)

  def createViewUsingForm = (form: Form[String]) => companyPreviousAddressPostcodeLookup(frontendAppConfig, form, NormalMode, index, companyName)(fakeRequest, messages)

  "CompanyPreviousAddressPostcodeLookup view" must {
    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__heading"))

    behave like pageWithBackLink(createView)

    behave like stringPage(createViewUsingForm, messageKeyPrefix, routes.CompanyPreviousAddressPostcodeLookupController.onSubmit(NormalMode, index).url,
      Some("messages__common__address_postcode"))

    "have company name rendered on the page" in {
      Jsoup.parse(createView().toString()) must haveDynamicText(companyName)
    }

    "have link for enter address manually" in {
      Jsoup.parse(createView().toString()).select("a[id=manual-address-link]") must haveLink(
        routes.CompanyPreviousAddressController.onPageLoad(NormalMode, index).url)
    }
  }
}
