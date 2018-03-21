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

import controllers.register.routes
import forms.address.AddressFormProvider
import models.NormalMode
import models.address.Address
import org.jsoup.Jsoup
import play.api.data.Form
import utils.InputOption
import views.behaviours.QuestionViewBehaviours
import views.html.register.insurerAddress


class InsurerAddressViewSpec extends QuestionViewBehaviours[Address] {

  val messageKeyPrefix = "benefits_insurance_addr"
  val validData: Seq[InputOption] = Seq(InputOption("AF", "Afghanistan"), InputOption("territory:AE-AZ", "Abu Dhabi"))
  val schemeName: String = "Test Scheme Name"

  override val form = new AddressFormProvider()()

  def createView: () => _root_.play.twirl.api.HtmlFormat.Appendable = () => insurerAddress(frontendAppConfig, new AddressFormProvider().apply(),
    NormalMode, validData, schemeName)(fakeRequest, messages)

  def createViewUsingForm: (Form[_]) => _root_.play.twirl.api.HtmlFormat.Appendable = (form: Form[_]) => insurerAddress(frontendAppConfig, form, NormalMode,
    validData, schemeName)(fakeRequest, messages)


  "ManualAddress view" must {

    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"))

    behave like pageWithBackLink(createView)

    behave like pageWithTextFields(createViewUsingForm, messageKeyPrefix,
      routes.InsurerAddressController.onSubmit(NormalMode).url, "addressLine1", "addressLine2", "addressLine3", "addressLine4")

    "have establisher name rendered on the page" in {
      Jsoup.parse(createView().toString()) must haveDynamicText(schemeName)
    }
  }
}