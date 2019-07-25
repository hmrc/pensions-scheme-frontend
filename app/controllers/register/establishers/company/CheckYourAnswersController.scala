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

package controllers.register.establishers.company

import config.{FeatureSwitchManagementService, FrontendAppConfig}
import controllers.Retrievals
import controllers.actions._
import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.establishers.company._
import identifiers.register.establishers.company.CompanyRegistrationNumberVariationsId
import javax.inject.Inject
import models.Mode.checkMode
import models.requests.DataRequest
import models.{Index, Mode, UpdateMode}
import navigators.Navigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils._
import utils.annotations.{EstablishersCompany, NoSuspendedCheck}
import utils.checkyouranswers.Ops._
import viewmodels.AnswerSection
import views.html.check_your_answers

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject()(
                                            appConfig: FrontendAppConfig,
                                            override val messagesApi: MessagesApi,
                                            authenticate: AuthAction,
                                            getData: DataRetrievalAction,
                                            @NoSuspendedCheck allowAccess: AllowAccessActionProvider,
                                            requireData: DataRequiredAction,
                                            implicit val countryOptions: CountryOptions,
                                            @EstablishersCompany navigator: Navigator,
                                            userAnswersService: UserAnswersService,
                                            allowChangeHelper: AllowChangeHelper,
                                            fs: FeatureSwitchManagementService
                                          )(implicit val ec: ExecutionContext) extends FrontendController
  with Retrievals with I18nSupport with Enumerable.Implicits {

  def onPageLoad(mode: Mode, srn: Option[String], index: Index): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        implicit val userAnswers: UserAnswers = request.userAnswers

        lazy val isVatVariationsEnabled = userAnswers.get(IsEstablisherNewId(index)) match {
          case Some(true) => false
          case _ => fs.get(Toggles.isSeparateRefCollectionEnabled)
        }

        val companyDetails = AnswerSection(
          Some("messages__common__company_details__title"),
          CompanyDetailsId(index).row(routes.CompanyDetailsController.onPageLoad(checkMode(mode), srn, index).url, mode) ++
            (if (mode == UpdateMode && isVatVariationsEnabled) {
              CompanyVatVariationsId(index).row(routes.CompanyVatVariationsController.onPageLoad(checkMode(mode), index, srn).url, mode) ++
                CompanyPayeVariationsId(index).row(routes.CompanyPayeVariationsController.onPageLoad(checkMode(mode), index, srn).url, mode)
            } else {
              CompanyVatId(index).row(routes.CompanyVatController.onPageLoad(checkMode(mode), index, srn).url, mode) ++
                CompanyPayeId(index).row(routes.CompanyPayeController.onPageLoad(checkMode(mode), index, srn).url, mode)
            }) ++
            companyRegistrationNumberCya(mode, srn, index) ++
            CompanyUniqueTaxReferenceId(index).row(routes.CompanyUniqueTaxReferenceController.onPageLoad(checkMode(mode), srn, Index(index)).url, mode) ++
            IsCompanyDormantId(index).row(routes.IsCompanyDormantController.onPageLoad(checkMode(mode), srn, Index(index)).url, mode)
        )

        val companyContactDetails = AnswerSection(
          Some("messages__establisher_company_contact_details__title"),
          CompanyAddressId(index).row(routes.CompanyAddressController.onPageLoad(checkMode(mode), srn, Index(index)).url, mode) ++
            CompanyAddressYearsId(index).row(routes.CompanyAddressYearsController.onPageLoad(checkMode(mode), srn, index).url, mode) ++
            CompanyPreviousAddressId(index).row(routes.CompanyPreviousAddressController.onPageLoad(checkMode(mode), srn, index).url, mode) ++
            CompanyContactDetailsId(index).row(routes.CompanyContactDetailsController.onPageLoad(checkMode(mode), srn, index).url, mode)
        )

        Future.successful(Ok(check_your_answers(
          appConfig,
          Seq(companyDetails, companyContactDetails),
          routes.CheckYourAnswersController.onSubmit(mode, srn, index),
          existingSchemeName,
          mode = mode,
          hideEditLinks = request.viewOnly || !userAnswers.get(IsEstablisherNewId(index)).getOrElse(true),
          hideSaveAndContinueButton = allowChangeHelper.hideSaveAndContinueButton(request, IsEstablisherNewId(index), mode),
          srn = srn
        )))

    }

  def onSubmit(mode: Mode, srn: Option[String], index: Index): Action[AnyContent] = (
    authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      userAnswersService.setCompleteFlag(mode, srn, IsCompanyCompleteId(index), request.userAnswers, true).map { _ =>
        Redirect(navigator.nextPage(CheckYourAnswersId(index), mode, request.userAnswers, srn))
      }
  }

  private def companyRegistrationNumberCya(mode: Mode, srn: Option[String], index: Index)(implicit request: DataRequest[AnyContent]) = {
    if (mode == UpdateMode && fs.get(Toggles.isSeparateRefCollectionEnabled) &&
      !request.userAnswers.get(IsEstablisherNewId(index)).getOrElse(false))
      CompanyRegistrationNumberVariationsId(index).row(routes.CompanyRegistrationNumberVariationsController.onPageLoad(checkMode(mode), srn, index).url, mode)
    else
      CompanyRegistrationNumberId(index).row(routes.CompanyRegistrationNumberController.onPageLoad(checkMode(mode), srn, Index(index)).url, mode)
  }

}
