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

package controllers.register.establishers.individual

import config.FrontendAppConfig
import controllers.HasReferenceNumberController
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import forms.HasUTRFormProvider
import identifiers.register.establishers.individual.{EstablisherHasUTRId, EstablisherNameId}
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

class EstablisherHasUTRController @Inject()(override val appConfig: FrontendAppConfig,
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

  def onPageLoad(mode: Mode, index: Index, srn: Option[String] = None): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async { implicit request =>
      EstablisherNameId(index).retrieve.right.map { details =>
        get(EstablisherHasUTRId(index), form(details.fullName), viewModel(mode, index, srn, details.fullName))
      }
    }

  def onSubmit(mode: Mode, index: Index, srn: Option[String] = None): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async { implicit request =>
      EstablisherNameId(index).retrieve.right.map { details =>
        post(EstablisherHasUTRId(index), mode, form(details.fullName), viewModel(mode, index, srn, details.fullName))
      }
    }

  private def viewModel(mode: Mode, index: Index, srn: Option[String], companyName: String)
                       (implicit request: DataRequest[AnyContent]): CommonFormWithHintViewModel =
    CommonFormWithHintViewModel(
      postCall = controllers.register.establishers.individual.routes.EstablisherHasUTRController.onSubmit(mode,
        index, srn),
      title = Message("messages__hasUTR", Message("messages__theIndividual")),
      heading = Message("messages__hasUTR", companyName),
      hint = Some(Message("messages__hasUtr__p1")),
      srn = srn
    )

  private def form(establisherName: String)(implicit request: DataRequest[AnyContent]) =
    formProvider("messages__hasUTR__error__required", establisherName)
}
