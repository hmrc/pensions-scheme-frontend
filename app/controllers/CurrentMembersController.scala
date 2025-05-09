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

package controllers

import connectors.UserAnswersCacheConnector
import controllers.actions._
import forms.CurrentMembersFormProvider
import identifiers.{CurrentMembersId, SchemeNameId}
import models.{Members, Mode}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.AboutMembers
import utils.{Enumerable, UserAnswers}
import views.html.currentMembers

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CurrentMembersController @Inject()(override val messagesApi: MessagesApi,
                                         dataCacheConnector: UserAnswersCacheConnector,
                                         @AboutMembers navigator: Navigator,
                                         authenticate: AuthAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: CurrentMembersFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         val view: currentMembers
                                        )(implicit val executionContext: ExecutionContext
                                        ) extends FrontendBaseController with I18nSupport with Enumerable.Implicits
  with Retrievals {

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate() andThen getData() andThen requireData).async {
    implicit request =>
      SchemeNameId.retrieve.map { schemeName =>
        val preparedForm = request.userAnswers.get(CurrentMembersId) match {
          case None => form(schemeName)
          case Some(value) => form(schemeName).fill(value)
        }
        Future.successful(Ok(view(preparedForm, mode, schemeName)))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate() andThen getData() andThen requireData).async {
    implicit request =>
      SchemeNameId.retrieve.map { schemeName =>
        form(schemeName).bindFromRequest().fold(
          (formWithErrors: Form[?]) =>
            Future.successful(BadRequest(view(formWithErrors, mode, schemeName))),
          value =>
            dataCacheConnector.save(request.externalId, CurrentMembersId, value).map { cacheMap =>
              Redirect(navigator.nextPage(CurrentMembersId, mode, UserAnswers(cacheMap)))
            }
        )
      }
  }

  private def form(schemeName: String)(implicit messages: Messages): Form[Members] = formProvider(schemeName)
}
