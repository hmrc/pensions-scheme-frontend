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

package controllers

import config.FrontendAppConfig
import connectors.{AddressLookupConnector, UserAnswersCacheConnector}
import controllers.actions._
import controllers.address.PostcodeLookupController
import forms.address.PostCodeLookupFormProvider
import identifiers.InsurerEnterPostCodeId
import javax.inject.Inject
import models.Mode
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call}
import utils.Navigator
import utils.annotations.AboutBenefitsAndInsurance
import viewmodels.address.PostcodeLookupViewModel

class InsurerEnterPostcodeController @Inject()(val appConfig: FrontendAppConfig,
                                               override val messagesApi: MessagesApi,
                                               val cacheConnector: UserAnswersCacheConnector,
                                               val addressLookupConnector: AddressLookupConnector,
                                               @AboutBenefitsAndInsurance val navigator: Navigator,
                                               authenticate: AuthAction,
                                               getData: DataRetrievalAction,
                                               requireData: DataRequiredAction,
                                               formProvider: PostCodeLookupFormProvider
                                              ) extends PostcodeLookupController {

  val postCall: Mode => Call = routes.InsurerEnterPostcodeController.onSubmit
  val manualCall: Mode => Call = routes.InsurerConfirmAddressController.onPageLoad

  val form: Form[String] = formProvider()

  def formWithError(messageKey: String): Form[String] = {
    form.withError("value", s"messages__error__postcode_$messageKey")
  }

  def viewModel(mode: Mode): PostcodeLookupViewModel =
    PostcodeLookupViewModel(
      postCall(mode),
      manualCall(mode),
      Messages("messages__insurer_enter_postcode__title"),
      "messages__insurer_enter_postcode__h1",
      None,
      None
    )

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      get(viewModel(mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      post(InsurerEnterPostCodeId, viewModel(mode), mode)
  }

}
