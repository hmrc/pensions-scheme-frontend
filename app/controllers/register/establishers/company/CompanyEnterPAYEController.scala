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
import controllers.PayeController
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import forms.PayeFormProvider
import identifiers.register.establishers.company.{CompanyDetailsId, CompanyEnterPAYEId}
import models.requests.DataRequest
import models.{Index, Mode, ReferenceValue}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import utils.annotations.EstablishersCompany
import viewmodels.{Message, PayeViewModel}
import views.html.paye

import scala.concurrent.ExecutionContext
import models.SchemeReferenceNumber

class CompanyEnterPAYEController @Inject()(
                                            val appConfig: FrontendAppConfig,
                                            override val messagesApi: MessagesApi,
                                            override val userAnswersService: UserAnswersService,
                                            @EstablishersCompany val navigator: Navigator,
                                            authenticate: AuthAction,
                                            getData: DataRetrievalAction,
                                            allowAccess: AllowAccessActionProvider,
                                            requireData: DataRequiredAction,
                                            formProvider: PayeFormProvider,
                                            val view: paye,
                                            val controllerComponents: MessagesControllerComponents
                                          )(implicit val ec: ExecutionContext) extends PayeController with I18nSupport {

  def onPageLoad(mode: Mode, index: Index, srn: Option[SchemeReferenceNumber]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        CompanyDetailsId(index).retrieve.map {
          details =>
            get(CompanyEnterPAYEId(index), form(details.companyName), viewmodel(mode, index, srn, details.companyName))
        }
    }

  def onSubmit(mode: Mode, index: Index, srn: Option[SchemeReferenceNumber]): Action[AnyContent] = (authenticate() andThen getData
  (mode, srn) andThen requireData).async {
    implicit request =>
      CompanyDetailsId(index).retrieve.map {
        details =>
          post(CompanyEnterPAYEId(index), mode, form(details.companyName), viewmodel(mode, index, srn, details
            .companyName))
      }
  }

  protected def form(companyName: String)(implicit request: DataRequest[AnyContent]): Form[ReferenceValue] =
    formProvider(companyName)

  private def viewmodel(mode: Mode, index: Index, srn: Option[SchemeReferenceNumber], companyName: String): PayeViewModel =
    PayeViewModel(
      postCall = routes.CompanyEnterPAYEController.onSubmit(mode, index, srn),
      title = Message("messages__enterPAYE", Message("messages__theCompany")),
      heading = Message("messages__enterPAYE", companyName),
      hint = Some(Message("messages__enterPAYE__hint")),
      srn = srn,
      entityName = Some(companyName)
    )
}
