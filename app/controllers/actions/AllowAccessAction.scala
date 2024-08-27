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

import com.google.inject.Inject
import connectors.PensionsSchemeConnector
import handlers.ErrorHandlerWithReturnLinkToManage
import identifiers.PsaMinimalFlagsId
import PsaMinimalFlagsId._
import config.FrontendAppConfig
import models.{PSAMinimalFlags, SchemeReferenceNumber, UpdateMode}
import models.requests.OptionalDataRequest
import play.api.Logging
import play.api.http.Status._
import play.api.mvc.Results._
import play.api.mvc.{ActionFilter, Result}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import uk.gov.hmrc.play.bootstrap.frontend.http.FrontendErrorHandler

import scala.concurrent.{ExecutionContext, Future}

abstract class AllowAccessAction(srn: SchemeReferenceNumber,
                                 pensionsSchemeConnector: PensionsSchemeConnector,
                                 config: FrontendAppConfig,
                                 errorHandler: FrontendErrorHandler,
                                 allowPsa: Boolean,
                                 allowPsp: Boolean
                                )(implicit val executionContext: ExecutionContext) extends
  ActionFilter[OptionalDataRequest] with Logging {

  protected def filter[A](request: OptionalDataRequest[A],
                          destinationForNoUserAnswersAndSRN: => Option[Result],
                          checkForSuspended: Boolean
                         ): Future[Option[Result]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    val optionUA = request.userAnswers

    val optionPsaMinimalFlagsId = optionUA.flatMap(_.get(PsaMinimalFlagsId))

    (optionUA, optionPsaMinimalFlagsId, srn) match {
      case (Some(_), Some(PSAMinimalFlags(true, false, _)), _) if checkForSuspended =>
        Future.successful(Some(Redirect(controllers.register.routes.CannotMakeChangesController.onPageLoad((srn)))))
      case (Some(_), Some(PSAMinimalFlags(_, true, _)), _) =>
        Future.successful(Some(Redirect(config.youMustContactHMRCUrl)))
      case (Some(_), _, (extractedSRN)) => checkForAssociation(request, extractedSRN)
      case (None, _, (extractedSRN)) => checkForAssociation(request, extractedSRN).map {
        case None => destinationForNoUserAnswersAndSRN
        case notAssociatedResult@Some(_) => notAssociatedResult
      }
      case _ =>
        request.psaId -> request.pspId match {
          case (Some(_), _) if allowPsa => Future.successful(None)
          case (_, Some(_)) if allowPsp => Future.successful(None)
          case _ =>
            logger.warn("Prevented PSP access to PSA page")
            errorHandler.onClientError(request, NOT_FOUND, "").map(Some.apply)
        }
    }
  }

  private def checkForAssociation[A](request: OptionalDataRequest[A],
                                     extractedsrn: SchemeReferenceNumber)(implicit hc: HeaderCarrier): Future[Option[Result]] = {

    def isAllowed(req: Future[Either[HttpResponse, Boolean]]) = req.map {
      case Right(true) => true
      case _ => false
    }.recover { _ => false}

    def optFtrToFtrOpt[T](x: Option[Future[T]]): Future[Option[T]] =
      x match {
        case Some(f) => f.map(Some(_))
        case None    => Future.successful(None)
      }

    val psaAllowedOpt = if(allowPsa) request.psaId.map { psaId =>
      isAllowed(pensionsSchemeConnector.checkForAssociation(psaId.id, extractedsrn, isPsa = true)(hc, implicitly, request))
    } else None

    val pspAllowedOpt = if (allowPsp) request.pspId.map { pspId =>
      isAllowed(pensionsSchemeConnector.checkForAssociation(pspId.id, extractedsrn, isPsa = false)(hc, implicitly, request))
    } else None


    val accessAllowed = for {
      psaAllowed <- optFtrToFtrOpt(psaAllowedOpt)
      pspAllowed <- optFtrToFtrOpt(pspAllowedOpt)
    } yield {
      psaAllowed -> pspAllowed match {
        case (Some(true), _) => true
        case (_, Some(true)) => true
        case _ => false
      }
    }

    accessAllowed.flatMap {
      case true => Future.successful(None)
      case false =>
        logger.warn("Potentially prevented unauthorised access")
        errorHandler.onClientError(request, NOT_FOUND, "").map(Some.apply)
    }
  }
}

