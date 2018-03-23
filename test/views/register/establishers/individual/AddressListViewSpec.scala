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
import forms.register.establishers.individual.AddressListFormProvider
import models.Address
import models.{Index, NormalMode}
import org.jsoup.Jsoup
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.register.establishers.individual.addressList

class AddressListViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "select_the_address"

  val form = new AddressListFormProvider()(Seq(0))
  val firstIndex = Index(0)
  val establisherName: String = "test first name test last name"

  def address(postCode: String): Address = Address("address line 1", "address line 2", Some("test town"),
    Some("test county"), Some(postCode), "GB")

  val addressSeq = Seq(address("postcode 1"), address("postcode 2"))
  val addressIndexes = Seq.range(0, 2)

  def createView: () => HtmlFormat.Appendable = () => addressList(frontendAppConfig, form, NormalMode, firstIndex, addressSeq,
    establisherName)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) => addressList(frontendAppConfig, form, NormalMode,
    firstIndex, addressSeq, establisherName)(fakeRequest, messages)

  "AddressListView view" must {
    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"))

    behave like pageWithBackLink(createView)

    behave like pageWithSecondaryHeader(createView, establisherName)

    "have link for enter address manually" in {
      Jsoup.parse(createView().toString()).select("a[id=manual-address-link]") must haveLink(
        routes.AddressController.onPageLoad(NormalMode, firstIndex).url)
    }
  }

  "AddressListView view" when {
    "rendered" must {
      "contain radio buttons for the value" in {
        val doc = asDocument(createViewUsingForm(form))
        for (i <- addressIndexes) {
          assertContainsRadioButton(doc, s"value-$i", "value", s"$i", isChecked = false)
        }
      }
    }

    for (index <- addressIndexes) {
      s"rendered with a value of '$index'" must {
        s"have the '$index' radio button selected" in {
          val doc = asDocument(createViewUsingForm(form.bind(Map("value" -> s"$index"))))
          assertContainsRadioButton(doc, s"value-$index", "value", s"$index", isChecked = true)

          for (unselectedIndex <- addressIndexes.filterNot(o => o == index)) {
            assertContainsRadioButton(doc, s"value-$unselectedIndex", "value", unselectedIndex.toString, isChecked = false)
          }
        }
      }
    }
  }
}
