/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.dateOfBirth

import java.time.LocalDate

import config.FrontendAppConfig
import controllers.Retrievals
import identifiers.TypedIdentifier
import models.Mode
import models.person.PersonName
import models.requests.DataRequest
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, Result}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.UserAnswers
import viewmodels.dateOfBirth.DateOfBirthViewModel
import views.html.register.DOB

import scala.concurrent.{ExecutionContext, Future}

trait DateOfBirthController extends FrontendBaseController with Retrievals with I18nSupport {
  protected implicit def ec: ExecutionContext

  protected val form: Form[LocalDate]

  protected def appConfig: FrontendAppConfig

  protected def userAnswersService: UserAnswersService

  protected def navigator: Navigator

  protected def view: DOB

  protected def get(dobId: TypedIdentifier[LocalDate], personNameId: TypedIdentifier[PersonName],
                    viewModel: DateOfBirthViewModel, mode: Mode)
                   (implicit request: DataRequest[AnyContent]): Future[Result] = {

    val preparedForm = request.userAnswers.get(dobId) match {
      case Some(value) => form.fill(value)
      case None => form
    }

    personNameId.retrieve.right.map {
      personName =>
        Future.successful(Ok(
          view(preparedForm, mode, existingSchemeName, personName.fullName, viewModel)
        ))
    }
  }

  protected def post[I <: TypedIdentifier[LocalDate]](dobId: I, personNameId: TypedIdentifier[PersonName],
                                                      viewModel: DateOfBirthViewModel, mode: Mode)
                                                     (implicit request: DataRequest[AnyContent]): Future[Result] = {

    form.bindFromRequest().fold(
      formWithErrors =>
        personNameId.retrieve.right.map {
          personName =>
            Future.successful(BadRequest(
              view(formWithErrors, mode, existingSchemeName, personName.fullName, viewModel)
            ))
        },
      value =>
        userAnswersService.save(mode, viewModel.srn, dobId, value).map {
          cacheMap =>
            Redirect(navigator.nextPage(dobId, mode, UserAnswers(cacheMap), viewModel.srn))
        }
    )
  }
}
