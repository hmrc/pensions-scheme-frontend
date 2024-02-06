/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers

import controllers.actions._
import identifiers._
import models.AdministratorOrPractitioner.Practitioner
import models.AuthEntity.PSP
import models.Mode._
import models.requests.DataRequest
import models.{CheckUpdateMode, Mode, NormalMode, UpdateMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.NoSuspendedCheck
import utils.checkyouranswers.Ops._
import utils.{CountryOptions, Enumerable, UserAnswers}
import viewmodels.{AnswerSection, CYAViewModel, Message}
import views.html.checkYourAnswers

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class CheckYourAnswersBenefitsAndInsuranceController @Inject()(override val messagesApi: MessagesApi,
                                                               authenticate: AuthAction,
                                                               getData: DataRetrievalAction,
                                                               getPspData: PspDataRetrievalAction,
                                                               @NoSuspendedCheck allowAccess: AllowAccessActionProvider,
                                                               requireData: DataRequiredAction,
                                                               implicit val countryOptions: CountryOptions,
                                                               val controllerComponents: MessagesControllerComponents,
                                                               val view: checkYourAnswers,
                                                               psaSchemeAuthAction: PsaSchemeAuthAction,
                                                               pspSchemeAuthAction: PspSchemeAuthAction
                                                              )(implicit val executionContext: ExecutionContext)
  extends FrontendBaseController
    with Enumerable.Implicits with I18nSupport with Retrievals {

  def onPageLoad(mode: Mode, srn: Option[String]): Action[AnyContent] =
    (authenticate() andThen psaSchemeAuthAction(srn) andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData) {
      implicit request =>
        Ok(view(vm(mode, srn)))
    }

  def pspOnPageLoad(srn: String): Action[AnyContent] =
    (authenticate(Some(PSP)) andThen pspSchemeAuthAction(srn) andThen getPspData(srn) andThen allowAccess(Some(srn)) andThen requireData) {
      implicit request =>
        Ok(view(vm(UpdateMode, Some(srn))))
    }

  private def vm(mode: Mode, srn: Option[String])(implicit request: DataRequest[AnyContent]): CYAViewModel = {

      implicit val userAnswers: UserAnswers = request.userAnswers
      val benefitsAndInsuranceSection = AnswerSection(
        None,
        InvestmentRegulatedSchemeId.row(routes.InvestmentRegulatedSchemeController.onPageLoad(checkMode(mode)).url, mode) ++
          OccupationalPensionSchemeId.row(routes.OccupationalPensionSchemeController.onPageLoad(checkMode(mode)).url, mode) ++
          TypeOfBenefitsId.row(routes.TypeOfBenefitsController.onPageLoad(checkMode(mode), srn).url, mode) ++
          MoneyPurchaseBenefitsId.row(routes.MoneyPurchaseBenefitsController.onPageLoad(checkMode(mode), srn).url, mode) ++
          BenefitsSecuredByInsuranceId.row(routes.BenefitsSecuredByInsuranceController.onPageLoad(checkMode(mode), srn).url, mode) ++
          InsuranceCompanyNameId.row(routes.InsuranceCompanyNameController.onPageLoad(checkMode(mode), srn).url, mode) ++
          InsurancePolicyNumberId.row(routes.InsurancePolicyNumberController.onPageLoad(checkMode(mode), srn).url, mode) ++
          InsurerConfirmAddressId.row(routes.InsurerConfirmAddressController.onPageLoad(checkMode(mode), srn).url, mode)
      )

      val heading = (name: String) => if (mode == NormalMode) Message("checkYourAnswers.hs.title") else
        Message("messages__benefitsAndInsuranceDetailsFor", name)

      val returnToTaskListCall:Option[Call] = (request.administratorOrPractitioner, srn) match {
        case (Practitioner, Some(srn)) => Option(controllers.routes.PspSchemeTaskListController.onPageLoad(srn))
        case _ => None
      }

      CYAViewModel(
        answerSections = Seq(benefitsAndInsuranceSection),
        href = controllers.routes.PsaSchemeTaskListController.onPageLoad(mode, srn),
        schemeName = existingSchemeName,
        returnOverview = false,
        hideEditLinks = request.viewOnly,
        srn = srn,
        hideSaveAndContinueButton = mode == UpdateMode || mode == CheckUpdateMode,
        title = heading(Message("messages__theScheme")),
        h1 = heading(existingSchemeName.getOrElse(Message("messages__theScheme"))),
        anotherReturn = returnToTaskListCall
      )

  }
}
