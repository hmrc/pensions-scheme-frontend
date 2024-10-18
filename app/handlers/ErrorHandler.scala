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

package handlers

import config.FrontendAppConfig

import javax.inject.{Inject, Singleton}
import models.Link
import play.api.i18n.{I18nSupport, Lang, MessagesApi}
import play.api.mvc.{Request, RequestHeader}
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.frontend.http.FrontendErrorHandler
import views.html.error_template_page_not_found
import views.html.error_template

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ErrorHandler @Inject()(
                              appConfig: FrontendAppConfig,
                              val messagesApi: MessagesApi,
                              val notFoundTemplateView: error_template_page_not_found,
                              val errorTemplateView: error_template
                            ) (implicit val ec: ExecutionContext)
  extends FrontendErrorHandler with I18nSupport {

  def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit request: RequestHeader): Future[Html] = {
    implicit def requestImplicit: Request[_] = Request(request, "")
    Future.successful(errorTemplateView(pageTitle, heading, message, appConfig))
  }

  def notFoundTemplate(implicit request: Request[_]): Future[Html] = {
    val linkContent = messagesApi.apply("messages__schemesOverview__manage__link")(Lang.defaultLang)
    Future.successful(notFoundTemplateView(Link(linkContent, appConfig.managePensionsYourPensionSchemesUrl, Some(linkContent))))
  }
}
