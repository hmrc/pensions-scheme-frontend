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

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import forms.register.adviser.AdviserEmailFormProvider
import identifiers.{AdviserEmailId, AdviserNameId, SchemeNameId}
import models.Mode
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.UserAnswers
import utils.annotations.WorkingKnowledge
import views.html.adviserEmailAddress

import scala.concurrent.{ExecutionContext, Future}

class AdviserEmailAddressController @Inject()(
                                               appConfig: FrontendAppConfig,
                                               override val messagesApi: MessagesApi,
                                               authenticate: AuthAction,
                                               @WorkingKnowledge navigator: Navigator,
                                               getData: DataRetrievalAction,
                                               requireData: DataRequiredAction,
                                               formProvider: AdviserEmailFormProvider,
                                               dataCacheConnector: UserAnswersCacheConnector,
                                               val controllerComponents: MessagesControllerComponents,
                                               val view: adviserEmailAddress
                                             )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController with I18nSupport with Retrievals {


  val form: Form[String] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate() andThen getData() andThen requireData).async {
    implicit request =>
      val form = formProvider()
      for {
        schemeName <- SchemeNameId.retrieve
        adviserName <- AdviserNameId.retrieve
      } yield {
        val preparedForm = request.userAnswers.get(AdviserEmailId) match {
          case None => form
          case Some(value) => form.fill(value)
        }
        Future.successful(Ok(view(preparedForm, mode, adviserName, schemeName)))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate() andThen getData() andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[?]) => {
          for {
            schemeName <- SchemeNameId.retrieve
            adviserName <- AdviserNameId.retrieve
          } yield {
            Future.successful(BadRequest(view(formWithErrors, mode, adviserName, schemeName)))
          }
        },
        value =>
          dataCacheConnector.save(request.externalId, AdviserEmailId, value).map {
            cacheMap =>
              Redirect(navigator.nextPage(AdviserEmailId, mode, UserAnswers(cacheMap)))
          }
      )
  }

}
