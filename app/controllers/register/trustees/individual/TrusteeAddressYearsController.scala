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

import com.google.inject.{Inject, Singleton}
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.AddressYearsController
import forms.address.AddressYearsFormProvider
import identifiers.register.trustees.individual.{TrusteeAddressYearsId, TrusteeDetailsId}
import models.{Index, Mode}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.TrusteesIndividual
import viewmodels.Message
import viewmodels.address.AddressYearsViewModel

@Singleton
class TrusteeAddressYearsController @Inject()(
                                               override val appConfig: FrontendAppConfig,
                                               override val cacheConnector: UserAnswersCacheConnector,
                                               @TrusteesIndividual override val navigator: Navigator,
                                               override val messagesApi: MessagesApi,
                                               authenticate: AuthAction,
                                               getData: DataRetrievalAction,
                                               requireData: DataRequiredAction
                                             ) extends AddressYearsController with Retrievals {

  private val form = new AddressYearsFormProvider()(Message("messages__trusteeAddressYears__error_required"))

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      TrusteeDetailsId(index).retrieve.right.map { trusteeDetails =>
        get(TrusteeAddressYearsId(index), form, viewModel(mode, index, trusteeDetails.fullName))
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      TrusteeDetailsId(index).retrieve.right.map { trusteeDetails =>
        post(TrusteeAddressYearsId(index), mode, form, viewModel(mode, index, trusteeDetails.fullName))
      }
  }

  private def viewModel(mode: Mode, index: Index, trusteeName: String) = AddressYearsViewModel(
    postCall = controllers.register.trustees.individual.routes.TrusteeAddressYearsController.onSubmit(mode, index),
    title = Message("messages__trusteeAddressYears__title"),
    heading = Message("messages__trusteeAddressYears__heading"),
    legend = Message("messages__trusteeAddressYears__title"),
    subHeading = Some(Message(trusteeName))
  )

}
