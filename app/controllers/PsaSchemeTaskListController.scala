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

package controllers
import config.FrontendAppConfig
import connectors.{MinimalPsaConnector, SchemeDetailsConnector}
import controllers.actions._
import identifiers.racdac.IsRacDacId
import models.AuthEntity.PSA
import models.{Mode, PSAMinimalFlags}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PsaSchemeTaskListController @Inject()(appConfig: FrontendAppConfig,
                                            schemeDetailsConnector: SchemeDetailsConnector,
  minimalPsaConnector: MinimalPsaConnector,
                                         override val messagesApi: MessagesApi,
                                         authenticate: AuthAction,
                                         val controllerComponents: MessagesControllerComponents
                                        )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController with I18nSupport with Retrievals {


  private def redirects(psaId:Option[PsaId])(implicit executionContext: ExecutionContext, hc: HeaderCarrier):Future[Option[Result]] = {
    psaId match {
      case Some(psaId) =>
        minimalPsaConnector.getMinimalFlags(psaId.id).map {
          case PSAMinimalFlags(_, true, false) => Some(Redirect(Call("GET", appConfig.youMustContactHMRCUrl)))
          case PSAMinimalFlags(_, false, true) => Some(Redirect(Call("GET",appConfig.psaUpdateContactDetailsUrl)))
          case _ => None
        }
      case _ => Future.successful(None)
    }
  }

  def onPageLoad(mode: Mode, srn: Option[String]): Action[AnyContent] = authenticate(Some(PSA)).async { implicit request =>
    (request.psaId, srn) match {
      case (Some(psaId), Some(srnNo)) =>
          redirects(request.psaId).flatMap {
            case Some(result) => Future.successful(result)
            case _ =>
              schemeDetailsConnector.getSchemeDetails(psaId.id, "srn", srnNo).map { ua =>
                ua.get(IsRacDacId) match {
                  case Some(true) =>
                    Redirect(controllers.racdac.routes.CheckYourAnswersController.onPageLoad(mode, srn))
                  case _ =>
                    Redirect(controllers.routes.PsaNormalSchemeTaskListController.onPageLoad(mode,srn))
                }
            }
          }
      case _ =>
        Future(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
    }
  }
}
