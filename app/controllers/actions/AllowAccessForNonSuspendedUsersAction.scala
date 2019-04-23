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

import com.google.inject.Inject
import connectors.{PensionSchemeVarianceLockConnector, UpdateSchemeCacheConnector, UserAnswersCacheConnector}
import identifiers.MinimalPsaDetailsId
import models.VarianceLock
import models.requests.AuthenticatedRequest
import play.api.mvc.Results._
import play.api.mvc.{ActionFilter, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import utils.UserAnswers
import utils.annotations.SchemeDetailsReadOnly

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AllowAccessForNonSuspendedUsersAction(
                                             lockConnector: PensionSchemeVarianceLockConnector,
                                             @SchemeDetailsReadOnly schemeDetailsReadOnlyCacheConnector: UserAnswersCacheConnector,
                                             updateConnector: UpdateSchemeCacheConnector,
                                             srn: String
                                           ) extends ActionFilter[AuthenticatedRequest] {

  override protected def filter[A](request: AuthenticatedRequest[A]): Future[Option[Result]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

    def errorOptionResult:Option[Result] = Some(Redirect(controllers.routes.PSASchemeDetailsController.onPageLoad(srn)))

    lockConnector.isLockByPsaIdOrSchemeId(request.psaId.id, srn).flatMap { optionLock =>
      val futureJsValue = optionLock match {
        case Some(VarianceLock) =>
          updateConnector.fetch(srn)
        case _ => schemeDetailsReadOnlyCacheConnector.fetch(request.externalId)
      }

      futureJsValue.map {
        case None => errorOptionResult
        case Some(ua) =>
          UserAnswers(ua).get(MinimalPsaDetailsId) match {
            case None => errorOptionResult
            case Some(md) =>
              if (md.isPsaSuspended) {
                Some(Redirect(controllers.register.routes.CannotMakeChangesController.onPageLoad(srn)))
              } else {
                None
              }
          }
      }
    }
  }
}

class AllowAccessForNonSuspendedUsersActionProviderImpl @Inject()(lockConnector: PensionSchemeVarianceLockConnector,
                                                                  @SchemeDetailsReadOnly schemeDetailsReadOnlyCacheConnector: UserAnswersCacheConnector,
                                                                  updateConnector: UpdateSchemeCacheConnector)
  extends AllowAccessForNonSuspendedUsersActionProvider {
  def apply(srn: String): AllowAccessForNonSuspendedUsersAction = new AllowAccessForNonSuspendedUsersAction(lockConnector,
    schemeDetailsReadOnlyCacheConnector,
    updateConnector, srn)
}

trait AllowAccessForNonSuspendedUsersActionProvider {
  def apply(srn: String): AllowAccessForNonSuspendedUsersAction
}

