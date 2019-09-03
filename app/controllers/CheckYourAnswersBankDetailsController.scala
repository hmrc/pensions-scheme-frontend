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

package controllers

import config.FrontendAppConfig
import controllers.actions._
import identifiers._
import javax.inject.Inject
import models.{CheckMode, NormalMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.checkyouranswers.Ops._
import utils.{CountryOptions, Enumerable, SectionComplete}
import viewmodels.AnswerSection
import views.html.check_your_answers_old

import scala.concurrent.ExecutionContext

class CheckYourAnswersBankDetailsController @Inject()(appConfig: FrontendAppConfig,
                                                      override val messagesApi: MessagesApi,
                                                      authenticate: AuthAction,
                                                      getData: DataRetrievalAction,
                                                      requireData: DataRequiredAction,
                                                      implicit val countryOptions: CountryOptions,
                                                      sectionComplete: SectionComplete)(implicit val ec: ExecutionContext)
  extends FrontendController with Enumerable.Implicits with I18nSupport with Retrievals {

  def onPageLoad: Action[AnyContent] = (authenticate andThen getData() andThen requireData) {
    implicit request =>

      implicit val userAnswers = request.userAnswers

      val bankAccountSection = AnswerSection(
        None,
        UKBankAccountId.row(controllers.routes.UKBankAccountController.onPageLoad(CheckMode).url) ++
        BankAccountDetailsId.row(controllers.routes.BankAccountDetailsController.onPageLoad(CheckMode).url)
      )

      Ok(check_your_answers_old(
        appConfig,
        Seq(bankAccountSection),
        controllers.routes.CheckYourAnswersBankDetailsController.onSubmit(),
        existingSchemeName,
        hideEditLinks = request.viewOnly,
        hideSaveAndContinueButton = request.viewOnly
      ))
  }

  def onSubmit: Action[AnyContent] = (authenticate andThen getData() andThen requireData).async {
    implicit request =>
      sectionComplete.setCompleteFlag(request.externalId, IsAboutBankDetailsCompleteId, request.userAnswers, value = true) map { _ =>
        Redirect(controllers.routes.SchemeTaskListController.onPageLoad(NormalMode, None))
      }
  }

}
