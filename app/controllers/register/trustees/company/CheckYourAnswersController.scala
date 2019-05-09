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

package controllers.register.trustees.company

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import identifiers.register.trustees.company._
import identifiers.register.trustees.{IsTrusteeCompleteId, IsTrusteeNewId}
import javax.inject.Inject
import models.Mode._
import models.{Index, Mode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils._
import utils.annotations.TrusteesCompany
import utils.checkyouranswers.Ops._
import viewmodels.AnswerSection
import views.html.check_your_answers

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

class CheckYourAnswersController @Inject()(appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           requiredData: DataRequiredAction,
                                           implicit val countryOptions: CountryOptions,
                                           @TrusteesCompany navigator: Navigator,
                                           userAnswersService: UserAnswersService
                                          )(implicit val ec: ExecutionContext) extends FrontendController with Retrievals with I18nSupport {

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requiredData).async {
    implicit request =>

      implicit val userAnswers = request.userAnswers

      val companyDetailsRow = CompanyDetailsId(index).row(routes.CompanyDetailsController.onPageLoad(checkMode(mode), index, srn).url, mode)

      val companyVatRow = CompanyVatId(index).row(routes.CompanyVatController.onPageLoad(checkMode(mode), index, srn).url, mode)

      val companyPayeRow = CompanyPayeId(index).row(routes.CompanyPayeController.onPageLoad(checkMode(mode), index, srn).url, mode)

      val companyRegistrationNumber = CompanyRegistrationNumberId(index).row(
        routes.CompanyRegistrationNumberController.onPageLoad(checkMode(mode), index, srn).url, mode
      )

      val companyUtr = CompanyUniqueTaxReferenceId(index).row(
        routes.CompanyUniqueTaxReferenceController.onPageLoad(checkMode(mode), index, srn).url, mode
      )

      val companyDetailsSection = AnswerSection(
        Some("messages__checkYourAnswers__section__company_details"),
        companyDetailsRow ++ companyVatRow ++ companyPayeRow ++ companyRegistrationNumber ++ companyUtr
      )

      val companyAddress = CompanyAddressId(index).row(
        routes.CompanyAddressController.onPageLoad(checkMode(mode), index, srn).url
      )

      val companyAddressYears = CompanyAddressYearsId(index).row(
        routes.CompanyAddressYearsController.onPageLoad(checkMode(mode), index, srn).url, mode
      )

      val companyPreviousAddress = CompanyPreviousAddressId(index).row(
        routes.CompanyPreviousAddressController.onPageLoad(checkMode(mode), index, srn).url, mode
      )

      val companyContactDetails = CompanyContactDetailsId(index).row(
        routes.CompanyContactDetailsController.onPageLoad(checkMode(mode), index, srn).url
      )

      val contactDetailsSection = AnswerSection(
        Some("messages__checkYourAnswers__section__contact_details"),
        companyAddress ++ companyAddressYears ++ companyPreviousAddress ++ companyContactDetails
      )

      Future.successful(Ok(check_your_answers(
        appConfig,
        Seq(companyDetailsSection, contactDetailsSection),
        routes.CheckYourAnswersController.onSubmit(mode, index, srn),
        existingSchemeName,
        mode = mode,
        hideEditLinks = request.viewOnly || !userAnswers.get(IsTrusteeNewId(index)).getOrElse(true)
      )))
  }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requiredData).async {
    implicit request =>
      userAnswersService.setCompleteFlag(mode, srn, IsTrusteeCompleteId(index), request.userAnswers, true).map { _ =>
        Redirect(navigator.nextPage(CheckYourAnswersId, mode, request.userAnswers, srn))
      }
  }
}
