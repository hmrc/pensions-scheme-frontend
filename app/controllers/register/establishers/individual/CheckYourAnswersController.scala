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

package controllers.register.establishers.individual

import javax.inject.Inject
import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import identifiers.register.establishers.individual.{CheckYourAnswersId, UniqueTaxReferenceId}
import models.{CheckMode, Index, NormalMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{CheckYourAnswersFactory, Navigator}
import viewmodels.AnswerSection
import views.html.check_your_answers
import utils.CheckYourAnswers.Ops._
import utils.annotations.EstablishersIndividual

import scala.concurrent.Future

class CheckYourAnswersController @Inject() (appConfig: FrontendAppConfig,
                                            override val messagesApi: MessagesApi,
                                            authenticate: AuthAction,
                                            getData: DataRetrievalAction,
                                            requiredData: DataRequiredAction,
                                            checkYourAnswersFactory: CheckYourAnswersFactory,
                                            @EstablishersIndividual navigator: Navigator) extends FrontendController with Retrievals with I18nSupport {

  def onPageLoad(index: Index): Action[AnyContent] = (authenticate andThen getData andThen requiredData).async {
    implicit request =>
      retrieveSchemeName {
        schemeName =>
        val checkYourAnswerHelper = checkYourAnswersFactory.checkYourAnswersHelper(request.userAnswers)
        val sections = Seq(
          AnswerSection(None, checkYourAnswerHelper.establisherDetails(index.id) ++
          checkYourAnswerHelper.establisherNino(index.id) ++ UniqueTaxReferenceId(index).row(
            routes.UniqueTaxReferenceController.onPageLoad(CheckMode, Index(index)).url
          ) ++
          checkYourAnswerHelper.address(index) ++ checkYourAnswerHelper.addressYears(index) ++
          checkYourAnswerHelper.previousAddress(index) ++
          checkYourAnswerHelper.contactDetails(index))
        )
        Future.successful(Ok(check_your_answers(appConfig, sections, Some(schemeName), routes.CheckYourAnswersController.onSubmit(index))))
      }
  }

  def onSubmit(index: Index): Action[AnyContent] = (authenticate andThen getData andThen requiredData) {
    implicit request =>
      Redirect(navigator.nextPage(CheckYourAnswersId, NormalMode, request.userAnswers))
  }

}
