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
import models.Link
import play.api.i18n.{I18nSupport, Lang, MessagesApi}
import play.api.mvc.{Request, RequestHeader}
import play.twirl.api.Html
import views.html.{error_template, error_template_page_not_found}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ErrorHandlerWithReturnLinkToManage @Inject()(
                                                    appConfig: FrontendAppConfig,
                                                    override val messagesApi: MessagesApi,
                                                    override val notFoundTemplateView: error_template_page_not_found,
                                                    override val errorTemplateView: error_template
                                                  ) (implicit ec: ExecutionContext) extends ErrorHandler(appConfig, messagesApi, notFoundTemplateView,
  errorTemplateView) with I18nSupport {

  override def notFoundTemplate(implicit request: RequestHeader): Future[Html] = {
    implicit def requestImplicit: Request[_] = Request(request, "")
    val linkContent = messagesApi.apply("messages__complete__returnToManagePensionSchemes")(Lang.defaultLang)
    Future.successful(notFoundTemplateView(Link(linkContent, appConfig.managePensionsSchemeOverviewUrl.url, Some(linkContent)))
    )
  }
}
