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

package controllers.register.establishers.individual

import config.FrontendAppConfig
import controllers.HasReferenceNumberController
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import forms.HasUTRFormProvider
import identifiers.register.establishers.individual.{EstablisherHasNINOId, EstablisherNameId}
import javax.inject.Inject
import models.requests.DataRequest
import models.{Index, Mode}
import navigators.Navigator
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.hasReferenceNumber

import scala.concurrent.ExecutionContext

class EstablisherHasNINOController @Inject()(override val appConfig: FrontendAppConfig,
                                             override val messagesApi: MessagesApi,
                                             override val userAnswersService: UserAnswersService,
                                             val navigator: Navigator,
                                             authenticate: AuthAction,
                                             allowAccess: AllowAccessActionProvider,
                                             getData: DataRetrievalAction,
                                             requireData: DataRequiredAction,
                                             formProvider: HasUTRFormProvider,
                                             val view: hasReferenceNumber,
                                             val controllerComponents: MessagesControllerComponents)
                                            (implicit val executionContext: ExecutionContext)
  extends HasReferenceNumberController {

  def onPageLoad(mode: Mode, index: Index, srn: SchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async { implicit request =>
      EstablisherNameId(index).retrieve.map { details =>
        get(EstablisherHasNINOId(index), form(details.fullName), viewModel(mode, index, srn, details.fullName))
      }
    }

  private def viewModel(mode: Mode, index: Index, srn: SchemeReferenceNumber, companyName: String): CommonFormWithHintViewModel =
    CommonFormWithHintViewModel(
      postCall = controllers.register.establishers.individual.routes.EstablisherHasNINOController.onSubmit(mode,
        index, srn),
      title = Message("messages__hasNINO", Message("messages__theIndividual")),
      heading = Message("messages__hasNINO", companyName),
      hint = None,
      srn = srn
    )

  private def form(establisherName: String)(implicit request: DataRequest[AnyContent]) =
    formProvider("messages__genericHasNino__error__required", establisherName)

  def onSubmit(mode: Mode, index: Index, srn: SchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen requireData).async { implicit request =>
      EstablisherNameId(index).retrieve.map { details =>
        post(EstablisherHasNINOId(index), mode, form(details.fullName), viewModel(mode, index, srn, details.fullName))
      }
    }
}
