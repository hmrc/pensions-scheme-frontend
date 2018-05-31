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

package controllers.register.establishers

import connectors.FakeDataCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.individual.EstablisherDetailsId
import models.register.establishers.EstablisherKind
import models.register.establishers.individual.EstablisherDetails
import models.{Index, NormalMode}
import org.joda.time.LocalDate
import play.api.libs.json._
import play.api.test.Helpers._
import utils.FakeNavigator
import views.html.register.establishers.confirmDeleteEstablisher

class ConfirmDeleteEstablisherControllerSpec extends ControllerSpecBase {

  import ConfirmDeleteEstablisherControllerSpec._

  "ConfirmDeleteEstablisher Controller" must {
    "return OK and the correct view for a GET" in {
      val data = new FakeDataRetrievalAction(Some(testData))
      val result = controller(data).onPageLoad(establisherIndex, establisherKind)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "delete the establisher on a POST" in {
      val data = new FakeDataRetrievalAction(Some(testData))
      val result = controller(data).onSubmit(establisherIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      FakeDataCacheConnector.verifyRemoved(EstablishersId(establisherIndex))
    }

    "redirect to the next page on a successful POST" in {
      val data = new FakeDataRetrievalAction(Some(testData))
      val result = controller(data).onSubmit(establisherIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(establisherIndex, establisherKind)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val result = controller(dontGetAnyData).onSubmit(establisherIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }

}

object ConfirmDeleteEstablisherControllerSpec extends ControllerSpecBase {

  private val establisherIndex = Index(0)
  private val schemeName = "MyScheme Ltd"
  private val establisherName = "John Doe"
  private val establisherKind = EstablisherKind.Indivdual
  private lazy val postCall = routes.ConfirmDeleteEstablisherController.onSubmit(establisherIndex)
  private lazy val cancelCall = routes.AddEstablisherController.onPageLoad(NormalMode)

  private val day = LocalDate.now().getDayOfMonth
  private val month = LocalDate.now().getMonthOfYear
  private val year = LocalDate.now().getYear - 20

  private val testData = Json.obj(
    EstablishersId.toString -> Json.arr(
      Json.obj(
        EstablisherDetailsId.toString -> EstablisherDetails("John", None, "Doe", new LocalDate(year, month, day))
      )
    )
  )

  private def onwardRoute = controllers.routes.IndexController.onPageLoad()

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new ConfirmDeleteEstablisherController(
      frontendAppConfig,
      messagesApi,
      FakeDataCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl
    )

  private def viewAsString() = confirmDeleteEstablisher(
    frontendAppConfig,
    schemeName,
    establisherName,
    postCall,
    cancelCall
  )(fakeRequest, messages).toString

}
