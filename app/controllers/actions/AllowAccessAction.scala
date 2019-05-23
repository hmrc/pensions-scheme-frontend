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

import com.google.inject.{ImplementedBy, Inject}
import connectors.{PensionsSchemeConnector, SchemeDetailsReadOnlyCacheConnector}
import identifiers.IsPsaSuspendedId
import models.UpdateMode
import models.requests.OptionalDataRequest
import play.api.mvc.Results._
import play.api.mvc.{ActionFilter, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AllowAccessAction(srn: Option[String],
                        pensionsSchemeConnector: PensionsSchemeConnector,
                        viewConnector: SchemeDetailsReadOnlyCacheConnector
                       ) extends ActionFilter[OptionalDataRequest] {

  override protected def filter[A](request: OptionalDataRequest[A]): Future[Option[Result]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

    val optionUA = request.userAnswers
    val optionIsSuspendedId = optionUA.flatMap(_.get(IsPsaSuspendedId))

    (optionUA, optionIsSuspendedId, srn) match {
      case (Some(_), Some(true), _) => Future.successful(Some(Redirect(controllers.register.routes.CannotMakeChangesController.onPageLoad(srn))))
      case (Some(ua), _, Some(extractedSRN)) =>
        pensionsSchemeConnector.checkForAssociation(request.psaId.id, extractedSRN)(hc, global, request) flatMap {
          case true =>
            if (request.viewOnly) {
              viewConnector.fetch(request.externalId).map {
                case None => Some(Redirect(controllers.routes.SchemeTaskListController.onPageLoad(UpdateMode, srn)))
                case Some(jsValue) => None
              }
            } else {
              Future.successful(None)
            }
          case _ => Future.successful(Some(NotFound))
        }
      case _ => Future.successful(None)
    }
  }

}

class AllowAccessActionProviderImpl @Inject()(pensionsSchemeConnector: PensionsSchemeConnector,
                                              viewConnector: SchemeDetailsReadOnlyCacheConnector) extends AllowAccessActionProvider {
  def apply(srn: Option[String]): AllowAccessAction = {
    new AllowAccessAction(srn, pensionsSchemeConnector, viewConnector)
  }
}

@ImplementedBy(classOf[AllowAccessActionProviderImpl])
trait AllowAccessActionProvider {
  def apply(srn: Option[String]): AllowAccessAction
}
