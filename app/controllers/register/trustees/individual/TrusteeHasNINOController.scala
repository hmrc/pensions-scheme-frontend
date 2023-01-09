/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.register.trustees.individual

import config.FrontendAppConfig
import controllers.HasReferenceNumberController
import controllers.actions._
import forms.HasReferenceNumberFormProvider
import identifiers.register.trustees.individual.{TrusteeHasNINOId, TrusteeNameId}
import javax.inject.Inject
import models.requests.DataRequest
import models.{Index, Mode}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.hasReferenceNumber

import scala.concurrent.ExecutionContext

class TrusteeHasNINOController @Inject()(val appConfig: FrontendAppConfig,
                                         override val messagesApi: MessagesApi,
                                         val userAnswersService: UserAnswersService,
                                         val navigator: Navigator,
                                         authenticate: AuthAction,
                                         allowAccess: AllowAccessActionProvider,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: HasReferenceNumberFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         val view: hasReferenceNumber
                                        )(implicit val executionContext: ExecutionContext) extends
  HasReferenceNumberController {

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        TrusteeNameId(index).retrieve.map {
          trusteeName =>
            get(TrusteeHasNINOId(index), form(trusteeName.fullName), viewModel(mode, index, srn, trusteeName.fullName))
        }
    }

  private def viewModel(mode: Mode, index: Index, srn: Option[String], personName: String): CommonFormWithHintViewModel =
    CommonFormWithHintViewModel(
      postCall = controllers.register.trustees.individual.routes.TrusteeHasNINOController.onSubmit(mode, index, srn),
      title = Message("messages__hasNINO", Message("messages__theIndividual")),
      heading = Message("messages__hasNINO", personName),
      hint = None,
      srn = srn
    )

  private def form(personName: String)(implicit request: DataRequest[AnyContent]): Form[Boolean] =
    formProvider("messages__genericHasNino__error__required", personName)

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        TrusteeNameId(index).retrieve.map {
          trusteeName =>
            post(TrusteeHasNINOId(index), mode, form(trusteeName.fullName), viewModel(mode, index, srn, trusteeName
              .fullName))
        }
    }
}
