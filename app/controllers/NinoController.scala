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

package controllers

import config.FrontendAppConfig
import identifiers.TypedIdentifier
import models.requests.DataRequest
import models.{Mode, ReferenceValue}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.*
import play.api.mvc.{AnyContent, Result}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.UserAnswers
import viewmodels.NinoViewModel
import views.html.nino

import scala.concurrent.{ExecutionContext, Future}

trait NinoController extends FrontendBaseController with Retrievals with I18nSupport {

  protected implicit def ec: ExecutionContext

  def get(id: TypedIdentifier[ReferenceValue], form: Form[ReferenceValue], viewmodel: NinoViewModel)
         (implicit request: DataRequest[AnyContent]): Future[Result] = {

    val preparedForm = request.userAnswers.get(id).map(form.fill).getOrElse(form)

    Future.successful(Ok(view(preparedForm, viewmodel, existingSchemeName)))
  }

  def post(id: TypedIdentifier[ReferenceValue], mode: Mode, form: Form[ReferenceValue], viewmodel: NinoViewModel, hasNINOId: TypedIdentifier[Boolean])
          (implicit request: DataRequest[AnyContent]): Future[Result] = {
    form.bindFromRequest().fold(
      (formWithErrors: Form[?]) =>
        Future.successful(BadRequest(view(formWithErrors, viewmodel, existingSchemeName))),
      value =>
        val ua: UserAnswers =
          request
            .userAnswers
            .set(hasNINOId)(true)
            .asOpt
            .getOrElse(request.userAnswers)
            .set(id)(value.copy(isEditable = true))
            .asOpt
            .getOrElse(request.userAnswers)
        
        userAnswersService.upsert(mode, viewmodel.srn, ua.json).map { _ =>
          Redirect(navigator.nextPage(id, mode, ua, viewmodel.srn))
        }
    )
  }

  protected def appConfig: FrontendAppConfig

  protected def userAnswersService: UserAnswersService

  protected def navigator: Navigator

  protected def view: nino
}
