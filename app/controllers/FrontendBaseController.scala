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

import identifiers.TypedIdentifier
import identifiers.register.SchemeDetailsId
import identifiers.register.establishers.company.CompanyDetailsId
import models.CompanyDetails
import models.register.SchemeDetails
import models.requests.DataRequest
import play.api.libs.json.Reads
import play.api.mvc.{AnyContent, Result}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.Future

trait FrontendBaseController extends FrontendController {

  private[controllers] def retrieveCompanyName(index: Int)
                                              (f: (String) => Future[Result])
                                              (implicit request: DataRequest[AnyContent]): Future[Result] = {
    retrieve[CompanyDetails](CompanyDetailsId(index)){ companyDetails =>
      f(companyDetails.companyName)
    }
  }


  private[controllers] def retrieveSchemeName(f: String => Future[Result])
                                             (implicit request: DataRequest[AnyContent]): Future[Result] = {
    retrieve[SchemeDetails](SchemeDetailsId){ schemeDetails =>
      f(schemeDetails.schemeName)
    }
  }

  private[controllers] def retrieve[A](id: TypedIdentifier[A])
                                      (f: (A) => Future[Result])
                                      (implicit request: DataRequest[AnyContent], r: Reads[A]): Future[Result] = {
    request.userAnswers.get(id).map(f).getOrElse {
      Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
    }

  }

}
