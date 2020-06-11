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

package controllers

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.actions._
import forms.register.AdviserNameFormProvider
import identifiers.AdviserNameId
import javax.inject.Inject
import models.Mode
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.{FrontendBaseController, FrontendController}
import utils.UserAnswers
import utils.annotations.WorkingKnowledge
import views.html.adviserName

import scala.concurrent.{ExecutionContext, Future}

class AdviserNameController @Inject()(
                                       appConfig: FrontendAppConfig,
                                       override val messagesApi: MessagesApi,
                                       dataCacheConnector: UserAnswersCacheConnector,
                                       @WorkingKnowledge navigator: Navigator,
                                       authenticate: AuthAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       formProvider: AdviserNameFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       val view: adviserName
                                     )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController with I18nSupport with Retrievals {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData() andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(AdviserNameId) match {
        case None => form
        case Some(value) => form.fill(value)
      }
      Ok(view(preparedForm, mode, existingSchemeName))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData() andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, mode, existingSchemeName))),
        value =>
          dataCacheConnector.save(request.externalId, AdviserNameId, value).map { cacheMap =>
            Redirect(navigator.nextPage(AdviserNameId, mode, UserAnswers(cacheMap)))
          }
      )
  }
}
