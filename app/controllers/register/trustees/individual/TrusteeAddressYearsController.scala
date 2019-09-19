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
import config.{FeatureSwitchManagementService, FrontendAppConfig}
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.AddressYearsController
import forms.address.AddressYearsFormProvider
import identifiers.register.trustees.individual.{TrusteeAddressYearsId, TrusteeNameId}
import models.{Index, Mode}
import navigators.Navigator
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import viewmodels.Message
import viewmodels.address.AddressYearsViewModel

import scala.concurrent.ExecutionContext

@Singleton
class TrusteeAddressYearsController @Inject()(
                                               override val appConfig: FrontendAppConfig,
                                               val userAnswersService: UserAnswersService,
                                               override val navigator: Navigator,
                                               override val messagesApi: MessagesApi,
                                               authenticate: AuthAction,
                                               getData: DataRetrievalAction,
                                               allowAccess: AllowAccessActionProvider,
                                               requireData: DataRequiredAction,
                                               fs: FeatureSwitchManagementService
                                             )(implicit val ec: ExecutionContext) extends AddressYearsController with Retrievals {

  private def form(trusteeName: String) = new AddressYearsFormProvider()(Message("messages__trusteeAddressYears__error_required", trusteeName))

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
    implicit request =>
      trusteeName(index).retrieve.right.map { name =>
        get(TrusteeAddressYearsId(index), form(name), viewModel(mode, index, name, srn))
      }
  }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      trusteeName(index).retrieve.right.map { name =>
        post(TrusteeAddressYearsId(index), mode, form(name), viewModel(mode, index, name, srn))
      }
  }

  val trusteeName: Index => Retrieval[String] = (trusteeIndex: Index) => Retrieval {
    implicit request =>
        TrusteeNameId(trusteeIndex).retrieve.right.map(_.fullName)
  }

  private def viewModel(mode: Mode, index: Index, trusteeName: String, srn: Option[String]) = AddressYearsViewModel(
    postCall = controllers.register.trustees.individual.routes.TrusteeAddressYearsController.onSubmit(mode, index, srn),
    title = Message("messages__trusteeAddressYears__title", Message("messages__common__address_years__trustee").resolve),
    heading = Message("messages__trusteeAddressYears__heading", trusteeName),
    legend = Message("messages__trusteeAddressYears__title", trusteeName),
    subHeading = Some(Message(trusteeName)),
    srn = srn
  )

}
