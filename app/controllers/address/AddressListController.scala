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

package controllers.address

import audit.{AddressEvent, AuditService}
import config.FrontendAppConfig
import controllers.Retrievals
import forms.address.AddressListFormProvider
import identifiers.TypedIdentifier
import models.address.{Address, TolerantAddress}
import models.requests.DataRequest
import models.{Mode, OptionalSchemeReferenceNumber}
import navigators.Navigator
import play.api.i18n.I18nSupport
import play.api.libs.json.JsValue
import play.api.mvc.{AnyContent, Result}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.UserAnswers
import viewmodels.address.AddressListViewModel
import views.html.address.addressList

import scala.concurrent.{ExecutionContext, Future}

trait AddressListController extends FrontendBaseController with Retrievals with I18nSupport {

  protected implicit def ec: ExecutionContext

  protected def appConfig: FrontendAppConfig

  protected def userAnswersService: UserAnswersService

  protected def auditService: AuditService

  protected def navigator: Navigator

  protected def view: addressList

  protected def get(viewModel: AddressListViewModel)
                   (implicit request: DataRequest[AnyContent]): Future[Result] = {

    val form = formProvider(viewModel.addresses)
    Future.successful(Ok(view(form, viewModel, existingSchemeName)))
  }

  protected def post(viewModel: AddressListViewModel,
                     navigatorId: TypedIdentifier[TolerantAddress],
                     dataId: TypedIdentifier[Address],
                     mode: Mode,
                     context: String,
                     postCodeLookupIdForCleanup: TypedIdentifier[Seq[TolerantAddress]]
                    )
                    (implicit request: DataRequest[AnyContent]): Future[Result] = {

    formProvider(viewModel.addresses).bindFromRequest().fold(
      formWithErrors =>
        Future.successful(BadRequest(view(formWithErrors, viewModel, existingSchemeName))),
      addressIndex => {
        val address = viewModel.addresses(addressIndex).copy(countryOpt = Some("GB"))
        removePostCodeLookupAddress(mode, viewModel.srn, postCodeLookupIdForCleanup)
          .flatMap { userAnswersJson =>
            if (address.toAddress.nonEmpty) {
              val auditEvent = AddressEvent.addressEntryEvent(request.externalId, address.toAddress.get,
                request.userAnswers.get(dataId), Some(address), context)
              val answers = userAnswersService
                .setExistingAddress(mode, dataId, UserAnswers(userAnswersJson))
                .set(dataId)(address.toAddress.get).flatMap(_.set(navigatorId)(address)).asOpt.getOrElse(request.userAnswers)

              userAnswersService.upsert(mode, viewModel.srn, answers.json).map {
                json =>
                  auditEvent.foreach(auditService.sendEvent(_))
                  Redirect(navigator.nextPage(navigatorId, mode, UserAnswers(json), viewModel.srn))
              }
            } else {
              val answers = userAnswersService
                .setExistingAddress(mode, dataId, UserAnswers(userAnswersJson))
                .set(navigatorId)(address).asOpt.getOrElse(request.userAnswers)
              userAnswersService.upsert(mode, viewModel.srn, answers.json).map { _ =>
                Redirect(viewModel.manualInputCall)
              }
            }
          }
      }
    )
  }

  protected def formProvider: AddressListFormProvider = new AddressListFormProvider()

  private def removePostCodeLookupAddress(mode: Mode, srn: OptionalSchemeReferenceNumber,
                                          postCodeLookupId: TypedIdentifier[Seq[TolerantAddress]])
                                         (implicit request: DataRequest[AnyContent]): Future[JsValue] = {
    if (request.userAnswers.get(postCodeLookupId).nonEmpty) {
      userAnswersService.remove(mode, srn, postCodeLookupId)
    } else {
      Future(request.userAnswers.json)
    }
  }
}
