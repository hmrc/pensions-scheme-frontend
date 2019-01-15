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

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.actions._
import forms.WorkingKnowledgeFormProvider
import identifiers.SchemeNameId
import identifiers.{DeclarationDutiesId, IsWorkingKnowledgeCompleteId}
import javax.inject.Inject
import models.Mode
import models.requests.OptionalDataRequest
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.BeforeYouStart
import utils.{Enumerable, Navigator, SectionComplete, UserAnswers}
import views.html.workingKnowledge

import scala.concurrent.{ExecutionContext, Future}

class WorkingKnowledgeController @Inject()(
                                            appConfig: FrontendAppConfig,
                                            override val messagesApi: MessagesApi,
                                            dataCacheConnector: UserAnswersCacheConnector,
                                            @BeforeYouStart navigator: Navigator,
                                            authenticate: AuthAction,
                                            getData: DataRetrievalAction,
                                            formProvider: WorkingKnowledgeFormProvider,
                                            sectionComplete: SectionComplete
                                          )(implicit val ec: ExecutionContext) extends FrontendController with I18nSupport with Enumerable.Implicits {

  private val form = formProvider()

  private def existingSchemeNameOrEmptyString(implicit request: OptionalDataRequest[AnyContent]): String =
    request.userAnswers.flatMap(_.get(SchemeNameId)).getOrElse("")

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData) {
    implicit request =>
      val preparedForm = request.userAnswers.flatMap(_.get(DeclarationDutiesId)) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(workingKnowledge(appConfig, preparedForm, mode, existingSchemeNameOrEmptyString))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(workingKnowledge(appConfig, formWithErrors, mode, existingSchemeNameOrEmptyString))),
        value => {

          dataCacheConnector.save(request.externalId, DeclarationDutiesId, value).flatMap(cacheMap =>
            setCompleteFlag(value, UserAnswers(cacheMap)).map { _ =>
              Redirect(navigator.nextPage(DeclarationDutiesId, mode, UserAnswers(cacheMap)))
            }
          )
        }
      )
  }

  private def setCompleteFlag(value: Boolean, userAnswers: UserAnswers)
                             (implicit request: OptionalDataRequest[AnyContent]): Future[UserAnswers] = {
    if (value) {
      sectionComplete.setCompleteFlag(request.externalId, IsWorkingKnowledgeCompleteId,
        userAnswers, value)
    } else {
      Future.successful(userAnswers)
    }
  }
}
