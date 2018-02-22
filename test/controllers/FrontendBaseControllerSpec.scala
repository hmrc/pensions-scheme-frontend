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

package controllers

import identifiers.register.SchemeDetailsId
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.company.{CompanyAddressId, CompanyDetailsId}
import models.CompanyDetails
import models.register.{SchemeDetails, SchemeType}
import models.requests.DataRequest
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.UserAnswers
import play.api.mvc.Results._

import scala.concurrent.Future

class FrontendBaseControllerSpec extends ControllerSpecBase {

  val controller = new FrontendBaseController{}

  val success: (String) => Future[Result] = { _: String =>
    Future.successful(Ok("Success"))
  }

  "retrieveCompanyName" must {

    "reach the intended result when companyName is found" in {

      val validData = Json.obj(
        EstablishersId.toString -> Json.arr(
          Json.obj(
            CompanyDetailsId.toString -> CompanyDetails("companyName", Some("123456"), Some("abcd"))
          ))
      )

      implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest("", ""), "", UserAnswers(validData))

      val result = controller.retrieveCompanyName(0)(success)

      status(result) must be(OK)
    }

    "redirect to Session Expired page when company name is not present" in {

      implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest("", ""), "", UserAnswers(Json.obj()))

      val result = controller.retrieveCompanyName(0)(success)

      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)

    }

  }

}
