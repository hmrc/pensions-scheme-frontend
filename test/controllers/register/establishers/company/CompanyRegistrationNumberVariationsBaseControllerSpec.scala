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

package controllers.register.establishers.company

import akka.stream.Materializer
import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.register.CompanyRegistrationNumberVariationsBaseController
import forms.CompanyRegistrationNumberVariationsFormProvider
import identifiers.TypedIdentifier
import identifiers.register.establishers.company.CompanyRegistrationNumberVariationsId
import models._
import models.requests.DataRequest
import navigators.Navigator
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.mvc.{AnyContent, Call, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{FakeUserAnswersService, UserAnswersService}
import uk.gov.hmrc.domain.PsaId
import utils.{FakeNavigator, UserAnswers}
import viewmodels.{CompanyRegistrationNumberViewModel, Message}
import views.html.register.companyRegistrationNumberVariations

import scala.concurrent.{ExecutionContext, Future}

class CompanyRegistrationNumberVariationsBaseControllerSpec extends WordSpec with MustMatchers with OptionValues with ScalaFutures with MockitoSugar {

  import CompanyRegistrationNumberVariationsBaseControllerSpec._

  val postCall = routes.CompanyEmailController.onSubmit _

  "get" must {

    "return a successful result when there is no existing answer" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>

          implicit val materializer: Materializer = app.materializer

          val appConfig = app.injector.instanceOf[FrontendAppConfig]
          val formProvider = app.injector.instanceOf[CompanyRegistrationNumberVariationsFormProvider]
          val request = FakeRequest()
          val messages = app.injector.instanceOf[MessagesApi].preferred(request)
          val controller = app.injector.instanceOf[TestBaseController]
          val result = controller.onPageLoad(UserAnswers())
          val postCall = routes.CompanyEmailController.onSubmit _

          status(result) mustEqual OK
          contentAsString(result) mustEqual companyRegistrationNumberVariations(
            appConfig,
            viewModel(),
            formProvider(companyName)(messages),
            None,
            postCall(NormalMode, None, firstIndex),
            None
          )(request, messages).toString

      }
    }

    "return a successful result when there is an existing answer" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>

          implicit val materializer: Materializer = app.materializer

          val appConfig = app.injector.instanceOf[FrontendAppConfig]
          val formProvider = app.injector.instanceOf[CompanyRegistrationNumberVariationsFormProvider]
          val request = FakeRequest()
          val messages = app.injector.instanceOf[MessagesApi].preferred(request)
          val controller = app.injector.instanceOf[TestBaseController]
          val answers = UserAnswers().set(CompanyRegistrationNumberVariationsId(firstIndex))(ReferenceValue("123456789")).get
          val result = controller.onPageLoad(answers)

          status(result) mustEqual OK
          contentAsString(result) mustEqual companyRegistrationNumberVariations(
            appConfig,
            viewModel(),
            formProvider(companyName)(messages).fill(ReferenceValue("123456789")),
            None,
            postCall(NormalMode, None, firstIndex),
            None
          )(request, messages).toString

      }
    }

  }

  "post" must {

    "return a redirect when the submitted data is valid" in {

      import play.api.inject._

      running(_.overrides(
        bind[UserAnswersService].toInstance(FakeUserAnswersService),
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>

          implicit val materializer: Materializer = app.materializer

          val request = FakeRequest().withFormUrlEncodedBody(
            ("companyRegistrationNumber", "12345678")
          )
          val controller = app.injector.instanceOf[TestBaseController]
          val result = controller.onSubmit(UserAnswers(), request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual "www.example.com"
          FakeUserAnswersService.verify(CompanyRegistrationNumberVariationsId(0), ReferenceValue("12345678", isEditable = true))
      }
    }
  }

  "return a bad request when the submitted data is invalid" in {

    running(_.overrides(
      bind[Navigator].toInstance(FakeNavigator)
    )) {
      app =>

        implicit val materializer: Materializer = app.materializer

        val appConfig = app.injector.instanceOf[FrontendAppConfig]
        val formProvider = app.injector.instanceOf[CompanyRegistrationNumberVariationsFormProvider]
        val controller = app.injector.instanceOf[TestBaseController]
        val request = FakeRequest().withFormUrlEncodedBody(("companyRegistrationNumber", "123456789012345"))

        val messages = app.injector.instanceOf[MessagesApi].preferred(request)

        val result = controller.onSubmit(UserAnswers(), request)

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual companyRegistrationNumberVariations(
          appConfig,
          viewModel(),
          formProvider(companyName)(messages).bind(Map("companyRegistrationNumber" -> "123456789012345")),
          None,
          postCall(NormalMode, None, firstIndex),
          None
        )(request, messages).toString
    }
  }
}

object CompanyRegistrationNumberVariationsBaseControllerSpec {

  val firstIndex = Index(0)
  val companyName = "test company name"

  def viewModel(companyName: String = companyName): CompanyRegistrationNumberViewModel = {
    CompanyRegistrationNumberViewModel(
      title = Message("messages__companyNumber__establisher__title"),
      heading = Message("messages__companyNumber__establisher__heading", companyName),
      hint = Message("messages__common__crn_hint", companyName)
    )
  }

  object FakeIdentifier extends TypedIdentifier[ReferenceValue]

  class TestBaseController @Inject()(
                                  override val appConfig: FrontendAppConfig,
                                  override val messagesApi: MessagesApi,
                                  override val userAnswersService: UserAnswersService,
                                  override val navigator: Navigator
                                )(implicit val ec: ExecutionContext) extends CompanyRegistrationNumberVariationsBaseController {

    def postCall: (Mode, Option[String], Index) => Call = routes.CompanyEmailController.onSubmit _

    override def identifier(index: Int): TypedIdentifier[ReferenceValue] = CompanyRegistrationNumberVariationsId(index)

    override protected def form(name: String): Form[ReferenceValue] = formProvider(name)

    def onPageLoad(answers: UserAnswers): Future[Result] = {
      get(NormalMode, None, firstIndex, viewModel(companyName), companyName)(DataRequest(FakeRequest(), "cacheId", answers, PsaId("A0000000")))
    }

    def onSubmit(answers: UserAnswers, fakeRequest: Request[AnyContent]): Future[Result] = {
      post(NormalMode, None, firstIndex, viewModel(companyName), companyName)(DataRequest(fakeRequest, "cacheId", answers, PsaId("A0000000")))
    }
  }

}
