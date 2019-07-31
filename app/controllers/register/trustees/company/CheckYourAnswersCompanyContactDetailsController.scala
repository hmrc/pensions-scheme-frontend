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
import controllers.actions._
import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.trustees.IsTrusteeNewId
import identifiers.register.trustees.company.{CompanyEmailId, CompanyPhoneId, IsContactDetailsCompleteId}
import javax.inject.Inject
import models.Mode.checkMode
import models.{Index, Mode}
import navigators.Navigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.NoSuspendedCheck
import utils.checkyouranswers.Ops._
import utils.{AllowChangeHelper, CountryOptions, UserAnswers}
import viewmodels.AnswerSection
import views.html.check_your_answers
import utils.annotations.TrusteesCompany
import utils.Enumerable

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersCompanyContactDetailsController @Inject()(appConfig: FrontendAppConfig,
                                                                override val messagesApi: MessagesApi,
                                                                authenticate: AuthAction,
                                                                getData: DataRetrievalAction,
                                                                @NoSuspendedCheck allowAccess: AllowAccessActionProvider,
                                                                requireData: DataRequiredAction,
                                                                implicit val countryOptions: CountryOptions,
                                                                allowChangeHelper: AllowChangeHelper,
                                                                userAnswersService: UserAnswersService
                                                               )(implicit val ec: ExecutionContext) extends FrontendController with I18nSupport with Retrievals {

  def onPageLoad(mode: Mode, index: Index, srn: Option[String] = None): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        implicit val userAnswers: UserAnswers = request.userAnswers
        val notNewEstablisher = !userAnswers.get(IsTrusteeNewId(index)).getOrElse(true)
        val contactDetails = AnswerSection(
          None,
          CompanyEmailId(index).row(routes.CompanyEmailController.onPageLoad(checkMode(mode), index, srn).url, mode) ++
            CompanyPhoneId(index).row(routes.CompanyPhoneController.onPageLoad(checkMode(mode), index, srn).url, mode)
        )

        Future.successful(Ok(check_your_answers(
          appConfig,
          Seq(contactDetails),
          routes.CheckYourAnswersCompanyContactDetailsController.onSubmit(mode, index, srn),
          existingSchemeName,
          mode = mode,
          hideEditLinks = request.viewOnly || notNewEstablisher,
          hideSaveAndContinueButton = allowChangeHelper.hideSaveAndContinueButton(request, IsTrusteeNewId(index), mode),
          srn = srn
        )))
    }

  def onSubmit(mode: Mode, index: Index, srn: Option[String] = None): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        userAnswersService.setCompleteFlag(mode, srn, IsContactDetailsCompleteId(index), request.userAnswers, value = true).map { _ =>
          Redirect(controllers.routes.SchemeTaskListController.onPageLoad(mode, srn))
        }
    }
}
