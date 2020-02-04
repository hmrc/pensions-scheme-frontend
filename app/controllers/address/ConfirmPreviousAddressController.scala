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

package controllers.address

import config.FrontendAppConfig
import controllers.Retrievals
import forms.address.ConfirmAddressFormProvider
import identifiers.TypedIdentifier
import models.Mode
import models.address.Address
import models.requests.DataRequest
import navigators.Navigator
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents, Result}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.{CountryOptions, UserAnswers}
import viewmodels.Message
import viewmodels.address.ConfirmAddressViewModel
import views.html.address.confirmPreviousAddress

import scala.concurrent.{ExecutionContext, Future}

trait ConfirmPreviousAddressController extends FrontendBaseController with Retrievals with I18nSupport {
  protected implicit def ec: ExecutionContext

  protected def controllerComponents: MessagesControllerComponents

  protected def appConfig: FrontendAppConfig

  protected def userAnswersService: UserAnswersService

  protected def navigator: Navigator

  protected def countryOptions: CountryOptions

  protected def view: confirmPreviousAddress

  protected def get(
                     id: TypedIdentifier[Boolean],
                     viewModel: ConfirmAddressViewModel
                   )(implicit request: DataRequest[AnyContent], messages: Messages): Future[Result] = {

    val preparedForm = request.userAnswers.get(id) match {
      case None => form(viewModel.name)
      case Some(value) => form(viewModel.name).fill(value)
    }
    Future.successful(Ok(view(preparedForm, viewModel, countryOptions, existingSchemeName)))
  }

  protected def form(name: String)(implicit messages: Messages) = formProvider(Message("messages__confirmPreviousAddress__error", name))

  protected def formProvider: ConfirmAddressFormProvider = new ConfirmAddressFormProvider()

  protected def post(
                      id: TypedIdentifier[Boolean],
                      contactId: TypedIdentifier[Address],
                      viewModel: ConfirmAddressViewModel,
                      mode: Mode
                    )(implicit request: DataRequest[AnyContent], messages: Messages): Future[Result] = {
    form(viewModel.name).bindFromRequest().fold(
      formWithError => {
        Future.successful(BadRequest(view(formWithError, viewModel, countryOptions, existingSchemeName)))
      },
      { case true =>

        val updatedUserAnswers = request.userAnswers
          .set(id)(true).flatMap(
          _.set(contactId)(viewModel.address)
        ).getOrElse(request.userAnswers)

        userAnswersService.upsert(mode, viewModel.srn, updatedUserAnswers.json).map {
          cacheMap =>
            Redirect(navigator.nextPage(id, mode, UserAnswers(cacheMap), viewModel.srn))
        }

      case _ =>
        userAnswersService.save(mode, viewModel.srn, id, false).flatMap { cacheMap =>
          Future.successful(Redirect(navigator.nextPage(id, mode, UserAnswers(cacheMap), viewModel.srn)))
        }
      }
    )
  }
}
