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

package controllers

import config.FrontendAppConfig
import controllers.actions._
import identifiers._

import javax.inject.Inject
import models.{CheckMode, Mode, NormalMode, SchemeReferenceNumber}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.checkyouranswers.Ops._
import utils.{CountryOptions, Enumerable, UserAnswers}
import viewmodels.{AnswerSection, CYAViewModel, Message}
import views.html.checkYourAnswers

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersBankDetailsController @Inject()(appConfig: FrontendAppConfig,
                                                      override val messagesApi: MessagesApi,
                                                      authenticate: AuthAction,
                                                      getData: DataRetrievalAction,
                                                      requireData: DataRequiredAction,
                                                      implicit val countryOptions: CountryOptions,
                                                      val controllerComponents: MessagesControllerComponents,
                                                      val view: checkYourAnswers
                                                     )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController with Enumerable.Implicits with I18nSupport with Retrievals {

  def onPageLoad(mode: Mode, srn: SchemeReferenceNumber): Action[AnyContent] = (authenticate() andThen getData(mode, srn) andThen requireData).async {
    implicit request =>

      implicit val userAnswers: UserAnswers = request.userAnswers

      val bankAccountSection = AnswerSection(
        None,
        UKBankAccountId.row(controllers.routes.UKBankAccountController.onPageLoad(CheckMode, srn).url) ++
          BankAccountDetailsId.row(controllers.routes.BankAccountDetailsController.onPageLoad(CheckMode, srn).url)
      )

      val vm = CYAViewModel(
        answerSections = Seq(bankAccountSection),
        href = controllers.routes.PsaSchemeTaskListController.onPageLoad(NormalMode, srn),
        schemeName = existingSchemeName,
        returnOverview = false,
        hideEditLinks = request.viewOnly,
        srn = srn,
        hideSaveAndContinueButton = request.viewOnly,
        title = Message("checkYourAnswers.hs.title"),
        h1 = Message("checkYourAnswers.hs.title")
      )

      Future.successful(Ok(view(vm)))
  }
}
