/*
 * Copyright 2020 HM Revenue & Customs
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
import controllers.ReasonController
import controllers.actions._
import forms.register.NoCompanyNumberFormProvider
import identifiers.register.trustees.company.{CompanyDetailsId, CompanyNoCRNReasonId}
import javax.inject.Inject
import models.requests.DataRequest
import models.{Index, Mode}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import viewmodels.{Message, ReasonViewModel}
import views.html.reason

import scala.concurrent.ExecutionContext

class CompanyNoCRNReasonController @Inject()(override val appConfig: FrontendAppConfig,
                                             override val messagesApi: MessagesApi,
                                             override val userAnswersService: UserAnswersService,
                                             val navigator: Navigator,
                                             authenticate: AuthAction,
                                             getData: DataRetrievalAction,
                                             allowAccess: AllowAccessActionProvider,
                                             requireData: DataRequiredAction,
                                             formProvider: NoCompanyNumberFormProvider,
                                             val controllerComponents: MessagesControllerComponents,
                                             val view: reason
                                            )(implicit val ec: ExecutionContext) extends ReasonController with
  I18nSupport {

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        CompanyDetailsId(index).retrieve.right.map { details =>
          val companyName = details.companyName
          get(CompanyNoCRNReasonId(index), viewModel(mode, index, srn, companyName), form(companyName))
        }
    }

  protected def form(name: String)(implicit request: DataRequest[AnyContent]): Form[String] = formProvider(name)

  private def viewModel(mode: Mode, index: Index, srn: Option[String], companyName: String): ReasonViewModel = {
    ReasonViewModel(
      postCall = controllers.register.trustees.company.routes.CompanyNoCRNReasonController.onSubmit(mode, index, srn),
      title = Message("messages__whyNoCRN", Message("messages__theCompany")),
      heading = Message("messages__whyNoCRN", companyName),
      srn = srn
    )
  }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        CompanyDetailsId(index).retrieve.right.map { details =>
          val companyName = details.companyName
          post(CompanyNoCRNReasonId(index), mode, viewModel(mode, index, srn, companyName), form(companyName))
        }
    }

}
