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

package controllers.register.adviser

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions._
import forms.register.adviser.AdviserPhoneFormProvider
import identifiers.register.adviser.{AdviserNameId, AdviserPhoneId}
import javax.inject.Inject
import models.Mode
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.Adviser
import utils.{Navigator, UserAnswers}
import views.html.register.adviser.adviserPhone

import scala.concurrent.{ExecutionContext, Future}

class AdviserPhoneController @Inject()(
                                        appConfig: FrontendAppConfig,
                                        override val messagesApi: MessagesApi,
                                        dataCacheConnector: UserAnswersCacheConnector,
                                        @Adviser navigator: Navigator,
                                        authenticate: AuthAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: AdviserPhoneFormProvider
                                      ) (implicit val ec: ExecutionContext) extends FrontendController with I18nSupport with Retrievals {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      val form = formProvider()
      AdviserNameId.retrieve.right.map { adviserName =>
        val preparedForm = request.userAnswers.get(AdviserPhoneId) match {
          case None => form
          case Some(value) => form.fill(value)
        }
        Future.successful(Ok(adviserPhone(appConfig, preparedForm, mode, adviserName)))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) => {
          AdviserNameId.retrieve.right.map { adviserName =>
            Future.successful(BadRequest(adviserPhone(appConfig, formWithErrors, mode, adviserName)))
          }
        },
        value =>
          dataCacheConnector.save(request.externalId, AdviserPhoneId, value).map {
            cacheMap =>
              Redirect(navigator.nextPage(AdviserPhoneId, mode, UserAnswers(cacheMap)))
          }
      )
  }
}
