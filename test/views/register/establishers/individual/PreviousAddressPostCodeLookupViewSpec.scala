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

package views.register.establishers.individual

import play.api.data.Form
import controllers.register.establishers.individual.routes
import forms.address.PostCodeLookupFormProvider
import models.{Index, NormalMode}
import org.jsoup.Jsoup
import play.twirl.api.HtmlFormat
import views.behaviours.StringViewBehaviours
import views.html.register.establishers.individual.previousPostCodeLookup

class PreviousAddressPostCodeLookupViewSpec extends StringViewBehaviours {

  val messageKeyPrefix = "establisher_individual_previous_address"

  val form = new PostCodeLookupFormProvider()()
  val firstIndex = Index(0)
  val establisherName = "test establisher name"

  def createView: () => HtmlFormat.Appendable = ()=> previousPostCodeLookup(frontendAppConfig, form, NormalMode,firstIndex, establisherName)(fakeRequest, messages)

  def createViewUsingForm = (form: Form[String]) => previousPostCodeLookup(frontendAppConfig, form, NormalMode, firstIndex, establisherName)(fakeRequest, messages)

  "PreviousAddress view" must {
    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"),"lede")

    behave like pageWithBackLink(createView)

    behave like stringPage(createViewUsingForm, messageKeyPrefix, routes.PreviousAddressPostCodeLookupController.onSubmit(NormalMode,firstIndex).url,
      Some("messages__common__address_postcode"), expectedHint = Some("messages__common__address_postcode_hint"))
  }
  "have establisher name rendered on the page" in {
    Jsoup.parse(createView().toString()) must haveDynamicText(establisherName)
  }

  "have link for enter address manually" in {
    Jsoup.parse(createView().toString()).select("a[id=manual-address-link]") must haveLink(
      routes.PreviousAddressController.onPageLoad(NormalMode, firstIndex).url)
  }
}
