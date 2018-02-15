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
import models.{Index, NormalMode}
import models.addresslookup.Address
import models.register.CountryOptions
import org.jsoup.Jsoup
import play.twirl.api.HtmlFormat
import utils.InputOption
import views.behaviours.QuestionViewBehaviours
import views.html.register.establishers.individual.previousAddress

class PreviousAddressViewSpec extends QuestionViewBehaviours[Address] {

  val messageKeyPrefix = "establisher_individual_previous_address"
  val firstIndex = Index(0)
  val validCountryData: Seq[InputOption] = Seq(InputOption("AF", "Afghanistan"), InputOption("territory:AE-AZ", "Abu Dhabi"))
  val countryOptions: CountryOptions = new CountryOptions(validCountryData)
  val establisherName: String = "test first name test last name"

  override val form = new AddressFormProvider()()

  def createView: () => HtmlFormat.Appendable = () => previousAddress(frontendAppConfig, form, NormalMode, firstIndex,
    countryOptions.options, establisherName)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) => previousAddress(frontendAppConfig, form, NormalMode,
    firstIndex, countryOptions.options, establisherName)(fakeRequest, messages)


  "Previous Address view" must {

    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"))

    behave like pageWithBackLink(createView)

    behave like pageWithTextFields(createViewUsingForm, messageKeyPrefix,
      routes.AddressController.onSubmit(NormalMode, firstIndex).url, "addressLine1", "addressLine2", "addressLine3", "addressLine4")

    "contain select input options for country" in {
      val doc = asDocument(createViewUsingForm(form))
      for (option <- validCountryData) {
        assertContainsSelectOption(doc, s"value-${option.value}", option.label, option.value, false)
      }
    }

    for (option <- validCountryData) {
      s"have the '${option.value}' select option selected" in {
        val doc = asDocument(createViewUsingForm(form.bind(Map("country" -> s"${option.value}"))))
        assertContainsSelectOption(doc, s"value-${option.value}", option.label, option.value, true)

        for (unselectedOption <- validCountryData.filterNot(o => o == option)) {
          assertContainsSelectOption(doc, s"value-${unselectedOption.value}", unselectedOption.label, unselectedOption.value, false)
        }
      }
    }

    "have establisher name rendered on the page" in {
      Jsoup.parse(createView().toString()) must haveDynamicText(establisherName)
    }
  }
}
