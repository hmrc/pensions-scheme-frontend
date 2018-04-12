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

package controllers.register.trustees.individual

import config.FrontendAppConfig
import connectors.{DataCacheConnector, FakeDataCacheConnector}
import controllers.actions._
import controllers.behaviours.ControllerBehaviours
import identifiers.register.trustees.TrusteesId
import identifiers.register.trustees.individual.{TrusteeAddressId, TrusteeDetailsId}
import models.person.PersonDetails
import models.{Index, NormalMode}
import org.joda.time.LocalDate
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import utils.annotations.TrusteesCompany
import utils.{CountryOptions, FakeNavigator, InputOption, Navigator}
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel

class TrusteeAddressControllerSpec extends ControllerBehaviours {

  val firstIndex = Index(0)

  val countryOptions = new CountryOptions(
    Seq(InputOption("GB", "GB"))
  )

  val personDetails = PersonDetails("First", None, "Last", LocalDate.now())

  val retrieval = new FakeDataRetrievalAction(Some(Json.obj(
    TrusteesId.toString -> Json.arr(Json.obj(TrusteeDetailsId.toString -> personDetails))
  )))

  implicit val builder = new GuiceApplicationBuilder()
    .overrides(
      bind[FrontendAppConfig].to(frontendAppConfig),
      bind[Navigator].qualifiedWith(classOf[TrusteesCompany]).toInstance(FakeNavigator),
      bind[DataCacheConnector].toInstance(FakeDataCacheConnector),
      bind[AuthAction].to(FakeAuthAction),
      bind[DataRetrievalAction].to(retrieval),
      bind[CountryOptions].to(countryOptions)
    )

  val controller = builder.build().injector.instanceOf[TrusteeAddressController]

  val viewmodel = ManualAddressViewModel(
    controller.postCall(NormalMode, firstIndex),
    countryOptions.options,
    Message(controller.title),
    Message(controller.heading),
    secondaryHeader = Some(personDetails.fullName),
    Message(controller.hint)
  )

  behave like manualAddress(
    routes.TrusteeAddressController.onPageLoad(NormalMode, firstIndex),
    routes.TrusteeAddressController.onSubmit(NormalMode, firstIndex),
    TrusteeAddressId(firstIndex),
    viewmodel
  )

}
