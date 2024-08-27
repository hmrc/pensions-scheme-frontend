/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.register.establishers.company

import connectors.AddressLookupConnector
import controllers.ControllerSpecBase
import forms.address.PostCodeLookupFormProvider
import models.address.TolerantAddress
import models.{CompanyDetails, Index, NormalMode}
import navigators.Navigator
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.mvc.Call
import play.api.test.Helpers._
import services.{FakeUserAnswersService, UserAnswersService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.FakeNavigator
import utils.annotations.EstablishersCompany
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import scala.concurrent.Future

class CompanyPostCodeLookupControllerSpec extends ControllerSpecBase with MockitoSugar with ScalaFutures with OptionValues {

  def onwardRoute: Call = routes.CompanyAddressListController.onPageLoad(NormalMode, srn, firstIndex)

  def manualInputCall: Call = routes.CompanyAddressController.onPageLoad(NormalMode, srn, firstIndex)

  val formProvider = new PostCodeLookupFormProvider()
  val form = formProvider()
  implicit val hc: HeaderCarrier = mock[HeaderCarrier]

  val firstIndex = Index(0)
  val companyName: String = "test company name"
  val company = CompanyDetails(companyName)

  private val addressLookupConnector = mock[AddressLookupConnector]
  private val address = TolerantAddress(Some("value 1"), Some("value 2"), None, None, Some("AB1 1AB"), Some("GB"))

  private val view = injector.instanceOf[postcodeLookup]

  lazy val viewModel = PostcodeLookupViewModel(
    postCall = routes.CompanyPostCodeLookupController.onSubmit(NormalMode, srn, firstIndex),
    manualInputCall = manualInputCall,
    title = Message("messages__establisherPostCode__title"),
    heading = Message("messages__establisherPostCode__h1", companyName),
    srn = srn
  )

  "render the view correctly on a GET request" in {
    running(_.overrides(modules(getMandatoryEstablisherCompany): _*)) {
      app =>
        val controller = app.injector.instanceOf[CompanyPostCodeLookupController]
        val result = controller.onPageLoad(NormalMode, srn, index = 0)(fakeRequest)
        status(result) mustBe OK
        contentAsString(result).removeAllNonces() mustBe view(form, viewModel, None)(fakeRequest, messages).toString
    }
  }

  "redirect to the next page on a POST request" in {
    val validPostcode = "ZZ1 1ZZ"
    running(_.overrides(modules(getMandatoryEstablisherCompany) ++
      Seq[GuiceableModule](bind[Navigator].qualifiedWith(classOf[EstablishersCompany]).toInstance(new FakeNavigator(onwardRoute)),
        bind[UserAnswersService].toInstance(FakeUserAnswersService),
        bind[AddressLookupConnector].toInstance(addressLookupConnector)
      ): _*)) {
      app =>
        when(addressLookupConnector.addressLookupByPostCode(ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(Seq(address)))
        val controller = app.injector.instanceOf[CompanyPostCodeLookupController]
        val postRequest = fakeRequest.withFormUrlEncodedBody("postcode" -> validPostcode)
        val result = controller.onSubmit(NormalMode, srn, index = 0)(postRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
    }
  }
}
