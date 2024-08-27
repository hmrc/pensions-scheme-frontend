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

package controllers.register.trustees.company

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.register.trustees.company.routes.HasCompanyCRNController
import identifiers.register.trustees.company.CompanyDetailsId
import javax.inject.Inject
import models.{CompanyDetails, Index, Mode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.register.trustees.company.whatYouWillNeedCompanyDetails

import scala.concurrent.Future
import models.SchemeReferenceNumber

class WhatYouWillNeedCompanyDetailsController @Inject()(appConfig: FrontendAppConfig,
                                                        override val messagesApi: MessagesApi,
                                                        authenticate: AuthAction,
                                                        getData: DataRetrievalAction,
                                                        allowAccess: AllowAccessActionProvider,
                                                        requireData: DataRequiredAction,
                                                        val controllerComponents: MessagesControllerComponents,
                                                        val view: whatYouWillNeedCompanyDetails
                                                       ) extends FrontendBaseController with I18nSupport with
  Retrievals {

  def onPageLoad(mode: Mode, index: Index, srn: SchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        CompanyDetailsId(index).retrieve.map {
          case CompanyDetails(companyName, _) =>
            Future.successful(Ok(
              view(
                schemeName = existingSchemeName,
                href = HasCompanyCRNController.onSubmit(mode, index, srn),
                srn = srn,
                companyName = companyName
              )
            )
            )
        }
    }
}
