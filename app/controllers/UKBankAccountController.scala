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
import forms.UKBankAccountFormProvider
import identifiers.{SchemeNameId, UKBankAccountId}
import javax.inject.Inject
import models.Mode
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.UserAnswers
import utils.annotations.AboutBankDetails
import views.html.uKBankAccount

import scala.concurrent.{ExecutionContext, Future}

class UKBankAccountController @Inject()(appConfig: FrontendAppConfig,
                                        override val messagesApi: MessagesApi,
                                        dataCacheConnector: UserAnswersCacheConnector,
                                        @AboutBankDetails navigator: Navigator,
                                        authenticate: AuthAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: UKBankAccountFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        val view: uKBankAccount
                                       )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController with I18nSupport with Retrievals {

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData() andThen requireData).async {
    implicit request =>
      SchemeNameId.retrieve.right.map { schemeName =>
        val preparedForm = request.userAnswers.get(UKBankAccountId) match {
          case None => form(schemeName)
          case Some(value) => form(schemeName).fill(value)
        }
        Future.successful(Ok(view(preparedForm, mode, schemeName)))
      }
  }

  private def form(schemeName: String)(implicit messages: Messages): Form[Boolean] = formProvider(schemeName)

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData() andThen requireData).async {
    implicit request =>
      SchemeNameId.retrieve.right.map { schemeName =>
        form(schemeName).bindFromRequest().fold(
          (formWithErrors: Form[_]) =>

            Future.successful(BadRequest(view(formWithErrors, mode, schemeName))),
          value =>
            dataCacheConnector.save(request.externalId, UKBankAccountId, value).map { cacheMap =>
              Redirect(navigator.nextPage(UKBankAccountId, mode, UserAnswers(cacheMap)))
            }
        )
      }
  }
}
