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
import identifiers.TypedIdentifier
import models.requests.DataRequest
import models.{AddressYears, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, Result}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{Navigator, UserAnswers}
import viewmodels.address.AddressYearsViewModel
import views.html.address.addressYears

import scala.concurrent.Future

trait AddressYearsController extends FrontendController with Retrievals with I18nSupport {

  protected implicit val ec = play.api.libs.concurrent.Execution.defaultContext

  protected def appConfig: FrontendAppConfig

  protected def userAnswersService: UserAnswersService

  protected def navigator: Navigator

  protected def get(id: TypedIdentifier[AddressYears], form: Form[AddressYears], viewmodel: AddressYearsViewModel)
                   (implicit request: DataRequest[AnyContent]): Future[Result] = {

    val filledForm =
      request.userAnswers.get(id).map(form.fill).getOrElse(form)

    Future.successful(Ok(addressYears(appConfig, filledForm, viewmodel, existingSchemeName)))
  }

  protected def post[I <: TypedIdentifier[AddressYears]](
                                                          id: I,
                                                          mode: Mode,
                                                          form: Form[AddressYears],
                                                          viewmodel: AddressYearsViewModel
                                                        )(implicit request: DataRequest[AnyContent]): Future[Result] = {
    form.bindFromRequest().fold(
      formWithErrors =>
        Future.successful(BadRequest(addressYears(appConfig, formWithErrors, viewmodel, existingSchemeName))),
      addressYears =>
        userAnswersService.save(mode, viewmodel.srn, id, addressYears).map {
          answers =>
            Redirect(navigator.nextPage(id, mode, UserAnswers(answers)))
        }
    )
  }
}
