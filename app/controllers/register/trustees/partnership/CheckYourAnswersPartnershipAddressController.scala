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
import identifiers.register.trustees.partnership.{PartnershipAddressId, PartnershipAddressYearsId, PartnershipHasBeenTradingId, PartnershipPreviousAddressId}
import javax.inject.Inject
import models.Mode.checkMode
import models.{Index, Mode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import utils.annotations.NoSuspendedCheck
import utils.checkyouranswers.Ops._
import utils.{Enumerable, _}
import viewmodels.{AnswerSection, CYAViewModel}
import views.html.checkYourAnswers

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersPartnershipAddressController @Inject()(appConfig: FrontendAppConfig,
                                                             override val messagesApi: MessagesApi,
                                                             authenticate: AuthAction,
                                                             getData: DataRetrievalAction,
                                                             @NoSuspendedCheck allowAccess: AllowAccessActionProvider,
                                                             requireData: DataRequiredAction,
                                                             implicit val countryOptions: CountryOptions,
                                                             allowChangeHelper: AllowChangeHelper
                                                            )(implicit val ec: ExecutionContext) extends CheckYourAnswersControllerCommon
  with Retrievals with I18nSupport with Enumerable.Implicits {

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        val answerSections = Seq(AnswerSection(
          None,
          PartnershipAddressId(index).row(routes.PartnershipAddressController.onPageLoad(checkMode(mode), index, srn).url, mode) ++
            PartnershipAddressYearsId(index).row(routes.PartnershipAddressYearsController.onPageLoad(checkMode(mode), index, srn).url, mode) ++
            PartnershipHasBeenTradingId(index).row(routes.PartnershipHasBeenTradingController.onPageLoad(checkMode(mode), index, srn).url, mode) ++
            PartnershipPreviousAddressId(index).row(routes.PartnershipPreviousAddressController.onPageLoad(checkMode(mode), index, srn).url, mode)
        ))

        val vm = CYAViewModel(
          answerSections = answerSections,
          href = controllers.routes.SchemeTaskListController.onPageLoad(mode, srn),
          schemeName = existingSchemeName,
          returnOverview = false,
          hideEditLinks = request.viewOnly || !request.userAnswers.get(IsTrusteeNewId(index)).getOrElse(true),
          srn = srn,
          hideSaveAndContinueButton = allowChangeHelper.hideSaveAndContinueButton(request, IsTrusteeNewId(index), mode),
          title = titlePartnershipAddressDetails(mode),
          h1 =  headingAddressDetails(mode, trusteePartnershipName(index))
        )

        Future.successful(Ok(checkYourAnswers(appConfig,vm)))

    }
}
