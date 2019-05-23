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
import connectors.PensionsSchemeConnector
import identifiers.IsPsaSuspendedId
import models.requests.OptionalDataRequest
import play.api.mvc.Results._
import play.api.mvc.{ActionFilter, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AllowAccessAction(srn: Option[String], pensionsSchemeConnector: PensionsSchemeConnector) extends ActionFilter[OptionalDataRequest] {

  override protected def filter[A](request: OptionalDataRequest[A]): Future[Option[Result]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))
    request.userAnswers match {
      case None =>
        Future.successful(None)
      case Some(userAnswers) => userAnswers.get(IsPsaSuspendedId) match {
        case Some(true) => Future.successful(Some(Redirect(controllers.register.routes.CannotMakeChangesController.onPageLoad(srn))))
        case _ => srn
          .map(pensionsSchemeConnector.checkForAssociation(request.psaId.id, _)(hc, global, request))
          .fold[Future[Option[Result]]](Future.successful(None)) {
          _.map {
            case true => None
            case _ => Some(NotFound)
          }
        }
      }
    }
  }

}

class AllowAccessActionProviderImpl @Inject()(pensionsSchemeConnector: PensionsSchemeConnector) extends AllowAccessActionProvider {
  def apply(srn: Option[String]): AllowAccessAction = {
    new AllowAccessAction(srn, pensionsSchemeConnector)
  }
}

@ImplementedBy(classOf[AllowAccessActionProviderImpl])
trait AllowAccessActionProvider {
  def apply(srn: Option[String]): AllowAccessAction
}
