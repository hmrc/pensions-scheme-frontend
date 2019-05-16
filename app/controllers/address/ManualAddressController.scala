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

import audit.{AddressEvent, AuditService}
import config.FrontendAppConfig
import controllers.Retrievals
import identifiers.TypedIdentifier
import models.{CheckUpdateMode, Mode, UpdateMode}
import models.address.{Address, TolerantAddress}
import models.requests.DataRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.JsValue
import play.api.mvc.{AnyContent, Call, Result}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{Navigator, UserAnswers}
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddress

import scala.concurrent.Future

trait ManualAddressController extends FrontendController with Retrievals with I18nSupport {

  protected implicit val ec = play.api.libs.concurrent.Execution.defaultContext

  protected def appConfig: FrontendAppConfig

  protected def userAnswersService: UserAnswersService

  protected def navigator: Navigator

  protected def auditService: AuditService

  protected val form: Form[Address]

  protected def get(
                     id: TypedIdentifier[Address],
                     selectedId: TypedIdentifier[TolerantAddress],
                     viewModel: ManualAddressViewModel
                   )(implicit request: DataRequest[AnyContent]): Future[Result] = {
    val preparedForm = request.userAnswers.get(id) match {
      case None => request.userAnswers.get(selectedId) match {
        case Some(value) => form.fill(value.toAddress)
        case None => form
      }
      case Some(value) => form.fill(value)
    }
    Future.successful(Ok(manualAddress(appConfig, preparedForm, viewModel, existingSchemeName)))
  }

  protected def post(
                      id: TypedIdentifier[Address],
                      selectedId: TypedIdentifier[TolerantAddress],
                      viewModel: ManualAddressViewModel,
                      mode: Mode,
                      context: String,
                      postCodeLookupIdForCleanup: TypedIdentifier[Seq[TolerantAddress]]
                    )(implicit request: DataRequest[AnyContent]): Future[Result] = {
    form.bindFromRequest().fold(
      (formWithError: Form[_]) => Future.successful(BadRequest(manualAddress(appConfig, formWithError, viewModel, existingSchemeName))),
      address => {
        val existingAddress = request.userAnswers.get(id)
        val selectedAddress = request.userAnswers.get(selectedId)

        val auditEvent = AddressEvent.addressEntryEvent(request.externalId, address, existingAddress, selectedAddress, context)

        removePostCodeLookupAddress(mode, viewModel.srn, postCodeLookupIdForCleanup)
          .flatMap { _ =>
            userAnswersService.save(mode, viewModel.srn, id, address).flatMap {
              cacheMap =>
                userAnswersService.setAddressCompleteFlagAfterPreviousAddress(mode, viewModel.srn, id, UserAnswers(cacheMap)).map {
                  answers =>
                    auditEvent.foreach(auditService.sendEvent(_))
                    Redirect(navigator.nextPage(id, mode, answers, viewModel.srn))
                }
            }
          }
      }
    )
  }

  private def removePostCodeLookupAddress(mode: Mode, srn: Option[String], postCodeLookupId: TypedIdentifier[Seq[TolerantAddress]])
                                         (implicit request: DataRequest[AnyContent]): Future[JsValue] = {
    if(request.userAnswers.get(postCodeLookupId).nonEmpty) {
      userAnswersService.remove(mode, srn, postCodeLookupId)
    } else {
      Future(request.userAnswers.json)
    }
  }

  protected def clear(id: TypedIdentifier[Address],
                      selectedId: TypedIdentifier[TolerantAddress],
                      mode: Mode, srn: Option[String], manualCall: Call)(implicit request: DataRequest[AnyContent]): Future[Result] =
    if (mode == CheckUpdateMode || mode == UpdateMode) {
      userAnswersService.remove(mode, srn, id).flatMap (_ =>
        userAnswersService.remove(mode, srn, selectedId).map (_ =>
          Redirect(manualCall)))
    } else {
      Future.successful(Redirect(manualCall))
    }

}
