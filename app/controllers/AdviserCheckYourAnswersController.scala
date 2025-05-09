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

import controllers.actions._
import identifiers._
import models.{CheckMode, EmptyOptionalSchemeReferenceNumber, NormalMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.checkyouranswers.Ops._
import utils.{CountryOptions, UserAnswers}
import viewmodels.{AnswerSection, CYAViewModel, Message}
import views.html.checkYourAnswers

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdviserCheckYourAnswersController @Inject()(override val messagesApi: MessagesApi,
                                                  authenticate: AuthAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  implicit val countryOptions: CountryOptions,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  val view: checkYourAnswers
                                                 )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController with I18nSupport with Retrievals {

  def onPageLoad: Action[AnyContent] = (authenticate() andThen getData() andThen requireData).async {
    implicit request =>
      implicit val userAnswers: UserAnswers = request.userAnswers

      val seqAnswerSection = {
        val adviserNameRow = AdviserNameId.row(routes.AdviserNameController.onPageLoad(CheckMode).url)
        val adviserEmailRow = AdviserEmailId.row(routes.AdviserEmailAddressController.onPageLoad(CheckMode).url)
        val adviserPhoneRow = AdviserPhoneId.row(routes.AdviserPhoneController.onPageLoad(CheckMode).url)
        val adviserAddressRow = AdviserAddressId.row(routes.AdviserAddressController.onPageLoad(CheckMode).url)
        Seq(AnswerSection(None, adviserNameRow ++ adviserEmailRow ++ adviserPhoneRow ++ adviserAddressRow))
      }
      val vm = CYAViewModel(
        answerSections = seqAnswerSection,
        href = controllers.routes.PsaSchemeTaskListController.onPageLoad(NormalMode, EmptyOptionalSchemeReferenceNumber),
        schemeName = existingSchemeName,
        returnOverview = false,
        hideEditLinks = request.viewOnly,
        srn = EmptyOptionalSchemeReferenceNumber,
        hideSaveAndContinueButton = request.viewOnly,
        title = Message("checkYourAnswers.hs.title"),
        h1 = Message("checkYourAnswers.hs.title")
      )

      Future.successful(Ok(view(vm)))
  }
}
