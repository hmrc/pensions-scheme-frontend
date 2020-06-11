/*
 * Copyright 2020 HM Revenue & Customs
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
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.{FrontendBaseController, FrontendController}
import utils.annotations.NoSuspendedCheck
import utils.checkyouranswers.Ops._
import utils.{AllowChangeHelper, CountryOptions, Enumerable, UserAnswers}
import viewmodels.{AnswerSection, CYAViewModel, Message}
import views.html.checkYourAnswers
import controllers.helpers.CheckYourAnswersControllerHelper._

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersIndividualDetailsController @Inject()(
                                                             val appConfig: FrontendAppConfig,
                                                             override val messagesApi: MessagesApi,
                                                             val userAnswersService: UserAnswersService,
                                                             val navigator: Navigator,
                                                             authenticate: AuthAction,
                                                             getData: DataRetrievalAction,
                                                             @NoSuspendedCheck allowAccess: AllowAccessActionProvider,
                                                             allowChangeHelper: AllowChangeHelper,
                                                             requireData: DataRequiredAction,
                                                             implicit val countryOptions: CountryOptions,
                                                             val controllerComponents: MessagesControllerComponents,
                                                             val view: checkYourAnswers
                                                           )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController
  with Retrievals with I18nSupport with Enumerable.Implicits {

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        implicit val userAnswers: UserAnswers = request.userAnswers
        val companyDetails = Seq(AnswerSection(
          None,
          TrusteeDOBId(index).row(routes.TrusteeDOBController.onPageLoad(checkMode(mode), index, srn).url, mode) ++
            TrusteeHasNINOId(index).row(routes.TrusteeHasNINOController.onPageLoad(checkMode(mode), index, srn).url,
              mode) ++
            TrusteeEnterNINOId(index).row(routes.TrusteeEnterNINOController.onPageLoad(checkMode(mode), index, srn)
              .url, mode) ++
            TrusteeNoNINOReasonId(index).row(routes.TrusteeNoNINOReasonController.onPageLoad(checkMode(mode), index,
              srn).url, mode) ++
            TrusteeHasUTRId(index).row(routes.TrusteeHasUTRController.onPageLoad(checkMode(mode), index, srn).url,
              mode) ++
            TrusteeUTRId(index).row(routes.TrusteeEnterUTRController.onPageLoad(checkMode(mode), index, srn).url,
              mode) ++
            TrusteeNoUTRReasonId(index).row(routes.TrusteeNoUTRReasonController.onPageLoad(checkMode(mode), index,
              srn).url, mode)
        ))

        val isNew = isNewItem(mode, userAnswers, IsTrusteeNewId(index))

        val title = if (isNew) Message("checkYourAnswers.hs.title") else Message("messages__detailsFor", Message
        ("messages__thePerson").resolve)

        val vm = CYAViewModel(
          answerSections = companyDetails,
          href = controllers.routes.SchemeTaskListController.onPageLoad(mode, srn),
          schemeName = existingSchemeName,
          returnOverview = false,
          hideEditLinks = request.viewOnly || !userAnswers.get(IsTrusteeNewId(index)).forall(identity),
          srn = srn,
          hideSaveAndContinueButton = allowChangeHelper.hideSaveAndContinueButton(request, IsTrusteeNewId(index), mode),
          title = title,
          h1 = headingDetails(mode, personName(TrusteeNameId(index)), isNew)
        )

        Future.successful(Ok(view(vm)))

    }
}
