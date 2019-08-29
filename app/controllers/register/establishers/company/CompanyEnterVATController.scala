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

import config.FrontendAppConfig
import controllers.EnterVATController
import controllers.actions._
import forms.EnterVATFormProvider
import identifiers.register.establishers.company.{CompanyDetailsId, CompanyEnterVATId}
import javax.inject.Inject
import models.{Index, Mode}
import navigators.Navigator
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import utils.annotations.EstablishersCompany
import viewmodels.{Message, EnterVATViewModel}

import scala.concurrent.ExecutionContext

class CompanyEnterVATController @Inject()(
                                                override val appConfig: FrontendAppConfig,
                                                override val messagesApi: MessagesApi,
                                                override val userAnswersService: UserAnswersService,
                                                @EstablishersCompany override val navigator: Navigator,
                                                authenticate: AuthAction,
                                                getData: DataRetrievalAction,
                                                allowAccess: AllowAccessActionProvider,
                                                requireData: DataRequiredAction,
                                                formProvider: EnterVATFormProvider
                                              )(implicit val ec: ExecutionContext) extends EnterVATController {

  private def form(companyName: String) = formProvider(companyName)

  private def viewModel(mode: Mode, index: Index, srn: Option[String], companyName: String): EnterVATViewModel = {
    EnterVATViewModel(
      postCall = routes.CompanyEnterVATController.onSubmit(mode, index, srn),
      title = Message("messages__enterVAT__company_title"),
      heading = Message("messages__enterVAT__heading", companyName),
      hint = Message("messages__enterVAT__hint", companyName),
      subHeading = None,
      srn = srn
    )
  }

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        CompanyDetailsId(index).retrieve.right.map { details =>
          val companyName = details.companyName
          get(CompanyEnterVATId(index), viewModel(mode, index, srn, companyName), form(companyName))
        }
    }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        CompanyDetailsId(index).retrieve.right.map { details =>
          val companyName = details.companyName
          post(CompanyEnterVATId(index), mode, viewModel(mode, index, srn, companyName), form(companyName))
        }
    }
}
