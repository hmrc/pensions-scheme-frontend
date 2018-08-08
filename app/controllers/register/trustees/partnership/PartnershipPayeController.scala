/*
 * Copyright 2018 HM Revenue & Customs
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

package controllers.register.trustees.partnership

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.PayeController
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import forms.PayeFormProvider
import identifiers.register.trustees.partnership.{PartnershipDetailsId, PartnershipPayeId}
import models.{Index, Mode, Paye}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.TrusteesPartnership
import viewmodels.{Message, PayeViewModel}

class PartnershipPayeController @Inject()(
                                           val appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           override val cacheConnector: DataCacheConnector,
                                           @TrusteesPartnership val navigator: Navigator,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           formProvider: PayeFormProvider
                                         ) extends PayeController with I18nSupport {

  protected val form: Form[Paye] = formProvider()

  private def viewmodel(mode: Mode, index: Index): Retrieval[PayeViewModel] =
    Retrieval {
      implicit request =>
        PartnershipDetailsId(index).retrieve.right.map {
          details =>
            PayeViewModel(
              postCall = routes.PartnershipPayeController.onSubmit(mode, index),
              title = Message("messages__partnershipPaye__title"),
              heading = Message("messages__partnershipPaye__heading"),
              hint = Some(Message("messages__common__paye_hint")),
              subHeading = Some(details.name)
            )
        }
    }

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(mode, index).retrieve.right.map {
        vm =>
          get(PartnershipPayeId(index), form, vm)
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(mode, index).retrieve.right.map {
        vm =>
          post(PartnershipPayeId(index), mode, form, vm)
      }
  }
}
