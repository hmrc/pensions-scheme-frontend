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
import controllers.HasReferenceNumberController
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import forms.HasPAYEFormProvider
import identifiers.register.establishers.company.{CompanyDetailsId, HasCompanyPAYEId}
import models.requests.DataRequest
import models.{Index, Mode}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import utils.annotations.EstablishersCompany
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.hasReferenceNumber

import scala.concurrent.ExecutionContext

class HasCompanyPAYEController @Inject()(override val appConfig: FrontendAppConfig,
                                         override val messagesApi: MessagesApi,
                                         override val userAnswersService: UserAnswersService,
                                         @EstablishersCompany override val navigator: Navigator,
                                         authenticate: AuthAction,
                                         allowAccess: AllowAccessActionProvider,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: HasPAYEFormProvider,
                                         val view: hasReferenceNumber,
                                         val controllerComponents: MessagesControllerComponents,
                                         implicit val executionContext: ExecutionContext
                                        ) extends HasReferenceNumberController {

  def onPageLoad(mode: Mode, srn: SchemeReferenceNumber = None, index: Index): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        CompanyDetailsId(index).retrieve.map {
          details =>
            get(HasCompanyPAYEId(index), form(details.companyName), viewModel(mode, index, srn, details.companyName))
        }
    }

  def onSubmit(mode: Mode, srn: SchemeReferenceNumber = None, index: Index): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        CompanyDetailsId(index).retrieve.map {
          details =>
            post(HasCompanyPAYEId(index), mode, form(details.companyName), viewModel(mode, index, srn, details
              .companyName))
        }
    }

  private def viewModel(mode: Mode, index: Index, srn: SchemeReferenceNumber, companyName: String): CommonFormWithHintViewModel =
    CommonFormWithHintViewModel(
      postCall = controllers.register.establishers.company.routes.HasCompanyPAYEController.onSubmit(mode, srn, index),
      title = Message("messages__hasPAYE", Message("messages__theCompany")),
      heading = Message("messages__hasPAYE", companyName),
      hint = Some(Message("messages__hasPaye__p1")),
      srn = srn,
      formFieldName = Some("hasPaye")
    )

  private def form(companyName: String)(implicit request: DataRequest[AnyContent]): Form[Boolean] =
    formProvider("messages__companyPayeRef__error__required", companyName)
}