class AllowAccessActionMain(
                             srn: SchemeReferenceNumber,
                             pensionsSchemeConnector: PensionsSchemeConnector,
                             config: FrontendAppConfig,
                             errorHandler: FrontendErrorHandler,
                             allowPsa: Boolean,
                             allowPsp: Boolean
                           )(implicit executionContext: ExecutionContext) extends AllowAccessAction(srn,
  pensionsSchemeConnector, config, errorHandler, allowPsa, allowPsp) {


  override protected def filter[A](request: OptionalDataRequest[A]): Future[Option[Result]] = {
    filter(request,
      destinationForNoUserAnswersAndSRN = Some(Redirect(controllers.routes.PsaSchemeTaskListController.onPageLoad
      (srn))),
      checkForSuspended = true
    )
  }
}

class AllowAccessActionTaskList(
                                 srn: SchemeReferenceNumber,
                                 pensionsSchemeConnector: PensionsSchemeConnector,
                                 config: FrontendAppConfig,
                                 errorHandler: FrontendErrorHandler,
                                 allowPsa: Boolean = true,
                                 allowPsp: Boolean = false
                               )(implicit ec: ExecutionContext) extends AllowAccessAction(srn,
  pensionsSchemeConnector, config, errorHandler, allowPsa, allowPsp) {


  override protected def filter[A](request: OptionalDataRequest[A]): Future[Option[Result]] = {
    filter(request,
      destinationForNoUserAnswersAndSRN = None,
      checkForSuspended = false
    )
  }
}

class AllowAccessActionNoSuspendedCheck(
                                         srn: SchemeReferenceNumber,
                                         pensionsSchemeConnector: PensionsSchemeConnector,
                                         config: FrontendAppConfig,
                                         errorHandler: FrontendErrorHandler,
                                         allowPsa: Boolean = true,
                                         allowPsp: Boolean = false
                                       )(implicit ec: ExecutionContext) extends AllowAccessAction(srn,
  pensionsSchemeConnector, config, errorHandler, allowPsa, allowPsp) {


  override protected def filter[A](request: OptionalDataRequest[A]): Future[Option[Result]] = {
    filter(request,
      destinationForNoUserAnswersAndSRN = Some(Redirect(controllers.routes.PsaSchemeTaskListController.onPageLoad(mode, srn))),
      checkForSuspended = false
    )
  }
}

class AllowAccessActionProviderMainImpl @Inject()(
                                                   pensionsSchemeConnector: PensionsSchemeConnector,
                                                   config: FrontendAppConfig,
                                                   errorHandler: ErrorHandlerWithReturnLinkToManage
                                                 )(implicit ec: ExecutionContext) extends AllowAccessActionProvider {

  def apply(srn: SchemeReferenceNumber, allowPsa: Boolean = true, allowPsp: Boolean = false): AllowAccessAction = {
    new AllowAccessActionMain(srn, pensionsSchemeConnector, config, errorHandler, allowPsa, allowPsp)
  }
}

class AllowAccessActionProviderTaskListImpl @Inject()(
                                                       pensionsSchemeConnector: PensionsSchemeConnector,
                                                       config: FrontendAppConfig,
                                                       errorHandler: ErrorHandlerWithReturnLinkToManage
                                                     )(implicit ec: ExecutionContext) extends
  AllowAccessActionProvider {

  def apply(srn: SchemeReferenceNumber, allowPsa: Boolean = true, allowPsp: Boolean = false): AllowAccessAction = {
    new AllowAccessActionTaskList(srn, pensionsSchemeConnector, config, errorHandler, allowPsa, allowPsp)
  }
}

class AllowAccessActionProviderNoSuspendedCheckImpl @Inject()(
                                                               pensionsSchemeConnector: PensionsSchemeConnector,
                                                               config: FrontendAppConfig,
                                                               errorHandler: ErrorHandlerWithReturnLinkToManage
                                                             )(implicit ec: ExecutionContext) extends
  AllowAccessActionProvider {

  def apply(srn: SchemeReferenceNumber, allowPsa: Boolean = true, allowPsp: Boolean = false): AllowAccessAction = {
    new AllowAccessActionNoSuspendedCheck(srn, pensionsSchemeConnector, config, errorHandler, allowPsa, allowPsp)
  }
}


trait AllowAccessActionProvider {
  def apply(srn: SchemeReferenceNumber, allowPsa: Boolean = true, allowPsp: Boolean = false): AllowAccessAction
}
