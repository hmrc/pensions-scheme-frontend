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

package controllers.racdac

import config.FrontendAppConfig
import connectors.{PensionAdministratorConnector, UserAnswersCacheConnector}
import controllers.Retrievals
import controllers.actions._
import identifiers.racdac.{ContractOrPolicyNumberId, DeclarationId, RACDACNameId}
import models.SchemeReferenceNumber
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsError, JsSuccess}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.Racdac
import views.html.racdac.schemeSuccess

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SchemeSuccessController @Inject()(appConfig: FrontendAppConfig,
                                        override val messagesApi: MessagesApi,
                                        @Racdac cacheConnector: UserAnswersCacheConnector,
                                        authenticate: AuthAction,
                                        @Racdac getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        allowAccess: AllowAccessActionProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        pensionAdministratorConnector: PensionAdministratorConnector,
                                        val view: schemeSuccess
                                       )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController with I18nSupport with Retrievals {

  def onPageLoad(srn: SchemeReferenceNumber): Action[AnyContent] = (authenticate() andThen getData(srn=srn) andThen allowAccess(srn) andThen requireData).async {
    implicit request =>
      withRACDACName { racdacName =>
        pensionAdministratorConnector.getPSAEmail.flatMap { email =>
          request.userAnswers.remove(RACDACNameId)
            .flatMap(_.remove(ContractOrPolicyNumberId))
            .flatMap(_.remove(DeclarationId)) match {
            case JsSuccess(value, _) =>
              cacheConnector.upsert(request.externalId, value.json)
                .map(_ => Ok(view(email, racdacName)))
            case JsError(_) => Future(Redirect(controllers.routes.SessionExpiredController.onPageLoad))
          }
        }
      }
  }

  def onSubmit(srn: SchemeReferenceNumber): Action[AnyContent] = authenticate() {
    Redirect(appConfig.managePensionsSchemeOverviewUrl)
  }
}
