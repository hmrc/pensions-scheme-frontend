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

package controllers.actions

import identifiers.MinimalPsaDetailsId
import models.requests.OptionalDataRequest
import play.api.mvc.Results._
import play.api.mvc.{ActionFilter, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter

import scala.concurrent.Future

class AllowAccessForNonSuspendedUsersAction(
                                             optionSRN: Option[String]
                                           ) extends ActionFilter[OptionalDataRequest] {

  override protected def filter[A](request: OptionalDataRequest[A]): Future[Option[Result]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))
    Future.successful(
      (optionSRN, request.userAnswers.flatMap(_.get(MinimalPsaDetailsId))) match {
        case (None, _) => Option(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
        case (Some(srn), None) => Option(Redirect(controllers.routes.PSASchemeDetailsController.onPageLoad(srn)))
        case (Some(srn), Some(md)) if md.isPsaSuspended => Some(Redirect(controllers.register.routes.CannotMakeChangesController.onPageLoad(srn)))
        case _ => None
      }
    )
  }
}

class AllowAccessForNonSuspendedUsersActionProviderImpl
  extends AllowAccessForNonSuspendedUsersActionProvider {
  def apply(srn: Option[String]): AllowAccessForNonSuspendedUsersAction = new AllowAccessForNonSuspendedUsersAction(srn)
}

trait AllowAccessForNonSuspendedUsersActionProvider {
  def apply(srn: Option[String]): AllowAccessForNonSuspendedUsersAction
}
