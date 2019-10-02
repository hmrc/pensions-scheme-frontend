/*
 * Copyright 2019 HM Revenue & Customs
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

package controllers.register.establishers.individual

import controllers.ControllerSpecBase
import forms.register.PersonNameFormProvider
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.individual.EstablisherNameId
import models.person.PersonName
import models.{Index, NormalMode}
import navigators.Navigator
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import play.api.data.Form
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import services.UserAnswersService
import utils.FakeNavigator
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.personName

import scala.concurrent.Future

class EstablisherNameControllerSpec extends ControllerSpecBase with OneAppPerSuite {
  
  import EstablisherNameControllerSpec._

  private val viewmodel = CommonFormWithHintViewModel(
    routes.EstablisherNameController.onSubmit(NormalMode, index, None),
    title = Message("messages__individualName__title"),
    heading = Message("messages__individualName__heading"))

  def viewAsString(form: Form[_] = form): String = personName(
    frontendAppConfig,
    form,
    viewmodel,
    None
  )(fakeRequest, messages).toString

  private val postRequest = fakeRequest.withFormUrlEncodedBody(("firstName", "Test"), ("lastName", "Name"))

  "EstablisherNameController" must {
    "return OK and the correct view for a GET" in {
      val app = applicationBuilder(getEmptyData, featureSwitchEnabled = true).build()

      val controller = app.injector.instanceOf[EstablisherNameController]

      val result = controller.onPageLoad(NormalMode, index, None)(fakeRequest)

      status(result) mustBe OK

      contentAsString(result) mustBe viewAsString()

      app.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val app = applicationBuilder(getMandatoryEstablisher, featureSwitchEnabled = true).build()

      val controller = app.injector.instanceOf[EstablisherNameController]

      val result = controller.onPageLoad(NormalMode, index, None)(fakeRequest)

      status(result) mustBe OK

      contentAsString(result) mustBe viewAsString(form.fill(PersonName("test first name", "test last name")))

      app.stop()
    }

    "redirect to the next page when valid data is submitted" in {
      val validData = Json.obj(
        EstablishersId.toString -> Json.arr(
          Json.obj(
            EstablisherNameId.toString -> PersonName("Test", "Name")
          )
        )
      )

      when(mockUserAnswersService.save(any(), any(), any(), any())(any(), any(), any(), any())).thenReturn(Future.successful(validData))

      val app = applicationBuilder(getEmptyData, featureSwitchEnabled = true)
        .overrides(
          bind[UserAnswersService].toInstance(mockUserAnswersService),
          bind(classOf[Navigator]).toInstance(new FakeNavigator(onwardRoute))
        ).build()

      val controller = app.injector.instanceOf[EstablisherNameController]

      val result = controller.onSubmit(NormalMode, index, None)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)

      app.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val app = applicationBuilder(getEmptyData, featureSwitchEnabled = true).build()

      val controller = app.injector.instanceOf[EstablisherNameController]

      val postRequest = fakeRequest.withFormUrlEncodedBody(("firstName", "01"), ("lastName", "?&^%$£"))

      val result = controller.onSubmit(NormalMode, index, None)(postRequest)

      status(result) mustBe BAD_REQUEST

      app.stop()
    }

    "return a Bad Request and errors when no data is submitted" in {
      val app = applicationBuilder(getEmptyData, featureSwitchEnabled = true).build()

      val controller = app.injector.instanceOf[EstablisherNameController]

      val result = controller.onSubmit(NormalMode, index, None)(fakeRequest)

      status(result) mustBe BAD_REQUEST

      app.stop()
    }
  }
}


object EstablisherNameControllerSpec extends ControllerSpecBase with MockitoSugar {
  private val formProvider: PersonNameFormProvider = new PersonNameFormProvider()
  private val form: Form[PersonName] = formProvider("messages__error__establisher")

  private val index: Index = Index(0)
  private val mockUserAnswersService: UserAnswersService = mock[UserAnswersService]
  private def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()
}


