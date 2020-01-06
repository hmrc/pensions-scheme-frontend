/*
 * Copyright 2020 HM Revenue & Customs
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

package views.address

import forms.address.AddressListFormProvider
import models.UpdateMode
import models.address.TolerantAddress
import org.jsoup.Jsoup
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewmodels.Message
import viewmodels.address.AddressListViewModel
import views.behaviours.ViewBehaviours
import views.html.address.addressList

class AddressListSpec extends ViewBehaviours {

  private val messageKeyPrefix = "select_the_address"

  private val form = new AddressListFormProvider()(Nil)

  private val addresses = Seq(address("postcode 1"), address("postcode 2"))
  private val addressIndexes = Seq.range(0, 2)
  private val call = controllers.routes.IndexController.onPageLoad()
  private val subHeading = "sub-heading"

  private val viewModel = AddressListViewModel(call, call, addresses)
  private val updateViewModel = AddressListViewModel(call, call, addresses, srn = Some("srn"))

  private def address(postCode: String): TolerantAddress =
    TolerantAddress(
      Some("address line 1"),
      Some("address line 2"),
      Some("test town"),
      Some("test county"),
      Some(postCode),
      Some("GB")
    )

  private def createView(): () => HtmlFormat.Appendable =
    () =>
      addressList(
        frontendAppConfig,
        form,
        viewModel,
        None
      )(fakeRequest, messages)

  private def createUpdateView(): () => HtmlFormat.Appendable =
    () =>
      addressList(
        frontendAppConfig,
        form,
        updateViewModel,
        None
      )(fakeRequest, messages)

  private def createViewUsingForm: Form[_] => HtmlFormat.Appendable =
    (form: Form[_]) =>
      addressList(
        frontendAppConfig,
        form,
        viewModel,
        None
      )(fakeRequest, messages)

  "AddressListView view" when {

    "rendered" must {
      behave like normalPage(createView(), messageKeyPrefix, viewModel.title.resolve)

      "have link for enter address manually" in {
        Jsoup.parse(createView()().toString()).select("a[id=manual-address-link]") must haveLink(call.url)
      }
      behave like pageWithReturnLink(createView(), getReturnLink)

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

    behave like pageWithReturnLink(createView(), getReturnLink)

    behave like pageWithReturnLinkAndSrn(createUpdateView(), getReturnLinkWithSrn)

  }

}
