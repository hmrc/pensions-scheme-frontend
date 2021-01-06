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

package views.address

import controllers.routes
import forms.address.AddressFormProvider
import models.NormalMode
import models.address.Address
import play.api.data.Form
import play.api.mvc.Call
import utils.{FakeCountryOptions, InputOption}
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel
import views.behaviours.QuestionViewBehaviours
import views.html.address.postcodeLookup

class PostCodeLookupViewSpec extends QuestionViewBehaviours[Address] {
  val messageKeyPrefix = "adviserPostCodeLookup"
  val countryOptions: Seq[InputOption] = Seq(InputOption("AF", "Afghanistan"), InputOption("territory:AE-AZ", "Abu Dhabi"))
  val schemeName: String = "Test Scheme Name"

  def manualInputCall: Call = routes.AdviserAddressController.onPageLoad(NormalMode)

  val viewModel: PostcodeLookupViewModel = PostcodeLookupViewModel(
    postCall = routes.AdviserPostCodeLookupController.onSubmit(NormalMode),
    manualInputCall = manualInputCall,
    title = Message("messages__adviserPostCodeLookup__title"),
    heading = Message("messages__adviserPostCodeLookup__heading", "name"),
    subHeading = Some(Message("messages__adviserPostCodeLookupAddress__secondary")),
    enterPostcode = Message("messages__adviserPostCodeLookupAddress__enterPostCode")
  )

  val updateViewModel: PostcodeLookupViewModel = PostcodeLookupViewModel(
    postCall = routes.AdviserPostCodeLookupController.onSubmit(NormalMode),
    manualInputCall = manualInputCall,
    title = Message("messages__adviserPostCodeLookup__title"),
    heading = Message("messages__adviserPostCodeLookup__heading", "name"),
    subHeading = Some(Message("messages__adviserPostCodeLookupAddress__secondary")),
    enterPostcode = Message("messages__adviserPostCodeLookupAddress__enterPostCode"),
    srn = Some("srn")
  )

  override val form = new AddressFormProvider(FakeCountryOptions())()

  val view: postcodeLookup = app.injector.instanceOf[postcodeLookup]

  def createView(): () => _root_.play.twirl.api.HtmlFormat.Appendable = () =>
    view(new AddressFormProvider(FakeCountryOptions()).apply(), viewModel, None)(fakeRequest, messages)

def createUpdateView(): () => _root_.play.twirl.api.HtmlFormat.Appendable = () =>
  view(new AddressFormProvider(FakeCountryOptions()).apply(), updateViewModel, None)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => _root_.play.twirl.api.HtmlFormat.Appendable = (form: Form[_]) =>
    view(form, viewModel, None)(fakeRequest, messages)

  "ManualAddress view" must {

    behave like normalPage(createView(), messageKeyPrefix, viewModel.heading)

    behave like pageWithErrorOutsideLabel(
      createViewUsingForm,
      messageKeyPrefix,
      manualInputCall.url,
      "postcode"
    )

    behave like pageWithReturnLink(createView(), getReturnLink)

    behave like pageWithReturnLinkAndSrn(createUpdateView(), getReturnLinkWithSrn)
  }
}