/*
 * Copyright 2024 HM Revenue & Customs
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
import controllers.register.CompanyRegistrationNumberBaseController
import identifiers.TypedIdentifier
import identifiers.register.establishers.company.{CompanyDetailsId, CompanyEnterCRNId}
import models.{Index, Mode, ReferenceValue}
import navigators.Navigator
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.UserAnswersService
import utils.annotations.EstablishersCompany
import viewmodels.{CompanyRegistrationNumberViewModel, Message}
import views.html.register.companyRegistrationNumber

import scala.concurrent.ExecutionContext
import models.SchemeReferenceNumber

class CompanyEnterCRNController @Inject()(
                                           override val appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           override val userAnswersService: UserAnswersService,
                                           @EstablishersCompany override val navigator: Navigator,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           allowAccess: AllowAccessActionProvider,
                                           requireData: DataRequiredAction,
                                           val view: companyRegistrationNumber,
                                           val controllerComponents: MessagesControllerComponents
                                         )(implicit val ec: ExecutionContext) extends
  CompanyRegistrationNumberBaseController {

  def identifier(index: Int): TypedIdentifier[ReferenceValue] = CompanyEnterCRNId(index)

  def postCall: (Mode, Option[SchemeReferenceNumber], Index) => Call = routes.CompanyEnterCRNController.onSubmit

  def onPageLoad(mode: Mode, index: Index, srn: Option[SchemeReferenceNumber] = None): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        CompanyDetailsId(index).retrieve.map { details =>
          val companyName = details.companyName
          get(mode, srn, index, viewModel(companyName), companyName)
        }
    }

  private def viewModel(companyName: String): CompanyRegistrationNumberViewModel = {
    CompanyRegistrationNumberViewModel(
      title = Message("messages__enterCRN", Message("messages__theCompany")),
      heading = Message("messages__enterCRN", companyName),
      hint = Message("messages__common__crn_hint", companyName)
    )
  }

  def onSubmit(mode: Mode, srn: Option[SchemeReferenceNumber], index: Index): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        CompanyDetailsId(index).retrieve.map { details =>
          val companyName = details.companyName
          post(mode, srn, index, viewModel(companyName), companyName)
        }
    }
}
