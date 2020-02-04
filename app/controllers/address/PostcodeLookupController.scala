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
import connectors.AddressLookupConnector
import controllers.Retrievals
import identifiers.TypedIdentifier
import models.Mode
import models.address.TolerantAddress
import models.requests.DataRequest
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.{FrontendBaseController, FrontendController}
import utils.UserAnswers
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import scala.concurrent.{ExecutionContext, Future}

trait PostcodeLookupController extends FrontendBaseController with Retrievals with I18nSupport {

  protected implicit def ec: ExecutionContext

  protected def appConfig: FrontendAppConfig

  protected def userAnswersService: UserAnswersService

  protected def addressLookupConnector: AddressLookupConnector

  protected def navigator: Navigator

  protected def form: Form[String]

  protected def view: postcodeLookup

  private val invalidPostcode: Message = "messages__error__postcode_failed"

  protected def get(viewmodel: PostcodeLookupViewModel)(implicit request: DataRequest[AnyContent]): Future[Result] = {
    Future.successful(Ok(view(form, viewmodel, existingSchemeName)))
  }

  protected def post(
                      id: TypedIdentifier[Seq[TolerantAddress]],
                      viewmodel: PostcodeLookupViewModel,
                      mode: Mode,
                      invalidPostcode: Message = invalidPostcode
                    )(implicit request: DataRequest[AnyContent]): Future[Result] = {

    form.bindFromRequest().fold(
      formWithErrors =>
        Future.successful {
          BadRequest(view(formWithErrors, viewmodel, existingSchemeName))
        }, lookupPostcode(id, viewmodel, invalidPostcode, mode, _)
    )
  }

  private def lookupPostcode(
                              id: TypedIdentifier[Seq[TolerantAddress]],
                              viewmodel: PostcodeLookupViewModel,
                              invalidPostcode: Message,
                              mode: Mode,
                              postCode: String
                            )(implicit request: DataRequest[AnyContent]): Future[Result] = {

    val noResults: Message = Message("messages__error__postcode_no_results", postCode)

    addressLookupConnector.addressLookupByPostCode(postCode).flatMap {

      case Nil => Future.successful(Ok(view(formWithError(noResults), viewmodel, existingSchemeName)))

      case addresses =>
        userAnswersService.save(
          mode,
          viewmodel.srn,
          id,
          addresses
        ).map {
          json =>
            Redirect(navigator.nextPage(id, mode, UserAnswers(json), viewmodel.srn))
        }

    } recoverWith {
      case _ =>
        Future.successful(BadRequest(view(formWithError(invalidPostcode), viewmodel, existingSchemeName)))
    }
  }

  protected def formWithError(message: Message)(implicit request: DataRequest[AnyContent]): Form[String] = {
    form.withError("value", message.resolve)
  }
}
