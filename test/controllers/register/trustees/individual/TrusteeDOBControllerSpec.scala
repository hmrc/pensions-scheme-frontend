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

package controllers.register.trustees.individual

import controllers.ControllerSpecBase
import controllers.actions._
import controllers.behaviours.DateOfBirthControllerBehaviours
import forms.DOBFormProvider
import identifiers.register.trustees.TrusteesId
import identifiers.register.trustees.individual.{TrusteeDOBId, TrusteeNameId}
import models.person.PersonName
import models.{Index, Mode, NormalMode}
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import utils.FakeNavigator
import viewmodels.Message
import viewmodels.dateOfBirth.DateOfBirthViewModel
import views.html.register.DOB

import java.time.LocalDate

//scalastyle:off magic.number

class TrusteeDOBControllerSpec extends ControllerSpecBase with DateOfBirthControllerBehaviours {

  import TrusteeDOBControllerSpec._

  private val view = injector.instanceOf[DOB]

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryTrustee): TrusteeDOBController =
    new TrusteeDOBController(
      frontendAppConfig,
      messagesApi,
      mockUserAnswersService,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      formProvider,
      controllerComponents,
      view)

  private val postCall = routes.TrusteeDOBController.onSubmit _

  private def viewModel(mode: Mode, index: Index, srn: Option[String], token: String): DateOfBirthViewModel = {
    DateOfBirthViewModel(
      postCall = postCall(mode, index, srn),
      srn = srn,
      token = token
    )
  }

  "TrusteeDOB Controller" must {

    behave like dateOfBirthController(
      get = data => controller(data).onPageLoad(NormalMode, 0, None),
      post = data => controller(data).onSubmit(NormalMode, 0, None),
      viewModel = viewModel(NormalMode, index, None, Message("messages__theIndividual").resolve),
      mode = NormalMode,
      requiredData = getMandatoryTrustee,
      validData = validData,
      fullName = s"${(validData \\ "firstName").head.as[String]} ${(validData \\ "lastName").head.as[String]}"
    )
  }
}

private object TrusteeDOBControllerSpec extends MockitoSugar {
  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad

  val formProvider: DOBFormProvider = new DOBFormProvider()
  val form: Form[LocalDate] = formProvider()

  val index: Index = Index(0)

  val day: Int = LocalDate.now().getDayOfMonth
  val month: Int = LocalDate.now().getMonthValue
  val year: Int = LocalDate.now().getYear - 20

  val validData: JsObject = Json.obj(
    TrusteesId.toString -> Json.arr(
      Json.obj(
        TrusteeNameId.toString -> PersonName("Test", "Name"),
        TrusteeDOBId.toString -> LocalDate.of(year, month, day)
      )
    )
  )
}



