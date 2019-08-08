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

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import identifiers.register.trustees.IsTrusteeNewId
import identifiers.register.trustees.individual._
import javax.inject.Inject
import models.Mode.checkMode
import models.{Index, Mode}
import navigators.Navigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{AllowChangeHelper, Enumerable, UserAnswers}
import utils.annotations.TrusteesIndividual
import viewmodels.AnswerSection
import utils.checkyouranswers.Ops._
import views.html.check_your_answers

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersIndividualDetailsController @Inject()(val appConfig: FrontendAppConfig,
                                                            val messagesApi: MessagesApi,
                                                            val userAnswersService: UserAnswersService,
                                                            @TrusteesIndividual val navigator: Navigator,
                                                            authenticate: AuthAction,
                                                            getData: DataRetrievalAction,
                                                            allowAccess: AllowAccessActionProvider,
                                                            allowChangeHelper: AllowChangeHelper,
                                                            requireData: DataRequiredAction
                                     )(implicit val ec: ExecutionContext) extends FrontendController with Retrievals with I18nSupport with Enumerable.Implicits {

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        implicit val userAnswers: UserAnswers = request.userAnswers
        val companyDetails = Seq(AnswerSection(
          None,
          TrusteeNameId(index).row(routes.TrusteeNameController.onPageLoad(checkMode(mode), index, srn).url, mode) ++
            TrusteeDOBId(index)
              .row(routes.TrusteeDOBController.onPageLoad(checkMode(mode), srn, index).url, mode) ++
            TrusteeHasNINOId(index).row(routes.TrusteeHasNINOController.onPageLoad(checkMode(mode), index, srn).url, mode) ++
            TrusteeNewNinoId(index).row(routes.TrusteeNinoNewController.onPageLoad(checkMode(mode), index, srn).url, mode) ++
            TrusteeNoNINOReasonId(index).row(routes.TrusteeNoNINOReasonController.onPageLoad(checkMode(mode), srn, index).url, mode) ++
            TrusteeHasUTRId(index).row(routes.TrusteeHasUTRController.onPageLoad(checkMode(mode), index, srn).url, mode) ++
            TrusteeUTRId(index).row(routes.TrusteeUTRController.onPageLoad(checkMode(mode), index, srn).url, mode) ++
            TrusteeNoUTRReasonId(index).row(routes.TrusteeNoUTRReasonController.onPageLoad(checkMode(mode), index, srn).url, mode)
          ))

        Future.successful(Ok(check_your_answers(
          appConfig,
          companyDetails,
          routes.CheckYourAnswersIndividualDetailsController.onSubmit(mode, index, srn),
          existingSchemeName,
          mode = mode,
          hideEditLinks = request.viewOnly || !userAnswers.get(IsTrusteeNewId(index)).getOrElse(true),
          hideSaveAndContinueButton = allowChangeHelper.hideSaveAndContinueButton(request, IsTrusteeNewId(index), mode),
          srn = srn
        )))

    }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] = (
    authenticate andThen getData(mode, srn) andThen requireData) {
      Redirect(controllers.routes.SchemeTaskListController.onPageLoad(mode, srn))

  }
}
