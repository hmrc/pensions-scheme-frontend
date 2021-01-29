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

package controllers.register.establishers.individual

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.register.establishers.individual.routes._
import identifiers.register.establishers.individual.EstablisherNameId
import javax.inject.Inject
import models.{Index, Mode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.register.whatYouWillNeedIndividualDetails

import scala.concurrent.Future

class WhatYouWillNeedIndividualDetailsController @Inject()(appConfig: FrontendAppConfig,
                                                           override val messagesApi: MessagesApi,
                                                           authenticate: AuthAction,
                                                           getData: DataRetrievalAction,
                                                           allowAccess: AllowAccessActionProvider,
                                                           requireData: DataRequiredAction,
                                                           val view: whatYouWillNeedIndividualDetails,
                                                           val controllerComponents: MessagesControllerComponents
                                                          ) extends FrontendBaseController with I18nSupport with
  Retrievals {
  def onPageLoad(mode: Mode, index: Index, srn: Option[String] = None): Action[AnyContent] = (authenticate() andThen
    getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
    implicit request =>
      EstablisherNameId(index).retrieve.right.map {
        details =>
          val href = EstablisherDOBController.onPageLoad(mode, index, srn)
          Future.successful(Ok(view(existingSchemeName, href, srn, details.fullName)))
      }

  }
}
