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

package controllers.address

import audit.testdoubles.StubSuccessfulAuditService
import audit.{AddressAction, AddressEvent, AuditService}
import base.SpecBase
import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.address.ManualAddressControllerSpec._
import forms.address.AddressListFormProvider
import identifiers.TypedIdentifier
import models._
import models.address.{Address, TolerantAddress}
import models.requests.DataRequest
import navigators.Navigator
import org.scalatest.OptionValues
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.{Call, MessagesControllerComponents, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{FakeUserAnswersService, UserAnswersService}
import uk.gov.hmrc.domain.PsaId
import utils.{FakeNavigator, UserAnswers}
import viewmodels.Message
import viewmodels.address.AddressListViewModel
import views.html.address.addressList

import scala.concurrent.{ExecutionContext, Future}

class AddressListControllerSpec extends SpecBase with OptionValues {

  def viewAsString(viewModel: AddressListViewModel, value: Option[Int]): String = {

    val request = FakeRequest()
    val messages = injector.instanceOf[MessagesApi].preferred(request)
    val view = injector.instanceOf[addressList]
    val form = value match {
      case Some(i) => new AddressListFormProvider()(viewModel.addresses).bind(Map("value" -> i.toString))
      case None => new AddressListFormProvider()(viewModel.addresses)
    }

    view(form, viewModel, None)(request, messages).toString()

  }

  import AddressListControllerSpec._

  "get" must {

    "return Ok and the correct view when no addresses" in {

      running(_.overrides()) { app =>
        val viewModel = addressListViewModel(Nil)
        val controller = app.injector.instanceOf[TestController]
        val result = controller.onPageLoad(viewModel)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(viewModel, None)
      }

    }

    "return Ok and the correct view when addresses are supplied" in {

      running(_.overrides()) { app =>
        val viewModel = addressListViewModel()
        val controller = app.injector.instanceOf[TestController]
        val result = controller.onPageLoad(viewModel)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(viewModel, None)
      }

    }

  }

  "post" must {

    "return See Other on submission of valid data" in {

      running(_.overrides()) { app =>
        val controller = app.injector.instanceOf[TestController]
        val result = controller.onSubmit(addressListViewModel(), 0)

        status(result) mustBe SEE_OTHER
      }

    }

    "redirect to the page specified by the navigator following submission of valid data" in {

      running(_.overrides()) { app =>
        val controller = app.injector.instanceOf[TestController]
        val result = controller.onSubmit(addressListViewModel(), 0)

        redirectLocation(result) mustBe Some(onwardRoute.url)
      }

    }

    "save the user answer on submission of valid data when address  is complete" in {

      running(_.overrides()) { app =>
        val viewModel = addressListViewModel()
        val controller = app.injector.instanceOf[TestController]
        val result = controller.onSubmit(viewModel, 0)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(postCall.url)
        FakeUserAnswersService.userAnswer.get(FakeSelectedAddressIdentifier).value mustBe viewModel.addresses.head
        FakeUserAnswersService.userAnswer.get(FakeAddressIdentifier).value mustBe viewModel.addresses.head.toPrepopAddress
      }

    }

    "save the user answer on submission of valid data when address is incomplete and redirect to manualInput page" in {

      running(_.overrides()) { app =>
        val viewModel = addressListViewModel(incompleteAddresses)
        val controller = app.injector.instanceOf[TestController]
        val result = controller.onSubmit(viewModel, 0, incompleteAddresses)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(manualInputCall.url)
        FakeUserAnswersService.userAnswer.get(FakeSelectedAddressIdentifier).value mustBe viewModel.addresses.head
        FakeUserAnswersService.userAnswer.get(FakeAddressIdentifier) mustBe None
      }

    }

    "shuffle the address lines and save fixed address when address is incomplete but fixable" in {

      running(_.overrides()) { app =>
        val viewModel = addressListViewModel(fixableAddress)
        val controller = app.injector.instanceOf[TestController]
        val result = controller.onSubmit(viewModel, 0, fixableAddress)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(postCall.url)

        FakeUserAnswersService.userAnswer.get(FakeAddressIdentifier).value mustBe
          Address("Address 2 Line 1", "Address 2 Line 4", None, None, Some("123"), "GB")
      }

    }

    "over-write any existing address on submission of valid data and remove list of addresses selected" in {

      running(_.overrides()) { app =>
        val viewModel = addressListViewModel()
        val controller = app.injector.instanceOf[TestController]
        val result = controller.onSubmit(viewModel, 0)

        status(result) mustBe SEE_OTHER
        FakeUserAnswersService.userAnswer.get(FakeAddressIdentifier) mustBe addresses.head.toAddress

        FakeUserAnswersService.verifyRemoved(fakeSeqTolerantAddressId)

      }

    }

    "return Bad Request and the correct view on submission of invalid data" in {
      running(_.overrides()) { app =>
        val viewModel = addressListViewModel()
        val controller = app.injector.instanceOf[TestController]
        val result = controller.onSubmit(viewModel, -1)

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe viewAsString(viewModel, Some(-1))
      }

    }

    "will send an audit event" in {
      val auditService = new StubSuccessfulAuditService()
      running(_.overrides(bind[AuditService].toInstance(auditService))) { app =>
        val viewModel = addressListViewModel()
        val controller = app.injector.instanceOf[TestController]
        val result = controller.onSubmit(viewModel, 0)

        status(result) mustBe SEE_OTHER
        auditService.verifySent(
          AddressEvent(
            externalId,
            AddressAction.LookupChanged,
            "test-context",
            Address(
              "value 1",
              "value 2",
              None,
              None,
              Some("AB1 1AB"),
              "GB"
            )
          )
        )
      }
    }
  }
}

object AddressListControllerSpec {
  class TestController @Inject()(
                                  override val appConfig: FrontendAppConfig,
                                  override val messagesApi: MessagesApi,
                                  override val controllerComponents: MessagesControllerComponents,
                                  override val view: addressList
                                )(implicit val ec: ExecutionContext) extends AddressListController {

    override protected def userAnswersService: UserAnswersService = FakeUserAnswersService

    override protected def navigator: Navigator = new FakeNavigator(onwardRoute)

    override def auditService: StubSuccessfulAuditService = new StubSuccessfulAuditService()

    def onPageLoad(viewModel: AddressListViewModel): Future[Result] = {

      get(
        viewModel
      )(DataRequest(FakeRequest(), "cacheId", UserAnswers(), Some(PsaId("A0000000"))))

    }

    def onSubmit(viewModel: AddressListViewModel, value: Int, addressSeq: Seq[TolerantAddress] = addresses): Future[Result] = {

      val json = Json.obj(
        fakeSeqTolerantAddressId.toString -> addressSeq
      )

      val request = FakeRequest().withFormUrlEncodedBody("value" -> value.toString)

      post(
        viewModel,
        FakeSelectedAddressIdentifier,
        FakeAddressIdentifier,
        NormalMode,
        "test-context",
        fakeSeqTolerantAddressId
      )(DataRequest(request, "cacheId", UserAnswers(json), Some(PsaId("A0000000"))))

    }
  }

  val tolerantAddress: TolerantAddress = TolerantAddress(Some("address line 1"), Some("address line 2"), None, None, Some("ZZ1 1ZZ"), Some("GB"))
  val address: Address = Address("address line 1", "address line 2", None, None, Some("ZZ1 1ZZ"), "GB")
  object FakeAddressIdentifier extends TypedIdentifier[Address]
  object FakeSelectedAddressIdentifier extends TypedIdentifier[TolerantAddress]

  val onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val fakeSeqTolerantAddressId: TypedIdentifier[Seq[TolerantAddress]] = new TypedIdentifier[Seq[TolerantAddress]] {
    override def toString = "abc"
  }
  private lazy val postCall = controllers.routes.IndexController.onPageLoad()
  private lazy val manualInputCall = controllers.routes.SessionExpiredController.onPageLoad

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

  private val incompleteAddresses = Seq(
    TolerantAddress(
      Some("Address 1 Line 1"),
      None, None, None,
      Some("A1 1PC"),
      Some("GB")
    ))

  private val fixableAddress = Seq(
    TolerantAddress(
      Some("Address 2 Line 1"),
      None,
      None,
      Some("Address 2 Line 4"),
      Some("123"),
      Some("GB")
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
      selectAddressLink = Message("select an address link text"),
      entityName = "test name"
    )

}
