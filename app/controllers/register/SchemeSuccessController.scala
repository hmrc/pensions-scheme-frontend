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

import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import controllers.actions._
import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.Retrievals
import identifiers.register.{SchemeDetailsId, SchemeSuccessId, SubmissionReferenceNumberId}
import models.NormalMode
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.Register
import views.html.register.schemeSuccess

import scala.concurrent.Future


class SchemeSuccessController @Inject()(appConfig: FrontendAppConfig,
                                        override val messagesApi: MessagesApi,
                                        cacheConnector: DataCacheConnector,
                                        authenticate: AuthAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        @Register navigator: Navigator) extends FrontendController with I18nSupport with Retrievals {

  def onPageLoad: Action[AnyContent] = (authenticate andThen getData andThen requireData).async{
    implicit request =>

      SubmissionReferenceNumberId.retrieve.right.map {
        submissionReferenceNumber =>
          Future.successful(Ok(schemeSuccess(
            appConfig,
            request.userAnswers.get(SchemeDetailsId).map(_.schemeName),
            LocalDate.now(),
            submissionReferenceNumber))
          )
      }
  }

  def onSubmit: Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>
      Redirect(navigator.nextPage(SchemeSuccessId, NormalMode)(request.userAnswers))
  }

}
