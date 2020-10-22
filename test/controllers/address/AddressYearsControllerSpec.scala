/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers.address

import akka.stream.Materializer
import base.SpecBase
import com.google.inject.Inject
import config.FrontendAppConfig
import forms.address.AddressYearsFormProvider
import identifiers.TypedIdentifier
import models.requests.DataRequest
import models.{AddressYears, NormalMode}
import navigators.Navigator
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.{MustMatchers, OptionValues}
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UserAnswersService
import uk.gov.hmrc.domain.PsaId
import utils.{FakeNavigator, UserAnswers}
import viewmodels.address.AddressYearsViewModel
import views.html.address.addressYears

import scala.concurrent.{ExecutionContext, Future}

object AddressYearsControllerSpec {

  object FakeIdentifier extends TypedIdentifier[AddressYears]

  class TestController @Inject()(
                                  override val appConfig: FrontendAppConfig,
                                  override val messagesApi: MessagesApi,
                                  override val userAnswersService: UserAnswersService,
                                  override val navigator: Navigator,
                                  formProvider: AddressYearsFormProvider,
                                  val controllerComponents: MessagesControllerComponents,
                                  val view: addressYears
                                )(implicit val ec: ExecutionContext) extends AddressYearsController {

    def onPageLoad(viewmodel: AddressYearsViewModel, answers: UserAnswers): Future[Result] = {
      implicit def request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "cacheId", answers, Some(PsaId("A0000000")))
      get(FakeIdentifier, formProvider("error"), viewmodel)
    }

    def onSubmit(viewmodel: AddressYearsViewModel, answers: UserAnswers, fakeRequest: Request[AnyContent]): Future[Result] = {
      implicit def request: DataRequest[AnyContent] = DataRequest(fakeRequest, "cacheId", answers, Some(PsaId("A0000000")))
      post(FakeIdentifier, NormalMode, formProvider("error"), viewmodel)
    }
  }

}

class AddressYearsControllerSpec extends SpecBase with MustMatchers with OptionValues with ScalaFutures with MockitoSugar {

  import AddressYearsControllerSpec._

  private val view = injector.instanceOf[addressYears]

  val viewmodel: AddressYearsViewModel = AddressYearsViewModel(
    postCall = Call("GET", "www.example.com"),
    title = "title",
    heading = "heading",
    legend = "legend"
  )

  "get" must {

    "return a successful result when there is no existing answer" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>

          implicit val materializer: Materializer = app.materializer

          val formProvider = app.injector.instanceOf[AddressYearsFormProvider]
          val request = FakeRequest()
          val messages = app.injector.instanceOf[MessagesApi].preferred(request)
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onPageLoad(viewmodel, UserAnswers())

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(formProvider("error")(messages), viewmodel, None)(request, messages).toString
      }
    }

    "return a successful result when there is an existing answer" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>

          implicit val materializer: Materializer = app.materializer

          val formProvider = app.injector.instanceOf[AddressYearsFormProvider]
          val request = FakeRequest()
          val messages = app.injector.instanceOf[MessagesApi].preferred(request)
          val controller = app.injector.instanceOf[TestController]
          val answers = UserAnswers().set(FakeIdentifier)(AddressYears.OverAYear).asOpt.value
          val result = controller.onPageLoad(viewmodel, answers)

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            formProvider("error")(messages).fill(AddressYears.OverAYear),
            viewmodel,
            None
          )(request, messages).toString
      }
    }
  }

  "post" must {

    "return a redirect when the submitted data is valid" in {

      import play.api.inject._

      val userAnswersService = mock[UserAnswersService]

      running(_.overrides(
        bind[UserAnswersService].toInstance(userAnswersService),
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>

          implicit val materializer: Materializer = app.materializer

          when(userAnswersService.save[AddressYears, FakeIdentifier.type](
            eqTo(NormalMode), eqTo(None),
            eqTo(FakeIdentifier), any())(any(), any(), any(), any())
          ) thenReturn Future.successful(UserAnswers().json)

          val request = FakeRequest().withFormUrlEncodedBody(
            "value" -> AddressYears.OverAYear.toString
          )
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onSubmit(viewmodel, UserAnswers(), request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual "www.example.com"
      }
    }

    "return a bad request when the submitted data is invalid" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>

          implicit val materializer: Materializer = app.materializer

          val formProvider = app.injector.instanceOf[AddressYearsFormProvider]
          val request = FakeRequest()
          val messages = app.injector.instanceOf[MessagesApi].preferred(request)
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onSubmit(viewmodel, UserAnswers(), request)

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(
            formProvider("error")(messages).bind(Map.empty[String, String]),
            viewmodel,
            None
          )(request, messages).toString
      }
    }
  }
}
