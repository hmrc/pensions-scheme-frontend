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

package controllers.register.trustees.partnership

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import identifiers.register.trustees.partnership.PartnershipDetailsId
import javax.inject.Inject
import models.{Index, Mode, PartnershipDetails}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.register.whatYouWillNeedContactDetails

import scala.concurrent.{ExecutionContext, Future}

class WhatYouWillNeedPartnershipContactDetailsController @Inject()(val appConfig: FrontendAppConfig,
                                                                   val messagesApi: MessagesApi,
                                                                   authenticate: AuthAction,
                                                                   getData: DataRetrievalAction,
                                                                   allowAccess: AllowAccessActionProvider,
                                                                   requireData: DataRequiredAction
                                                                  )(implicit val ec: ExecutionContext) extends FrontendController with Retrievals with I18nSupport {

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        PartnershipDetailsId(index).retrieve.right.map {
          case PartnershipDetails(partnershipName, _) =>
            val nextPageHref = controllers.register.trustees.partnership.routes.PartnershipEmailController.onPageLoad(mode, index, srn)

            Future.successful(Ok(whatYouWillNeedContactDetails(appConfig, existingSchemeName, nextPageHref, srn, partnershipName)))
        }
    }
}
