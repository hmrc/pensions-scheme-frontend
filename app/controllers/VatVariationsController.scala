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

package controllers

import config.FrontendAppConfig
import identifiers.TypedIdentifier
import models.{Mode, VatNew}
import models.requests.DataRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, Result}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{Navigator, UserAnswers}
import viewmodels.VatViewModel
import views.html.vatVariations

import scala.concurrent.Future

trait VatVariationsController extends FrontendController with Retrievals with I18nSupport {

  protected implicit val ec = play.api.libs.concurrent.Execution.defaultContext

  protected def appConfig: FrontendAppConfig

  protected def userAnswersService: UserAnswersService

  protected def navigator: Navigator

  def get(id: TypedIdentifier[VatNew], viewmodel: VatViewModel, form: Form[VatNew])
         (implicit request: DataRequest[AnyContent]): Future[Result] = {
    val preparedForm =
      request.userAnswers.get(id) match {
        case Some(vatNew) => form.fill(vatNew)
        case _ => form
      }

    Future.successful(Ok(vatVariations(appConfig, preparedForm, viewmodel, existingSchemeName)))
  }

  def post(id: TypedIdentifier[VatNew], mode: Mode, viewmodel: VatViewModel, form: Form[VatNew])
          (implicit request: DataRequest[AnyContent]): Future[Result] = {
    form.bindFromRequest().fold(
      (formWithErrors: Form[_]) =>
        Future.successful(BadRequest(vatVariations(appConfig, formWithErrors, viewmodel, existingSchemeName))),
      vat => {
        userAnswersService.save(mode, viewmodel.srn, id, vat.copy(isEditable = true)).map(cacheMap =>
          Redirect(navigator.nextPage(id, mode, UserAnswers(cacheMap), viewmodel.srn)))
      }
    )
  }
}
