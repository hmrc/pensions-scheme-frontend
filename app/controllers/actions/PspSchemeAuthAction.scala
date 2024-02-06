/*
 * Copyright 2024 HM Revenue & Customs
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

import connectors.SchemeDetailsConnector
import handlers.ErrorHandler
import models.details.AuthorisedPractitioner
import models.requests.AuthenticatedRequest
import play.api.Logging
import play.api.mvc.Results.NotFound
import play.api.mvc.{ActionFunction, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendHeaderCarrierProvider

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

private class PspSchemeActionImpl (srn: String, schemeDetailsConnector: SchemeDetailsConnector, errorHandler: ErrorHandler)
                          (implicit val executionContext: ExecutionContext)
  extends ActionFunction[AuthenticatedRequest, AuthenticatedRequest] with FrontendHeaderCarrierProvider with Logging {

  private def notFoundTemplate(implicit request: AuthenticatedRequest[_]) = NotFound(errorHandler.notFoundTemplate)
  override def invokeBlock[A](request: AuthenticatedRequest[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] = {

    request.pspId.map { pspId =>
      schemeDetailsConnector.getPspSchemeDetails(
        pspId = pspId.id,
        srn = srn
      )(hc(request), executionContext).flatMap { schemeDetails =>

        val pspDetails = (schemeDetails.json \ "pspDetails").as[AuthorisedPractitioner]
        if (pspDetails.id == pspId.id) {
          block(request)
        } else {
          Future.successful(notFoundTemplate(request))
        }
      } recover {
        case err =>
          logger.error("getPspSchemeDetails failed", err)
          notFoundTemplate(request)
      }
    }.getOrElse(Future.successful(notFoundTemplate(request)))
  }
}


class PspSchemeAuthAction @Inject()(schemeDetailsConnector: SchemeDetailsConnector, errorHandler: ErrorHandler)(implicit ec: ExecutionContext){
  /**
   * @param srn - If empty, srn is expected to be retrieved from Session. If present srn is expected to be retrieved form the URL
   * @return
   */
  def apply(srn: String): ActionFunction[AuthenticatedRequest, AuthenticatedRequest] =
    new PspSchemeActionImpl(srn, schemeDetailsConnector, errorHandler)

}
