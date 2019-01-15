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

package controllers.register.establishers.partnership.partner

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import identifiers.register.establishers.partnership.partner._
import javax.inject.Inject
import models.{CheckMode, Index, NormalMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.EstablishersPartner
import utils.checkyouranswers.Ops._
import utils.{CountryOptions, Navigator, SectionComplete}
import viewmodels.AnswerSection
import views.html.check_your_answers

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject()(appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           requiredData: DataRequiredAction,
                                           sectionComplete: SectionComplete,
                                           @EstablishersPartner navigator: Navigator,
                                           implicit val countryOptions: CountryOptions
                                          )(implicit val ec: ExecutionContext) extends FrontendController with Retrievals with I18nSupport {

  def onPageLoad(establisherIndex: Index, partnerIndex: Index): Action[AnyContent] = (authenticate andThen getData andThen requiredData).async {
    implicit request =>
      val partnerDetails = AnswerSection(
        Some("messages__partner__cya__details_heading"),
        Seq(
          PartnerDetailsId(establisherIndex, partnerIndex).
            row(routes.PartnerDetailsController.onPageLoad(CheckMode, establisherIndex, partnerIndex).url),
          PartnerNinoId(establisherIndex, partnerIndex).
            row(routes.PartnerNinoController.onPageLoad(CheckMode, establisherIndex, partnerIndex).url),
          PartnerUniqueTaxReferenceId(establisherIndex, partnerIndex).
            row(routes.PartnerUniqueTaxReferenceController.onPageLoad(CheckMode, establisherIndex, partnerIndex).url)
        ).flatten
      )

      val partnerContactDetails = AnswerSection(
        Some("messages__partner__cya__contact__details_heading"),
        Seq(
          PartnerAddressId(establisherIndex, partnerIndex).
            row(routes.PartnerAddressController.onPageLoad(CheckMode, establisherIndex, partnerIndex).url),
          PartnerAddressYearsId(establisherIndex, partnerIndex).
            row(routes.PartnerAddressYearsController.onPageLoad(CheckMode, establisherIndex, partnerIndex).url),
          PartnerPreviousAddressId(establisherIndex, partnerIndex).
            row(routes.PartnerPreviousAddressController.onPageLoad(CheckMode, establisherIndex, partnerIndex).url),
          PartnerContactDetailsId(establisherIndex, partnerIndex).
            row(routes.PartnerContactDetailsController.onPageLoad(CheckMode, establisherIndex, partnerIndex).url)
        ).flatten
      )

      Future.successful(Ok(check_your_answers(
        appConfig,
        Seq(partnerDetails, partnerContactDetails),
        routes.CheckYourAnswersController.onSubmit(establisherIndex, partnerIndex)
      )))

  }

  def onSubmit(establisherIndex: Index, partnerIndex: Index): Action[AnyContent] = (authenticate andThen getData andThen requiredData).async {
    implicit request =>
      sectionComplete.setCompleteFlag(request.externalId, IsPartnerCompleteId(establisherIndex, partnerIndex), request.userAnswers, value = true) map { _ =>
        Redirect(navigator.nextPage(CheckYourAnswersId(establisherIndex, partnerIndex), NormalMode, request.userAnswers))
      }
  }
}
