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

package controllers.register.establishers

import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.establishers.AddEstablisherFormProvider
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.individual.{EstablisherDetailsId, EstablisherNameId}
import identifiers.register.establishers.{EstablishersId, IsEstablisherNewId}
import models.person.PersonDetails
import models.register.{Establisher, EstablisherCompanyEntity, EstablisherIndividualEntity, EstablisherIndividualEntityNonHnS}
import models.{CompanyDetails, NormalMode}
import org.joda.time.LocalDate
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.{FakeFeatureSwitchManagementService, FakeNavigator}
import views.html.register.establishers.addEstablisher

class AddEstablisherControllerSpec extends ControllerSpecBase {

  import AddEstablisherControllerSpec._

  "AddEstablisher Controller with HnS feature toggle set to true" must {

    "continue button should be enabled" in {
      val establishersAsEntities = Seq(johnDoe, testLtd)
      val getRelevantData = establisherWithDeletedDataRetrieval
      val result = controller(getRelevantData, toggle = true).onPageLoad(NormalMode, None)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form, establishersAsEntities)
    }
  }
}

object AddEstablisherControllerSpec extends AddEstablisherControllerSpec {

  private def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  private val schemeName = "Test Scheme Name"

  private val formProvider = new AddEstablisherFormProvider()
  private val form = formProvider(Seq.empty)

  protected def fakeNavigator() = new FakeNavigator(desiredRoute = onwardRoute)

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData, toggle:Boolean = false): AddEstablisherController =
    new AddEstablisherController(
      frontendAppConfig,
      messagesApi,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      formProvider,
      new FakeFeatureSwitchManagementService(toggle)
    )

  private def viewAsString(form: Form[_] = form, allEstablishers: Seq[Establisher[_]] = Seq.empty): String =
    addEstablisher(
      frontendAppConfig,
      form,
      NormalMode,
      allEstablishers,
      None,
      None
    )(fakeRequest, messages).toString

  private val day = LocalDate.now().getDayOfMonth
  private val month = LocalDate.now().getMonthOfYear
  private val year = LocalDate.now().getYear - 20

  private val personDetails = PersonDetails("John", None, "Doe", new LocalDate(year, month, day))
  private val johnDoe = EstablisherIndividualEntity(
    EstablisherNameId(0),
    "John Doe",
    false,
    false,
    true,
    1
  )

  private val johnDoeNonHnS = EstablisherIndividualEntityNonHnS(
    EstablisherDetailsId(0),
    "John Doe",
    false,
    false,
    true,
    1
  )

  private val companyDetails = CompanyDetails("Test Ltd")
  private val testLtd = EstablisherCompanyEntity(
    CompanyDetailsId(1),
    "Test Ltd",
    false,
    false,
    true,
    1
  )

  private val deletedEstablisher = personDetails.copy(isDeleted = true)

  private def individualEstablisherDataRetrieval: FakeDataRetrievalAction = {
    val validData = Json.obj(
      EstablishersId.toString -> Json.arr(
        Json.obj(
          EstablisherDetailsId.toString -> personDetails,
          IsEstablisherNewId.toString -> true
        )
      )
    )
    new FakeDataRetrievalAction(Some(validData))
  }

  private def establisherWithDeletedDataRetrieval: FakeDataRetrievalAction = {
    val validData = Json.obj(
      EstablishersId.toString -> Json.arr(
        Json.obj(
          EstablisherDetailsId.toString -> personDetails,
          IsEstablisherNewId.toString -> true
        ),
        Json.obj(
          CompanyDetailsId.toString -> companyDetails,
          IsEstablisherNewId.toString -> true
        ),
        Json.obj(
          EstablisherDetailsId.toString -> deletedEstablisher,
          IsEstablisherNewId.toString -> true
        )
      )
    )
    new FakeDataRetrievalAction(Some(validData))
  }

}
