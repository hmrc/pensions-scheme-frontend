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
import controllers.actions._
import controllers.register.CompanyRegistrationNumberBaseController
import forms.CompanyRegistrationNumberFormProvider
import identifiers.register.establishers.company.CompanyRegistrationNumberId
import javax.inject.Inject
import models.requests.DataRequest
import models.{CompanyRegistrationNumber, Index, Mode}
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{AnyContent, Call}
import services.UserAnswersService
import utils._
import utils.annotations.EstablishersCompany
import views.html.register.companyRegistrationNumberUpdate

import scala.concurrent.ExecutionContext.Implicits.global

class CompanyRegistrationNumberUpdateController @Inject()(
                                                     val appConfig: FrontendAppConfig,
                                                     override val messagesApi: MessagesApi,
                                                     userAnswersService: UserAnswersService,
                                                     @EstablishersCompany navigator: Navigator,
                                                     authenticate: AuthAction,
                                                     getData: DataRetrievalAction,
                                                     allowAccess: AllowAccessActionProvider,
                                                     requireData: DataRequiredAction,
                                                     formProvider: CompanyRegistrationNumberFormProvider
                                                   ) extends
  CompanyRegistrationNumberBaseController(
    appConfig, messagesApi, userAnswersService, navigator, authenticate, getData, allowAccess, requireData, formProvider) {

  override def addView(mode: Mode, index: Index, srn: Option[String])(implicit request: DataRequest[AnyContent]) = companyRegistrationNumberUpdate(appConfig, form, mode, index, existingSchemeName, postCall(mode, srn, index), srn)

  override def errorView(mode: Mode, index: Index, srn: Option[String], form: Form[_])(implicit request: DataRequest[AnyContent]) = companyRegistrationNumberUpdate(appConfig, form, mode, index, existingSchemeName, postCall(mode, srn, index), srn)

  override def updateView(mode: Mode, index: Index, srn: Option[String], value: CompanyRegistrationNumber)(implicit request: DataRequest[AnyContent]) = companyRegistrationNumberUpdate(appConfig, form.fill(value), mode, index, existingSchemeName, postCall(mode, srn, index), srn)

  override def id(index: Index) = CompanyRegistrationNumberId(index)

  override def postCall: (Mode, Option[String], Index) => Call = routes.CompanyRegistrationNumberController.onSubmit _
}
