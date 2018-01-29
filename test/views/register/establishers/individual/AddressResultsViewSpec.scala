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
import play.api.data.Form
import forms.register.establishers.individual.AddressResultsFormProvider
import models.addresslookup.Address
import models.{Index, NormalMode}
import org.jsoup.Jsoup
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.register.establishers.individual.addressResults

class AddressResultsViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "select_the_address"

  val form = new AddressResultsFormProvider()()
  val firstIndex = Index(1)
  val establisherName: String = "test first name test last name"

  def address(postCode: String): Address = Address("address line 1", "address line 2", Some("test town"),
    Some("test county"), Some(postCode), "GB")

  val addressSeq = Seq(address("postcode 1"), address("postcode 2"))
  val addressSeqWithIndex: Seq[(Address, Int)] = addressSeq.zipWithIndex

  def createView: () => HtmlFormat.Appendable = () => addressResults(frontendAppConfig, form, NormalMode, firstIndex, addressSeq,
    establisherName)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) => addressResults(frontendAppConfig, form, NormalMode,
    firstIndex, addressSeq, establisherName)(fakeRequest, messages)

  def getAddressValue(address: Address): String = s"${address.addressLine1}, ${address.addressLine2}" +
    s"${address.addressLine3.map(town => s", $town").getOrElse("")}" +
    s"${address.addressLine4.map(county => s", $county").getOrElse("")}, " +
    s"${address.postcode.map(postcode => s"$postcode").getOrElse("")}"


  "AddressResults view" must {
    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"))

    "have link for enter address manually" in {
      Jsoup.parse(createView().toString()).select("a[id=manual-address-link]") must haveLink(
        routes.AddressResultsController.onPageLoad(NormalMode, firstIndex).url)
    }

    "have establisher name rendered on the page" in {
      Jsoup.parse(createView().toString()) must haveDynamicText(establisherName)
    }
  }

  "AddressResults view" when {
    "rendered" must {
      "contain radio buttons for the value" in {
        val doc = asDocument(createViewUsingForm(form))
        for ((address, i) <- addressSeqWithIndex) {
          assertContainsRadioButton(doc, s"addr-opts-$i", "value", getAddressValue(address), isChecked = false)
        }
      }
    }

    for ((address, i) <- addressSeqWithIndex) {

      s"rendered with a value of '${address.postcode}'" must {
        s"have the '${address.postcode}' radio button selected" in {
          val doc = asDocument(createViewUsingForm(form.bind(Map("value" -> s"${getAddressValue(address)}"))))
          assertContainsRadioButton(doc, s"addr-opts-$i", "value", getAddressValue(address), isChecked = true)

          for ((unselectedOptionAddress, j) <- addressSeqWithIndex.filterNot { o =>
            val (oaddress, _) = o
            oaddress == address
          }) {
            assertContainsRadioButton(doc, s"addr-opts-$j", "value", getAddressValue(unselectedOptionAddress), isChecked = false)
          }
        }
      }
    }
  }
}
