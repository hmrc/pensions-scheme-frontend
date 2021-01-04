/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers

import config.FrontendAppConfig
import identifiers.TypedIdentifier
import models.Mode
import models.requests.DataRequest
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, Result}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.UserAnswers
import viewmodels.CommonFormWithHintViewModel
import views.html.phoneNumber

import scala.concurrent.{ExecutionContext, Future}

trait PhoneNumberController extends FrontendBaseController with Retrievals with I18nSupport {

  protected implicit def ec: ExecutionContext

  def get(id: TypedIdentifier[String], form: Form[String], viewModel: CommonFormWithHintViewModel)
         (implicit request: DataRequest[AnyContent]): Future[Result] = {
    val preparedForm = request.userAnswers.get(id).map(form.fill).getOrElse(form)

    Future.successful(Ok(view(preparedForm, viewModel, existingSchemeName)))
  }

  def post(id: TypedIdentifier[String], mode: Mode, form: Form[String], viewModel: CommonFormWithHintViewModel)
          (implicit request: DataRequest[AnyContent]): Future[Result] = {
    form.bindFromRequest().fold(
      (formWithErrors: Form[_]) =>
        Future.successful(BadRequest(view(formWithErrors, viewModel, existingSchemeName))),
      value =>
        userAnswersService.save(mode, viewModel.srn, id, value).map { cacheMap =>
          Redirect(navigator.nextPage(id, mode, UserAnswers(cacheMap), viewModel.srn))
        }
    )
  }

  protected def appConfig: FrontendAppConfig

  protected def userAnswersService: UserAnswersService

  protected def navigator: Navigator

  protected def view: phoneNumber
}
