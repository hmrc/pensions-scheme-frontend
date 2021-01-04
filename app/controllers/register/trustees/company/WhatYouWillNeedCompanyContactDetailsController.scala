/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.register.trustees.company

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import identifiers.register.trustees.company.CompanyDetailsId
import javax.inject.Inject
import models.{CompanyDetails, Index, Mode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import viewmodels.Message
import views.html.register.whatYouWillNeedContactDetails

import scala.concurrent.Future

class WhatYouWillNeedCompanyContactDetailsController @Inject()(appConfig: FrontendAppConfig,
                                                               override val messagesApi: MessagesApi,
                                                               authenticate: AuthAction,
                                                               getData: DataRetrievalAction,
                                                               allowAccess: AllowAccessActionProvider,
                                                               requireData: DataRequiredAction,
                                                               val controllerComponents: MessagesControllerComponents,
                                                               val view: whatYouWillNeedContactDetails
                                                              ) extends FrontendBaseController with Retrievals with
  I18nSupport {

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        CompanyDetailsId(index).retrieve.right.map {
          case CompanyDetails(companyName, _) =>

            val href = controllers.register.trustees.company.routes.CompanyEmailController.onSubmit(mode, index, srn)
            Future.successful(Ok(view(existingSchemeName, href, srn, companyName, Message("messages__theCompany"))))
        }
    }
}
