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

package controllers.register.establishers.company.director

import config.FrontendAppConfig
import controllers.actions._
import controllers.{ReasonController, Retrievals}
import forms.ReasonFormProvider
import identifiers.register.establishers.company.director.{DirectorNameId, DirectorNoNINOReasonId}
import javax.inject.Inject
import models.{Index, Mode}
import navigators.Navigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import utils.annotations.EstablishersCompanyDirector
import utils.Enumerable
import viewmodels.{Message, ReasonViewModel}

import scala.concurrent.ExecutionContext

class DirectorNoNINOReasonController @Inject()(
                                           override val appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           override val userAnswersService: UserAnswersService,
                                           @EstablishersCompanyDirector override val navigator: Navigator,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           allowAccess: AllowAccessActionProvider,
                                           requireData: DataRequiredAction,
                                           formProvider: ReasonFormProvider
                                         )(implicit val ec: ExecutionContext) extends ReasonController with Retrievals with I18nSupport with Enumerable.Implicits {

  private def form(name: String) = formProvider("messages__reason__error_ninoRequired", name)

  private def viewModel(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String], name: String): ReasonViewModel = {
    ReasonViewModel(
      postCall = routes.DirectorNoNINOReasonController.onSubmit(mode, establisherIndex, directorIndex, srn),
      title = Message("messages__noNinoReason__director_title"),
      heading = Message("messages__noGenericNino__heading", name),
      srn = srn
    )
  }

  def onPageLoad(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        DirectorNameId(establisherIndex, directorIndex).retrieve.right.map { name =>
          get(DirectorNoNINOReasonId(establisherIndex, directorIndex),
            viewModel(mode, establisherIndex, directorIndex, srn, name.fullName), form(name.fullName))
        }
    }

  def onSubmit(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        DirectorNameId(establisherIndex, directorIndex).retrieve.right.map { name =>
          post(DirectorNoNINOReasonId(establisherIndex, directorIndex), mode,
            viewModel(mode, establisherIndex, directorIndex, srn, name.fullName), form(name.fullName))
        }
    }

}
