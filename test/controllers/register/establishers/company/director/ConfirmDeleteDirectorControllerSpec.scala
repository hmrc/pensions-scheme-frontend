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

package controllers.register.establishers.company.director

import connectors.FakeDataCacheConnector
import controllers.actions._
import play.api.test.Helpers._
import models.{CompanyDetails, Index, NormalMode}
import views.html.register.establishers.company.director.confirmDeleteDirector
import controllers.ControllerSpecBase
import controllers.register.establishers.company.routes.AddCompanyDirectorsController
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.company.director.{DirectorDetailsId, DirectorId}
import models.register.establishers.company.director.DirectorDetails
import org.joda.time.LocalDate
import play.api.libs.json._
import utils.FakeNavigator

class ConfirmDeleteDirectorControllerSpec extends ControllerSpecBase {

  import ConfirmDeleteDirectorControllerSpec._

  "ConfirmDeleteDirector Controller" must {
    "return OK and the correct view for a GET" in {
      val data = new FakeDataRetrievalAction(Some(testData))
      val result = controller(data).onPageLoad(establisherIndex, directorIndex)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "delete the director on a POST" in {
      val data = new FakeDataRetrievalAction(Some(testData))
      val result = controller(data).onSubmit(establisherIndex, directorIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      FakeDataCacheConnector.verify(DirectorDetailsId(establisherIndex, directorIndex), directorDetails.copy(isDeleted = true))
    }

    "redirect to the next page on a successful POST" in {
      val data = new FakeDataRetrievalAction(Some(testData))
      val result = controller(data).onSubmit(establisherIndex, directorIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(establisherIndex, directorIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val result = controller(dontGetAnyData).onSubmit(establisherIndex, directorIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }

}

object ConfirmDeleteDirectorControllerSpec extends ControllerSpecBase {

  private val establisherIndex = Index(0)
  private val directorIndex = Index(0)
  private val companyName = "MyCo Ltd"
  private val directorName = "John Doe"
  private lazy val postCall = routes.ConfirmDeleteDirectorController.onSubmit(establisherIndex, directorIndex)
  private lazy val cancelCall = AddCompanyDirectorsController.onPageLoad(NormalMode, establisherIndex)
  private val directorDetails = DirectorDetails("John", None, "Doe", LocalDate.now(), false)

  private val testData = Json.obj(
    EstablishersId.toString -> Json.arr(
      Json.obj(
        CompanyDetailsId.toString -> CompanyDetails(companyName, None, None),
        "director" -> Json.arr(
          Json.obj(
            DirectorDetailsId.toString ->
              directorDetails
          )
        )
      )
    )
  )

  private def onwardRoute = controllers.routes.IndexController.onPageLoad()

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new ConfirmDeleteDirectorController(
      frontendAppConfig,
      messagesApi,
      FakeDataCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl
    )

  private def viewAsString() = confirmDeleteDirector(
    frontendAppConfig,
    companyName,
    directorName,
    postCall,
    cancelCall
  )(fakeRequest, messages).toString

}
