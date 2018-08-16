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

import forms.register.SchemeEstablishedCountryFormProvider
import models.NormalMode
import models.register.SchemeDetails
import models.register.SchemeType.SingleTrust
import play.api.data.Form
import play.twirl.api.HtmlFormat
import utils.{CountryOptions, InputOption}
import views.behaviours.StringViewBehaviours
import views.html.register.schemeEstablishedCountry

class SchemeEstablishedCountryViewSpec extends StringViewBehaviours {

  val messageKeyPrefix = "scheme_country"

  val inputOptions: Seq[InputOption] = Seq(InputOption("country:AF", "Afghanistan"), InputOption("territory:AE-AZ", "Abu Dhabi"))
  val countryOptions: CountryOptions = new CountryOptions(inputOptions)

  val schemeDetails = SchemeDetails("MyScheme", SingleTrust)

  val form = new SchemeEstablishedCountryFormProvider(countryOptions)()

  def createView: () => HtmlFormat.Appendable = () =>
    schemeEstablishedCountry(frontendAppConfig, form, NormalMode, schemeDetails.schemeName, Seq.empty)(fakeRequest, messages)

  def createViewUsingForm: Form[String] => HtmlFormat.Appendable = (form: Form[String]) =>
    schemeEstablishedCountry(frontendAppConfig, form, NormalMode, schemeDetails.schemeName, inputOptions)(fakeRequest, messages)

  "SchemeEstablishedCountry view" must {
    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"))

    behave like pageWithBackLink(createView)

    behave like pageWithSecondaryHeader(createView, schemeDetails.schemeName)

    "contain select input options for the value" in {
      val doc = asDocument(createViewUsingForm(form))
      for (option <- inputOptions) {
        assertContainsSelectOption(doc, s"value-${option.value}", option.label, option.value, false)
      }
    }

    for (option <- inputOptions) {
      s"have the '${option.value}' select option selected" in {
        val doc = asDocument(createViewUsingForm(form.bind(Map("value" -> s"${option.value}"))))
        assertContainsSelectOption(doc, s"value-${option.value}", option.label, option.value, true)

        for (unselectedOption <- inputOptions.filterNot(o => o == option)) {
          assertContainsSelectOption(doc, s"value-${unselectedOption.value}", unselectedOption.label, unselectedOption.value, false)
        }
      }
    }
  }
}
