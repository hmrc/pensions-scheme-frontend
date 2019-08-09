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

package controllers.address

import com.google.inject.Inject
import config.FrontendAppConfig
import forms.address.AddressListFormProvider
import identifiers.TypedIdentifier
import models._
import models.address.{Address, TolerantAddress}
import models.requests.DataRequest
import navigators.Navigator
import org.scalatest.{Matchers, OptionValues, WordSpec}
import play.api.Application
import play.api.i18n.MessagesApi
import play.api.mvc.{Call, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{FakeUserAnswersService, UserAnswersService}
import uk.gov.hmrc.domain.PsaId
import utils.{FakeNavigator, UserAnswers}
import viewmodels.Message
import viewmodels.address.AddressListViewModel
import views.html.address.addressList

import scala.concurrent.{ExecutionContext, Future}

class AddressListControllerSpec extends WordSpec with Matchers with OptionValues {

  import AddressListControllerSpec._

  "get" must {

    "return Ok and the correct view when no addresses" in {

      running(_.overrides()) { app =>
        val viewModel = addressListViewModel(Nil)
        val controller = app.injector.instanceOf[TestController]
        val result = controller.onPageLoad(viewModel)

        status(result) shouldBe OK
        contentAsString(result) shouldBe viewAsString(app, viewModel, None)
      }

    }

    "return Ok and the correct view when addresses are supplied" in {

      running(_.overrides()) { app =>
        val viewModel = addressListViewModel()
        val controller = app.injector.instanceOf[TestController]
        val result = controller.onPageLoad(viewModel)

        status(result) shouldBe OK
        contentAsString(result) shouldBe viewAsString(app, viewModel, None)
      }

    }

  }

  "post" must {

    "return See Other on submission of valid data" in {

      running(_.overrides()) { app =>
        val controller = app.injector.instanceOf[TestController]
        val result = controller.onSubmit(addressListViewModel(), 0)

        status(result) shouldBe SEE_OTHER
      }

    }

    "redirect to the page specified by the navigator following submission of valid data" in {

      running(_.overrides()) { app =>
        val controller = app.injector.instanceOf[TestController]
        val result = controller.onSubmit(addressListViewModel(), 0)

        redirectLocation(result) shouldBe Some(onwardRoute.url)
      }

    }

    "save the user answer on submission of valid data" in {

      running(_.overrides()) { app =>
        val viewModel = addressListViewModel()
        val controller = app.injector.instanceOf[TestController]
        val result = controller.onSubmit(viewModel, 0)

        status(result) shouldBe SEE_OTHER
        FakeUserAnswersService.userAnswer.get(FakeSelectedAddressIdentifier).value shouldBe viewModel.addresses.head
      }

    }

    "delete any existing address on submission of valid data" in {

      running(_.overrides()) { app =>
        val viewModel = addressListViewModel()
        val controller = app.injector.instanceOf[TestController]
        val result = controller.onSubmit(viewModel, 0)

        status(result) shouldBe SEE_OTHER
        FakeUserAnswersService.userAnswer.get(FakeAddressIdentifier) shouldBe None
      }

    }

    "return Bad Request and the correct view on submission of invalid data" in {

      running(_.overrides()) { app =>
        val viewModel = addressListViewModel()
        val controller = app.injector.instanceOf[TestController]
        val result = controller.onSubmit(viewModel, -1)

        status(result) shouldBe BAD_REQUEST
        contentAsString(result) shouldBe viewAsString(app, viewModel, Some(-1))
      }

    }
  }

}

object AddressListControllerSpec {

  class TestController @Inject()(
                                  override val appConfig: FrontendAppConfig,
                                  override val messagesApi: MessagesApi
                                )(implicit val ec: ExecutionContext) extends AddressListController {

    override protected def userAnswersService: UserAnswersService = FakeUserAnswersService

    override protected def navigator: Navigator = new FakeNavigator(onwardRoute)

    def onPageLoad(viewModel: AddressListViewModel): Future[Result] = {

      get(
        viewModel
      )(DataRequest(FakeRequest(), "cacheId", UserAnswers(), PsaId("A0000000")))

    }

    def onSubmit(viewModel: AddressListViewModel, value: Int): Future[Result] = {

      val request = FakeRequest().withFormUrlEncodedBody("value" -> value.toString)

      post(
        viewModel,
        FakeSelectedAddressIdentifier,
        FakeAddressIdentifier,
        NormalMode
      )(DataRequest(request, "cacheId", UserAnswers(), PsaId("A0000000")))

    }
  }

  val tolerantAddress = TolerantAddress(Some("address line 1"), Some("address line 2"), None, None, Some("ZZ1 1ZZ"), Some("GB"))
  val address = Address("address line 1", "address line 2", None, None, Some("ZZ1 1ZZ"), "GB")
  object FakeAddressIdentifier extends TypedIdentifier[Address]
  object FakeSelectedAddressIdentifier extends TypedIdentifier[TolerantAddress]

  val onwardRoute: Call = controllers.routes.IndexController.onPageLoad()


  private lazy val postCall = controllers.routes.IndexController.onPageLoad()
  private lazy val manualInputCall = controllers.routes.SessionExpiredController.onPageLoad()

  private val addresses = Seq(
    TolerantAddress(
      Some("Address 1 Line 1"),
      Some("Address 1 Line 2"),
      Some("Address 1 Line 3"),
      Some("Address 1 Line 4"),
      Some("A1 1PC"),
      Some("GB")
    ),
    TolerantAddress(
      Some("Address 2 Line 1"),
      Some("Address 2 Line 2"),
      Some("Address 2 Line 3"),
      Some("Address 2 Line 4"),
      Some("123"),
      Some("FR")
    )
  )

  def addressListViewModel(addresses: Seq[TolerantAddress] = addresses): AddressListViewModel =
    AddressListViewModel(
      postCall = postCall,
      manualInputCall = manualInputCall,
      addresses = addresses,
      title = Message("title text"),
      heading = Message("heading text"),
      selectAddress = Message("select an address text"),
      selectAddressLink = Message("select an address link text")
    )

  def viewAsString(app: Application, viewModel: AddressListViewModel, value: Option[Int]): String = {

    val appConfig = app.injector.instanceOf[FrontendAppConfig]
    val request = FakeRequest()
    val messages = app.injector.instanceOf[MessagesApi].preferred(request)

    val form = value match {
      case Some(i) => new AddressListFormProvider()(viewModel.addresses).bind(Map("value" -> i.toString))
      case None => new AddressListFormProvider()(viewModel.addresses)
    }

    addressList(appConfig, form, viewModel, None)(request, messages).toString()

  }

}
