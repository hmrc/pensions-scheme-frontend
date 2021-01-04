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

import controllers.ControllerSpecBase
import forms.UTRFormProvider
import models.{Index, NormalMode}
import navigators.Navigator
import org.scalatest.MustMatchers
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.mvc.Call
import play.api.test.Helpers.{contentAsString, status, _}
import services.{FakeUserAnswersService, UserAnswersService}
import utils.FakeNavigator
import utils.annotations.EstablishersCompanyDirector
import viewmodels.{Message, UTRViewModel}
import views.html.utr

class DirectorEnterUTRControllerSpec extends ControllerSpecBase with MustMatchers {

  import DirectorEnterUTRControllerSpec._

  "DirectorEnterUTRController" must {
    "render the view correctly on a GET request" in {
      running(_.overrides(modules(getMandatoryEstablisherCompanyDirectorWithDirectorName): _*)) {
        app =>
          val controller = app.injector.instanceOf[DirectorEnterUTRController]
          val result = controller.onPageLoad(NormalMode, establisherIndex = 0, directorIndex = 0, srn)(fakeRequest)
          status(result) mustBe OK
          contentAsString(result) mustBe view(form, viewModel, Some("pension scheme details"))(fakeRequest, messages).toString()
      }
    }

    "redirect to the next page on a POST request" in {
      running(_.overrides(modules(getMandatoryEstablisherCompanyDirectorWithDirectorName) ++
        Seq[GuiceableModule](bind[Navigator].qualifiedWith(classOf[EstablishersCompanyDirector]).toInstance(new FakeNavigator(onwardRoute)),
          bind[UserAnswersService].toInstance(FakeUserAnswersService)
        ): _*)) {
        app =>
          val controller = app.injector.instanceOf[DirectorEnterUTRController]
          val postRequest = fakeRequest.withFormUrlEncodedBody(("utr", "1234567890"))
          val result = controller.onSubmit(NormalMode, establisherIndex = 0, directorIndex = 0, None)(postRequest)
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(onwardRoute.url)
      }
    }
  }
}

object DirectorEnterUTRControllerSpec extends DirectorEnterUTRControllerSpec {
  val establisherIndex = Index(0)
  val directorIndex = Index(0)
  val form = new UTRFormProvider()()
  val firstIndex = Index(0)
  val srn = None

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val viewModel = UTRViewModel(
    routes.DirectorEnterUTRController.onSubmit(NormalMode, establisherIndex, directorIndex, srn),
    title = Message("messages__enterUTR", Message("messages__theDirector")),
    heading = Message("messages__enterUTR", "first last"),
    hint = Message("messages_utr__hint"),
    srn = srn
  )

  private val view = injector.instanceOf[utr]
}
