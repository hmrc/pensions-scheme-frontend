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

package controllers.register.trustees.company

import config.FrontendAppConfig
import controllers.HasReferenceNumberController
import controllers.actions._
import forms.HasPAYEFormProvider
import identifiers.register.trustees.company.{CompanyDetailsId, HasCompanyPAYEId}
import javax.inject.Inject
import models.{Index, Mode}
import navigators.Navigator
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, Call}
import services.UserAnswersService
import utils.annotations.TrusteesCompany
import viewmodels.{CommonFormWithHintViewModel, Message}

import scala.concurrent.ExecutionContext

class HasCompanyPAYEController @Inject()(override val appConfig: FrontendAppConfig,
                                         override val messagesApi: MessagesApi,
                                         override val userAnswersService: UserAnswersService,
                                         @TrusteesCompany override val navigator: Navigator,
                                         authenticate: AuthAction,
                                         allowAccess: AllowAccessActionProvider,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: HasPAYEFormProvider,
                                         implicit val ec: ExecutionContext
                                        ) extends HasReferenceNumberController {

 private def viewModel(mode: Mode, index: Index, srn: Option[String], companyName: String): CommonFormWithHintViewModel =
    CommonFormWithHintViewModel(
      postCall = controllers.register.trustees.company.routes.HasCompanyPAYEController.onSubmit(mode, index, srn),
      title = Message("messages__companyPayeRef__trustee_title"),
      heading = Message("messages__companyPayeRef__h1", companyName),
      hint = Some(Message("messages__companyPayeRef__p1")),
      srn = srn,
      formFieldName = Some("hasPaye")
    )

  private def form(companyName: String) = formProvider("messages__companyPayeRef__error__required", companyName)

  def onPageLoad(mode: Mode, index: Index, srn: Option[String] = None): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        CompanyDetailsId(index).retrieve.right.map {
          details =>
            get(HasCompanyPAYEId(index), form(details.companyName), viewModel(mode, index, srn, details.companyName))
        }
    }

  def onSubmit(mode: Mode, index: Index, srn: Option[String] = None): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        CompanyDetailsId(index).retrieve.right.map {
          details =>
            post(HasCompanyPAYEId(index), mode, form(details.companyName), viewModel(mode, index, srn, details.companyName))
        }
    }
}
