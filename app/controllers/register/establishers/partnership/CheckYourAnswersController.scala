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
import identifiers.register.SchemeDetailsId
import identifiers.register.establishers.partnership._
import javax.inject.{Inject, Singleton}
import models.{CheckMode, Index, NormalMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.EstablisherPartnership
import utils.checkyouranswers.Ops._
import utils.{CountryOptions, Enumerable, Navigator, SectionComplete}
import viewmodels.{AnswerSection, Message}
import views.html.check_your_answers

import scala.concurrent.Future

@Singleton
class CheckYourAnswersController @Inject()(appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           requiredData: DataRequiredAction,
                                           sectionComplete: SectionComplete,
                                           @EstablisherPartnership navigator: Navigator,
                                           implicit val countryOptions: CountryOptions
                                          ) extends FrontendController with Retrievals with I18nSupport with Enumerable.Implicits {

  def onPageLoad(index: Index): Action[AnyContent] = (authenticate andThen getData andThen requiredData).async {
    implicit request =>

      SchemeDetailsId.retrieve.right.map { details =>

        val partnershipDetails = AnswerSection(
          Some("messages__partnership__checkYourAnswers__partnership_details"),
          Seq(
            PartnershipDetailsId(index).row(routes.PartnershipDetailsController.onPageLoad(CheckMode, index).url),
            PartnershipVatId(index).row(routes.PartnershipVatController.onPageLoad(CheckMode, index).url),
            PartnershipPayeId(index).row(routes.PartnershipPayeController.onPageLoad(CheckMode, index).url),
            PartnershipUniqueTaxReferenceID(index).row(routes.PartnershipUniqueTaxReferenceController.onPageLoad(CheckMode, index).url),
            IsPartnershipDormantId(index).row(routes.IsPartnershipDormantController.onPageLoad(CheckMode, index).url)
          ).flatten
        )

        val partnershipContactDetails = AnswerSection(
          Some("messages__partnership__checkYourAnswers__partnership_contact_details"),
          Seq(
            PartnershipAddressId(index).row(routes.PartnershipAddressController.onPageLoad(CheckMode, index).url),
            PartnershipAddressYearsId(index).row(routes.PartnershipAddressYearsController.onPageLoad(CheckMode, index).url),
            PartnershipPreviousAddressId(index).row(routes.PartnershipPreviousAddressController.onPageLoad(CheckMode, index).url),
            PartnershipContactDetailsId(index).row(routes.PartnershipContactDetailsController.onPageLoad(CheckMode, index).url)
          ).flatten
        )

        Future.successful(Ok(check_your_answers(
          appConfig,
          Seq(partnershipDetails, partnershipContactDetails),
          Some(Message("messages__establishers__secondaryHeading", details.schemeName)),
          routes.CheckYourAnswersController.onSubmit(index)
        )))

      }
  }

  def onSubmit(index: Index): Action[AnyContent] = (authenticate andThen getData andThen requiredData).async {
    implicit request =>
      sectionComplete.setCompleteFlag(IsPartnershipCompleteId(index), request.userAnswers, value = true) map { _ =>
        Redirect(navigator.nextPage(CheckYourAnswersId(index), NormalMode, request.userAnswers))
      }
  }

}
