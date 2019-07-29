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

import com.google.inject.Inject
import controllers.actions.{AuthAction, DataRetrievalAction}
import identifiers.register.ContinueRegistrationId
import models.NormalMode
import navigators.Navigator
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.Register
import utils.UserAnswers

import scala.concurrent.ExecutionContext

class ContinueRegistrationController @Inject()(
                                                authenticate: AuthAction,
                                                getData: DataRetrievalAction,
                                                @Register navigator: Navigator
                                              )(implicit val ec: ExecutionContext) extends FrontendController {

  def continue(): Action[AnyContent] = (authenticate andThen getData()) {
    implicit request =>
      Redirect(navigator.nextPage(ContinueRegistrationId, NormalMode, request.userAnswers.getOrElse(UserAnswers())))
  }

}
