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
import controllers.ReasonController
import controllers.actions._
import forms.ReasonFormProvider
import identifiers.register.establishers.company.{CompanyDetailsId, CompanyNoUTRReasonId}

import javax.inject.Inject
import models.requests.DataRequest
import models.{Index, Mode, SchemeReferenceNumber}
import navigators.Navigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import utils.annotations.EstablishersCompany
import viewmodels.{Message, ReasonViewModel}
import views.html.reason

import scala.concurrent.ExecutionContext

class CompanyNoUTRReasonController @Inject()(override val appConfig: FrontendAppConfig,
                                             override val messagesApi: MessagesApi,
                                             override val userAnswersService: UserAnswersService,
                                             @EstablishersCompany override val navigator: Navigator,
                                             authenticate: AuthAction,
                                             getData: DataRetrievalAction,
                                             allowAccess: AllowAccessActionProvider,
                                             requireData: DataRequiredAction,
                                             formProvider: ReasonFormProvider,
                                             val view: reason,
                                             val controllerComponents: MessagesControllerComponents
                                            )(implicit val ec: ExecutionContext) extends ReasonController with I18nSupport {

  private def form(companyName: String)(implicit request: DataRequest[AnyContent]) = formProvider("messages__reason__error_utrRequired", companyName)

  private def viewModel(mode: Mode, index: Index, srn: SchemeReferenceNumber, companyName: String): ReasonViewModel = {
    ReasonViewModel(
      postCall = routes.CompanyNoUTRReasonController.onSubmit(mode, srn, index),
      title = Message("messages__whyNoUTR", Message("messages__theCompany")),
      heading = Message("messages__whyNoUTR", companyName),
      srn = srn
    )
  }

  def onPageLoad(mode: Mode, srn: SchemeReferenceNumber, index: Index): Action[AnyContent] =
    (authenticate() andThen getData() andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        CompanyDetailsId(index).retrieve.map { details =>
          val companyName = details.companyName
          get(CompanyNoUTRReasonId(index), viewModel(mode, index, srn, companyName), form(companyName))
        }
    }

  def onSubmit(mode: Mode, srn: SchemeReferenceNumber, index: Index): Action[AnyContent] =
    (authenticate() andThen getData() andThen requireData).async {
      implicit request =>
        CompanyDetailsId(index).retrieve.map { details =>
          val companyName = details.companyName
          post(CompanyNoUTRReasonId(index), mode, viewModel(mode, index, srn, companyName), form(companyName))
        }
    }
}
