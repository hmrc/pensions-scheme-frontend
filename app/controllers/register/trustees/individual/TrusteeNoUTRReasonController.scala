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

package controllers.register.trustees.individual

import config.FrontendAppConfig
import controllers.ReasonController
import controllers.actions._
import forms.ReasonFormProvider
import identifiers.register.trustees.individual.{TrusteeNameId, TrusteeNoUTRReasonId}
import javax.inject.Inject
import models.{Index, Mode}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import utils.annotations.TrusteesIndividual
import viewmodels.{Message, ReasonViewModel}

import scala.concurrent.ExecutionContext

class TrusteeNoUTRReasonController @Inject()(val appConfig: FrontendAppConfig,
                                             val messagesApi: MessagesApi,
                                             val userAnswersService: UserAnswersService,
                                             val navigator: Navigator,
                                             authenticate: AuthAction,
                                             getData: DataRetrievalAction,
                                             allowAccess: AllowAccessActionProvider,
                                             requireData: DataRequiredAction,
                                             formProvider: ReasonFormProvider
                                            )(implicit val ec: ExecutionContext) extends ReasonController {

  private def form(trusteeName: String): Form[String] = formProvider("messages__reason__error_utrRequired", trusteeName)

  private def viewModel(mode: Mode, index: Index, srn: Option[String], trusteeName: String): ReasonViewModel = {
    ReasonViewModel(
      postCall = routes.TrusteeNoUTRReasonController.onSubmit(mode, index, srn),
      title = Message("messages__whyNoUTR", Message("messages__theIndividual").resolve),
      heading = Message("messages__whyNoUTR", trusteeName),
      srn = srn
    )
  }

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        TrusteeNameId(index).retrieve.right.map {
          trusteeName =>
            get(TrusteeNoUTRReasonId(index), viewModel(mode, index, srn, trusteeName.fullName), form(trusteeName.fullName))
        }
    }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        TrusteeNameId(index).retrieve.right.map {
          trusteeName =>
            post(TrusteeNoUTRReasonId(index), mode, viewModel(mode, index, srn, trusteeName.fullName), form(trusteeName.fullName))
        }
    }
}
