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

package controllers.register.trustees.individual

import audit.testdoubles.StubSuccessfulAuditService
import audit.{AddressAction, AddressEvent, AuditService}
import base.CSRFRequest
import config.{FeatureSwitchManagementService, FrontendAppConfig}
import services.{FakeUserAnswersService, UserAnswersService}
import controllers.ControllerSpecBase
import controllers.actions._
import controllers.register.trustees.individual.routes._
import forms.address.AddressFormProvider
import identifiers.register.trustees.TrusteesId
import identifiers.register.trustees.individual.{TrusteeDetailsId, TrusteeNameId, TrusteePreviousAddressId}
import models.address.Address
import models.person.{PersonDetails, PersonName}
import models.{Index, NormalMode}
import navigators.Navigator
import org.joda.time.LocalDate
import org.scalatest.concurrent.ScalaFutures
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{FakeUserAnswersService, UserAnswersService}
import utils.annotations.TrusteesIndividual
import utils.{CountryOptions, FakeCountryOptions, FakeFeatureSwitchManagementService, FakeNavigator, InputOption, UserAnswers}
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddress

class TrusteePreviousAddressControllerSpec extends ControllerSpecBase with CSRFRequest with ScalaFutures {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  def countryOptions: CountryOptions = new CountryOptions(options)

  private val options = Seq(InputOption("territory:AE-AZ", "Abu Dhabi"), InputOption("country:AF", "Afghanistan"))

  val firstIndex = Index(0)

  val formProvider = new AddressFormProvider(FakeCountryOptions())
  val trusteeDetails = PersonDetails("Test", None, "Name", LocalDate.now)
  val trusteeName = PersonName("Test", "Name")
  lazy val fakeNavigator = new FakeNavigator(desiredRoute = onwardRoute)
  val fakeAuditService = new StubSuccessfulAuditService()
  val form: Form[Address] = formProvider()

  private def retrieval(isHnsEnabled: Boolean = false): DataRetrievalAction = {
    if(isHnsEnabled){
      UserAnswers().set(TrusteeNameId(0))(trusteeName).asOpt.value.dataRetrievalAction
    } else {
      UserAnswers().set(TrusteeDetailsId(0))(trusteeDetails).asOpt.value.dataRetrievalAction
    }
  }

  "PreviousAddress Controller" must {

    Seq(true, false).foreach { isHnsEnabled =>
      s"render manualAddress from GET request when toggle is $isHnsEnabled" in {
        FakeUserAnswersService.reset()
        running(_.overrides(
          bind[FrontendAppConfig].to(frontendAppConfig),
          bind[Navigator].toInstance(FakeNavigator),
          bind[UserAnswersService].toInstance(FakeUserAnswersService),
          bind[AuthAction].to(FakeAuthAction),
          bind[DataRetrievalAction].toInstance(retrieval(isHnsEnabled)),
          bind[CountryOptions].to(countryOptions),
          bind[FeatureSwitchManagementService].toInstance(new FakeFeatureSwitchManagementService(isHnsEnabled))
        )) {
          implicit app =>

            val controller = app.injector.instanceOf[TrusteePreviousAddressController]

            val viewmodel = ManualAddressViewModel(
              controller.postCall(NormalMode, firstIndex, None),
              countryOptions.options,
              Message("messages__trustee_individual_confirm__previous_address__title"),
              Message("messages__common__confirmPreviousAddress__h1", trusteeDetails.fullName)
            )

            val request = addToken(
              FakeRequest(TrusteePreviousAddressController.onPageLoad(NormalMode, firstIndex, None))
                .withHeaders("Csrf-Token" -> "nocheck")
            )

            val result = route(app, request).value

            status(result) must be(OK)

            contentAsString(result) mustEqual manualAddress(
              frontendAppConfig,
              form,
              viewmodel,
              None
            )(request, messages).toString

        }

      }

      s"redirect to next page on POST request when toggle is $isHnsEnabled" which {
        "saves address" in {

          val address = Address(
            addressLine1 = "value 1",
            addressLine2 = "value 2",
            None, None,
            postcode = Some("AB1 1AB"),
            country = "GB"
          )

          running(_.overrides(
            bind[FrontendAppConfig].to(frontendAppConfig),
            bind[MessagesApi].to(messagesApi),
            bind[UserAnswersService].toInstance(FakeUserAnswersService),
            bind(classOf[Navigator]).toInstance(fakeNavigator),
            bind[AuthAction].to(FakeAuthAction),
            bind[DataRetrievalAction].toInstance(retrieval(isHnsEnabled)),
            bind[DataRequiredAction].to(new DataRequiredActionImpl),
            bind[AddressFormProvider].to(formProvider),
            bind[FeatureSwitchManagementService].toInstance(new FakeFeatureSwitchManagementService(isHnsEnabled))
          )) {
            implicit app =>

              val fakeRequest = addToken(FakeRequest(TrusteePreviousAddressController.onSubmit(NormalMode, firstIndex, None))
                .withHeaders("Csrf-Token" -> "nocheck")
                .withFormUrlEncodedBody(
                  ("addressLine1", address.addressLine1),
                  ("addressLine2", address.addressLine2),
                  ("postCode", address.postcode.get),
                  "country" -> address.country))

              val result = route(app, fakeRequest).value

              status(result) must be(SEE_OTHER)
              redirectLocation(result).value mustEqual onwardRoute.url

              FakeUserAnswersService.userAnswer.get(TrusteePreviousAddressId(firstIndex)).value mustEqual address
          }
        }
      }

      s"send an audit event when valid data is submitted when toggle is $isHnsEnabled" in {

        val address = Address(
          addressLine1 = "value 1",
          addressLine2 = "value 2",
          None, None,
          postcode = Some("AB1 1AB"),
          country = "GB"
        )

        running(_.overrides(
          bind[FrontendAppConfig].to(frontendAppConfig),
          bind[Navigator].toInstance(FakeNavigator),
          bind[UserAnswersService].toInstance(FakeUserAnswersService),
          bind[AuthAction].to(FakeAuthAction),
          bind[CountryOptions].to(countryOptions),
          bind[DataRetrievalAction].to(retrieval(isHnsEnabled)),
          bind[AuditService].toInstance(fakeAuditService),
        bind[FeatureSwitchManagementService].to(new FakeFeatureSwitchManagementService(false)))) {
          implicit app =>

            val fakeRequest = addToken(FakeRequest(routes.TrusteePreviousAddressController.onSubmit(NormalMode, firstIndex, None))
              .withHeaders("Csrf-Token" -> "nocheck")
              .withFormUrlEncodedBody(
                ("addressLine1", address.addressLine1),
                ("addressLine2", address.addressLine2),
                ("postCode", address.postcode.get),
                "country" -> address.country))

            fakeAuditService.reset()

            val result = route(app, fakeRequest).value

            whenReady(result) {
              _ =>
                fakeAuditService.verifySent(
                  AddressEvent(
                    FakeAuthAction.externalId,
                    AddressAction.LookupChanged,
                    s"Trustee Previous Address: ${trusteeDetails.fullName}",
                    address
                  )
                )
            }
        }
      }
    }
  }
}
