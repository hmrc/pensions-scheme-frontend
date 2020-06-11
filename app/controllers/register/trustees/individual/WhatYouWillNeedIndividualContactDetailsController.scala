/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers.register.trustees.individual

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import identifiers.register.trustees.individual.TrusteeNameId
import javax.inject.Inject
import models.{Index, Mode}
import navigators.Navigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.{FrontendBaseController, FrontendController}
import viewmodels.Message
import views.html.register.whatYouWillNeedContactDetails

import scala.concurrent.{ExecutionContext, Future}

class WhatYouWillNeedIndividualContactDetailsController @Inject()(val appConfig: FrontendAppConfig,
                                                                  override val messagesApi: MessagesApi,
                                                                  val userAnswersService: UserAnswersService,
                                                                  val navigator: Navigator,
                                                                  authenticate: AuthAction,
                                                                  getData: DataRetrievalAction,
                                                                  allowAccess: AllowAccessActionProvider,
                                                                  requireData: DataRequiredAction,
                                                                  val
                                                                  controllerComponents: MessagesControllerComponents,
                                                                  val view: whatYouWillNeedContactDetails
                                                                 )(implicit val ec: ExecutionContext)
  extends FrontendBaseController with Retrievals with I18nSupport {

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request => {
        val nextPageHref = routes.TrusteeEmailController.onPageLoad(mode, index, srn)

        TrusteeNameId(index).retrieve.right.map {
          name =>
            Future.successful(Ok(view(
              existingSchemeName, nextPageHref, srn, name.fullName, Message("messages__theIndividual"))))
        }
      }
    }

}
