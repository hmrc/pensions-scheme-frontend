/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.register.trustees.partnership

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import identifiers.register.trustees.partnership.PartnershipDetailsId
import javax.inject.Inject
import models.{Index, Mode, PartnershipDetails}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.Message
import views.html.register.whatYouWillNeedContactDetails

import scala.concurrent.{ExecutionContext, Future}

class WhatYouWillNeedPartnershipContactDetailsController @Inject()(
                                                                    val appConfig: FrontendAppConfig,
                                                                    override val messagesApi: MessagesApi,
                                                                    authenticate: AuthAction,
                                                                    getData: DataRetrievalAction,
                                                                    allowAccess: AllowAccessActionProvider,
                                                                    requireData: DataRequiredAction,
                                                                    val
                                                                    controllerComponents: MessagesControllerComponents,
                                                                    val view: whatYouWillNeedContactDetails
                                                                  )(implicit val executionContext: ExecutionContext)
  extends FrontendBaseController with Retrievals with I18nSupport {

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        PartnershipDetailsId(index).retrieve.map {
          case PartnershipDetails(partnershipName, _) =>
            val href = controllers.register.trustees.partnership.routes.PartnershipEmailController.onPageLoad(mode,
              index, srn)
            Future.successful(Ok(view(existingSchemeName, href, srn, partnershipName, Message
            ("messages__thePartnership"))))
        }
    }
}
