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
import controllers.HasReferenceNumberController
import controllers.actions._
import forms.HasReferenceNumberFormProvider
import identifiers.register.establishers.company.director.{DirectorHasUTRId, DirectorNameId}

import javax.inject.Inject
import models.requests.DataRequest
import models.{Index, Mode, OptionalSchemeReferenceNumber, SchemeReferenceNumber}
import navigators.Navigator
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import utils.annotations.EstablishersCompanyDirector
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.hasReferenceNumber

import scala.concurrent.ExecutionContext

class DirectorHasUTRController @Inject()(override val appConfig: FrontendAppConfig,
                                         override val messagesApi: MessagesApi,
                                         override val userAnswersService: UserAnswersService,
                                         @EstablishersCompanyDirector override val navigator: Navigator,
                                         authenticate: AuthAction,
                                         allowAccess: AllowAccessActionProvider,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: HasReferenceNumberFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         val view: hasReferenceNumber
                                        )(implicit val executionContext: ExecutionContext) extends
  HasReferenceNumberController {

  def onPageLoad(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: OptionalSchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        DirectorNameId(establisherIndex, directorIndex).retrieve.map {
          details =>
            get(DirectorHasUTRId(establisherIndex, directorIndex), form(details.fullName),
              viewModel(mode, establisherIndex, directorIndex, srn, details.fullName))
        }
    }

  private def viewModel(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: OptionalSchemeReferenceNumber,
                        personName: String): CommonFormWithHintViewModel =
    CommonFormWithHintViewModel(
      postCall = controllers.register.establishers.company.director.routes.DirectorHasUTRController.onSubmit(mode,
        establisherIndex, directorIndex, srn),
      title = Message("messages__hasUTR", Message("messages__theDirector")),
      heading = Message("messages__hasUTR", personName),
      hint = Some(Message("messages__hasUtr__p1")),
      srn = srn
    )

  private def form(personName: String)(implicit request: DataRequest[AnyContent]) =
    formProvider("messages__hasUtr__error__required", personName)

  def onSubmit(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: OptionalSchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        DirectorNameId(establisherIndex, directorIndex).retrieve.map {
          details =>
            post(DirectorHasUTRId(establisherIndex, directorIndex), mode, form(details.fullName),
              viewModel(mode, establisherIndex, directorIndex, srn, details.fullName))
        }
    }
}
