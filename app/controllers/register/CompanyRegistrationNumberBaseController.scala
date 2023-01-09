/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.register

import config.FrontendAppConfig
import controllers.Retrievals
import forms.CompanyRegistrationNumberFormProvider
import identifiers.TypedIdentifier
import models.requests.DataRequest
import models.{Index, Mode, ReferenceValue}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, Call, Result}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils._
import viewmodels.CompanyRegistrationNumberViewModel
import views.html.register.companyRegistrationNumber

import scala.concurrent.{ExecutionContext, Future}

trait CompanyRegistrationNumberBaseController extends FrontendBaseController with Retrievals with I18nSupport {

  protected implicit def ec: ExecutionContext

  protected val formProvider: CompanyRegistrationNumberFormProvider = new CompanyRegistrationNumberFormProvider()

  def postCall: (Mode, Option[String], Index) => Call

  def identifier(index: Int): TypedIdentifier[ReferenceValue]

  def get(mode: Mode, srn: Option[String], index: Index, viewModel: CompanyRegistrationNumberViewModel,
          companyName: String)
         (implicit request: DataRequest[AnyContent]): Future[Result] = {

    val preparedForm =
      request.userAnswers.get(identifier(index)).fold(form(companyName))(form(companyName).fill)

    val updatedView = view(viewModel, preparedForm, existingSchemeName, postCall(mode, srn, index), srn)

    Future.successful(Ok(updatedView))
  }

  protected def form(name: String)(implicit request: DataRequest[AnyContent]): Form[ReferenceValue] = formProvider(name)

  def post(mode: Mode, srn: Option[String], index: Index, viewModel: CompanyRegistrationNumberViewModel,
           companyName: String)
          (implicit request: DataRequest[AnyContent]): Future[Result] = {
    form(companyName).bindFromRequest().fold(
      (formWithErrors: Form[_]) =>

        Future.successful(BadRequest(
          view(viewModel, formWithErrors, existingSchemeName, postCall(mode, srn, index), srn))),

      crnNumber => {
        userAnswersService.save(mode, srn, identifier(index), crnNumber.copy(isEditable = true)).map(cacheMap =>
          Redirect(navigator.nextPage(identifier(index), mode, UserAnswers(cacheMap), srn)))
      }
    )
  }

  protected def appConfig: FrontendAppConfig

  protected def userAnswersService: UserAnswersService

  protected def navigator: Navigator

  protected def view: companyRegistrationNumber
}

