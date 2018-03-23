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

package views.register

import forms.register.establishers.individual.AddressListFormProvider
import models.NormalMode
import models.Address
import org.jsoup.Jsoup
import play.api.data.Form
import views.behaviours.ViewBehaviours
import views.html.register.insurerAddressList

class InsurerAddressListViewSpec extends ViewBehaviours {

  val schemeName = "ThisSchemeName"
  val messageKeyPrefix = "select_the_address"
  val addressIndexes = Seq.range(0, 2)
  val addresses = Seq(
    address("test post code 1"),
    address("test post code 2")
  )

  def address(postCode: String): Address = Address("address line 1", "address line 2", Some("test town"),
    Some("test county"), postcode = Some(postCode), country = "United Kingdom")
  
  val form = new AddressListFormProvider()(Seq.empty)

  def createView: () => _root_.play.twirl.api.HtmlFormat.Appendable =
    () => insurerAddressList(frontendAppConfig, form, NormalMode, schemeName, addresses)(fakeRequest, messages)

  def createViewUsingForm: (Form[_]) => _root_.play.twirl.api.HtmlFormat.Appendable =
    (form: Form[_]) => insurerAddressList(frontendAppConfig, form, NormalMode, schemeName, addresses)(fakeRequest, messages)

  "InsurerAddressList view" must {

    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"))

    behave like pageWithBackLink(createView)

    behave like pageWithSecondaryHeader(createView, schemeName)

    "have link for enter address manually" in {
      Jsoup.parse(createView().toString).select("a[id=manual-address-link]") must haveLink(
        controllers.register.routes.InsurerAddressController.onPageLoad(NormalMode).url)
    }

    }

  "InsurerAddressList view" when {
    "rendered" must {
      "contain radio buttons for the value" in {
        val doc = asDocument(createViewUsingForm(form))
        for (option <- addressIndexes) {
          assertContainsRadioButton(doc, s"value-$option", "value", option.toString, false)
        }
      }
    }

    for(option <- addressIndexes) {
      s"rendered with a value of '$option'" must {
        s"have the '$option' radio button selected" in {
          val doc = asDocument(createViewUsingForm(form.bind(Map("value" -> s"$option"))))
          assertContainsRadioButton(doc, s"value-$option", "value", option.toString, true)

          for(unselectedOption <- addressIndexes.filterNot(o => o == option)) {
            assertContainsRadioButton(doc, s"value-$unselectedOption", "value", unselectedOption.toString, false)
          }
        }
      }
    }
  }
}
