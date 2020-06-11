/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers.register.establishers.company.director

import java.time.LocalDate

import controllers.ControllerSpecBase
import controllers.actions._
import controllers.behaviours.DateOfBirthControllerBehaviours
import forms.DOBFormProvider
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.company.director.{DirectorDOBId, DirectorNameId}
import models.person.PersonName
import models.{CompanyDetails, Index, Mode, NormalMode}
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.FakeNavigator
import viewmodels.Message
import viewmodels.dateOfBirth.DateOfBirthViewModel
import views.html.register.DOB

//scalastyle:off magic.number

class DirectorDOBControllerSpec extends ControllerSpecBase with DateOfBirthControllerBehaviours {

  import DirectorDOBControllerSpec._

  private val view = injector.instanceOf[DOB]

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisherCompanyDirectorWithDirectorName): DirectorDOBController =
    new DirectorDOBController(
      frontendAppConfig,
      messagesApi,
      mockUserAnswersService,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      formProvider,
      stubMessagesControllerComponents(),
      view)

  private val postCall: (Mode, Index, Index, Option[String]) => Call = routes.DirectorDOBController.onSubmit

  private def viewModel(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String], token: String): DateOfBirthViewModel =
    DateOfBirthViewModel(
      postCall = postCall(mode, establisherIndex, directorIndex, srn),
      srn = srn,
      token = token
    )

  "DirectorDOB Controller" must {

    behave like dateOfBirthController(
      get = data => controller(data).onPageLoad(NormalMode, firstEstablisherIndex, firstDirectorIndex, None),
      post = data => controller(data).onSubmit(NormalMode, firstEstablisherIndex, firstDirectorIndex, None),
      viewModel = viewModel(NormalMode, firstEstablisherIndex, firstDirectorIndex, None, Message("messages__theDirector")),
      mode = NormalMode,
      requiredData = getMandatoryEstablisherCompanyDirectorWithDirectorName,
      validData = validData,
      fullName = s"${(validData \\ "firstName").head.as[String]} ${(validData \\ "lastName").head.as[String]}"
    )
  }
}

object DirectorDOBControllerSpec extends MockitoSugar {
  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val formProvider: DOBFormProvider = new DOBFormProvider()
  val form: Form[LocalDate] = formProvider()

  val firstEstablisherIndex: Index = Index(0)
  val firstDirectorIndex: Index = Index(0)

  val day: Int = LocalDate.now().getDayOfMonth
  val month: Int = LocalDate.now().getMonthValue
  val year: Int = LocalDate.now().getYear - 20

  val validData: JsObject = Json.obj(
    EstablishersId.toString -> Json.arr(
      Json.obj(
        CompanyDetailsId.toString -> CompanyDetails("test company name"),
        "director" -> Json.arr(
          Json.obj(
            DirectorNameId.toString -> PersonName("first", "last"),
            DirectorDOBId.toString  -> LocalDate.of(year, month, day)
          )
        )
      )
    )
  )
}


