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

package controllers.register.establishers.partnership

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import identifiers.register.establishers.partnership.PartnershipDetailsId
import javax.inject.{Inject, Singleton}
import models.Index
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.EstablisherPartnership
import utils.{Navigator, SectionComplete}
import viewmodels.{AnswerRow, AnswerSection}
import views.html.check_your_answers

import scala.concurrent.Future

@Singleton
class CheckYourAnswersController @Inject()(appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           requiredData: DataRequiredAction,
                                           sectionComplete: SectionComplete,
                                           @EstablisherPartnership navigator: Navigator
                                          ) extends FrontendController with Retrievals with I18nSupport {

  def onPageLoad(index: Index): Action[AnyContent] = (authenticate andThen getData andThen requiredData).async {
    implicit request =>

      PartnershipDetailsId(index).retrieve.right.map { details =>

        val partnershipDetails = AnswerSection(
          Some("messages__partnership__checkYourAnswers__partnership_details"),
          Seq.empty[AnswerRow]
        )

        val partnershipContactDetails = AnswerSection(
          Some("messages__partnership__checkYourAnswers__partnership_contact_details"),
          Seq.empty[AnswerRow]
        )

        Future.successful(Ok(check_your_answers(
          appConfig,
          Seq(partnershipDetails, partnershipContactDetails),
          Some(details.name),
          routes.CheckYourAnswersController.onSubmit(index)
        )))

      }
  }

  def onSubmit(index: Index): Action[AnyContent] = (authenticate andThen getData andThen requiredData) {
    implicit request =>
      ???
  }

}
