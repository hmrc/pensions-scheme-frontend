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
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import identifiers.register.establishers.partnership.partner._
import javax.inject.Inject
import models.Mode.checkMode
import models._
import navigators.Navigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils._
import utils.annotations.NoSuspendedCheck
import utils.checkyouranswers.Ops._
import viewmodels.AnswerSection
import views.html.checkYourAnswers

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject()(appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           @NoSuspendedCheck allowAccess: AllowAccessActionProvider,
                                           requiredData: DataRequiredAction,
                                           userAnswersService: UserAnswersService,
                                           navigator: Navigator,
                                           implicit val countryOptions: CountryOptions,
                                           allowChangeHelper: AllowChangeHelper
                                          )(implicit val ec: ExecutionContext) extends FrontendController with Retrievals with I18nSupport {

  def onPageLoad(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requiredData).async {
    implicit request =>
      lazy val displayNewNino = !request.userAnswers.get(IsNewPartnerId(establisherIndex, partnerIndex)).getOrElse(false)

      val partnerDetails = AnswerSection(
        Some("messages__partner__cya__details_heading"),
        Seq(
          PartnerDetailsId(establisherIndex, partnerIndex).
            row(routes.PartnerDetailsController.onPageLoad(checkMode(mode), establisherIndex, partnerIndex, srn).url, mode),
          mode match {
            case UpdateMode| CheckUpdateMode if displayNewNino => PartnerNewNinoId(establisherIndex, partnerIndex).
              row(routes.PartnerNinoNewController.onPageLoad(checkMode(mode), establisherIndex, partnerIndex, srn).url, mode)
            case _ => PartnerNinoId(establisherIndex, partnerIndex).
              row(routes.PartnerNinoController.onPageLoad(checkMode(mode), establisherIndex, partnerIndex, srn).url, mode)
          },
          PartnerUniqueTaxReferenceId(establisherIndex, partnerIndex).
            row(routes.PartnerUniqueTaxReferenceController.onPageLoad(checkMode(mode), establisherIndex, partnerIndex, srn).url, mode)
        ).flatten
      )

      val partnerContactDetails = AnswerSection(
        Some("messages__partner__cya__contact__details_heading"),
        Seq(
          PartnerAddressId(establisherIndex, partnerIndex).
            row(routes.PartnerAddressController.onPageLoad(checkMode(mode), establisherIndex, partnerIndex, srn).url),
          PartnerAddressYearsId(establisherIndex, partnerIndex).
            row(routes.PartnerAddressYearsController.onPageLoad(checkMode(mode), establisherIndex, partnerIndex, srn).url, mode),
          PartnerPreviousAddressId(establisherIndex, partnerIndex).
            row(routes.PartnerPreviousAddressController.onPageLoad(checkMode(mode), establisherIndex, partnerIndex, srn).url, mode),
          PartnerContactDetailsId(establisherIndex, partnerIndex).
            row(routes.PartnerContactDetailsController.onPageLoad(checkMode(mode), establisherIndex, partnerIndex, srn).url)
        ).flatten
      )

      val answers = Seq(AnswerSection(
        None,
        Seq(
          PartnerNameId(establisherIndex, partnerIndex)
            .row(routes.PartnerNameController.onPageLoad(checkMode(mode), establisherIndex, partnerIndex, srn).url, mode),

          PartnerDOBId(establisherIndex, partnerIndex)
            .row(routes.PartnerDOBController.onPageLoad(checkMode(mode), establisherIndex, partnerIndex, srn).url, mode),

          PartnerHasNINOId(establisherIndex, partnerIndex)
            .row(routes.PartnerHasNINOController.onPageLoad(checkMode(mode), establisherIndex, partnerIndex, srn).url, mode),

          PartnerNewNinoId(establisherIndex, partnerIndex)
            .row(routes.PartnerNinoNewController.onPageLoad(checkMode(mode), establisherIndex, partnerIndex, srn).url, mode),

          PartnerNoNINOReasonId(establisherIndex, partnerIndex)
            .row(routes.PartnerNoNINOReasonController.onPageLoad(checkMode(mode), establisherIndex, partnerIndex, srn).url, mode),

          PartnerHasUTRId(establisherIndex, partnerIndex)
            .row(routes.PartnerHasUTRController.onPageLoad(checkMode(mode), establisherIndex, partnerIndex, srn).url, mode),

          PartnerEnterUTRId(establisherIndex, partnerIndex)
            .row(routes.PartnerEnterUTRController.onPageLoad(checkMode(mode), establisherIndex, partnerIndex, srn).url, mode),

          PartnerNoUTRReasonId(establisherIndex, partnerIndex)
            .row(routes.PartnerNoUTRReasonController.onPageLoad(checkMode(mode), establisherIndex, partnerIndex, srn).url, mode),

          PartnerAddressId(establisherIndex, partnerIndex)
            .row(routes.PartnerAddressController.onPageLoad(checkMode(mode), establisherIndex, partnerIndex, srn).url, mode),

          PartnerAddressYearsId(establisherIndex, partnerIndex)
            .row(routes.PartnerAddressYearsController.onPageLoad(checkMode(mode), establisherIndex, partnerIndex, srn).url, mode),

          PartnerPreviousAddressId(establisherIndex, partnerIndex)
            .row(routes.PartnerPreviousAddressController.onPageLoad(checkMode(mode), establisherIndex, partnerIndex, srn).url, mode),

          PartnerEmailId(establisherIndex, partnerIndex)
            .row(routes.PartnerEmailController.onPageLoad(checkMode(mode), establisherIndex, partnerIndex, srn).url, mode),

          PartnerPhoneId(establisherIndex, partnerIndex)
            .row(routes.PartnerPhoneController.onPageLoad(checkMode(mode), establisherIndex, partnerIndex, srn).url, mode)
        ).flatten
      ))

      Future.successful(Ok(checkYourAnswers(
        appConfig,
        answers,
        controllers.register.establishers.partnership.routes.AddPartnersController.onPageLoad(mode, establisherIndex, srn),
        existingSchemeName,
        mode = mode,
        hideEditLinks = request.viewOnly,
        hideSaveAndContinueButton = allowChangeHelper.hideSaveAndContinueButton(request, IsNewPartnerId(establisherIndex, partnerIndex), mode),
        srn = srn
      )))

  }
}
