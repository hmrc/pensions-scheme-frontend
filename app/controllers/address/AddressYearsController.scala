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
import identifiers.TypedIdentifier
import models.requests.DataRequest
import models.{AddressYears, Mode}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, MessagesControllerComponents, Result}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.UserAnswers
import viewmodels.address.AddressYearsViewModel
import views.html.address.addressYears

import scala.concurrent.{ExecutionContext, Future}

trait AddressYearsController extends FrontendBaseController with Retrievals with I18nSupport {

  protected implicit def ec: ExecutionContext

  protected def appConfig: FrontendAppConfig

  protected def userAnswersService: UserAnswersService

  protected def navigator: Navigator

  protected def view: addressYears

  protected def get(id: TypedIdentifier[AddressYears], form: Form[AddressYears], viewmodel: AddressYearsViewModel)
                   (implicit request: DataRequest[AnyContent]): Future[Result] = {

    val filledForm =
      request.userAnswers.get(id).map(form.fill).getOrElse(form)

    Future.successful(Ok(view(filledForm, viewmodel, existingSchemeName)))
  }

  protected def post[I <: TypedIdentifier[AddressYears]](
                                                          id: I,
                                                          mode: Mode,
                                                          form: Form[AddressYears],
                                                          viewmodel: AddressYearsViewModel
                                                        )(implicit request: DataRequest[AnyContent]): Future[Result] = {
    form.bindFromRequest().fold(
      formWithErrors =>
        Future.successful(BadRequest(view(formWithErrors, viewmodel, existingSchemeName))),
      addressYears =>
        userAnswersService.save[AddressYears, TypedIdentifier[AddressYears]](mode, viewmodel.srn, id, addressYears).map {
          json =>
                Redirect(navigator.nextPage(id, mode, UserAnswers(json), viewmodel.srn))
            }
    )
  }
}
