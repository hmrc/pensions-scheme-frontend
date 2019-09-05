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

package controllers.register.establishers.partnership

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.PayeController
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import forms.PayeFormProvider
import identifiers.register.establishers.partnership.{PartnershipDetailsId, PartnershipPayeId}
import models.{Index, Mode, Paye}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import utils.annotations.EstablisherPartnership
import viewmodels.{Message, PayeViewModel}

import scala.concurrent.ExecutionContext

class PartnershipPayeController @Inject()(
                                           val appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           val userAnswersService: UserAnswersService,
                                           @EstablisherPartnership val navigator: Navigator,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           allowAccess: AllowAccessActionProvider,
                                           requireData: DataRequiredAction,
                                           formProvider: PayeFormProvider
                                         )(implicit val ec: ExecutionContext) extends PayeController with I18nSupport {

  protected val form: Form[Paye] = formProvider()

  private def viewmodel(mode: Mode, index: Index, srn: Option[String]): Retrieval[PayeViewModel] =
    Retrieval {
      implicit request =>
        PartnershipDetailsId(index).retrieve.right.map {
          details =>
            PayeViewModel(
              postCall = routes.PartnershipPayeController.onSubmit(mode, index, srn),
              title = Message("messages__partnershipPaye__title"),
              heading = Message("messages__partnershipPaye__heading"),
              hint = Some(Message("messages__common__paye_hint")),
              srn = srn
            )
        }
    }

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
    implicit request =>
      viewmodel(mode, index, srn).retrieve.right.map {
        vm =>
          get(PartnershipPayeId(index), form, vm)
      }
  }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      viewmodel(mode, index, srn).retrieve.right.map {
        vm =>
          post(PartnershipPayeId(index), mode, form, vm)
      }
  }
}
