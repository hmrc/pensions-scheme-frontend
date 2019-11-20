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

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import controllers.routes._
import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.establishers.company.{CompanyEmailId, CompanyPhoneId}
import javax.inject.Inject
import models.Mode.checkMode
import models.{Index, Mode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.NoSuspendedCheck
import utils.checkyouranswers.Ops._
import utils.{AllowChangeHelper, CountryOptions, UserAnswers}
import viewmodels.{AnswerSection, CYAViewModel, Message}
import views.html.checkYourAnswers

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

  def onPageLoad(mode: Mode, srn: Option[String] = None, index: Index): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        implicit val userAnswers: UserAnswers = request.userAnswers
        val notNewEstablisher = !userAnswers.get(IsEstablisherNewId(index)).getOrElse(true)
        val contactDetails = AnswerSection(
          None,
          CompanyEmailId(index).row(routes.CompanyEmailController.onPageLoad(checkMode(mode), srn, index).url, mode) ++
            CompanyPhoneId(index).row(routes.CompanyPhoneController.onPageLoad(checkMode(mode), srn, index).url, mode)
        )

        val vm = CYAViewModel(
          answerSections = Seq(contactDetails),
          href = SchemeTaskListController.onPageLoad(mode, srn),
          schemeName = existingSchemeName,
          returnOverview = false,
          hideEditLinks = request.viewOnly || notNewEstablisher,
          srn = srn,
          hideSaveAndContinueButton = allowChangeHelper.hideSaveAndContinueButton(request, IsEstablisherNewId(index), mode),
          title = Message("checkYourAnswers.hs.title")
        )

        Future.successful(Ok(checkYourAnswers(appConfig, vm)))
    }
}
