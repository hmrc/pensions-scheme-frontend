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

package controllers.address

import config.FrontendAppConfig
import controllers.Retrievals
import forms.address.ConfirmAddressFormProvider
import identifiers.TypedIdentifier
import models.Mode
import models.address.Address
import models.requests.DataRequest
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, Result}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{CountryOptions, Navigator, UserAnswers}
import viewmodels.Message
import viewmodels.address.ConfirmAddressViewModel
import views.html.address.confirmPreviousAddress

import scala.concurrent.Future

trait ConfirmPreviousAddressController extends FrontendController with Retrievals with I18nSupport {
  implicit val ec = play.api.libs.concurrent.Execution.defaultContext

  protected def appConfig: FrontendAppConfig

  protected def userAnswersService: UserAnswersService

  protected def navigator: Navigator

  protected def formProvider: ConfirmAddressFormProvider = new ConfirmAddressFormProvider()

  protected def countryOptions: CountryOptions

  protected def form(name: String) = formProvider(Message("confirmPreviousAddress.error", name))

  protected def get(
                     id: TypedIdentifier[Boolean],
                     viewModel: ConfirmAddressViewModel
                   )(implicit request: DataRequest[AnyContent]): Future[Result] = {

    val preparedForm = request.userAnswers.get(id) match {
      case None => form(viewModel.name)
      case Some(value) => form(viewModel.name).fill(value)
    }
    Future.successful(Ok(confirmPreviousAddress(appConfig, preparedForm, viewModel, countryOptions, existingSchemeName)))
  }

  protected def post(
                      id: TypedIdentifier[Boolean],
                      contactId: TypedIdentifier[Address],
                      viewModel: ConfirmAddressViewModel,
                      mode: Mode
                    )(implicit request: DataRequest[AnyContent]): Future[Result] = {
    form(viewModel.name).bindFromRequest().fold(
      formWithError => {
          Future.successful(BadRequest(confirmPreviousAddress(appConfig, formWithError, viewModel, countryOptions, existingSchemeName)))},
      { case true => userAnswersService.save(mode, viewModel.srn, id, true).flatMap { _ =>
        userAnswersService.save(mode, viewModel.srn, contactId, viewModel.address).map {
          cacheMap =>
            Redirect(navigator.nextPage(id, mode, UserAnswers(cacheMap)))
        }
      }
      case _ => userAnswersService.save(mode, viewModel.srn, id, false).flatMap { cacheMap =>
        Future.successful(Redirect(navigator.nextPage(id, mode, UserAnswers(cacheMap))))
      }
      }
    )
  }
}
