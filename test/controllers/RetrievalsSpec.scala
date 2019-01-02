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

package controllers

import identifiers.TypedIdentifier
import identifiers.register.SchemeDetailsId
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.partnership.PartnershipDetailsId
import models.register.{SchemeDetails, SchemeType}
import models.requests.DataRequest
import models.{CompanyDetails, PartnershipDetails}
import org.scalatest.EitherValues
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContent, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.UserAnswers

import scala.concurrent.Future

class RetrievalsSpec extends ControllerSpecBase with FrontendController with Retrievals with EitherValues with ScalaFutures {

  def dataRequest(data: JsValue): DataRequest[AnyContent] = DataRequest(FakeRequest("", ""), "", UserAnswers(data), PsaId("A0000000"))

  class TestController extends FrontendController with Retrievals

  val controller = new TestController()

  val success: (String) => Future[Result] = { _: String =>
    Future.successful(Ok("Success"))
  }

  val testIdentifier: TypedIdentifier[String] = new TypedIdentifier[String] {
    override def toString: String = "test"
  }

  val secondIdentifier: TypedIdentifier[String] = new TypedIdentifier[String] {
    override def toString: String = "second"
  }

  "static" must {
    "return a retrieval which always successfully returns the argument" in {
      Retrieval.static("foobar").retrieve(dataRequest(Json.obj())).right.value mustEqual "foobar"
    }
  }

  "retrieveCompanyName" must {
    "reach the intended result when companyName is found" in {

      val validData = Json.obj(
        EstablishersId.toString -> Json.arr(
          Json.obj(
            CompanyDetailsId.toString -> CompanyDetails("companyName", Some("123456"), Some("abcd"))
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


  "retrieveSchemeName" must {
    "reach the intended result when schemeName is found" in {

      val validData = Json.obj(
        SchemeDetailsId.toString -> Json.toJson(SchemeDetails("schemeName", SchemeType.SingleTrust))
      )

      implicit val request: DataRequest[AnyContent] = dataRequest(validData)

      val result = controller.retrieveSchemeName(success)

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
          _ mustEqual Redirect(routes.SessionExpiredController.onPageLoad())
        }
      }

      "company name is not present" in {

        implicit val request: DataRequest[AnyContent] = dataRequest(Json.obj())

        val result = controller.retrieve(testIdentifier)(success)

        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }
    }
  }
}
