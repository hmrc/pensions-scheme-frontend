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

package controllers.register.establishers.company.director

import config.FrontendAppConfig
import controllers.EmailAddressController
import controllers.actions._
import forms.EmailFormProvider
import identifiers.register.establishers.company.director.{DirectorEmailId, DirectorNameId}
import javax.inject.Inject
import models.{Index, Mode}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import utils.annotations.EstablishersCompanyDirector
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.emailAddress

import scala.concurrent.ExecutionContext

class DirectorEmailController @Inject()(val appConfig: FrontendAppConfig,
                                        override val messagesApi: MessagesApi,
                                        authenticate: AuthAction,
                                        getData: DataRetrievalAction,
                                        override val userAnswersService: UserAnswersService,
                                        allowAccess: AllowAccessActionProvider,
                                        requireData: DataRequiredAction,
                                        @EstablishersCompanyDirector val navigator: Navigator,
                                        formProvider: EmailFormProvider,
                                        val view: emailAddress,
                                        val controllerComponents: MessagesControllerComponents
                                       )(implicit val executionContext: ExecutionContext
            ) extends EmailAddressController with I18nSupport {

  protected val form: Form[String] = formProvider()

  def onPageLoad(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        viewModel(mode, establisherIndex, directorIndex, srn).retrieve.right.map {
          vm =>
            get(DirectorEmailId(establisherIndex, directorIndex), form, vm)
        }
    }

  private def viewModel(mode: Mode,
                        establisherIndex: Index,
                        directorIndex: Index,
                        srn: Option[String]): Retrieval[CommonFormWithHintViewModel] =
    Retrieval {
      implicit request =>
        DirectorNameId(establisherIndex, directorIndex).retrieve.right.map {
          details =>
            CommonFormWithHintViewModel(
              routes.DirectorEmailController.onSubmit(mode, establisherIndex, directorIndex, srn),
              Message("messages__director_email__title"),
              Message("messages__enterEmail", details.fullName),
              Some(Message("messages__contact_details__hint", details.fullName)),
              srn = srn
            )
        }
    }

  def onSubmit(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        viewModel(mode, establisherIndex, directorIndex, srn).retrieve.right.map {
          vm =>
            post(DirectorEmailId(establisherIndex, directorIndex), mode, form, vm, None)
        }
    }

}
