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

package controllers.address

import akka.stream.Materializer
import com.google.inject.Inject
import play.api.inject._
import config.FrontendAppConfig
import connectors.DataCacheConnector
import forms.address.AddressFormProvider
import identifiers.TypedIdentifier
import models.address.Address
import models.register.CountryOptions
import models.requests.DataRequest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Assertion, MustMatchers, OptionValues, WordSpec}
import play.api.Application
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.{InputOption, Navigator, UserAnswers}
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddress

import scala.concurrent.Future

object ManualAddressControllerSpec {

  class TestController @Inject()(
                                  override val appConfig: FrontendAppConfig,
                                  override val messagesApi: MessagesApi,
                                  override val cacheConnector: DataCacheConnector,
                                  override val navigator: Navigator,
                                  override val countryOptions: CountryOptions,
                                  formProvider: AddressFormProvider
                                ) extends ManualAddressController {

    object FakeIdentifier extends TypedIdentifier[Address]

    def onPageLoad(viewModel: ManualAddressViewModel, answers: UserAnswers): Future[Result] =
      get(FakeIdentifier, viewModel)(DataRequest(FakeRequest(), "cacheId", answers))

    def onSubmit(viewModel: ManualAddressViewModel, answers: UserAnswers, request: Request[AnyContent] = FakeRequest()): Future[Result] =
      post(FakeIdentifier, viewModel)(DataRequest(request, "cacheId", answers))

    private val invalidError: Message = "foo"

    private val noResultError: Message = "bar"

    override protected def form: Form[Address] = formProvider()
  }

}

class ManualAddressControllerSpec extends WordSpec with MustMatchers with MockitoSugar with ScalaFutures with OptionValues {

  import ManualAddressControllerSpec._

  val addressData: Map[String, String] = Map(
    "addressLine1" -> "address line 1",
    "addressLine2" -> "address line 2",
    "addressLine3" -> "address line 3",
    "addressLine4" -> "address line 4",
    "postCode.postCode" -> "AB1 1AP",
    "country" -> "GB"
  )

  val viewModel = ManualAddressViewModel(
    Call("GET", "/"),
    "title",
    "heading",
    Some("secondary.header")
  )

  val countryOptions = new CountryOptions(
    Seq(InputOption("GB", "GB"))
  )

  "get" must {
    "return OK with view" in {

      running(_.overrides(
        bind[CountryOptions].to(new CountryOptions(
          Seq(InputOption("GB", "GB"))
        ))
      )) {
        app =>

          val request = FakeRequest()

          val appConfig = app.injector.instanceOf[FrontendAppConfig]
          val formProvider = app.injector.instanceOf[AddressFormProvider]
          val messages = app.injector.instanceOf[MessagesApi].preferred(request)
          val controller = app.injector.instanceOf[TestController]

          val result = controller.onPageLoad(viewModel, UserAnswers())

          status(result) mustEqual OK
          contentAsString(result) mustEqual manualAddress(appConfig, formProvider(), viewModel, countryOptions.options)(request, messages).toString

      }

    }
  }

  "post" must {

    "redirect to the postCall on valid data request" in {

      running(_.overrides(
        bind[CountryOptions].to(new CountryOptions(
          Seq(InputOption("GB", "GB"))
        ))
      )) {
        app =>

          val controller = app.injector.instanceOf[TestController]

          val result = controller.onSubmit(viewModel, UserAnswers(), FakeRequest().withFormUrlEncodedBody(
            ("addressLine1", "value 1"),
            ("addressLine2", "value 2"),
            ("postCode.postCode", "AB1 1AB"),
            "country" -> "GB")
          )

          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustEqual Some(viewModel.postCall.url)
      }

    }

    "return BAD_REQUEST with view" in {

      running(_.overrides(
        bind[CountryOptions].to(new CountryOptions(
          Seq(InputOption("GB", "GB"))
        ))
      )) {
        app =>

          val request = FakeRequest()

          val appConfig = app.injector.instanceOf[FrontendAppConfig]
          val formProvider = app.injector.instanceOf[AddressFormProvider]
          val messages = app.injector.instanceOf[MessagesApi].preferred(request)
          val controller = app.injector.instanceOf[TestController]

          val form = formProvider()
            .withError("addressLine1", "messages__error__addr1")
            .withError("addressLine2", "messages__error__addr2")
            .withError("country", "messages__error__scheme_country")

          val result = controller.onSubmit(viewModel, UserAnswers(), request.withFormUrlEncodedBody())

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual manualAddress(appConfig, form, viewModel, countryOptions.options)(request, messages).toString
      }

    }

  }

}
