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

package controllers

import audit.{AuditService, TcmpAuditEvent}
import connectors._
import controllers.actions._
import controllers.routes.VariationDeclarationController
import identifiers._
import models.OptionalSchemeReferenceNumber.toSrn
import models.{OptionalSchemeReferenceNumber, TypeOfBenefits, UpdateMode}
import models.requests.DataRequest
import navigators.Navigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.http.HttpErrorFunctions.is5xx
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.Register
import utils.{Enumerable, UserAnswers}
import views.html.variationDeclaration

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class VariationDeclarationController @Inject()(
                                                override val messagesApi: MessagesApi,
                                                @Register navigator: Navigator,
                                                authenticate: AuthAction,
                                                getData: DataRetrievalAction,
                                                allowAccess: AllowAccessActionProvider,
                                                requireData: DataRequiredAction,
                                                pensionsSchemeConnector: PensionsSchemeConnector,
                                                lockConnector: PensionSchemeVarianceLockConnector,
                                                updateSchemeCacheConnector: UpdateSchemeCacheConnector,
                                                viewConnector: SchemeDetailsReadOnlyCacheConnector,
                                                val controllerComponents: MessagesControllerComponents,
                                                val view: variationDeclaration,
                                                auditService: AuditService,
                                                schemeDetailsConnector: SchemeDetailsConnector
                                              )(implicit val executionContext: ExecutionContext)
  extends FrontendBaseController
    with Retrievals
    with I18nSupport
    with Enumerable.Implicits {

  def onPageLoad(srn: OptionalSchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(UpdateMode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        srn.fold(Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad))) {
          actualSrn =>
            updateSchemeCacheConnector.fetch(actualSrn).map {
              case Some(_) =>
                Ok(view(
                  schemeName = request.userAnswers.get(SchemeNameId),
                  srn = srn,
                  href = VariationDeclarationController.onClickAgree(srn)
                ))
              case _ =>
                Redirect(controllers.routes.PsaSchemeTaskListController.onPageLoad(UpdateMode, srn))
            }
        }
    }

  def onClickAgree(srn: OptionalSchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(UpdateMode, srn) andThen requireData).async {
      implicit request =>
        val psaId: PsaId = request.psaId.getOrElse(throw MissingPsaId)
        (toSrn(srn), request.userAnswers.get(PstrId)) match {
          case (Some(srnId), Some(pstr)) =>
            val ua =
              request
                .userAnswers
                .set(VariationDeclarationId)(value = true)
                .asOpt
                .getOrElse(request.userAnswers)

            pensionsSchemeConnector.updateSchemeDetails(psaId.id, pstr, ua) flatMap { _ =>
              for {
                schemeDetails <- schemeDetailsConnector.getSchemeDetails(psaId.id, "pstr", pstr, srnId)
                _ <- auditTcmp(psaId.id, schemeDetails.get(TypeOfBenefitsId), ua)
                _ <- updateSchemeCacheConnector.removeAll(srnId)
                _ <- viewConnector.removeAll(request.externalId)
                _ <- lockConnector.releaseLock(psaId.id, srnId)
              } yield Redirect(navigator.nextPage(VariationDeclarationId, UpdateMode, UserAnswers(), srn))
            } recoverWith {
              case ex: UpstreamErrorResponse if is5xx(ex.statusCode) =>
                Future.successful(Redirect(controllers.routes.YourActionWasNotProcessedController.onPageLoad(UpdateMode, srn)))
              case _ => Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad))
            }
          case _ =>
            Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad))
        }
    }

  private def auditTcmp(
                         psaId: String,
                         originalTypeOfBenefits: Option[TypeOfBenefits],
                         ua: UserAnswers
                       )(
                         implicit request: DataRequest[AnyContent]
                       ): Future[Unit] =
    Future.successful(
      (
        originalTypeOfBenefits,
        ua.get(TypeOfBenefitsId),
        ua.get(TcmpChangedId),
        ua.get(MoneyPurchaseBenefitsId)
      ) match {
        case (Some(originalBenefits), Some(updatedBenefits), Some(tcmpChanged), Some(tcmp))
          if updatedBenefits != originalBenefits || tcmpChanged =>
          auditService.sendExtendedEvent(
            TcmpAuditEvent(
              psaId = psaId,
              tcmp = tcmp.toString,
              payload = ua.json,
              auditType = "TaxationCollectiveMoneyPurchaseAuditEvent"
            )
          )
        case _ => ()
      }
    )

  case object MissingPsaId extends Exception("Psa ID missing in request")

}
