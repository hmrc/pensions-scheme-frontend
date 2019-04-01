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

package controllers.register.establishers.individual

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import identifiers.register.establishers.IsEstablisherCompleteId
import identifiers.register.establishers.individual.{CheckYourAnswersId, UniqueTaxReferenceId}
import javax.inject.Inject
import models.{CheckMode, Index, Mode, NormalMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.EstablishersIndividual
import utils.checkyouranswers.Ops._
import utils.{CheckYourAnswersFactory, Navigator, SectionComplete}
import viewmodels.AnswerSection
import views.html.check_your_answers

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject()(appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           requiredData: DataRequiredAction,
                                           checkYourAnswersFactory: CheckYourAnswersFactory,
                                           sectionComplete: SectionComplete,
                                           @EstablishersIndividual navigator: Navigator)(implicit val ec: ExecutionContext)
  extends FrontendController with Retrievals with I18nSupport {

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData andThen requiredData).async {
    implicit request =>
      val checkYourAnswerHelper = checkYourAnswersFactory.checkYourAnswersHelper(request.userAnswers)
      val sections = Seq(
        AnswerSection(None, checkYourAnswerHelper.establisherDetails(index.id) ++
          checkYourAnswerHelper.establisherNino(index.id) ++ UniqueTaxReferenceId(index).row(
          routes.UniqueTaxReferenceController.onPageLoad(CheckMode, Index(index), srn).url
        ) ++
          checkYourAnswerHelper.address(index) ++ checkYourAnswerHelper.addressYears(index) ++
          checkYourAnswerHelper.previousAddress(index) ++
          checkYourAnswerHelper.contactDetails(index))
      )
      Future.successful(Ok(check_your_answers(appConfig, sections, routes.CheckYourAnswersController.onSubmit(mode, index, srn), existingSchemeName)))
  }

  def onSubmit(mode: Mode,index: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData andThen requiredData).async {
    implicit request =>
      sectionComplete.setCompleteFlag(request.externalId, IsEstablisherCompleteId(index), request.userAnswers, true).map { _ =>
        Redirect(navigator.nextPage(CheckYourAnswersId, NormalMode, request.userAnswers))
      }
  }

}
