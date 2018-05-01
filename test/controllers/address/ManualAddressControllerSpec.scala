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

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.{DataCacheConnector, FakeDataCacheConnector}
import forms.address.AddressFormProvider
import identifiers.TypedIdentifier
import models.NormalMode
import models.address.Address
import models.requests.DataRequest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.inject._
import play.api.libs.json.Json
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.domain.PsaId
import utils._
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddress

import scala.concurrent.Future

object ManualAddressControllerSpec {

  val fakeIdentifier = new TypedIdentifier[Address]{
    override def toString = "testPath"
  }

  class TestController @Inject()(
                                  override val appConfig: FrontendAppConfig,
                                  override val messagesApi: MessagesApi,
                                  override val dataCacheConnector: DataCacheConnector,
                                  override val navigator: Navigator,
                                  override val countryOptions: CountryOptions,
                                  formProvider: AddressFormProvider
                                ) extends ManualAddressController {


    def onPageLoad(viewModel: ManualAddressViewModel, answers: UserAnswers): Future[Result] =
      get(fakeIdentifier, viewModel)(DataRequest(FakeRequest(), "cacheId", answers, PsaId("A0000000")))

    def onSubmit(viewModel: ManualAddressViewModel, answers: UserAnswers, request: Request[AnyContent] = FakeRequest()): Future[Result] =
      post(fakeIdentifier, viewModel, NormalMode)(DataRequest(request, "cacheId", answers, PsaId("A0000000")))

    override protected val form: Form[Address] = formProvider()
  }

}

class ManualAddressControllerSpec extends WordSpec with MustMatchers with MockitoSugar with ScalaFutures with OptionValues {

  import ManualAddressControllerSpec._

  val addressData: Map[String, String] = Map(
    "addressLine1" -> "address line 1",
    "addressLine2" -> "address line 2",
    "addressLine3" -> "address line 3",
    "addressLine4" -> "address line 4",
    "postCode" -> "AB1 1AP",
    "country" -> "GB"
  )

  val countryOptions = new CountryOptions(
    Seq(InputOption("GB", "GB"))
  )

  val viewModel = ManualAddressViewModel(
    Call("GET", "/"),
    countryOptions.options,
    "title",
    "heading",
    Some("secondary.header")
  )

  "get" must {
    "return OK with view" when {
      "data is retrieved" in {

        running(_.overrides(
          bind[CountryOptions].to(new CountryOptions(
            Seq(InputOption("GB", "GB"))
          )),
          bind[Navigator].to(FakeNavigator)
        )) {
          app =>

            val request = FakeRequest()

            val appConfig = app.injector.instanceOf[FrontendAppConfig]
            val formProvider = app.injector.instanceOf[AddressFormProvider]
            val messages = app.injector.instanceOf[MessagesApi].preferred(request)
            val controller = app.injector.instanceOf[TestController]

            val result = controller.onPageLoad(viewModel, UserAnswers())

            status(result) mustEqual OK
            contentAsString(result) mustEqual manualAddress(appConfig, formProvider(), viewModel)(request, messages).toString

        }

      }
      "data is not retrieved" in {
        running(_.overrides(
          bind[CountryOptions].to(new CountryOptions(
            Seq(InputOption("GB", "GB"))
          )),
          bind[Navigator].to(FakeNavigator)
        )) {
          app =>

            val testAddress = Address(
              "address line 1",
              "address line 2",
              Some("test town"),
              Some("test county"),
              Some("test post code"), "GB"
            )

            val request = FakeRequest()

            val appConfig = app.injector.instanceOf[FrontendAppConfig]
            val formProvider = app.injector.instanceOf[AddressFormProvider]
            val messages = app.injector.instanceOf[MessagesApi].preferred(request)
            val controller = app.injector.instanceOf[TestController]

            val result = controller.onPageLoad(viewModel, UserAnswers(Json.obj(fakeIdentifier.toString -> testAddress)))

            status(result) mustEqual OK
            contentAsString(result) mustEqual manualAddress(appConfig, formProvider().fill(testAddress), viewModel)(request, messages).toString

        }
      }
    }
  }

  "post" must {

    "redirect to the postCall on valid data request" which {
      "will save address to answers" in {

        val onwardRoute = Call("GET", "/")

        val navigator = new FakeNavigator(onwardRoute, NormalMode)

        running(_.overrides(
          bind[CountryOptions].to(new CountryOptions(Seq(InputOption("GB", "GB")))),
          bind[DataCacheConnector].to(FakeDataCacheConnector),
          bind[Navigator].to(navigator)
        )) {
          app =>

            val controller = app.injector.instanceOf[TestController]

            val result = controller.onSubmit(viewModel, UserAnswers(), FakeRequest().withFormUrlEncodedBody(
              ("addressLine1", "value 1"),
              ("addressLine2", "value 2"),
              ("postCode", "AB1 1AB"),
              "country" -> "GB")
            )

            status(result) mustEqual SEE_OTHER
            redirectLocation(result) mustEqual Some(onwardRoute.url)

            FakeDataCacheConnector.verify(fakeIdentifier, Address(
              "value 1", "value 2", None, None, Some("AB1 1AB"), "GB"
            ))
        }

      }
    }

    "return BAD_REQUEST with view on invalid data request" in {

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
            .withError("addressLine1", "messages__error__address_line_1_required")
            .withError("addressLine2", "messages__error__address_line_2_required")
            .withError("country", "messages__error_country_required")

          val result = controller.onSubmit(viewModel, UserAnswers(), request.withFormUrlEncodedBody())

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual manualAddress(appConfig, form, viewModel)(request, messages).toString
      }

    }

  }

}
