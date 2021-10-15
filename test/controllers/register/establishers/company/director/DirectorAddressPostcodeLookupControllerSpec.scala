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

package controllers.register.establishers.company.director

import connectors.AddressLookupConnector
import controllers.ControllerSpecBase
import forms.address.PostCodeLookupFormProvider
import models.address.TolerantAddress
import models.person.PersonName
import models.{Index, NormalMode}
import navigators.Navigator
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.mockito.MockitoSugar
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.mvc.Call
import play.api.test.Helpers._
import services.{FakeUserAnswersService, UserAnswersService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.FakeNavigator
import utils.annotations.EstablishersCompanyDirector
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import scala.concurrent.Future

class DirectorAddressPostcodeLookupControllerSpec extends ControllerSpecBase with MockitoSugar {
  val estIndex = Index(0)
  val dirIndex = Index(0)

  def onwardRoute: Call = routes.DirectorAddressListController.onPageLoad(NormalMode, estIndex, dirIndex, None)
  def postCall: Call = routes.DirectorAddressPostcodeLookupController.onSubmit(NormalMode, estIndex, dirIndex, None)
  def manualInputCall: Call = routes.DirectorAddressController.onPageLoad(NormalMode, estIndex, dirIndex, None)

  private val addressLookupConnector = mock[AddressLookupConnector]
  private val address = TolerantAddress(Some("value 1"), Some("value 2"), None, None, Some("AB1 1AB"), Some("GB"))

  val formProvider = new PostCodeLookupFormProvider()
  val form = formProvider()

  implicit val hc: HeaderCarrier = mock[HeaderCarrier]

  val director = PersonName("first", "last")

  lazy val viewmodel = PostcodeLookupViewModel(
    postCall,
    manualInputCall,
    Message("messages__directorCompanyAddressPostcodeLookup__title"),
    Message("messages__addressPostcodeLookup__heading", director.fullName),
    Some(director.fullName)
  )
  private val view = injector.instanceOf[postcodeLookup]

  "render the view correctly on a GET request" in {
    running(_.overrides(modules(getMandatoryEstablisherCompanyDirectorWithDirectorName): _*)) {
      app =>
        val controller = app.injector.instanceOf[DirectorAddressPostcodeLookupController]
        val result = controller.onPageLoad(NormalMode, establisherIndex = 0, directorIndex = 0, None)(fakeRequest)
        status(result) mustBe OK
        contentAsString(result) mustBe view(form, viewmodel, None)(fakeRequest, messages).toString
    }
  }

  "redirect to the next page on a POST request" in {
    val validPostcode = "ZZ1 1ZZ"
    running(_.overrides(modules(getMandatoryEstablisherCompanyDirectorWithDirectorName) ++
      Seq[GuiceableModule](bind[Navigator].qualifiedWith(classOf[EstablishersCompanyDirector]).toInstance(new FakeNavigator(onwardRoute)),
        bind[UserAnswersService].toInstance(FakeUserAnswersService),
        bind[AddressLookupConnector].toInstance(addressLookupConnector)
      ): _*)) {
      app =>
        when(addressLookupConnector.addressLookupByPostCode(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Seq(address)))
        val controller = app.injector.instanceOf[DirectorAddressPostcodeLookupController]
        val postRequest = fakeRequest.withFormUrlEncodedBody("postcode" -> validPostcode)
        val result = controller.onSubmit(NormalMode, establisherIndex = 0, directorIndex = 0, None)(postRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
    }
  }
}
