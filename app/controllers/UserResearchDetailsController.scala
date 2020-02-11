/*
 * Copyright 2020 HM Revenue & Customs
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

import audit.{AuditService, UserResearchEvent}
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.actions._
import forms.UserResearchDetailsFormProvider
import identifiers.UserResearchDetailsId
import javax.inject.Inject
import models.NormalMode
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.{FrontendBaseController, FrontendController}
import utils.annotations.Register
import utils.UserAnswers
import views.html.userResearchDetails

import scala.concurrent.ExecutionContext

class UserResearchDetailsController @Inject()(
                                               appConfig: FrontendAppConfig,
                                               override val messagesApi: MessagesApi,
                                               dataCacheConnector: UserAnswersCacheConnector,
                                               @Register navigator: Navigator,
                                               authenticate: AuthAction,
                                               getData: DataRetrievalAction,
                                               requireData: DataRequiredAction,
                                               formProvider: UserResearchDetailsFormProvider,
                                               auditService: AuditService,
                                               val controllerComponents: MessagesControllerComponents,
                                               val view: userResearchDetails
                                              )(implicit val executionContext: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()

  def onPageLoad: Action[AnyContent] = authenticate {
    implicit request =>
      Ok(view(form))
  }

  def onSubmit: Action[AnyContent] = authenticate {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
        BadRequest(view(formWithErrors)),
        value => {
          auditService.sendEvent(UserResearchEvent.userResearchAgreementSchemeEvent(request.externalId, value.name, value.email))
          Redirect(navigator.nextPage(UserResearchDetailsId, NormalMode, UserAnswers()))
        }
      )
  }
}
