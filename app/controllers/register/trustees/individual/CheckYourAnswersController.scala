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
import identifiers.register.trustees.{IsTrusteeNewId, individual}
import javax.inject.Inject
import models.Mode.checkMode
import models.{CheckUpdateMode, Index, Mode, UpdateMode}
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
import scala.language.implicitConversions

class CheckYourAnswersController @Inject()(appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           navigator: Navigator,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           @NoSuspendedCheck allowAccess: AllowAccessActionProvider,
                                           requiredData: DataRequiredAction,
                                           userAnswersService: UserAnswersService,
                                           implicit val countryOptions: CountryOptions,
                                           allowChangeHelper: AllowChangeHelper,
                                           implicit val featureSwitchManagementService: FeatureSwitchManagementService
                                          )(implicit val ec: ExecutionContext) extends FrontendController with Retrievals with I18nSupport {

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requiredData).async {
    implicit request =>

      implicit val userAnswers: UserAnswers = request.userAnswers

      val trusteeDetailsRow = TrusteeDetailsId(index).row(routes.TrusteeDetailsController.onPageLoad(checkMode(mode), index, srn).url, mode)
      val trusteeNinoRow = mode match {
        case UpdateMode | CheckUpdateMode if !userAnswers.get(trustees.IsTrusteeNewId(index)).getOrElse(false) =>
          individual.TrusteeNewNinoId(index).row(routes.TrusteeNinoNewController.onPageLoad(checkMode(mode), index, srn).url, mode)
        case _ =>
          individual.TrusteeNinoId(index).row(routes.TrusteeNinoController.onPageLoad(checkMode(mode), index, srn).url, mode)
      }
      val trusteeUtrRow = UniqueTaxReferenceId(index).row(routes.UniqueTaxReferenceController.onPageLoad(checkMode(mode), index, srn).url, mode)
      val trusteeAddressRow = TrusteeAddressId(index).row(routes.TrusteeAddressController.onPageLoad(checkMode(mode), index, srn).url, mode)
      val trusteeAddressYearsRow = TrusteeAddressYearsId(index).row(routes.TrusteeAddressYearsController.onPageLoad(checkMode(mode), index, srn).url, mode)
      val trusteePreviousAddressRow = TrusteePreviousAddressId(index).row(
        routes.TrusteePreviousAddressController.onPageLoad(checkMode(mode), index, srn).url, mode)
      val trusteeContactDetails = TrusteeContactDetailsId(index).row(routes.TrusteeContactDetailsController.onPageLoad(checkMode(mode), index, srn).url, mode)

      val trusteeDetailsSection = AnswerSection(None,
        trusteeDetailsRow ++ trusteeNinoRow ++ trusteeUtrRow
      )
      val contactDetailsSection = AnswerSection(
        Some("messages__checkYourAnswers__section__contact_details"),
        trusteeAddressRow ++ trusteeAddressYearsRow ++ trusteePreviousAddressRow ++ trusteeContactDetails
      )

      Future.successful(Ok(checkYourAnswers(
        appConfig,
        Seq(trusteeDetailsSection, contactDetailsSection),
        controllers.register.trustees.routes.AddTrusteeController.onPageLoad(mode, srn),
        existingSchemeName,
        mode = mode,
        hideEditLinks = request.viewOnly || !userAnswers.get(IsTrusteeNewId(index)).getOrElse(true),
        hideSaveAndContinueButton = allowChangeHelper.hideSaveAndContinueButton(request, IsTrusteeNewId(index), mode),
        srn = srn
      )))
  }
}
