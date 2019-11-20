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

package controllers.register.trustees.partnership

import config.FrontendAppConfig
import controllers.actions._
import controllers.{CheckYourAnswersControllerCommon, Retrievals}
import identifiers.register.trustees.IsTrusteeNewId
import identifiers.register.trustees.partnership._
import javax.inject.Inject
import models.Mode.checkMode
import models.{Index, Mode}
import navigators.Navigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import utils.annotations.NoSuspendedCheck
import utils.checkyouranswers.Ops._
import utils.{AllowChangeHelper, CountryOptions, Enumerable, UserAnswers}
import viewmodels.{AnswerSection, CYAViewModel}
import views.html.checkYourAnswers

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersPartnershipDetailsController @Inject()(
                                                              appConfig: FrontendAppConfig,
                                                              override val messagesApi: MessagesApi,
                                                              authenticate: AuthAction,
                                                              getData: DataRetrievalAction,
                                                              @NoSuspendedCheck allowAccess: AllowAccessActionProvider,
                                                              requireData: DataRequiredAction,
                                                              implicit val countryOptions: CountryOptions,
                                                              navigator: Navigator,
                                                              userAnswersService: UserAnswersService,
                                                              allowChangeHelper: AllowChangeHelper
                                                            )(implicit val ec: ExecutionContext) extends CheckYourAnswersControllerCommon
  with Retrievals with I18nSupport with Enumerable.Implicits {

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        implicit val userAnswers: UserAnswers = request.userAnswers
        val companyDetails = Seq(AnswerSection(
          None,
          PartnershipHasUTRId(index).row(routes.PartnershipHasUTRController.onPageLoad(checkMode(mode), index, srn).url, mode) ++
            PartnershipEnterUTRId(index).row(routes.PartnershipEnterUTRController.onPageLoad(checkMode(mode), index, srn).url, mode) ++
            PartnershipNoUTRReasonId(index).row(routes.PartnershipNoUTRReasonController.onPageLoad(checkMode(mode), index, srn).url, mode) ++
            PartnershipHasVATId(index).row(routes.PartnershipHasVATController.onPageLoad(checkMode(mode), index, srn).url, mode) ++
            PartnershipEnterVATId(index).row(routes.PartnershipEnterVATController.onPageLoad(checkMode(mode), index, srn).url, mode) ++
            PartnershipHasPAYEId(index).row(routes.PartnershipHasPAYEController.onPageLoad(checkMode(mode), index, srn).url, mode) ++
            PartnershipEnterPAYEId(index).row(routes.PartnershipEnterPAYEController.onPageLoad(checkMode(mode), index, srn).url, mode)
        ))

        val vm = CYAViewModel(
          answerSections = companyDetails,
          href = controllers.routes.SchemeTaskListController.onPageLoad(mode, srn),
          schemeName = existingSchemeName,
          returnOverview = false,
          hideEditLinks = request.viewOnly || !userAnswers.get(IsTrusteeNewId(index)).getOrElse(true),
          srn = srn,
          hideSaveAndContinueButton = allowChangeHelper.hideSaveAndContinueButton(request, IsTrusteeNewId(index), mode),
          title = titlePartnershipDetails(mode),
          h1 =  headingDetails(mode, trusteePartnershipName(index))
        )

        Future.successful(Ok(checkYourAnswers( appConfig,vm )))
    }
}
