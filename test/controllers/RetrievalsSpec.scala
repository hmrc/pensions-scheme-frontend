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

package controllers

import com.google.inject.Inject
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.partnership.PartnershipDetailsId
import identifiers.{SchemeNameId, TypedIdentifier}
import models.requests.{DataRequest, IdentifiedRequest}
import models.{CompanyDetails, PartnershipDetails}
import org.scalatest.EitherValues
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.Results._
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import utils.{FakeDataRequest, FakeOptionalDataRequest, UserAnswers}

import scala.concurrent.Future

class RetrievalsSpec extends ControllerSpecBase with Retrievals with EitherValues with ScalaFutures {

  def dataRequest(data: JsValue): DataRequest[AnyContent] = DataRequest(FakeRequest("", ""), "", UserAnswers(data), Some(PsaId("A0000000")))

  class TestController @Inject()(
                                  val controllerComponents: MessagesControllerComponents
                                ) extends FrontendBaseController with Retrievals

  val controller = new TestController(controllerComponents)

  val success: String => Future[Result] = { _: String =>
    Future.successful(Ok("Success"))
  }

  val testIdentifier: TypedIdentifier[String] = new TypedIdentifier[String] {
    override def toString: String = "test"
  }

  val secondIdentifier: TypedIdentifier[String] = new TypedIdentifier[String] {
    override def toString: String = "second"
  }

  case class OtherDataRequest[A](request: Request[A], externalId: String, psaId: PsaId)
    extends WrappedRequest[A](request) with IdentifiedRequest

  class FakeOtherDataRequest(request: Request[AnyContentAsEmpty.type], externalId: String, psaId: PsaId)
    extends OtherDataRequest[AnyContent](request, externalId, psaId)

  object FakeOtherDataRequest {
    def apply(): FakeOtherDataRequest = {
      new FakeOtherDataRequest(FakeRequest("", ""), "test-external-id", PsaId("A0000000"))
    }
  }

  val validData: JsObject = Json.obj(SchemeNameId.toString -> "Test Scheme")

  "retrieveCompanyName" must {
    "reach the intended result when companyName is found" in {

      val validData = Json.obj(
        EstablishersId.toString -> Json.arr(
          Json.obj(
            CompanyDetailsId.toString -> CompanyDetails("companyName")
          ))
      )

      implicit val request: DataRequest[AnyContent] = dataRequest(validData)

      val result = controller.retrieveCompanyName(0)(success)

      status(result) must be(OK)
    }
  }

  "retrievePartnershipName" must {
    "reach the intended result when partnershipName is found" in {

      val validData = Json.obj(
        EstablishersId.toString -> Json.arr(
          Json.obj(
            PartnershipDetailsId.toString -> PartnershipDetails("test name")
          ))
      )

      implicit val request: DataRequest[AnyContent] = dataRequest(validData)

      val result = controller.retrievePartnershipName(0)(success)

      status(result) must be(OK)
    }
  }

  "retrieve" must {

    "reach the intended result" when {
      "identifier gets value from answers" in {

        implicit val request: DataRequest[AnyContent] = dataRequest(Json.obj("test" -> "result"))

        testIdentifier.retrieve.right.value mustEqual "result"
      }

      "identifier uses and to get the value from answers" in {

        implicit val request: DataRequest[AnyContent] = dataRequest(Json.obj("test" -> "result", "second" -> "answer"))

        (testIdentifier and secondIdentifier).retrieve.right.value mustEqual new ~("result", "answer")
      }
    }

    "redirect to the session expired page" when {
      "cant find identifier" in {

        implicit val request: DataRequest[AnyContent] = dataRequest(Json.obj("test1" -> "result"))

        whenReady(testIdentifier.retrieve.left.value) {
          _ mustEqual Redirect(routes.SessionExpiredController.onPageLoad)
        }
      }

      "company name is not present" in {

        implicit val request: DataRequest[AnyContent] = dataRequest(Json.obj())

        val result = controller.retrieve(testIdentifier)(success)

        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
      }
    }

    "existingSchemeName" must{

      "return data if calling with DataRequest" in {
        implicit val request: FakeDataRequest = FakeDataRequest(UserAnswers(validData))
        controller.existingSchemeName mustBe  Some("Test Scheme")
      }

      "return data if calling with OptionalDataRequest" in {
        implicit val request: FakeOptionalDataRequest = FakeOptionalDataRequest(Some(UserAnswers(validData)))
        controller.existingSchemeName mustBe  Some("Test Scheme")
      }

      "return none if calling with FakeOptionalDataRequest" in {
        implicit val request: FakeOptionalDataRequest = FakeOptionalDataRequest(Some(UserAnswers(Json.obj())))
        controller.existingSchemeName mustBe None
      }

      "return none if calling with FakeOtherDataRequest" in {
        implicit val request: FakeOtherDataRequest = FakeOtherDataRequest()
        controller.existingSchemeName mustBe None
      }
    }
  }
}
