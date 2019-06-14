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

package controllers.register.trustees.partnership

import config.{FeatureSwitchManagementService, FrontendAppConfig}
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import identifiers.register.trustees.IsTrusteeNewId
import identifiers.register.trustees.partnership._
import javax.inject.{Inject, Singleton}
import models.Mode._
import models.requests.DataRequest
import models.{Index, Mode, UpdateMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils._
import utils.annotations.{NoSuspendedCheck, TrusteesPartnership}
import utils.checkyouranswers.Ops._
import viewmodels.{AnswerRow, AnswerSection}
import views.html.check_your_answers

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CheckYourAnswersController @Inject()(appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           @NoSuspendedCheck allowAccess: AllowAccessActionProvider,
                                           requiredData: DataRequiredAction,
                                           userAnswersService: UserAnswersService,
                                           @TrusteesPartnership navigator: Navigator,
                                           implicit val countryOptions: CountryOptions,
                                           allowChangeHelper: AllowChangeHelper,
                                           fs: FeatureSwitchManagementService
                                          )(implicit val ec: ExecutionContext) extends FrontendController with Retrievals with I18nSupport {

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requiredData).async {
      implicit request =>

        implicit val userAnswers: UserAnswers = request.userAnswers

        lazy val isVatVariationsEnabled = userAnswers.get(IsTrusteeNewId(index)) match {
          case Some(true) => false
          case _ => fs.get(Toggles.separateRefCollectionEnabled)
        }

        val partnershipDetails = AnswerSection(
          Some("messages__partnership__checkYourAnswers__partnership_details"),
          PartnershipDetailsId(index).row(routes.TrusteeDetailsController.onPageLoad(checkMode(mode), index, srn).url, mode) ++
            (if (mode == UpdateMode && isVatVariationsEnabled) {
              PartnershipVatVariationsId(index).row(routes.PartnershipVatVariationsController.onPageLoad(checkMode(mode), index, srn).url, mode) ++
                PartnershipPayeVariationsId(index).row(routes.PartnershipPayeVariationsController.onPageLoad(checkMode(mode), index, srn).url, mode)
            } else {
              PartnershipVatId(index).row(routes.PartnershipVatController.onPageLoad(checkMode(mode), index, srn).url, mode) ++
                PartnershipPayeId(index).row(routes.PartnershipPayeController.onPageLoad(checkMode(mode), index, srn).url, mode)
            }) ++
            PartnershipUniqueTaxReferenceId(index).row(routes.PartnershipUniqueTaxReferenceController.onPageLoad(checkMode(mode), index, srn).url, mode)
        )

        val partnershipContactDetails = AnswerSection(
          Some("messages__partnership__checkYourAnswers__partnership_contact_details"),
          Seq(
            PartnershipAddressId(index).row(routes.PartnershipAddressController.onPageLoad(checkMode(mode), index, srn).url),
            PartnershipAddressYearsId(index).row(routes.PartnershipAddressYearsController.onPageLoad(checkMode(mode), index, srn).url, mode),
            PartnershipPreviousAddressId(index).row(routes.PartnershipPreviousAddressController.onPageLoad(checkMode(mode), index, srn).url, mode),
            PartnershipContactDetailsId(index).row(routes.PartnershipContactDetailsController.onPageLoad(checkMode(mode), index, srn).url)
          ).flatten
        )

        Future.successful(Ok(check_your_answers(
          appConfig,
          Seq(partnershipDetails, partnershipContactDetails),
          routes.CheckYourAnswersController.onSubmit(mode, index, srn),
          existingSchemeName,
          mode = mode,
          hideEditLinks = request.viewOnly || !userAnswers.get(IsTrusteeNewId(index)).getOrElse(true),
          hideSaveAndContinueButton = allowChangeHelper.hideSaveAndContinueButton(request, IsTrusteeNewId(index), mode),
          srn = srn
        )))
    }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requiredData).async {
    implicit request =>
      userAnswersService.setCompleteFlag(mode, srn, IsPartnershipCompleteId(index), request.userAnswers, value = true) map { _ =>
        Redirect(navigator.nextPage(CheckYourAnswersId(index), mode, request.userAnswers, srn))
      }
  }
}
