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
import controllers.PayeVariationsController
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import forms.PayeVariationsFormProvider
import identifiers.register.establishers.partnership.{PartnershipDetailsId, PartnershipPayeVariationsId}
import models.{Index, Mode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import utils.Navigator
import utils.annotations.EstablisherPartnership
import viewmodels.{Message, PayeViewModel}

class PartnershipPayeVariationsController @Inject()(
                                                     val appConfig: FrontendAppConfig,
                                                     override val messagesApi: MessagesApi,
                                                     override val userAnswersService: UserAnswersService,
                                                     @EstablisherPartnership val navigator: Navigator,
                                                     authenticate: AuthAction,
                                                     getData: DataRetrievalAction,
                                                     allowAccess: AllowAccessActionProvider,
                                                     requireData: DataRequiredAction,
                                                     formProvider: PayeVariationsFormProvider
                                     ) extends PayeVariationsController with I18nSupport {

  private def viewmodel(mode: Mode, index: Index, srn: Option[String]): Retrieval[PayeViewModel] =
    Retrieval {
      implicit request =>
        PartnershipDetailsId(index).retrieve.right.map {
          details =>
            PayeViewModel(
              postCall = routes.PartnershipPayeVariationsController.onSubmit(mode, index, srn),
              title = Message("messages__payeVariations__partnership_title"),
              heading = Message("messages__payeVariations__heading", details.name),
              hint = Some(Message("messages__payeVariations__hint")),
              subHeading = None,
              srn = srn
            )
        }
    }

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
    implicit request =>
      viewmodel(mode, index, srn).retrieve.right.map {
        vm =>
          get(PartnershipPayeVariationsId(index), vm)
      }
  }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      viewmodel(mode, index, srn).retrieve.right.map {
        vm =>
          post(PartnershipPayeVariationsId(index), mode, vm)
      }
  }
}
