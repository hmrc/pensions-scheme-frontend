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

package handlers

import config.FrontendAppConfig
import javax.inject.{Inject, Singleton}
import models.Link
import play.api.i18n.{I18nSupport, Lang, MessagesApi}
import play.api.mvc.Request
import play.twirl.api.Html
import views.html.{error_template, error_template_page_not_found}

@Singleton
class ErrorHandlerWithReturnLinkToManage @Inject()(
                                                    appConfig: FrontendAppConfig,
                                                    override val messagesApi: MessagesApi,
                                                    override val notFoundView: error_template_page_not_found,
                                                    override val errorView: error_template
                                                  ) extends ErrorHandler(appConfig, messagesApi, notFoundView,
  errorView) with I18nSupport {

  override def notFoundTemplate(implicit request: Request[_]): Html = {
    val linkContent = messagesApi.apply("messages__complete__returnToManagePensionSchemes")(Lang.defaultLang)
    notFoundView(Link(linkContent, appConfig.managePensionsSchemeOverviewUrl.url, Some(linkContent))
    )
  }
}
