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

package controllers.register.establishers.company

import controllers.Retrievals
import controllers.actions._
import identifiers.register.establishers.company.CompanyDetailsId
import models.{EmptyOptionalSchemeReferenceNumber, Index, Mode, OptionalSchemeReferenceNumber}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.register.establishers.company.whatYouWillNeedCompanyAddress

import javax.inject.Inject
import scala.concurrent.Future

class WhatYouWillNeedCompanyAddressController @Inject()(override val messagesApi: MessagesApi,
                                                        authenticate: AuthAction,
                                                        getData: DataRetrievalAction,
                                                        allowAccess: AllowAccessActionProvider,
                                                        requireData: DataRequiredAction,
                                                        val view: whatYouWillNeedCompanyAddress,
                                                        val controllerComponents: MessagesControllerComponents
                                                       ) extends FrontendBaseController with I18nSupport with Retrievals {

  def onPageLoad(mode: Mode, srn: OptionalSchemeReferenceNumber = EmptyOptionalSchemeReferenceNumber, index: Index): Action[AnyContent] = (authenticate() andThen
    getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
    implicit request =>
      CompanyDetailsId(index).retrieve.map { details =>
        val href = controllers.register.establishers.company.routes.CompanyPostCodeLookupController
          .onSubmit(mode, srn, index)
        Future.successful(Ok(view(existingSchemeName, href, srn, details.companyName)))
      }
  }
}
