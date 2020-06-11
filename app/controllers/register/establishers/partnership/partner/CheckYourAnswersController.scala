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

package controllers.register.establishers.partnership.partner

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.helpers.CheckYourAnswersControllerHelper._
import identifiers.register.establishers.partnership.partner._
import javax.inject.Inject
import models.Mode.checkMode
import models._
import models.requests.DataRequest
import navigators.Navigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils._
import utils.annotations.NoSuspendedCheck
import utils.checkyouranswers.Ops._
import viewmodels.{AnswerSection, CYAViewModel, Message}
import views.html.checkYourAnswers

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject()(appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           @NoSuspendedCheck allowAccess: AllowAccessActionProvider,
                                           requiredData: DataRequiredAction,
                                           userAnswersService: UserAnswersService,
                                           navigator: Navigator,
                                           implicit val countryOptions: CountryOptions,
                                           allowChangeHelper: AllowChangeHelper,
                                           val controllerComponents: MessagesControllerComponents,
                                           val view: checkYourAnswers
                                          )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController with Retrievals with I18nSupport {

  private def seqAnswerSection(mode: Mode,
                               establisherIndex: Index,
                               partnerIndex: Index,
                               srn: Option[String])(implicit
                                                    request: DataRequest[AnyContent]) =
    Seq(AnswerSection(
      None,
      Seq(
        PartnerNameId(establisherIndex, partnerIndex)
          .row(routes.PartnerNameController.onPageLoad(checkMode(mode), establisherIndex, partnerIndex, srn).url,
            mode),
        PartnerDOBId(establisherIndex, partnerIndex)
          .row(routes.PartnerDOBController.onPageLoad(checkMode(mode), establisherIndex, partnerIndex, srn).url,
            mode),
        PartnerHasNINOId(establisherIndex, partnerIndex)
          .row(routes.PartnerHasNINOController.onPageLoad(checkMode(mode), establisherIndex, partnerIndex, srn)
            .url, mode),
        PartnerEnterNINOId(establisherIndex, partnerIndex)
          .row(routes.PartnerEnterNINOController.onPageLoad(checkMode(mode), establisherIndex, partnerIndex, srn)
            .url, mode),
        PartnerNoNINOReasonId(establisherIndex, partnerIndex)
          .row(routes.PartnerNoNINOReasonController.onPageLoad(checkMode(mode), establisherIndex, partnerIndex,
            srn).url, mode),
        PartnerHasUTRId(establisherIndex, partnerIndex)
          .row(routes.PartnerHasUTRController.onPageLoad(checkMode(mode), establisherIndex, partnerIndex, srn)
            .url, mode),
        PartnerEnterUTRId(establisherIndex, partnerIndex)
          .row(routes.PartnerEnterUTRController.onPageLoad(checkMode(mode), establisherIndex, partnerIndex, srn)
            .url, mode),
        PartnerNoUTRReasonId(establisherIndex, partnerIndex)
          .row(routes.PartnerNoUTRReasonController.onPageLoad(checkMode(mode), establisherIndex, partnerIndex,
            srn).url, mode),
        PartnerAddressId(establisherIndex, partnerIndex)
          .row(routes.PartnerAddressController.onPageLoad(checkMode(mode), establisherIndex, partnerIndex, srn)
            .url, mode),
        PartnerAddressYearsId(establisherIndex, partnerIndex)
          .row(routes.PartnerAddressYearsController.onPageLoad(checkMode(mode), establisherIndex, partnerIndex,
            srn).url, mode),
        PartnerPreviousAddressId(establisherIndex, partnerIndex)
          .row(routes.PartnerPreviousAddressController.onPageLoad(checkMode(mode), establisherIndex,
            partnerIndex, srn).url, mode),
        PartnerEmailId(establisherIndex, partnerIndex)
          .row(routes.PartnerEmailController.onPageLoad(checkMode(mode), establisherIndex, partnerIndex, srn)
            .url, mode),
        PartnerPhoneId(establisherIndex, partnerIndex)
          .row(routes.PartnerPhoneController.onPageLoad(checkMode(mode), establisherIndex, partnerIndex, srn)
            .url, mode)
      ).flatten
    )
    )

  def onPageLoad(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requiredData).async {
      implicit request =>
        val answers = seqAnswerSection(mode, establisherIndex, partnerIndex, srn)

        val isNew = isNewItem(mode, request.userAnswers, IsNewPartnerId(establisherIndex, partnerIndex))

        val title = if (isNew) Message("checkYourAnswers.hs.title") else
          Message("messages__detailsFor", Message("messages__thePartner").resolve)

        val vm = CYAViewModel(
          answerSections = answers,
          href = controllers.register.establishers.partnership.routes.AddPartnersController.onPageLoad(mode,
            establisherIndex, srn),
          schemeName = existingSchemeName,
          returnOverview = false,
          hideEditLinks = request.viewOnly,
          srn = srn,
          hideSaveAndContinueButton = allowChangeHelper.hideSaveAndContinueButton(request, IsNewPartnerId
          (establisherIndex, partnerIndex), mode),
          title = title,
          h1 = headingDetails(mode, personName(PartnerNameId(establisherIndex, partnerIndex)),
            isNew)
        )

        Future.successful(Ok(view(vm)))

    }
}
