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

package controllers.register.establishers.partnership

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import identifiers.register.establishers.partnership.PartnershipDetailsId

import javax.inject.Inject
import models.{Index, Mode, PartnershipDetails, SchemeReferenceNumber}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.Message
import views.html.register.whatYouWillNeedAddress

import scala.concurrent.{ExecutionContext, Future}

class WhatYouWillNeedPartnershipAddressController @Inject()(val appConfig: FrontendAppConfig,
                                                            override val messagesApi: MessagesApi,
                                                            authenticate: AuthAction,
                                                            getData: DataRetrievalAction,
                                                            allowAccess: AllowAccessActionProvider,
                                                            requireData: DataRequiredAction,
                                                            val controllerComponents: MessagesControllerComponents,
                                                            val view: whatYouWillNeedAddress
                                                           )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController with Retrievals with I18nSupport {

  def onPageLoad(mode: Mode, index: Index, srn: SchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData() andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        PartnershipDetailsId(index).retrieve.map {
          case PartnershipDetails(partnershipName, _) =>
            val href = routes.PartnershipPostcodeLookupController.onPageLoad(mode, index, srn)
            Future.successful(Ok(view(existingSchemeName, href, srn, partnershipName, Message
            ("messages__thePartnership"))))
        }
    }
}
