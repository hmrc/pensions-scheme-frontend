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

package controllers.register.establishers.partnership

import base.CSRFRequest
import services.{FakeUserAnswersService, UserAnswersService}
import controllers.ControllerSpecBase
import controllers.actions.{AuthAction, DataRetrievalAction, FakeAuthAction}
import forms.VatFormProvider
import models.{Index, NormalMode}
import navigators.Navigator
import org.scalatest.MustMatchers
import play.api.Application
import play.api.http.Writeable
import play.api.inject.bind
import play.api.mvc.{Call, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.annotations.EstablisherPartnership
import utils.FakeNavigator
import viewmodels.{Message, EnterVATViewModel}
import views.html.vat

import scala.concurrent.Future

class PartnershipVatControllerSpec extends ControllerSpecBase with MustMatchers with CSRFRequest {

  import PartnershipVatControllerSpec._

  "PartnershipVatController" must {

    "render the view correctly on a GET request" in {
      requestResult(
        implicit app => addToken(FakeRequest(routes.PartnershipVatController.onPageLoad(NormalMode, firstIndex, None))),
        (request, result) => {
          status(result) mustBe OK
          contentAsString(result) mustBe vat(frontendAppConfig, form, viewModel, None)(request, messages).toString()
        }
      )
    }

    "redirect to the next page on a POST request" in {
      requestResult(
        implicit app => addToken(FakeRequest(routes.PartnershipVatController.onSubmit(NormalMode, firstIndex, None))
          .withFormUrlEncodedBody(("vat.hasVat", "true"), ("vat.vat", "123456789"))),
        (_, result) => {
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(onwardRoute.url)
        }
      )
    }

  }

}

object PartnershipVatControllerSpec extends PartnershipVatControllerSpec {

  val form = new VatFormProvider()()
  val firstIndex = Index(0)

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val viewModel = EnterVATViewModel(
    routes.PartnershipVatController.onSubmit(NormalMode, firstIndex, None),
    title = Message("messages__partnershipVat__title"),
    heading = Message("messages__partnershipVat__heading"),
    hint = Message("messages__common__vat__hint"),
    subHeading = Some(Message("test partnership name"))
  )

  private def requestResult[T](request: Application => Request[T], test: (Request[_], Future[Result]) => Unit)
                              (implicit writeable: Writeable[T]): Unit = {

    running(_.overrides(
      bind[AuthAction].to(FakeAuthAction),
      bind[DataRetrievalAction].toInstance(getMandatoryEstablisherPartnership),
      bind(classOf[Navigator]).toInstance(new FakeNavigator(onwardRoute)),
      bind[UserAnswersService].toInstance(FakeUserAnswersService)
    )) {
      app =>
        val req = request(app)
        val result = route[T](app, req).value
        test(req, result)
    }
  }

}
