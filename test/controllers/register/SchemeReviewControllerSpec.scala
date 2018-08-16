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

package controllers.register

import config.FrontendAppConfig
import controllers.ControllerSpecBase
import controllers.actions._
import identifiers.register.SchemeDetailsId
import identifiers.register.establishers.EstablisherKindId
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.individual.EstablisherDetailsId
import identifiers.register.trustees.HaveAnyTrusteesId
import identifiers.register.trustees.individual.TrusteeDetailsId
import models.person.PersonDetails
import models.register.{SchemeDetails, SchemeType}
import models.{CheckMode, CompanyDetails, NormalMode}
import org.joda.time.LocalDate
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import play.api.test.Helpers.{contentAsString, _}
import utils.FakeNavigator
import views.html.register.schemeReview

class SchemeReviewControllerSpec extends ControllerSpecBase {

  import SchemeReviewControllerSpec._

  "SchemeReview Controller" when {

    "called GET" must {

      "return OK, the correct view and the correct Edit Urls for Trustees and establishers for individuals" in {
        val getRelevantData = new FakeDataRetrievalAction(Some(validData))
        val result = controller(getRelevantData).onPageLoad()(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(establisherIndvUrl, haveAnyTrusteeUrl, establisherIndv, trustees)
      }

      Seq(SchemeType.SingleTrust, SchemeType.MasterTrust).foreach { schemeType =>
        s"return OK, the correct view and the correct Edit Urls for Trustees and establishers for ${schemeType.toString}" in {
          val validData: JsObject = Json.obj(
            SchemeDetailsId.toString ->
              SchemeDetails("Test Scheme Name", schemeType),
            "establishers" -> Json.arr(
              Json.obj(
                EstablisherKindId.toString -> "company",
                CompanyDetailsId.toString -> CompanyDetails("establisher", None, None)
              )
            )
          )
          val getRelevantData = new FakeDataRetrievalAction(Some(validData))
          val result = controller(getRelevantData).onPageLoad()(fakeRequest)

          status(result) mustBe OK
          contentAsString(result) mustBe viewAsString(
            controllers.register.establishers.routes.AddEstablisherController.onPageLoad(NormalMode),
            addTrusteeUrl)
        }
      }
    }

    "redirect to the next page on submit" in {
      val result = controller().onSubmit()(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }
  }
}

object SchemeReviewControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val validData: JsObject = Json.obj(
    SchemeDetailsId.toString ->
      SchemeDetails("Test Scheme Name", SchemeType.SingleTrust),
    "establishers" -> Json.arr(
      Json.obj(
        EstablisherKindId.toString -> "individual",
        EstablisherDetailsId.toString -> PersonDetails("establisher", None, "name", LocalDate.now())
      )
    ),
    HaveAnyTrusteesId.toString -> false,
    "trustees" -> Json.arr(
      Json.obj(
        TrusteeDetailsId.toString -> PersonDetails("trustee", None, "name", LocalDate.now())
      ),
      Json.obj(
        identifiers.register.trustees.company.CompanyDetailsId.toString -> CompanyDetails("trustee company name", None, None)
      )
    )
  )

  val schemeName = "Test Scheme Name"
  val establisherIndv = Seq("establisher name")
  val establisherOrg = Seq("establisher")
  val trustees = Seq("trustee name", "trustee company name")
  val establisherIndvUrl: Call = controllers.register.establishers.routes.AddEstablisherController.onPageLoad(NormalMode)
  val establisherCompanyUrl: Call = controllers.register.establishers.company.routes.CompanyReviewController.onPageLoad(0)

  private val addTrusteeUrl = controllers.register.trustees.routes.AddTrusteeController.onPageLoad(CheckMode)
  private val haveAnyTrusteeUrl = controllers.register.trustees.routes.HaveAnyTrusteesController.onPageLoad(NormalMode)

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData):
  SchemeReviewController = {
    val application = new GuiceApplicationBuilder()
    val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

    new SchemeReviewController(appConfig, messagesApi, new FakeNavigator(desiredRoute = onwardRoute), FakeAuthAction,
      dataRetrievalAction, new DataRequiredActionImpl)
  }

  def viewAsString(establisherUrl: Call, trusteeUrl: Call, establishers: Seq[String] = establisherOrg, trustees: Seq[String] = Seq.empty): String =
    schemeReview(frontendAppConfig, schemeName, establishers, trustees, establisherUrl, trusteeUrl)(fakeRequest, messages).toString
}
