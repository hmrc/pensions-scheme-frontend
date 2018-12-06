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

import controllers.register.establishers.individual.routes
import forms.address.PostCodeLookupFormProvider
import models.{Index, NormalMode}
import org.jsoup.Jsoup
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.StringViewBehaviours
import views.html.register.establishers.individual.postCodeLookup

class PostCodeLookupViewSpec extends StringViewBehaviours {

  val messageKeyPrefix = "establisher_individual_address"

  val form = new PostCodeLookupFormProvider()()
  val firstIndex = Index(0)

  def createView(isHubEnabled:Boolean): () => HtmlFormat.Appendable = () =>
    postCodeLookup(appConfig(isHubEnabled), form, NormalMode, firstIndex)(fakeRequest, messages)

  def createViewUsingForm: Form[String] => HtmlFormat.Appendable = (form: Form[String]) => postCodeLookup(frontendAppConfig, form,
    NormalMode, firstIndex)(fakeRequest, messages)

  "Address view" must {
    behave like normalPage(createView(isHubEnabled = false), messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"), "lede")

    behave like pageWithBackLink(createView(isHubEnabled = false))

    "not have a return link" in {
      val doc = asDocument(createView(isHubEnabled = false)())
      assertNotRenderedById(doc, "return-link")
    }

    behave like stringPage(createViewUsingForm, messageKeyPrefix, routes.PostCodeLookupController.onSubmit(NormalMode, firstIndex).url,
      Some("messages__common__address_postcode"))

    "have link for enter address manually" in {
      Jsoup.parse(createView(isHubEnabled = false)().toString()).select("a[id=manual-address-link]") must haveLink(
        routes.AddressController.onPageLoad(NormalMode, firstIndex).url)
    }
  }

  "Address view with hub enabled" must {
    behave like pageWithReturnLink(createView(isHubEnabled = true), url = controllers.register.routes.SchemeTaskListController.onPageLoad().url)

    "not have a back link" in {
      val doc = asDocument(createView(isHubEnabled = true)())
      assertNotRenderedById(doc, "back-link")
    }
  }


}
