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

package controllers.register.establishers.partnership

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.establishers.partnership._
import javax.inject.{Inject, Singleton}
import models.{CheckMode, Index, Mode, NormalMode}
import models.Mode._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.EstablisherPartnership
import utils.checkyouranswers.Ops._
import utils._
import viewmodels.AnswerSection
import views.html.check_your_answers

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CheckYourAnswersController @Inject()(appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           requiredData: DataRequiredAction,
                                           sectionComplete: SectionComplete,
                                           @EstablisherPartnership navigator: Navigator,
                                           implicit val countryOptions: CountryOptions
                                          )(implicit val ec: ExecutionContext) extends FrontendController
  with Retrievals with I18nSupport with Enumerable.Implicits {

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requiredData).async {
    implicit request =>

      implicit val userAnswers = request.userAnswers

      val partnershipDetails = AnswerSection(
        Some("messages__partnership__checkYourAnswers__partnership_details"),
        Seq(
          PartnershipDetailsId(index).row(routes.PartnershipDetailsController.onPageLoad(checkMode(mode), index, srn).url, mode),
          PartnershipVatId(index).row(routes.PartnershipVatController.onPageLoad(checkMode(mode), index, srn).url, mode),
          PartnershipPayeId(index).row(routes.PartnershipPayeController.onPageLoad(checkMode(mode), index, srn).url, mode),
          PartnershipUniqueTaxReferenceID(index).row(routes.PartnershipUniqueTaxReferenceController.onPageLoad(checkMode(mode), index, srn).url, mode),
          IsPartnershipDormantId(index).row(routes.IsPartnershipDormantController.onPageLoad(checkMode(mode), index, srn).url, mode)
        ).flatten
      )

      val partnershipContactDetails = AnswerSection(
        Some("messages__partnership__checkYourAnswers__partnership_contact_details"),
        Seq(
          PartnershipAddressId(index).row(routes.PartnershipAddressController.onPageLoad(checkMode(mode), index, srn).url, mode),
          PartnershipAddressYearsId(index).row(routes.PartnershipAddressYearsController.onPageLoad(checkMode(mode), index, srn).url, mode),
          PartnershipPreviousAddressId(index).row(routes.PartnershipPreviousAddressController.onPageLoad(checkMode(mode), index, srn).url, mode),
          PartnershipContactDetailsId(index).row(routes.PartnershipContactDetailsController.onPageLoad(checkMode(mode), index, srn).url, mode)
        ).flatten
      )

      Future.successful(Ok(check_your_answers(
        appConfig,
        Seq(partnershipDetails, partnershipContactDetails),
        routes.CheckYourAnswersController.onSubmit(mode, index, srn),
        existingSchemeName,
        mode = mode,
        viewOnly = request.viewOnly && userAnswers.get(IsEstablisherNewId(index)).getOrElse(false)
      )))
  }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requiredData).async {
    implicit request =>
      sectionComplete.setCompleteFlag(request.externalId, IsPartnershipCompleteId(index), request.userAnswers, value = true) map { _ =>
        Redirect(navigator.nextPage(CheckYourAnswersId(index), NormalMode, request.userAnswers))
      }
  }

}
