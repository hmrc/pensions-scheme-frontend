/*
 * Copyright 2021 HM Revenue & Customs
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

package views

import forms.EstablishedCountryFormProvider
import models.{CheckMode, NormalMode}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import utils.{CountryOptions, InputOption}
import views.behaviours.StringViewBehaviours
import views.html.establishedCountry

class EstablishedCountryViewSpec extends StringViewBehaviours {

  val messageKeyPrefix = "established_country"
  private val schemeName = "Test Scheme Name"


  val inputOptions: Seq[InputOption] = Seq(InputOption("country:AF", "Afghanistan"), InputOption("territory:AE-AZ", "Abu Dhabi"))
  val countryOptions: CountryOptions = new CountryOptions(inputOptions)

  val form = new EstablishedCountryFormProvider(countryOptions)()

  val view: establishedCountry = app.injector.instanceOf[establishedCountry]

  def createView(): () => HtmlFormat.Appendable = () =>
    view(form, NormalMode, Seq.empty, schemeName)(fakeRequest, messages)

  def createViewInCheckMode: () => HtmlFormat.Appendable = () =>
    view(form, CheckMode, Seq.empty, schemeName)(fakeRequest, messages)

  def createViewUsingForm: Form[String] => HtmlFormat.Appendable = (form: Form[String]) =>
    view(form, NormalMode, inputOptions, schemeName)(fakeRequest, messages)

  "EstablishedCountry view" must {
    behave like normalPage(createView(), messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__h1", schemeName))

    behave like pageWithReturnLink(createView(), frontendAppConfig.managePensionsSchemeOverviewUrl.url)

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

  "EstablishedCountry view in check mode" must {
    behave like pageWithReturnLink(createViewInCheckMode, controllers.routes.PsaSchemeTaskListController.onPageLoad(NormalMode, None).url)
  }
}
