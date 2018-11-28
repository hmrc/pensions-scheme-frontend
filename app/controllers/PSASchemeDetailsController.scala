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

package controllers

import config.FrontendAppConfig
import connectors.SchemeDetailsConnector
import controllers.actions._
import javax.inject.Inject
import models.details.transformation.SchemeDetailsMasterSection
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.psa_scheme_details

import scala.concurrent.Future

class PSASchemeDetailsController @Inject()(appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           schemeDetailsConnector: SchemeDetailsConnector,
                                           schemeTransformer: SchemeDetailsMasterSection,
                                           authenticate: AuthAction
                                       ) extends FrontendController with I18nSupport {

  def onPageLoad(srn: String): Action[AnyContent] = authenticate.async {
    implicit request =>
      schemeDetailsConnector.getSchemeDetails(request.psaId.id, schemeIdType ="srn", srn).flatMap { scheme =>
        if (scheme.psaDetails.exists(_.id == request.psaId.id)) {
          val schemeDetailMasterSection = schemeTransformer.transformMasterSection(scheme)
          Future.successful(Ok(psa_scheme_details(appConfig, schemeDetailMasterSection, scheme.schemeDetails.name, srn)))
        } else {
          Future.successful(NotFound)
        }
      }
  }
}
