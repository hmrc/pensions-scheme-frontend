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
import models.address.Address
import models.{Index, NormalMode}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import utils.{FakeCountryOptions, InputOption}
import views.behaviours.QuestionViewBehaviours
import views.html.register.establishers.company.companyAddress

import scala.util.Random

class CompanyAddressViewSpec extends QuestionViewBehaviours[Address] {

  val messageKeyPrefix = "companyAddress"

  val companyName: String = Random.alphanumeric take 6 mkString ""

  val options = Seq.empty[InputOption]

  override val form: Form[Address] = new AddressFormProvider(FakeCountryOptions())()

  def createView: () => HtmlFormat.Appendable = () => companyAddress(frontendAppConfig, form, NormalMode, Index(0), companyName, options)(fakeRequest, messages)

  def createViewUsingForm: Form[_] =>
    HtmlFormat.Appendable = (form: Form[_]) => companyAddress(frontendAppConfig, form, NormalMode, Index(0), companyName, options)(fakeRequest, messages)

  "CompanyAddress view" must {

    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"))

    behave like pageWithBackLink(createView)

    behave like pageWithSecondaryHeader(createView, companyName)

    behave like pageWithTextFields(
      createViewUsingForm,
      messageKeyPrefix,
      controllers.register.establishers.company.routes.CompanyAddressController.onSubmit(NormalMode, Index(0)).url,
      "addressLine1", "addressLine2", "addressLine3", "addressLine4"
    )
  }
}
