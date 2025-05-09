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
import forms.WorkingKnowledgeFormProvider
import identifiers.DeclarationDutiesId
import models.Mode
import models.requests.OptionalDataRequest
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils._
import utils.annotations.BeforeYouStart
import views.html.workingKnowledge

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WorkingKnowledgeController @Inject()(override val messagesApi: MessagesApi,
                                            dataCacheConnector: UserAnswersCacheConnector,
                                            @BeforeYouStart navigator: Navigator,
                                            authenticate: AuthAction,
                                            getData: DataRetrievalAction,
                                            formProvider: WorkingKnowledgeFormProvider,
                                            val controllerComponents: MessagesControllerComponents,
                                            val view: workingKnowledge
                                          )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController with I18nSupport with Retrievals with Enumerable.Implicits {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate() andThen getData()) {
    implicit request =>
      val preparedForm = request.userAnswers.flatMap(_.get(DeclarationDutiesId)) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, existingSchemeNameOrEmptyString))
  }

  private def existingSchemeNameOrEmptyString(implicit request: OptionalDataRequest[AnyContent]): String =
    existingSchemeName.getOrElse("")

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate() andThen getData()).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[?]) =>
          Future.successful(BadRequest(view(formWithErrors, mode, existingSchemeNameOrEmptyString))),
        value => {

          dataCacheConnector.save(request.externalId, DeclarationDutiesId, value).map(cacheMap =>
            Redirect(navigator.nextPage(DeclarationDutiesId, mode, UserAnswers(cacheMap)))
          )
        }
      )
  }
}
