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

package controllers.register.trustees.company

import base.CSRFRequest
import config.FrontendAppConfig
import connectors.{DataCacheConnector, FakeDataCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import controllers.behaviours.ControllerBehaviours
import controllers.register.trustees.company.routes._
import forms.address.AddressFormProvider
import identifiers.register.trustees.TrusteesId
import identifiers.register.trustees.company.CompanyDetailsId
import models.address.Address
import models.{CompanyDetails, Index, NormalMode}
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.Form
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.Helpers._
import utils.{CountryOptions, FakeNavigator, InputOption, Navigator}
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.{manualAddress => manualAddressView}

class CompanyAddressControllerSpec extends ControllerSpecBase
  with MockitoSugar
  with ScalaFutures
  with CSRFRequest
  with OptionValues
  with ControllerBehaviours
  with GuiceOneAppPerSuite {

  val firstIndex = Index(0)

  val companyDetails = CompanyDetails("companyName", None, None)

  val countryOptions = new CountryOptions(
    Seq(InputOption("GB", "GB"))
  )

  val formProvider = new AddressFormProvider(countryOptions)
  val form: Form[Address] = formProvider()

  val retrieval = new FakeDataRetrievalAction(Some(Json.obj(
    TrusteesId.toString -> Json.arr(Json.obj(CompanyDetailsId.toString -> companyDetails))
  )))

  running(_.overrides(
    bind[FrontendAppConfig].to(frontendAppConfig),
    bind[Navigator].toInstance(FakeNavigator),
    bind[DataCacheConnector].toInstance(FakeDataCacheConnector),
    bind[AuthAction].to(FakeAuthAction),
    bind[DataRetrievalAction].to(retrieval),
    bind[CountryOptions].to(countryOptions)
  )) {
    implicit app =>


        val controller = app.injector.instanceOf[CompanyAddressController]

        val viewmodel = ManualAddressViewModel(
          controller.postCall(NormalMode, firstIndex),
          countryOptions.options,
          Message(controller.title),
          Message(controller.heading),
          secondaryHeader = Some(companyDetails.companyName),
          Message(controller.hint)
        )

        behave like manualAddress(
          CompanyAddressController.onPageLoad(NormalMode, firstIndex),
          CompanyAddressController.onSubmit(NormalMode, firstIndex),
          viewmodel
        )

      }



}
