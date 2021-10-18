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

package controllers.register.trustees.individual

import audit.testdoubles.StubSuccessfulAuditService
import audit.{AddressAction, AddressEvent, AuditService}
import config.FrontendAppConfig
import controllers.ControllerSpecBase
import controllers.actions._
import controllers.register.trustees.individual.routes._
import forms.address.AddressFormProvider
import identifiers.register.trustees.individual.{TrusteeNameId, TrusteePreviousAddressId}
import models.address.Address
import models.person.PersonName
import models.{Index, NormalMode, person}
import navigators.Navigator
import org.scalatest.concurrent.ScalaFutures
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.CSRFTokenHelper.addCSRFToken
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{FakeUserAnswersService, UserAnswersService}
import utils.{CountryOptions, FakeCountryOptions, FakeNavigator, InputOption, UserAnswers}
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddress

class TrusteePreviousAddressControllerSpec extends ControllerSpecBase with ScalaFutures {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad
  private val view = injector.instanceOf[manualAddress]

  def countryOptions: CountryOptions = new CountryOptions(options)

  private val options = Seq(InputOption("territory:AE-AZ", "Abu Dhabi"), InputOption("country:AF", "Afghanistan"))

  val firstIndex: Index = Index(0)

  val formProvider = new AddressFormProvider(FakeCountryOptions())
  val trusteeDetails: PersonName = person.PersonName("Test", "Name")
  val trusteeName: PersonName = PersonName("Test", "Name")
  lazy val fakeNavigator = new FakeNavigator(desiredRoute = onwardRoute)
  val fakeAuditService = new StubSuccessfulAuditService()
  val form: Form[Address] = formProvider()

  private def retrieval: DataRetrievalAction = {
      UserAnswers().set(TrusteeNameId(0))(trusteeName).asOpt.value.dataRetrievalAction
  }

  "PreviousAddress Controller" must {

      "render manualAddress from GET request" in {
        FakeUserAnswersService.reset()
        running(_.overrides(
          bind[FrontendAppConfig].to(frontendAppConfig),
          bind[Navigator].toInstance(FakeNavigator),
          bind[UserAnswersService].toInstance(FakeUserAnswersService),
          bind[AuthAction].to(FakeAuthAction),
          bind[DataRetrievalAction].toInstance(retrieval),
          bind[CountryOptions].to(countryOptions)
        )) {
          implicit app =>

            val controller = app.injector.instanceOf[TrusteePreviousAddressController]

            val viewmodel = ManualAddressViewModel(
              controller.postCall(NormalMode, firstIndex, None),
              countryOptions.options,
              Message("messages__common__confirmPreviousAddress__h1",Message("messages__theIndividual")),
              Message("messages__common__confirmPreviousAddress__h1", trusteeDetails.fullName)
            )

            val request = addCSRFToken(
              FakeRequest(TrusteePreviousAddressController.onPageLoad(NormalMode, firstIndex, None))
                .withHeaders("Csrf-Token" -> "nocheck")
            )

            val result = route(app, request).value

            status(result) must be(OK)

            contentAsString(result) mustEqual view(
              form,
              viewmodel,
              None
            )(request, messages).toString

        }

      }

      s"redirect to next page on POST request" which {
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
            bind[DataRetrievalAction].toInstance(retrieval),
            bind[DataRequiredAction].to(new DataRequiredActionImpl),
            bind[AddressFormProvider].to(formProvider)
          )) {
            implicit app =>

              val fakeRequest = addCSRFToken(FakeRequest()
                .withHeaders("Csrf-Token" -> "nocheck")
                .withFormUrlEncodedBody(
                  ("addressLine1", address.addressLine1),
                  ("addressLine2", address.addressLine2),
                  ("postCode", address.postcode.get),
                  "country" -> address.country))

              val controller = app.injector.instanceOf[TrusteePreviousAddressController]
              val result = controller.onSubmit(NormalMode, firstIndex, None)(fakeRequest)

              status(result) must be(SEE_OTHER)
              redirectLocation(result).value mustEqual onwardRoute.url

              FakeUserAnswersService.userAnswer.get(TrusteePreviousAddressId(firstIndex)).value mustEqual address
          }
        }
      }

      s"send an audit event when valid data is submitted" in {

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
          bind[DataRetrievalAction].to(retrieval),
          bind[AuditService].toInstance(fakeAuditService))) {
          implicit app =>

            val fakeRequest = addCSRFToken(FakeRequest()
              .withHeaders("Csrf-Token" -> "nocheck")
              .withFormUrlEncodedBody(
                ("addressLine1", address.addressLine1),
                ("addressLine2", address.addressLine2),
                ("postCode", address.postcode.get),
                "country" -> address.country))

            fakeAuditService.reset()

            val controller = app.injector.instanceOf[TrusteePreviousAddressController]
            val result = controller.onSubmit(NormalMode, firstIndex, None)(fakeRequest)

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
