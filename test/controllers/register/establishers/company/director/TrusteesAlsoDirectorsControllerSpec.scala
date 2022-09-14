/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.establishers.company.director

import controllers.ControllerSpecBase
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAuthAction, FakeDataRetrievalAction}
import forms.dataPrefill.DataPrefillCheckboxFormProvider
import identifiers.establishers.company.CompanyDetailsId
import matchers.JsonMatchers
import models.CompanyDetails
import models.prefill.IndividualDetails
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.scalatest.{BeforeAndAfterEach, TryValues}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{AnyContentAsJson, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import renderer.Renderer
import services.DataPrefillService
import uk.gov.hmrc.nunjucks.NunjucksSupport
import utils.Data.ua
import utils.{Data, FakeNavigator, UserAnswers}

import scala.concurrent.Future

class TrusteesAlsoDirectorsControllerSpec extends ControllerSpecBase
  with NunjucksSupport
  with JsonMatchers
  with TryValues
  with BeforeAndAfterEach {

  private val formProvider: DataPrefillCheckboxFormProvider = new DataPrefillCheckboxFormProvider()
  private val form = formProvider(6,"", "", "")
  private val onwardRoute: String = "/add-pension-scheme/index"
  private val templateToBeRendered: String = "dataPrefillCheckbox.njk"
  private val mockDataPrefillService = mock[DataPrefillService]
  private val companyDetails: CompanyDetails = CompanyDetails("test company")
  private val userAnswers: UserAnswers = ua.set(CompanyDetailsId(0), companyDetails).success.value

  private val commonJson: JsObject =
    Json.obj(
      "form" -> form,
      "schemeName" -> Data.schemeName
    )
  override def beforeEach: Unit = {
    reset(
      mockRenderer,
      mockUserAnswersCacheConnector,
      mockDataPrefillService
    )
    when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))
    when(mockDataPrefillService.getListOfTrusteesToBeCopied(any())(any())).thenReturn(Nil)
  }

  private def controller(
                          dataRetrievalAction: DataRetrievalAction
                        ): TrusteesAlsoDirectorsController =
    new TrusteesAlsoDirectorsController(
      messagesApi = messagesApi,
      navigator = new FakeNavigator(desiredRoute = controllers.routes.IndexController.onPageLoad()),
      authenticate = new FakeAuthAction(),
      getData = dataRetrievalAction,
      requireData = new DataRequiredActionImpl,
      formProvider = formProvider,
      dataPrefillService = mockDataPrefillService,
      config = appConfig,
      controllerComponents = controllerComponents,
      userAnswersCacheConnector = mockUserAnswersCacheConnector,
      renderer = new Renderer(mockAppConfig, mockRenderer)
    )

  private val templateCaptor : ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
  private val jsonCaptor: ArgumentCaptor[JsObject] = ArgumentCaptor.forClass(classOf[JsObject])

  "TrusteesAlsoDirectorsController" must {
    "return OK and the correct view for a GET" in {
      when(mockDataPrefillService.getListOfTrusteesToBeCopied(any())(any())).thenReturn(Seq(IndividualDetails("", "", false, None, None, 0, true, None)))
      val getData = new FakeDataRetrievalAction(Some(userAnswers))

      val result: Future[Result] = controller(getData).onPageLoad(0)(fakeDataRequest(userAnswers))

      status(result) mustBe OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())
      templateCaptor.getValue mustEqual templateToBeRendered
      val json: JsObject = Json.obj("form" -> form)
      jsonCaptor.getValue must containJson(commonJson ++ json)
    }

    "redirect to spoke task list page for a GET when there are no trustees to be copied" in {
      when(mockDataPrefillService.getListOfTrusteesToBeCopied(any())(any())).thenReturn(Nil)
      val getData = new FakeDataRetrievalAction(Some(userAnswers))

      val result: Future[Result] = controller(getData).onPageLoad(0)(fakeDataRequest(userAnswers))

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.establishers.company.routes.SpokeTaskListController.onPageLoad(0).url)
    }

    "copy the directors and redirect to the next page when valid data is submitted with value less than max directors" in {
      when(mockDataPrefillService.copyAllTrusteesToDirectors(any(), any(), any())).thenReturn(userAnswers)
      when(mockUserAnswersCacheConnector.save(any(), any())(any(), any())).thenReturn(Future.successful(Json.obj()))
      val request: FakeRequest[AnyContentAsJson] = fakeRequest.withJsonBody(Json.obj("value" -> Seq("0")))

      val getData = new FakeDataRetrievalAction(Some(userAnswers))
      val result: Future[Result] = controller(getData).onSubmit(0)(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute)
      verify(mockUserAnswersCacheConnector, times(1)).save(any(), any())(any(), any())
      verify(mockDataPrefillService, times(1)).copyAllTrusteesToDirectors(any(), any(), any())
    }

    "don't copy the trustees and redirect to the next page when the value is none of the above" in {
      when(mockUserAnswersCacheConnector.save(any(), any())(any(), any())).thenReturn(Future.successful(Json.obj()))
      val request: FakeRequest[AnyContentAsJson] = fakeRequest.withJsonBody(Json.obj("value" -> Seq("-1")))

      val getData = new FakeDataRetrievalAction(Some(userAnswers))
      val result: Future[Result] = controller(getData).onSubmit(0)(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute)
      verify(mockUserAnswersCacheConnector, times(1)).save(any(), any())(any(), any())
      verify(mockDataPrefillService, never).copyAllTrusteesToDirectors(any(), any(), any())
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val request: FakeRequest[AnyContentAsJson] = fakeRequest.withJsonBody(Json.obj("value" -> Seq("invalid")))
      val getData = new FakeDataRetrievalAction(Some(userAnswers))

      val result: Future[Result] = controller(getData).onSubmit(0)(request)

      status(result) mustBe BAD_REQUEST
      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())
      templateCaptor.getValue mustEqual templateToBeRendered
    }
  }
}
