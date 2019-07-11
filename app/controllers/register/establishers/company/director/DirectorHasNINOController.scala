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
import controllers.{HasReferenceNumberController, Retrievals}
import controllers.actions._
import forms.HasReferenceNumberFormProvider
import identifiers.register.establishers.company.{CompanyDetailsId, HasCompanyVATId}
import identifiers.register.establishers.company.director.{DirectorDetailsId, DirectorHasNINOId, DirectorNameId}
import javax.inject.Inject
import models.{Index, Mode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.EstablishersCompanyDirector
import utils.{Enumerable, Navigator}
import viewmodels.{CommonFormWithHintViewModel, Message}

import scala.concurrent.{ExecutionContext, Future}

class DirectorHasNINOController @Inject()(override val appConfig: FrontendAppConfig,
                                        override val messagesApi: MessagesApi,
                                        override val userAnswersService: UserAnswersService,
                                        @EstablishersCompanyDirector override val navigator: Navigator,
                                        authenticate: AuthAction,
                                        allowAccess: AllowAccessActionProvider,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: HasReferenceNumberFormProvider
                                       )(implicit val ec: ExecutionContext) extends HasReferenceNumberController {

  private def viewModel(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String], personName: String): CommonFormWithHintViewModel =
    CommonFormWithHintViewModel(
      postCall = controllers.register.establishers.company.director.routes.DirectorHasNINOController.onSubmit(mode, establisherIndex, directorIndex, srn),
      title = Message("messages__directorHasNino__title"),
      heading = Message("messages__directorHasNino__h1", personName),
      hint = None,
      srn = srn
    )

  private def form(personName: String) = formProvider("messages__directorHasNino__error__required", personName)

  def onPageLoad(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        (CompanyDetailsId(establisherIndex) and DirectorNameId(establisherIndex, directorIndex)).retrieve.right.map {
          details =>
            get(DirectorHasNINOId(establisherIndex, directorIndex), form(details.b.fullName),
              viewModel(mode, establisherIndex, directorIndex, srn, details.b.fullName))
        }
    }

  def onSubmit(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        (CompanyDetailsId(establisherIndex) and DirectorNameId(establisherIndex, directorIndex)).retrieve.right.map {
          details =>
            post(DirectorHasNINOId(establisherIndex, directorIndex), mode, form(details.b.fullName),
              viewModel(mode, establisherIndex, directorIndex, srn, details.b.fullName))
        }
    }
}
