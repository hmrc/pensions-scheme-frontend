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
import connectors.UserAnswersCacheConnector
import controllers.Retrievals
import forms.address.AddressListFormProvider
import identifiers.TypedIdentifier
import models.{CheckUpdateMode, Mode, UpdateMode}
import models.address.{Address, TolerantAddress}
import models.requests.DataRequest
import navigators.Navigator
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, Call, Result}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.UserAnswers
import viewmodels.address.AddressListViewModel
import views.html.address.addressList

import scala.concurrent.{ExecutionContext, Future}

trait AddressListController extends FrontendController with Retrievals  with I18nSupport {

  protected implicit def ec: ExecutionContext

  protected def appConfig: FrontendAppConfig

  protected def userAnswersService: UserAnswersService

  protected def navigator: Navigator

  protected def formProvider: AddressListFormProvider = new AddressListFormProvider()

  protected def get(viewModel: AddressListViewModel)
                   (implicit request: DataRequest[AnyContent]): Future[Result] = {

    val form = formProvider(viewModel.addresses)
    Future.successful(Ok(addressList(appConfig, form, viewModel, existingSchemeName)))
  }

  protected def post(viewModel: AddressListViewModel, navigatorId: TypedIdentifier[TolerantAddress], dataId: TypedIdentifier[Address], mode: Mode)
                    (implicit request: DataRequest[AnyContent]): Future[Result] = {

    formProvider(viewModel.addresses).bindFromRequest().fold(
      formWithErrors =>
        Future.successful(BadRequest(addressList(appConfig, formWithErrors, viewModel, existingSchemeName))),
      addressIndex => {
        val address = viewModel.addresses(addressIndex).copy(country = Some("GB"))
        val answers = request.userAnswers.remove(dataId).flatMap(_.set(navigatorId)(address)).asOpt.getOrElse(request.userAnswers)
        userAnswersService.upsert(mode, viewModel.srn, answers.json).map{
          json =>
            Redirect(navigator.nextPage(navigatorId, mode, UserAnswers(json), viewModel.srn))
        }
      }
    )
  }
}
