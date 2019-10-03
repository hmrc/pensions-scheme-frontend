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

package controllers.register.trustees.individual

import config.FrontendAppConfig
import controllers.actions._
import controllers.{ReasonController, Retrievals}
import forms.ReasonFormProvider
import identifiers.register.trustees.individual.{TrusteeNameId, TrusteeNoNINOReasonId}
import javax.inject.Inject
import models.{Index, Mode}
import navigators.Navigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import utils.Enumerable
import utils.annotations.TrusteesIndividual
import viewmodels.{Message, ReasonViewModel}

import scala.concurrent.ExecutionContext

class TrusteeNoNINOReasonController @Inject()(val appConfig: FrontendAppConfig,
                                              val messagesApi: MessagesApi,
                                              val userAnswersService: UserAnswersService,
                                              val navigator: Navigator,
                                              authenticate: AuthAction,
                                              getData: DataRetrievalAction,
                                              allowAccess: AllowAccessActionProvider,
                                              requireData: DataRequiredAction,
                                              formProvider: ReasonFormProvider
                                            )(implicit val ec: ExecutionContext) extends ReasonController with Retrievals
                                            with I18nSupport with Enumerable.Implicits {

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        TrusteeNameId(index).retrieve.right.map { name =>
          get(TrusteeNoNINOReasonId(index),
            viewModel(mode, index, srn, name.fullName), form(name.fullName))
        }
    }

  private def form(name: String) = formProvider("messages__reason__error_ninoRequired", name)

  private def viewModel(mode: Mode, index: Index, srn: Option[String], name: String): ReasonViewModel = {
    ReasonViewModel(
      postCall = routes.TrusteeNoNINOReasonController.onSubmit(mode, index, srn),
      title = Message("messages__whyNoNINO", Message("messages__theIndividual").resolve),
      heading = Message("messages__whyNoNINO", name),
      srn = srn
    )
  }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        TrusteeNameId(index).retrieve.right.map { name =>
          post(TrusteeNoNINOReasonId(index), mode,
            viewModel(mode, index, srn, name.fullName), form(name.fullName))
        }
    }

}
