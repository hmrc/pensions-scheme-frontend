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

import config.FrontendAppConfig
import controllers.actions._
import models.SchemeReferenceNumber
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UrlsPartialService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.urlsPartial

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class UrlsPartialController @Inject()(appConfig: FrontendAppConfig,
                                     override val messagesApi: MessagesApi,
                                     authenticate: AuthAction,
                                      getData: DataRetrievalAction,
                                      urlsPartialService: UrlsPartialService,
                                     val controllerComponents: MessagesControllerComponents
                                    )(implicit val executionContext: ExecutionContext) extends FrontendBaseController with I18nSupport with Retrievals {

  def retrieveUrlsPartial(srn: SchemeReferenceNumber): Action[AnyContent] = (authenticate() andThen getData()).async {

    implicit request =>
      request.psaId.map { psaId =>
        urlsPartialService.schemeLinks(psaId.id).map{links =>
          Ok(urlsPartial(links))}
      }.getOrElse(throw PsaIdMissingException)
  }

  object PsaIdMissingException extends Exception("PSA ID cannot be retrieved from request")

  def checkIfSchemeCanBeRegistered(srn: SchemeReferenceNumber): Action[AnyContent] = (authenticate() andThen getData()).async {
    implicit request =>
      request.psaId.map { psaId =>
        urlsPartialService.checkIfSchemeCanBeRegistered(psaId.id)
      }.getOrElse(throw PsaIdMissingException)

  }
}
