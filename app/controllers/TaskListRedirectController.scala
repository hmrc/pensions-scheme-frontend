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
import config.FrontendAppConfig
import connectors.{DelimitedAdminException, MinimalPsaConnector, SchemeDetailsConnector}
import controllers.actions._
import identifiers.racdac.IsRacDacId
import models.AuthEntity.PSA
import models._
import models.requests.OptionalDataRequest
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TaskListRedirectController @Inject()(appConfig: FrontendAppConfig,
                                           schemeDetailsConnector: SchemeDetailsConnector,
                                           minimalPsaConnector: MinimalPsaConnector,
                                           allowAccess: AllowAccessActionProvider,
                                           override val messagesApi: MessagesApi,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           val controllerComponents: MessagesControllerComponents
                                        )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController with I18nSupport with Retrievals {

  private def redirects(implicit request: OptionalDataRequest[AnyContent], hc: HeaderCarrier): Future[Option[Result]] = {
    request.psaId match {
      case None => Future.successful(None)
      case Some(psaId) =>
        minimalPsaConnector.getMinimalFlags(psaId.id).map {
          case PSAMinimalFlags(_, true, false) => Some(Redirect(Call("GET", appConfig.youMustContactHMRCUrl)))
          case PSAMinimalFlags(_, false, true) => Some(Redirect(Call("GET", appConfig.psaUpdateContactDetailsUrl)))
          case _ => None
        } recoverWith {
          case _: DelimitedAdminException =>
            Future.successful(Some(Redirect(Call("GET", appConfig.delimitedPsaUrl))))
        }
    }
  }

  def onPageLoad(mode: Mode, srn: SchemeReferenceNumber): Action[AnyContent] = (authenticate(Some(PSA)) andThen getData(srn=srn) andThen allowAccess(srn)).async {
    implicit request =>

      redirects.flatMap {
        case Some(result) => Future.successful(result)
        case _ =>

          (mode, srn, request.psaId) match {
            case (UpdateMode, (srnNo), Some(psaId)) =>
              schemeDetailsConnector.getSchemeDetails(psaId.id, schemeIdType = "srn", srnNo).map { ua =>
                ua.get(IsRacDacId) match {
                  case Some(true) =>
                    Redirect(controllers.racdac.routes.CheckYourAnswersController.onPageLoad(mode, srn))
                  case _ =>
                    Redirect(controllers.routes.PsaSchemeTaskListController.onPageLoad(srn))
                }
              }

            case (NormalMode, _, _) => Future.successful(Redirect(controllers.routes.PsaSchemeTaskListController.onPageLoad(mode, srn)))

            case _ => Future.successful(Redirect(appConfig.managePensionsSchemeOverviewUrl))
          }


      }
  }
}

