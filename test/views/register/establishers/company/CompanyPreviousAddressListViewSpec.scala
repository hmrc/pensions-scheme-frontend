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

import forms.register.establishers.individual.AddressListFormProvider
import models.addresslookup.Address
import play.api.data.Form
import models.{Index, NormalMode}
import controllers.register.establishers.company.routes
import org.jsoup.Jsoup
import views.behaviours.ViewBehaviours
import views.html.register.establishers.company.companyPreviousAddressList

class CompanyPreviousAddressListViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "select_the_address"

  val form = new AddressListFormProvider()(Seq(0, 1))
  val index = Index(0)
  val companyName = "test company name"
  val addresses = Seq(
    address("test post code 1"),
    address("test post code 2")
  )

  def address(postCode: String): Address = Address("address line 1", "address line 2", Some("test town"),
    Some("test county"), postcode = Some(postCode), country = "United Kingdom")



  def createView = () => companyPreviousAddressList(frontendAppConfig, form, NormalMode, index, companyName, addresses)(fakeRequest, messages)

  def createViewUsingForm = (form: Form[_]) => companyPreviousAddressList(frontendAppConfig, form, NormalMode, index, companyName, addresses)(fakeRequest, messages)

  "CompanyPreviousAddressList view" must {
    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"))

    behave like pageWithBackLink(createView)
    behave like pageWithSecondaryHeader(createView, companyName)

    "have link for enter address manually" in {
      Jsoup.parse(createView().toString).select("a[id=manual-address-link]") must haveLink(
        routes.CompanyPreviousAddressListController.onPageLoad(NormalMode, index).url
      )
    }
  }

  "CompanyPreviousAddressList view" when {
    "rendered" must {
      "contain radio buttons for the value" in {
        val doc = asDocument(createViewUsingForm(form))
        for ((_, option) <- addresses.zipWithIndex) {
          assertContainsRadioButton(doc, s"value-$option", "value", option.toString, false)
        }
      }
    }

    for ((_, option) <- addresses.zipWithIndex) {
      s"rendered with a value of '$option'" must {
        s"have the '$option' radio button selected" in {
          val doc = asDocument(createViewUsingForm(form.bind(Map("value" -> option.toString))))
          assertContainsRadioButton(doc, s"value-$option", "value", option.toString, true)

          for((_, option) <-addresses.zipWithIndex.filterNot {case (_, falseOption) => falseOption == option }) {
            assertContainsRadioButton(doc, s"value-$option", "value", option.toString, false)
          }
        }
      }
    }
  }

}
