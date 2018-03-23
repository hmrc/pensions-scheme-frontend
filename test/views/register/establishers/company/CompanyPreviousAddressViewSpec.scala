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

package views.register.establishers.company

import forms.register.establishers.individual.AddressFormProvider
import models.Address
import models.{Index, NormalMode}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import utils.{FakeCountryOptions, InputOption}
import views.behaviours.QuestionViewBehaviours
import views.html.register.establishers.company.companyPreviousAddress

class CompanyPreviousAddressViewSpec extends QuestionViewBehaviours[Address] {

  val messageKeyPrefix = "companyPreviousAddress"
  val index = Index(0)
  val companyName = "test company name"
  val options = Seq(InputOption("territory:AX", "Åland Islands"), InputOption("country:ZW", "Zimbabwe"))

  override val form = new AddressFormProvider(FakeCountryOptions())()

  def createView: () => HtmlFormat.Appendable = () => companyPreviousAddress(
    frontendAppConfig,
    form,
    NormalMode,
    index,
    companyName,
    options
  )(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) => companyPreviousAddress(
    frontendAppConfig,
    form,
    NormalMode,
    index,
    companyName,
    options)(fakeRequest, messages)


  "CompanyPreviousAddress view" must {

    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"))

    behave like pageWithBackLink(createView)

    behave like pageWithSecondaryHeader(createView, companyName)

    behave like pageWithTextFields(
      createViewUsingForm,
      messageKeyPrefix,
      controllers.register.establishers.company.routes.CompanyPreviousAddressController.onSubmit(NormalMode, index).url,  "addressLine1", "addressLine2", "addressLine3", "addressLine4")
  }
}
