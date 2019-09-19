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

package controllers.register.establishers.individual

import config.FrontendAppConfig
import controllers.ReasonController
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import forms.ReasonFormProvider
import identifiers.register.establishers.individual.{EstablisherNameId, EstablisherNoNINOReasonId}
import javax.inject.Inject
import models.{Index, Mode}
import navigators.Navigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import viewmodels.{Message, ReasonViewModel}

import scala.concurrent.ExecutionContext

class EstablisherNoNINOReasonController @Inject()(override val appConfig: FrontendAppConfig,
                                                  override val messagesApi: MessagesApi,
                                                  override val userAnswersService: UserAnswersService,
                                                  val navigator: Navigator,
                                                  authenticate: AuthAction,
                                                  getData: DataRetrievalAction,
                                                  allowAccess: AllowAccessActionProvider,
                                                  requireData: DataRequiredAction,
                                                  formProvider: ReasonFormProvider)
                                                 (implicit val ec: ExecutionContext) extends ReasonController with I18nSupport {

  private def form(name: String) = formProvider("messages__reason__error_ninoRequired", name)

  private def viewModel(mode: Mode, index: Index, srn: Option[String], name: String): ReasonViewModel = {
    ReasonViewModel(
      postCall = routes.EstablisherNoNINOReasonController.onSubmit(mode, index, srn),
      title = Message("messages__whyPersonNoNINO"),
      heading = Message("messages__noGenericNino__heading", name),
      srn = srn
    )
  }

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        EstablisherNameId(index).retrieve.right.map { details =>
          val name = details.fullName
          get(EstablisherNoNINOReasonId(index), viewModel(mode, index, srn, name), form(name))
        }
    }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        EstablisherNameId(index).retrieve.right.map { details =>
          val name = details.fullName
          post(EstablisherNoNINOReasonId(index), mode, viewModel(mode, index, srn, name), form(name))
        }
    }
}
