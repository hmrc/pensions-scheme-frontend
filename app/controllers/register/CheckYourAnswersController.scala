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

package controllers.register

import javax.inject.Inject

import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import controllers.actions._
import config.FrontendAppConfig
import identifiers.register._
import models.CheckMode
import play.api.mvc.{Action, AnyContent}
import views.html.check_your_answers
import utils.CheckYourAnswers.Ops._
import viewmodels.AnswerSection

class CheckYourAnswersController @Inject()(appConfig: FrontendAppConfig,
                                         override val messagesApi: MessagesApi,
                                         authenticate: AuthAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction) extends FrontendController with I18nSupport {

  def onPageLoad: Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>

      val schemeDetailsSection = AnswerSection(
        Some("messages__scheme_details__title"),
        SchemeDetailsId.row(controllers.register.routes.SchemeDetailsController.onPageLoad(CheckMode).url) ++
        SchemeEstablishedCountryId.row(controllers.register.routes.SchemeDetailsController.onPageLoad(CheckMode).url) ++
        InvestmentRegulatedId.row(controllers.register.routes.SchemeDetailsController.onPageLoad(CheckMode).url) ++
        OccupationalPensionSchemeId.row(controllers.register.routes.SchemeDetailsController.onPageLoad(CheckMode).url)
      )

      Ok(check_your_answers(
        appConfig,
        Seq(schemeDetailsSection),
        None,
        controllers.register.routes.CheckYourAnswersController.onPageLoad())
      )
  }
}
