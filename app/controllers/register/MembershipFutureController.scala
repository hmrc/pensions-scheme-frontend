/*
 * Copyright 2018 HM Revenue & Customs
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

package controllers.register

import javax.inject.Inject

import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import connectors.DataCacheConnector
import controllers.actions._
import config.FrontendAppConfig
import forms.register.MembershipFutureFormProvider
import identifiers.register.MembershipFutureId
import models.Mode
import models.register.MembershipFuture
import play.api.mvc.{Action, AnyContent}
import utils.{Enumerable, Navigator, UserAnswers}
import views.html.register.membershipFuture

import scala.concurrent.Future
import play.api.libs.json._

class MembershipFutureController @Inject()(
                                        appConfig: FrontendAppConfig,
                                        override val messagesApi: MessagesApi,
                                        dataCacheConnector: DataCacheConnector,
                                        navigator: Navigator,
                                        authenticate: AuthAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: MembershipFutureFormProvider) extends FrontendController with I18nSupport with Enumerable.Implicits {

  private val key = __ \ MembershipFutureId

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.membershipFuture match {
        case None => form
        case Some(value) => form.fill(value)
      }
      Ok(membershipFuture(appConfig, preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(membershipFuture(appConfig, formWithErrors, mode))),
        (value) =>
          dataCacheConnector.save[MembershipFuture](request.externalId, key, value).map(cacheMap =>
            Redirect(navigator.nextPage(MembershipFutureId, mode)(new UserAnswers(cacheMap))))
      )
  }
}
