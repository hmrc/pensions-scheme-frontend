/*
 * Copyright 2024 HM Revenue & Customs
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
import controllers.actions.FakeDataRetrievalAction
import forms.address.AddressFormProvider
import identifiers.TypedIdentifier
import models._
import models.address.{Address, TolerantAddress}
import models.requests.DataRequest
import navigators.Navigator
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.inject._
import play.api.libs.json.Json
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{FakeUserAnswersService, UserAnswersService}
import uk.gov.hmrc.domain.PsaId
import utils.{CountryOptions, FakeCountryOptions, FakeNavigator, UserAnswers}
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddress

import scala.concurrent.{ExecutionContext, Future}

object ManualAddressControllerSpec {

  val fakeAddressId: TypedIdentifier[Address] = new TypedIdentifier[Address] {
    override def toString = "fakeAddressId"
  }

  val fakeExistingAddressId: TypedIdentifier[Address] = new TypedIdentifier[Address] {
    override def toString = "fakeExistingAddressId"
  }

  val fakeAddressListId: TypedIdentifier[TolerantAddress] = new TypedIdentifier[TolerantAddress] {
    override def toString = "fakeAddressListId"
  }

  val externalId: String = "test-external-id"

  val fakeSeqTolerantAddressId: TypedIdentifier[Seq[TolerantAddress]] = new TypedIdentifier[Seq[TolerantAddress]] {
    override def toString = "abc"
  }

  val tolerantAddress: TolerantAddress = TolerantAddress(Some("address line 1"), Some("address line 2"), None, None, Some("ZZ1 1ZZ"), Some("GB"))
  val address: Address = Address("address line 1", "address line 2", None, None, Some("ZZ1 1ZZ"), "GB")

  object FakeAddressIdentifier extends TypedIdentifier[Address]

  object FakeSelectedAddressIdentifier extends TypedIdentifier[TolerantAddress]

  private val answers = Json.obj(fakeAddressId.toString -> address,
    fakeAddressListId.toString -> tolerantAddress)
  val preSavedAddress = new FakeDataRetrievalAction(Some(answers))

  private val psaId = PsaId("A0000000")

  class TestController @Inject()(
                                  override val appConfig: FrontendAppConfig,
                                  override val messagesApi: MessagesApi,
                                  override val userAnswersService: UserAnswersService,
                                  override val navigator: Navigator,
                                  formProvider: AddressFormProvider,
                                  override val auditService: AuditService,
                                  val controllerComponents: MessagesControllerComponents,
                                  val view: manualAddress
                                )(implicit val ec: ExecutionContext) extends ManualAddressController {

    def onPageLoad(viewModel: ManualAddressViewModel, answers: UserAnswers): Future[Result] =
      get(fakeAddressId, fakeAddressListId, viewModel)(DataRequest(FakeRequest(), "cacheId", answers, Some(psaId)))

    def onSubmit(viewModel: ManualAddressViewModel, answers: UserAnswers, request: Request[AnyContent] = FakeRequest()): Future[Result] =
      post(fakeAddressId, fakeAddressListId, viewModel, NormalMode, "test-context", fakeSeqTolerantAddressId)(
        DataRequest(request, externalId, answers, Some(psaId)))

    override protected val form: Form[Address] = formProvider()
  }

}

class ManualAddressControllerSpec extends SpecBase with Matchers with MockitoSugar with ScalaFutures with OptionValues {

  import ManualAddressControllerSpec._

  private val view = injector.instanceOf[manualAddress]

  val addressData: Map[String, String] = Map(
    "addressLine1" -> "address line 1",
    "addressLine2" -> "address line 2",
    "addressLine3" -> "address line 3",
    "addressLine4" -> "address line 4",
    "postCode" -> "AB1 1AP",
    "country" -> "GB"
  )

  private val countryOptions = FakeCountryOptions.fakeCountries

  private val viewModel = ManualAddressViewModel(
    Call("GET", "/"),
    countryOptions,
    "title",
    "heading",
    Some(SchemeReferenceNumber("secondary.header"))
  )

  "get" must {
    "return OK with view" when {
      "data is not retrieved" in {

        running(_.overrides(
          bind[CountryOptions].to[FakeCountryOptions],
          bind[Navigator].to(FakeNavigator),
          bind[AuditService].to[StubSuccessfulAuditService]
        )) {
          app =>

            val request = FakeRequest()

            val formProvider = app.injector.instanceOf[AddressFormProvider]
            val messages = app.injector.instanceOf[MessagesApi].preferred(request)
            val controller = app.injector.instanceOf[TestController]

            val result = controller.onPageLoad(viewModel, UserAnswers())

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(formProvider(), viewModel, None)(request, messages).toString

        }

      }

      "data is retrieved" in {
        running(_.overrides(
          bind[CountryOptions].to[FakeCountryOptions],
          bind[Navigator].to(FakeNavigator),
          bind[AuditService].to[StubSuccessfulAuditService]
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

            val formProvider = app.injector.instanceOf[AddressFormProvider]
            val messages = app.injector.instanceOf[MessagesApi].preferred(request)
            val controller = app.injector.instanceOf[TestController]

            val result = controller.onPageLoad(viewModel, UserAnswers(Json.obj(fakeAddressId.toString -> testAddress)))

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(formProvider().fill(testAddress), viewModel, None)(request, messages).toString

        }
      }

      "data is not retrieved but there is a selected address" in {

        running(_.overrides(
          bind[CountryOptions].to[FakeCountryOptions],
          bind[Navigator].to(FakeNavigator),
          bind[AuditService].to[StubSuccessfulAuditService]
        )) {
          app =>

            val testAddress = TolerantAddress(
              Some("address line 1"),
              Some("address line 2"),
              None,
              None,
              Some("test post code"),
              Some("GB")
            )

            val userAnswers = UserAnswers().set(fakeAddressListId)(testAddress).asOpt.value

            val request = FakeRequest()

            val formProvider = app.injector.instanceOf[AddressFormProvider]
            val messages = app.injector.instanceOf[MessagesApi].preferred(request)
            val controller = app.injector.instanceOf[TestController]
            val form = formProvider().fill(testAddress.toPrepopAddress)

            val result = controller.onPageLoad(viewModel, userAnswers)

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form, viewModel, None)(request, messages).toString

        }

      }

    }
  }

  "post" must {

    "redirect to the postCall on valid data request" which {
      "will save address to answers and remove the address postcode lookup list" in {

        val onwardRoute = Call("GET", "/")

        val navigator = new FakeNavigator(onwardRoute, NormalMode)

        running(_.overrides(
          bind[CountryOptions].to[FakeCountryOptions],
          bind[UserAnswersService].to(FakeUserAnswersService),
          bind[Navigator].to(navigator),
          bind[AuditService].to[StubSuccessfulAuditService]
        )) {
          app =>

            val controller = app.injector.instanceOf[TestController]

            val currentAddress = Address("current1", "current2", None, None, None, "GB")
            val result = controller.onSubmit(viewModel, UserAnswers().set(fakeSeqTolerantAddressId)(Seq(TolerantAddress(
              None, None, None, None, None, None))).flatMap(_.set(fakeAddressId)(currentAddress)).asOpt.value, FakeRequest().withFormUrlEncodedBody(
              ("addressLine1", "value 1"),
              ("addressLine2", "value 2"),
              ("postCode", "AB1 1AB"),
              "country" -> "GB")
            )

            status(result) mustEqual SEE_OTHER
            redirectLocation(result) mustBe Some(onwardRoute.url)

            val address = Address("value 1", "value 2", None, None, Some("AB1 1AB"), "GB")

            FakeUserAnswersService.userAnswer.get(fakeAddressId).value mustEqual address
            FakeUserAnswersService.verify(fakeExistingAddressId, currentAddress)
            FakeUserAnswersService.verifyRemoved(fakeSeqTolerantAddressId)
        }
      }

      "will send an audit event" in {

        val onwardRoute = Call("GET", "/")

        val navigator = new FakeNavigator(onwardRoute, NormalMode)
        val auditService = new StubSuccessfulAuditService()

        running(_.overrides(
          bind[CountryOptions].to[FakeCountryOptions],
          bind[UserAnswersService].to(FakeUserAnswersService),
          bind[Navigator].to(navigator),
          bind[AuditService].toInstance(auditService)
        )) {
          app =>
            val existingAddress = Address(
              "existing-line-1",
              "existing-line-2",
              None,
              None,
              None,
              "existing-country"
            )

            val selectedAddress = TolerantAddress(None, None, None, None, None, None)

            val userAnswers =
              UserAnswers()
                .set(fakeAddressId)(existingAddress).asOpt.value
                .set(fakeAddressListId)(selectedAddress).asOpt.value

            val controller = app.injector.instanceOf[TestController]

            val result = controller.onSubmit(viewModel, userAnswers, FakeRequest().withFormUrlEncodedBody(
              ("addressLine1", "value 1"),
              ("addressLine2", "value 2"),
              ("postCode", "AB1 1AB"),
              "country" -> "GB")
            )

            whenReady(result) {
              _ =>
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

    "return BAD_REQUEST with view on invalid data request" in {

      running(_.overrides(
        bind[CountryOptions].to[FakeCountryOptions],
        bind[AuditService].to[StubSuccessfulAuditService],
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>

          val request = FakeRequest()

          val formProvider = app.injector.instanceOf[AddressFormProvider]
          val messages = app.injector.instanceOf[MessagesApi].preferred(request)
          val controller = app.injector.instanceOf[TestController]

          val form = formProvider().bind(Map.empty[String, String])

          val result = controller.onSubmit(viewModel, UserAnswers(), request.withFormUrlEncodedBody())

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(form, viewModel, None)(request, messages).toString
      }

    }
  }

}
