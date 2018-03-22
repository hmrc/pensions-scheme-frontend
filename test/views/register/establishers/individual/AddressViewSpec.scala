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
import forms.register.establishers.individual.AddressFormProvider
import models.addresslookup.Address
import models.{Index, NormalMode}
import org.jsoup.Jsoup
import utils.InputOption
import views.behaviours.QuestionViewBehaviours
import views.html.register.establishers.individual.address

class AddressViewSpec extends QuestionViewBehaviours[Address] {

  val messageKeyPrefix = "establisher_individual_address"
  val firstIndex = Index(0)
  val validData: Seq[InputOption] = Seq(InputOption("AF", "Afghanistan"), InputOption("territory:AE-AZ", "Abu Dhabi"))
  val countryOptions: CountryOptions = new CountryOptions(validData)
  val establisherName: String = "test first name test last name"

  override val form = new AddressFormProvider()()

  def createView: () => _root_.play.twirl.api.HtmlFormat.Appendable = () => address(frontendAppConfig, new AddressFormProvider().apply(),
    NormalMode, firstIndex, validData, establisherName)(fakeRequest, messages)

  def createViewUsingForm: (Form[_]) => _root_.play.twirl.api.HtmlFormat.Appendable = (form: Form[_]) => address(frontendAppConfig, form, NormalMode,
    firstIndex, validData, establisherName)(fakeRequest, messages)


  "Address view" must {

    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"), "lede")

    behave like pageWithBackLink(createView)

    behave like pageWithTextFields(createViewUsingForm, messageKeyPrefix,
      routes.AddressController.onSubmit(NormalMode, firstIndex).url, "addressLine1", "addressLine2", "addressLine3", "addressLine4")

    "contain select input options for country" in {
      val doc = asDocument(createViewUsingForm(form))
      for (option <- validData) {
        assertContainsSelectOption(doc, s"value-${option.value}", option.label, option.value, false)
      }
    }

    for (option <- validData) {
      s"have the '${option.value}' select option selected" in {
        val doc = asDocument(createViewUsingForm(form.bind(Map("country" -> s"${option.value}"))))
        assertContainsSelectOption(doc, s"value-${option.value}", option.label, option.value, true)

        for (unselectedOption <- validData.filterNot(o => o == option)) {
          assertContainsSelectOption(doc, s"value-${unselectedOption.value}", unselectedOption.label, unselectedOption.value, false)
        }
      }
    }

    "have establisher name rendered on the page" in {
      Jsoup.parse(createView().toString()) must haveDynamicText(establisherName)
    }
  }
}
