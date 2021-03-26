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

import com.google.inject.{ImplementedBy, Inject}
import config.FrontendAppConfig
import connectors.SessionDataCacheConnector
import controllers.routes
import identifiers.AdministratorOrPractitionerId
import models.AdministratorOrPractitioner.{Administrator, Practitioner}
import models.AuthEntity
import models.AuthEntity.{PSA, PSP}
import models.requests.AuthenticatedRequest
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.domain.{PsaId, PspId}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import utils.UserAnswers

import scala.concurrent.{ExecutionContext, Future}

class AuthImpl(override val authConnector: AuthConnector,
                         config: FrontendAppConfig,
                         sessionDataCacheConnector: SessionDataCacheConnector,
                         val parser: BodyParsers.Default,
                        authEntity: Option[AuthEntity])
                              (implicit val executionContext: ExecutionContext) extends Auth with
  AuthorisedFunctions {

  override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter
      .fromHeadersAndSession(request.headers, Some(request.session))

    authorised().retrieve(Retrievals.externalId and Retrievals.allEnrolments) {
      case Some(id) ~ enrolments =>
        createAuthRequest(id, enrolments, request, block)
      case _ =>
        Future.successful(Redirect(routes.UnauthorisedController.onPageLoad()))
    } recover {
      case _: NoActiveSession => Redirect(config.loginUrl, Map("continue" -> Seq(config
        .managePensionsSchemeOverviewUrl.url)))

      case _: InsufficientEnrolments =>
        Redirect(routes.UnauthorisedController.onPageLoad())
      case _: InsufficientConfidenceLevel =>
        Redirect(routes.UnauthorisedController.onPageLoad())
      case _: UnsupportedAuthProvider =>
        Redirect(routes.UnauthorisedController.onPageLoad())
      case _: UnsupportedAffinityGroup =>
        Redirect(routes.UnauthorisedController.onPageLoad())
      case _: UnsupportedCredentialRole =>
        Redirect(routes.UnauthorisedController.onPageLoad())
      case _: IdNotFound =>
        Redirect(controllers.routes.YouNeedToRegisterController.onPageLoad())
    }
  }

  private def createAuthRequest[A](id: String, enrolments: Enrolments, request: Request[A],
                                   block: AuthenticatedRequest[A] => Future[Result]): Future[Result] = {
    (enrolments.getEnrolment("HMRC-PODS-ORG").flatMap(_.getIdentifier("PSAID")).map(p=>PsaId(p.value)),
      enrolments.getEnrolment("HMRC-PODSPP-ORG").flatMap(_.getIdentifier("PSPID")).map(p=>PspId(p.value))) match {
      case (psaId@Some(_), pspId@Some(_)) => handleWhereBothEnrolments(id, request, psaId, pspId, block)
      case (None, pspId@Some(_)) => block(AuthenticatedRequest(request, id, None, pspId))
      case (psaId@Some(_), None) => block(AuthenticatedRequest(request, id, psaId, None))
      case _ => Future.successful(Redirect(controllers.routes.YouNeedToRegisterController.onPageLoad()))
    }
  }

  private def handleWhereBothEnrolments[A](id: String, request: Request[A],
                                           psaId:Option[PsaId], pspId: Option[PspId],
                     block: AuthenticatedRequest[A] => Future[Result]): Future[Result] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))
    sessionDataCacheConnector.fetch(id).flatMap { optionJsValue =>
      optionJsValue.map(UserAnswers).flatMap(_.get(AdministratorOrPractitionerId)) match {
        case None => Future.successful(Redirect(config.administratorOrPractitionerUrl))
        case Some(aop) =>
          (aop, authEntity) match {
            case (Administrator, Some(PSP)) =>
              Future.successful(
                Redirect(Call("GET", config.cannotAccessPageAsAdministratorUrl(config.friendlyUrl(request.uri))))
              )
            case (Practitioner, Some(PSA)) =>
              Future.successful(
                Redirect(Call("GET", config.cannotAccessPageAsPractitionerUrl(config.friendlyUrl(request.uri))))
              )
            case _ => block(AuthenticatedRequest(request, id, psaId, pspId))
          }
      }
    }
  }

}

@ImplementedBy(classOf[AuthImpl])
trait Auth extends ActionBuilder[AuthenticatedRequest, AnyContent] with ActionFunction[Request,
  AuthenticatedRequest]

case class IdNotFound(msg: String = "PsaIdNotFound") extends AuthorisationException(msg)

class AuthActionImpl @Inject()(authConnector: AuthConnector,
                               config: FrontendAppConfig,
                               sessionDataCacheConnector: SessionDataCacheConnector,
                               val parser: BodyParsers.Default)
                              (implicit ec: ExecutionContext) extends AuthAction {

  override def apply(authEntity: Option[AuthEntity]): Auth = new AuthImpl(authConnector, config, sessionDataCacheConnector, parser, authEntity)
}

@ImplementedBy(classOf[AuthActionImpl])
trait AuthAction {
  def apply(authEntity: Option[AuthEntity] = None): Auth
}
