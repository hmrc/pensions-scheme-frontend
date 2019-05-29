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
import connectors.PensionsSchemeConnector
import handlers.ErrorHandlerWithReturnLinkToManage
import identifiers.IsPsaSuspendedId
import models.UpdateMode
import models.requests.OptionalDataRequest
import play.api.http.Status._
import play.api.mvc.Results._
import play.api.mvc.{ActionFilter, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.bootstrap.http.FrontendErrorHandler

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.hmrc.http.BadRequestException

abstract class AllowAccessAction(srn: Option[String],
                              pensionsSchemeConnector: PensionsSchemeConnector,
                              errorHandler: FrontendErrorHandler
                             ) extends ActionFilter[OptionalDataRequest] {
  protected def filter[A](request: OptionalDataRequest[A], destinationForNoUAAndSRN: => Future[Option[Result]] ): Future[Option[Result]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

    val optionUA = request.userAnswers
    val optionIsSuspendedId = optionUA.flatMap(_.get(IsPsaSuspendedId))

    (optionUA, optionIsSuspendedId, srn) match {
      case (Some(_), Some(true), _) => Future.successful(Some(Redirect(controllers.register.routes.CannotMakeChangesController.onPageLoad(srn))))
      case (Some(_), _, Some(extractedSRN)) =>
        pensionsSchemeConnector.checkForAssociation(request.psaId.id, extractedSRN)(hc, global, request).flatMap {
          case true => Future.successful(None)
          case _ => errorHandler.onClientError(request, NOT_FOUND, "").map(Some.apply)
        }.recoverWith {
          case ex:BadRequestException if ex.message.contains("INVALID_SRN") =>
            errorHandler.onClientError(request, NOT_FOUND, "").map(Some.apply)
        }
      case (None, _, Some(_)) => destinationForNoUAAndSRN
      case _ => Future.successful(None)
    }
  }
}

class AllowAccessActionMain(srn: Option[String],
                        pensionsSchemeConnector: PensionsSchemeConnector,
                        errorHandler: FrontendErrorHandler
                       ) extends AllowAccessAction(srn,pensionsSchemeConnector, errorHandler) {


  override protected def filter[A](request: OptionalDataRequest[A]): Future[Option[Result]] = {
    filter(request, Future.successful(Some(Redirect(controllers.routes.SchemeTaskListController.onPageLoad(UpdateMode, srn)))))
  }
}

class AllowAccessActionTaskList(srn: Option[String],
                            pensionsSchemeConnector: PensionsSchemeConnector,
                            errorHandler: FrontendErrorHandler
                           ) extends AllowAccessAction(srn,pensionsSchemeConnector, errorHandler) {


  override protected def filter[A](request: OptionalDataRequest[A]): Future[Option[Result]] = {
    filter(request, Future.successful(None))
  }
}

class AllowAccessActionProviderMainImpl @Inject()(pensionsSchemeConnector: PensionsSchemeConnector,
                                              errorHandler: ErrorHandlerWithReturnLinkToManage) extends AllowAccessActionProvider {
  def apply(srn: Option[String]): AllowAccessAction = {
    new AllowAccessActionMain(srn, pensionsSchemeConnector, errorHandler)
  }
}

class AllowAccessActionProviderTaskListImpl @Inject()(pensionsSchemeConnector: PensionsSchemeConnector,
                                                  errorHandler: ErrorHandlerWithReturnLinkToManage) extends AllowAccessActionProvider {
  def apply(srn: Option[String]): AllowAccessAction = {
    new AllowAccessActionTaskList(srn, pensionsSchemeConnector, errorHandler)
  }
}

trait AllowAccessActionProvider {
  def apply(srn: Option[String]): AllowAccessAction
}
