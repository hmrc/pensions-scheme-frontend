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

package controllers.register.establishers.company.director

import config.FrontendAppConfig
import controllers.actions._
import controllers.{ReasonController, Retrievals}
import forms.ReasonFormProvider
import identifiers.register.establishers.company.director.{DirectorNameId, DirectorNoNINOReasonId}

import javax.inject.Inject
import models.requests.DataRequest
import models.{Index, Mode, SchemeReferenceNumber}
import navigators.Navigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import utils.Enumerable
import utils.annotations.EstablishersCompanyDirector
import viewmodels.{Message, ReasonViewModel}
import views.html.reason

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
                                                formProvider: ReasonFormProvider,
                                                val controllerComponents: MessagesControllerComponents,
                                                val view: reason
                                              )(implicit val ec: ExecutionContext) extends ReasonController with
  Retrievals with I18nSupport with Enumerable.Implicits {

  def onPageLoad(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: SchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData() andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        DirectorNameId(establisherIndex, directorIndex).retrieve.map { name =>
          get(DirectorNoNINOReasonId(establisherIndex, directorIndex),
            viewModel(mode, establisherIndex, directorIndex, srn, name.fullName), form(name.fullName))
        }
    }

  def onSubmit(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: SchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData() andThen requireData).async {
      implicit request =>
        DirectorNameId(establisherIndex, directorIndex).retrieve.map { name =>
          post(DirectorNoNINOReasonId(establisherIndex, directorIndex), mode,
            viewModel(mode, establisherIndex, directorIndex, srn, name.fullName), form(name.fullName))
        }
    }


  private def form(name: String)(implicit request: DataRequest[AnyContent]) =
    formProvider("messages__reason__error_ninoRequired", name)

  private def viewModel(mode: Mode, establisherIndex: Index,
                        directorIndex: Index, srn: SchemeReferenceNumber, name: String): ReasonViewModel =
    ReasonViewModel(
      postCall = routes.DirectorNoNINOReasonController.onSubmit(mode, establisherIndex, directorIndex, srn),
      title = Message("messages__whyNoNINO", Message("messages__theDirector")),
      heading = Message("messages__whyNoNINO", name),
      srn = srn
    )

}
