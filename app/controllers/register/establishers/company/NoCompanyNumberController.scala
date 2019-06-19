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

package controllers.register.establishers.company

import config.FrontendAppConfig
import controllers.actions._
import javax.inject.Inject
import models.{Index, Mode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.Future

class NoCompanyNumberController @Inject()(appConfig: FrontendAppConfig,
                                          override val messagesApi: MessagesApi,
                                          authenticate: AuthAction,
                                          getData: DataRetrievalAction
                                                    ) extends FrontendController with I18nSupport {

  def onPageLoad(mode: Mode, srn: Option[String] = None, index: Index): Action[AnyContent] = (authenticate andThen getData()).async {
    implicit request =>
      Future.successful(Ok)
  }

  def onSubmit(mode: Mode, srn: Option[String] = None, index: Index): Action[AnyContent] = authenticate {
    implicit request =>
      Redirect(controllers.routes.IndexController.onPageLoad())
  }
}
