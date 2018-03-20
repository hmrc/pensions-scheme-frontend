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

package controllers.address

import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.Retrievals
import models.requests.DataRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, Result}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.Navigator
import viewmodels.Message

import scala.concurrent.Future

trait ManualAddressController extends FrontendController with Retrievals with I18nSupport {

  protected def appConfig: FrontendAppConfig
  protected def cacheConnector: DataCacheConnector
  protected def navigator: Navigator

  protected def form: Form[String]

  protected def get()(implicit request: DataRequest[AnyContent]): Future[Result] = ???

  protected def post()(implicit request: DataRequest[AnyContent]): Future[Result] = ???

  protected def formWithError(message: Message)(implicit request: DataRequest[AnyContent]): Form[String] = {
    form.withError("value", message.resolve)
  }

}
