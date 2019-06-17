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

package controllers.register.trustees.individual

import config.{FeatureSwitchManagementService, FrontendAppConfig}
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import identifiers.register.trustees
import identifiers.register.trustees.individual._
import identifiers.register.trustees.{IsTrusteeCompleteId, IsTrusteeNewId, individual}
import javax.inject.Inject
import models.Mode.checkMode
import models.{CheckUpdateMode, Index, Mode, UpdateMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.{NoSuspendedCheck, TrusteesIndividual}
import utils.checkyouranswers.Ops._
import utils._
import viewmodels.AnswerSection
import views.html.check_your_answers

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

class CheckYourAnswersController @Inject()(appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           @TrusteesIndividual navigator: Navigator,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           @NoSuspendedCheck allowAccess: AllowAccessActionProvider,
                                           requiredData: DataRequiredAction,
                                           userAnswersService: UserAnswersService,
                                           implicit val countryOptions: CountryOptions,
                                           allowChangeHelper: AllowChangeHelper,
                                           fs: FeatureSwitchManagementService
                                          )(implicit val ec: ExecutionContext) extends FrontendController with Retrievals with I18nSupport {

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requiredData).async {
    implicit request =>

      implicit val userAnswers: UserAnswers = request.userAnswers

      lazy val displayNewNino = userAnswers.get(trustees.IsTrusteeNewId(index)) match {
        case Some(true) => false
        case _ => fs.get(Toggles.isSeparateRefCollectionEnabled)
      }

      val trusteeDetailsRow = TrusteeDetailsId(index).row(routes.TrusteeDetailsController.onPageLoad(checkMode(mode), index, srn).url, mode)
      val trusteeNinoRow = mode match {
        case UpdateMode | CheckUpdateMode if displayNewNino => individual.TrusteeNewNinoId(index).
          row(routes.TrusteeNinoNewController.onPageLoad(checkMode(mode), index, srn).url, mode)
        case _ => individual.TrusteeNinoId(index).
          row(routes.TrusteeNinoController.onPageLoad(checkMode(mode), index, srn).url, mode)
      }
      val trusteeUtrRow = UniqueTaxReferenceId(index).row(routes.UniqueTaxReferenceController.onPageLoad(checkMode(mode), index, srn).url, mode)
      val trusteeAddressRow = TrusteeAddressId(index).row(routes.TrusteeAddressController.onPageLoad(checkMode(mode), index, srn).url, mode)
      val trusteeAddressYearsRow = TrusteeAddressYearsId(index).row(routes.TrusteeAddressYearsController.onPageLoad(checkMode(mode), index, srn).url, mode)
      val trusteePreviousAddressRow = TrusteePreviousAddressId(index).row(routes.TrusteePreviousAddressController.onPageLoad(checkMode(mode), index, srn).url, mode)
      val trusteeContactDetails = TrusteeContactDetailsId(index).row(routes.TrusteeContactDetailsController.onPageLoad(checkMode(mode), index, srn).url, mode)

      val trusteeDetailsSection = AnswerSection(None,
        trusteeDetailsRow ++ trusteeNinoRow ++ trusteeUtrRow
      )
      val contactDetailsSection = AnswerSection(
        Some("messages__checkYourAnswers__section__contact_details"),
        trusteeAddressRow ++ trusteeAddressYearsRow ++ trusteePreviousAddressRow ++ trusteeContactDetails
      )

      Future.successful(Ok(check_your_answers(
        appConfig,
        Seq(trusteeDetailsSection, contactDetailsSection),
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
      userAnswersService.setCompleteFlag(mode, srn, IsTrusteeCompleteId(index), request.userAnswers, true).map { _ =>
        Redirect(navigator.nextPage(CheckYourAnswersId, mode, request.userAnswers, srn))
      }
  }
}
