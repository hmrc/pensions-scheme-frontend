/*
 * Copyright 2023 HM Revenue & Customs
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

import config.FrontendAppConfig
import controllers.actions._
import forms.InsurancePolicyNumberFormProvider
import identifiers.{InsuranceCompanyNameId, InsurancePolicyNumberId}
import javax.inject.Inject
import models.Mode
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.UserAnswers
import utils.annotations.{AboutBenefitsAndInsurance, InsuranceService}
import views.html.insurancePolicyNumber

import scala.concurrent.{ExecutionContext, Future}

class InsurancePolicyNumberController @Inject()(appConfig: FrontendAppConfig,
                                                override val messagesApi: MessagesApi,
                                                @InsuranceService userAnswersService: UserAnswersService,
                                                @AboutBenefitsAndInsurance navigator: Navigator,
                                                authenticate: AuthAction,
                                                getData: DataRetrievalAction,
                                                allowAccess: AllowAccessActionProvider,
                                                requireData: DataRequiredAction,
                                                formProvider: InsurancePolicyNumberFormProvider,
                                                val controllerComponents: MessagesControllerComponents,
                                                val view: insurancePolicyNumber
                                               )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController with I18nSupport with Retrievals {

  private val form = formProvider()

  def onPageLoad(mode: Mode, srn: Option[String]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>

        val companyName = request.userAnswers.get(InsuranceCompanyNameId)

        val preparedForm = request.userAnswers.get(InsurancePolicyNumberId) match {
          case None => form
          case Some(value) => form.fill(value)
        }
        Future.successful(Ok(view(preparedForm, mode, companyName, existingSchemeName, postCall(mode, srn), srn)))
    }

  def postCall: (Mode, Option[String]) => Call = routes.InsurancePolicyNumberController.onSubmit

  def onSubmit(mode: Mode, srn: Option[String]): Action[AnyContent] = (authenticate() andThen getData(mode, srn)
    andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) => {
          val companyName = request.userAnswers.get(InsuranceCompanyNameId)
          Future.successful(BadRequest(view(formWithErrors,
            mode, companyName, existingSchemeName, postCall(mode, srn), srn)))
        },
        value =>
          userAnswersService.save(mode, srn, InsurancePolicyNumberId, value).map(cacheMap =>
            Redirect(navigator.nextPage(InsurancePolicyNumberId, mode, UserAnswers(cacheMap), srn)))
      )
  }
}
