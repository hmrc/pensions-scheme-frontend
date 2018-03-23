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
import forms.register.establishers.individual.AddressListFormProvider
import models.NormalMode
import models.address.Address
import org.jsoup.Jsoup
import play.api.data.Form
import views.behaviours.ViewBehaviours
import views.html.register.establishers.company.companyAddressList

class CompanyAddressListViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "select_the_address"

  val form = new AddressListFormProvider()(Seq(0, 1))
  val addresses: Seq[Address] = Seq(
    address("test address 1"),
    address("test address 2")
  )

  private def address(postCode: String): Address = Address("address line 1", "address line 2", Some("test town"),
    Some("test county"), postcode = Some(postCode), country = "United Kingdom")

  private def createView =
    () => companyAddressList(frontendAppConfig, form, NormalMode, 0, addresses, "Company Name")(fakeRequest, messages)

  private def createViewUsingForm =
    (form: Form[_]) => companyAddressList(frontendAppConfig, form, NormalMode, 0, addresses, "Company Name")(fakeRequest, messages)

  "CompanyAddressList view" must {

    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"))

    behave like pageWithBackLink(createView)

    behave like pageWithSecondaryHeader(createView, "Company Name")

    "have link for enter address manually" in {
      Jsoup.parse(createView().toString()).select("a[id=manual-address-link]") must haveLink(
        routes.CompanyAddressController.onPageLoad(NormalMode, 0).url)
    }
  }

  "CompanyAddressList view" when {
    "rendered" must {
      "contain radio buttons for the value" in {
        val doc = asDocument(createViewUsingForm(form))
        for ((_, id) <- addresses.zipWithIndex) {
          assertContainsRadioButton(doc, s"value-$id", "value", id.toString, isChecked = false)
        }
      }
    }

    for((_, id) <- addresses.zipWithIndex) {
      s"rendered with a value of '$id'" must {
        s"have the '$id' radio button selected" in {
          val doc = asDocument(createViewUsingForm(form.bind(Map("value" -> id.toString))))
          assertContainsRadioButton(doc, s"value-$id", "value", id.toString, isChecked = true)

          for((_, id) <- addresses.zipWithIndex.filterNot { case (_, otherId) => id == otherId }) {
            assertContainsRadioButton(doc, s"value-$id", "value", id.toString, isChecked = false)
          }
        }
      }
    }
  }
}
