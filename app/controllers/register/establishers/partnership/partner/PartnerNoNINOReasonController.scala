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

package controllers.register.establishers.partnership.partner

import config.FrontendAppConfig
import controllers.actions._
import controllers.{ReasonController, Retrievals}
import forms.ReasonFormProvider
import identifiers.register.establishers.partnership.partner.{PartnerNameId, PartnerNoNINOReasonId}
import javax.inject.Inject
import models.requests.DataRequest
import models.{Index, Mode}
import navigators.Navigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import utils.Enumerable
import viewmodels.{Message, ReasonViewModel}
import views.html.reason

import scala.concurrent.ExecutionContext

class PartnerNoNINOReasonController @Inject()(
                                               override val appConfig: FrontendAppConfig,
                                               override val messagesApi: MessagesApi,
                                               override val userAnswersService: UserAnswersService,
                                               override val navigator: Navigator,
                                               authenticate: AuthAction,
                                               getData: DataRetrievalAction,
                                               allowAccess: AllowAccessActionProvider,
                                               requireData: DataRequiredAction,
                                               formProvider: ReasonFormProvider,
                                               val controllerComponents: MessagesControllerComponents,
                                               val view: reason
                                             )(implicit val ec: ExecutionContext) extends
  ReasonController with Retrievals with I18nSupport with Enumerable.Implicits {

  private def form(name: String)(implicit request: DataRequest[AnyContent]) = formProvider("messages__reason__error_ninoRequired", name)

  private def viewModel(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[String], name: String)
                       (implicit request: DataRequest[AnyContent]): ReasonViewModel = {
    ReasonViewModel(
      postCall = routes.PartnerNoNINOReasonController.onSubmit(mode, establisherIndex, partnerIndex, srn),
      title = Message("messages__whyNoNINO", Message("messages__thePartner").resolve),
      heading = Message("messages__whyNoNINO", name),
      srn = srn
    )
  }

  def onPageLoad(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        PartnerNameId(establisherIndex, partnerIndex).retrieve.right.map { name =>
          get(PartnerNoNINOReasonId(establisherIndex, partnerIndex),
            viewModel(mode, establisherIndex, partnerIndex, srn, name.fullName), form(name.fullName))
        }
    }

  def onSubmit(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        PartnerNameId(establisherIndex, partnerIndex).retrieve.right.map { name =>
          post(PartnerNoNINOReasonId(establisherIndex, partnerIndex), mode,
            viewModel(mode, establisherIndex, partnerIndex, srn, name.fullName), form(name.fullName))
        }
    }

}
