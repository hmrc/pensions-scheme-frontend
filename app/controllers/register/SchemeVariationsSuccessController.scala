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

package controllers.register

import connectors.UpdateSchemeCacheConnector
import controllers.Retrievals
import controllers.actions._
import models.{OptionalSchemeReferenceNumber, SchemeReferenceNumber, UpdateMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.register.schemeVariationsSuccess

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class SchemeVariationsSuccessController @Inject()(override val messagesApi: MessagesApi,
                                                  cacheConnector: UpdateSchemeCacheConnector,
                                                  authenticate: AuthAction,
                                                  getData: DataRetrievalAction,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  val view: schemeVariationsSuccess
                                                 )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController
  with I18nSupport with Retrievals {

  def onPageLoad(srn: SchemeReferenceNumber): Action[AnyContent] = (authenticate() andThen getData(UpdateMode, OptionalSchemeReferenceNumber(Some(srn)))).async {
    implicit request =>
      val schemeName = existingSchemeName
      cacheConnector.removeAll(srn).map { _ =>
        Ok(
          view(schemeName,  OptionalSchemeReferenceNumber(Some(srn))
          )
        )
      }
  }
}
