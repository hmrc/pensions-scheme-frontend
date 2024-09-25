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

import config.FrontendAppConfig
import controllers.EnterVATController
import controllers.actions._
import forms.EnterVATFormProvider
import identifiers.register.establishers.company.{CompanyDetailsId, CompanyEnterVATId}
import javax.inject.Inject
import models.requests.DataRequest
import models.{Index, Mode, ReferenceValue}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import utils.annotations.EstablishersCompany
import viewmodels.{EnterVATViewModel, Message}
import views.html.enterVATView

import scala.concurrent.ExecutionContext
import models.SchemeReferenceNumber

class CompanyEnterVATController @Inject()(override val appConfig: FrontendAppConfig,
                                          override val messagesApi: MessagesApi,
                                          override val userAnswersService: UserAnswersService,
                                          @EstablishersCompany override val navigator: Navigator,
                                          authenticate: AuthAction,
                                          getData: DataRetrievalAction,
                                          allowAccess: AllowAccessActionProvider,
                                          requireData: DataRequiredAction,
                                          formProvider: EnterVATFormProvider,
                                          val view: enterVATView,
                                          val controllerComponents: MessagesControllerComponents
                                         )(implicit val ec: ExecutionContext) extends EnterVATController {

  def onPageLoad(mode: Mode, index: Index, srn: Option[SchemeReferenceNumber]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        CompanyDetailsId(index).retrieve.map { details =>
          val companyName = details.companyName
          get(CompanyEnterVATId(index), viewModel(mode, index, srn, companyName), form(companyName))
        }
    }

  def onSubmit(mode: Mode, index: Index, srn: Option[SchemeReferenceNumber]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        CompanyDetailsId(index).retrieve.map { details =>
          val companyName = details.companyName
          post(CompanyEnterVATId(index), mode, viewModel(mode, index, srn, companyName), form(companyName))
        }
    }

  private def form(companyName: String)(implicit request: DataRequest[AnyContent]): Form[ReferenceValue] =
    formProvider(companyName)

  private def viewModel(mode: Mode, index: Index, srn: Option[SchemeReferenceNumber], companyName: String): EnterVATViewModel = {
    EnterVATViewModel(
      postCall = routes.CompanyEnterVATController.onSubmit(mode, index, srn),
      title = Message("messages__enterVAT", Message("messages__theCompany")),
      heading = Message("messages__enterVAT", companyName),
      hint = Message("messages__enterVAT__hint", companyName),
      subHeading = None,
      srn = srn
    )
  }
}
