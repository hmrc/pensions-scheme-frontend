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

package views.address

import config.FrontendAppConfig
import controllers.register.routes
import forms.address.AddressFormProvider
import models.NormalMode
import models.address.Address
import play.api.data.Form
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Call
import utils.{FakeCountryOptions, InputOption}
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.behaviours.QuestionViewBehaviours
import views.html.address.manualAddress

class ManualAddressViewSpec extends QuestionViewBehaviours[Address] {

  def appConfig(isHubEnabled: Boolean): FrontendAppConfig = new GuiceApplicationBuilder().configure(
    "features.is-hub-enabled" -> isHubEnabled
  ).build().injector.instanceOf[FrontendAppConfig]

  val messageKeyPrefix = "common__manual__address"
  val countryOptions: Seq[InputOption] = Seq(InputOption("AF", "Afghanistan"), InputOption("territory:AE-AZ", "Abu Dhabi"))
  val schemeName: String = "Test Scheme Name"

  val viewModel = ManualAddressViewModel(
    Call("GET", "/"),
    countryOptions,
    Message("messages__common__manual__address__title"),
    Message("messages__common__manual__address__heading"),
    Some("secondary.header")
  )

  override val form = new AddressFormProvider(FakeCountryOptions())()

  def createView(isHubEnabled: Boolean): () => _root_.play.twirl.api.HtmlFormat.Appendable = () =>
    manualAddress(appConfig(isHubEnabled), new AddressFormProvider(FakeCountryOptions()).apply(), viewModel)(fakeRequest, messages)

  def createViewUsingForm: (Form[_]) => _root_.play.twirl.api.HtmlFormat.Appendable = (form: Form[_]) =>
    manualAddress(frontendAppConfig, form, viewModel)(fakeRequest, messages)

  "ManualAddress view" must {

    behave like normalPage(createView(isHubEnabled = false), messageKeyPrefix, viewModel.heading)

    behave like pageWithBackLink(createView(isHubEnabled = false))

    behave like pageWithTextFields(
      createViewUsingForm,
      messageKeyPrefix,
      routes.InsurerAddressController.onSubmit(NormalMode).url,
      "addressLine1", "addressLine2", "addressLine3", "addressLine4"
    )

    "not have a return link" in {
      val doc = asDocument(createView(isHubEnabled = false)())
      assertNotRenderedById(doc, "return-link")
    }

  }

  "ManualAddress view with hub enabled" must {
    behave like pageWithReturnLink(createView(isHubEnabled = true), url = controllers.register.routes.SchemeTaskListController.onPageLoad().url)

    "not have a back link" in {
      val doc = asDocument(createView(isHubEnabled = true)())
      assertNotRenderedById(doc, "back-link")
    }
  }

}