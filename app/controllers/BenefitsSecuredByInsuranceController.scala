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

import config.FrontendAppConfig
import controllers.actions._
import forms.BenefitsSecuredByInsuranceFormProvider
import identifiers.{BenefitsSecuredByInsuranceId, SchemeNameId}

import javax.inject.Inject
import models.{Mode, OptionalSchemeReferenceNumber, SchemeReferenceNumber}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.UserAnswers
import utils.annotations.{AboutBenefitsAndInsurance, InsuranceService}
import views.html.benefitsSecuredByInsurance

import scala.concurrent.{ExecutionContext, Future}

class BenefitsSecuredByInsuranceController @Inject()(appConfig: FrontendAppConfig,
                                                     override val messagesApi: MessagesApi,
                                                     @InsuranceService userAnswersService: UserAnswersService,
                                                     @AboutBenefitsAndInsurance navigator: Navigator,
                                                     authenticate: AuthAction,
                                                     getData: DataRetrievalAction,
                                                     allowAccess: AllowAccessActionProvider,
                                                     requireData: DataRequiredAction,
                                                     formProvider: BenefitsSecuredByInsuranceFormProvider,
                                                     val controllerComponents: MessagesControllerComponents,
                                                     val view: benefitsSecuredByInsurance
                                                    )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController with I18nSupport with Retrievals {

  val postCall: (Mode, Option[SchemeReferenceNumber]) => Call = routes.BenefitsSecuredByInsuranceController.onSubmit

  def onPageLoad(mode: Mode, srn: OptionalSchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        SchemeNameId.retrieve.map { schemeName =>
          val preparedForm = request.userAnswers.get(BenefitsSecuredByInsuranceId) match {
            case None => form(schemeName)
            case Some(value) => form(schemeName).fill(value)
          }
          Future.successful(Ok(view(preparedForm, mode, existingSchemeName, postCall(mode, srn), srn)))
        }
    }

  private def form(schemeName: String)(implicit messages: Messages): Form[Boolean] = formProvider(schemeName)

  def onSubmit(mode: Mode, srn: OptionalSchemeReferenceNumber): Action[AnyContent] = (authenticate() andThen getData(mode, srn)
    andThen requireData).async {
    implicit request =>
      SchemeNameId.retrieve.map { schemeName =>
        form(schemeName).bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            Future.successful(BadRequest(view(formWithErrors, mode, existingSchemeName, postCall(mode, srn), srn))),
          value =>
            userAnswersService.save(mode, srn, BenefitsSecuredByInsuranceId, value).map { userAnswers =>
              Redirect(navigator.nextPage(BenefitsSecuredByInsuranceId, mode, UserAnswers(userAnswers), srn))
            }
        )
      }
  }
}
