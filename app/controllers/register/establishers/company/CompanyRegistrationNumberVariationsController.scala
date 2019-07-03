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

package controllers.register.establishers.company

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.register.CompanyRegistrationNumberVariationsBaseController
import identifiers.TypedIdentifier
import identifiers.register.establishers.company.{CompanyDetailsId, CompanyRegistrationNumberVariationsId}
import models.{Index, Mode, ReferenceValue}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, Call}
import services.UserAnswersService
import utils.Navigator
import utils.annotations.EstablishersCompany
import viewmodels.{CompanyRegistrationNumberViewModel, Message}

import scala.concurrent.ExecutionContext

class CompanyRegistrationNumberVariationsController @Inject()(
                                                               override val appConfig: FrontendAppConfig,
                                                               override val messagesApi: MessagesApi,
                                                               override val userAnswersService: UserAnswersService,
                                                               @EstablishersCompany override val navigator: Navigator,
                                                               authenticate: AuthAction,
                                                               getData: DataRetrievalAction,
                                                               allowAccess: AllowAccessActionProvider,
                                                               requireData: DataRequiredAction
                                                             )(implicit val ec: ExecutionContext) extends CompanyRegistrationNumberVariationsBaseController {

  def identifier(index: Int): TypedIdentifier[ReferenceValue] = CompanyRegistrationNumberVariationsId(index)

  def postCall: (Mode, Option[String], Index) => Call = routes.CompanyRegistrationNumberVariationsController.onSubmit

  private def viewModel(mode: Mode, index: Index, srn: Option[String], companyName: String): CompanyRegistrationNumberViewModel = {
    CompanyRegistrationNumberViewModel(
      title = Message("messages__companyNumber__establisher__title"),
      heading = Message("messages__companyNumber__establisher__heading", companyName),
      hint = Message("messages__common__crn_hint", companyName)
    )
  }

  def onPageLoad(mode: Mode, srn: Option[String], index: Index): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        CompanyDetailsId(index).retrieve.right.map { details =>
          val companyName = details.companyName
          get(mode, srn, index, viewModel(mode, index, srn, companyName), companyName)
        }
    }

  def onSubmit(mode: Mode, srn: Option[String], index: Index): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        CompanyDetailsId(index).retrieve.right.map { details =>
          val companyName = details.companyName
          post(mode, srn, index, viewModel(mode, index, srn, companyName), companyName)
        }
    }
}
