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

package controllers.actions

import com.google.inject.Inject
import connectors.PensionsSchemeConnector
import handlers.ErrorHandlerWithReturnLinkToManage
import identifiers.PsaMinimalFlagsId
import PsaMinimalFlagsId._
import config.FrontendAppConfig
import models.{PSAMinimalFlags, UpdateMode}
import models.requests.OptionalDataRequest
import play.api.http.Status._
import play.api.mvc.Results._
import play.api.mvc.{ActionFilter, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import uk.gov.hmrc.play.bootstrap.frontend.http.FrontendErrorHandler

import scala.concurrent.{ExecutionContext, Future}

abstract class AllowAccessAction(srn: Option[String],
                                 pensionsSchemeConnector: PensionsSchemeConnector,
                                 config: FrontendAppConfig,
                                 errorHandler: FrontendErrorHandler
                                )(implicit val executionContext: ExecutionContext) extends
  ActionFilter[OptionalDataRequest] {

  protected def filter[A](request: OptionalDataRequest[A],
                          destinationForNoUserAnswersAndSRN: => Option[Result],
                          checkForSuspended: Boolean
                         ): Future[Option[Result]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    val optionUA = request.userAnswers

    val optionPsaMinimalFlagsId = optionUA.flatMap(_.get(PsaMinimalFlagsId))

    (optionUA, optionPsaMinimalFlagsId, srn) match {
      case (Some(_), Some(PSAMinimalFlags(true, false, _)), _) if checkForSuspended =>
        Future.successful(Some(Redirect(controllers.register.routes.CannotMakeChangesController.onPageLoad(srn))))
      case (Some(_), Some(PSAMinimalFlags(_, true, _)), _) =>
        Future.successful(Some(Redirect(config.youMustContactHMRCUrl)))
      case (Some(_), _, Some(extractedSRN)) => checkForAssociation(request, extractedSRN)
      case (None, _, Some(extractedSRN)) => checkForAssociation(request, extractedSRN).map {
        case None => destinationForNoUserAnswersAndSRN
        case notAssociatedResult@Some(_) => notAssociatedResult
      }
      case _ => Future.successful(None)
    }
  }

  private def checkForAssociation[A](request: OptionalDataRequest[A],
                                     extractedSRN: String)(implicit hc: HeaderCarrier): Future[Option[Result]] = {
    request.psaId.map { psaId =>
      pensionsSchemeConnector.checkForAssociation(psaId.id, extractedSRN)(hc, implicitly, request).flatMap {
        case Right(true) => Future.successful(None)
        case _ => errorHandler.onClientError(request, NOT_FOUND, "").map(Some.apply)
      }
    }
  }.getOrElse(errorHandler.onClientError(request, NOT_FOUND, "").map(Some.apply))
}

class AllowAccessActionMain(
                             srn: Option[String],
                             pensionsSchemeConnector: PensionsSchemeConnector,
                             config: FrontendAppConfig,
                             errorHandler: FrontendErrorHandler
                           )(implicit executionContext: ExecutionContext) extends AllowAccessAction(srn,
  pensionsSchemeConnector, config, errorHandler) {


  override protected def filter[A](request: OptionalDataRequest[A]): Future[Option[Result]] = {
    filter(request,
      destinationForNoUserAnswersAndSRN = Some(Redirect(controllers.routes.PsaSchemeTaskListController.onPageLoad
      (UpdateMode, srn))),
      checkForSuspended = true
    )
  }
}

class AllowAccessActionTaskList(
                                 srn: Option[String],
                                 pensionsSchemeConnector: PensionsSchemeConnector,
                                 config: FrontendAppConfig,
                                 errorHandler: FrontendErrorHandler
                               )(implicit ec: ExecutionContext) extends AllowAccessAction(srn,
  pensionsSchemeConnector, config, errorHandler) {


  override protected def filter[A](request: OptionalDataRequest[A]): Future[Option[Result]] = {
    filter(request,
      destinationForNoUserAnswersAndSRN = None,
      checkForSuspended = false
    )
  }
}

class AllowAccessActionNoSuspendedCheck(
                                         srn: Option[String],
                                         pensionsSchemeConnector: PensionsSchemeConnector,
                                         config: FrontendAppConfig,
                                         errorHandler: FrontendErrorHandler
                                       )(implicit ec: ExecutionContext) extends AllowAccessAction(srn,
  pensionsSchemeConnector, config, errorHandler) {


  override protected def filter[A](request: OptionalDataRequest[A]): Future[Option[Result]] = {
    filter(request,
      destinationForNoUserAnswersAndSRN = Some(Redirect(controllers.routes.PsaSchemeTaskListController.onPageLoad
      (UpdateMode, srn))),
      checkForSuspended = false
    )
  }
}

class AllowAccessActionProviderMainImpl @Inject()(
                                                   pensionsSchemeConnector: PensionsSchemeConnector,
                                                   config: FrontendAppConfig,
                                                   errorHandler: ErrorHandlerWithReturnLinkToManage
                                                 )(implicit ec: ExecutionContext) extends AllowAccessActionProvider {

  def apply(srn: Option[String]): AllowAccessAction = {
    new AllowAccessActionMain(srn, pensionsSchemeConnector, config, errorHandler)
  }
}

class AllowAccessActionProviderTaskListImpl @Inject()(
                                                       pensionsSchemeConnector: PensionsSchemeConnector,
                                                       config: FrontendAppConfig,
                                                       errorHandler: ErrorHandlerWithReturnLinkToManage
                                                     )(implicit ec: ExecutionContext) extends
  AllowAccessActionProvider {

  def apply(srn: Option[String]): AllowAccessAction = {
    new AllowAccessActionTaskList(srn, pensionsSchemeConnector, config, errorHandler)
  }
}

class AllowAccessActionProviderNoSuspendedCheckImpl @Inject()(
                                                               pensionsSchemeConnector: PensionsSchemeConnector,
                                                               config: FrontendAppConfig,
                                                               errorHandler: ErrorHandlerWithReturnLinkToManage
                                                             )(implicit ec: ExecutionContext) extends
  AllowAccessActionProvider {

  def apply(srn: Option[String]): AllowAccessAction = {
    new AllowAccessActionNoSuspendedCheck(srn, pensionsSchemeConnector, config, errorHandler)
  }
}


trait AllowAccessActionProvider {
  def apply(srn: Option[String]): AllowAccessAction
}
