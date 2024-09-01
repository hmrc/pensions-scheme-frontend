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

package controllers.register.trustees.partnership

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import controllers.helpers.CheckYourAnswersControllerHelper._
import identifiers.register.trustees.IsTrusteeNewId
import identifiers.register.trustees.partnership.{PartnershipDetailsId, PartnershipEmailId, PartnershipPhoneId}

import javax.inject.Inject
import models.Mode.checkMode
import models.{FeatureToggleName, Index, Mode, NormalMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.FeatureToggleService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.NoSuspendedCheck
import utils.checkyouranswers.Ops._
import utils.{AllowChangeHelper, CountryOptions, UserAnswers}
import viewmodels.{AnswerSection, CYAViewModel, Message}
import views.html.checkYourAnswers

import scala.concurrent.{ExecutionContext, Future}
import models.SchemeReferenceNumber

class CheckYourAnswersPartnershipContactDetailsController @Inject()(appConfig: FrontendAppConfig,
                                                                    override val messagesApi: MessagesApi,
                                                                    authenticate: AuthAction,
                                                                    getData: DataRetrievalAction,
                                                                    @NoSuspendedCheck
                                                                    allowAccess: AllowAccessActionProvider,
                                                                    requireData: DataRequiredAction,
                                                                    implicit val countryOptions: CountryOptions,
                                                                    allowChangeHelper: AllowChangeHelper,
                                                                    val
                                                                    controllerComponents: MessagesControllerComponents,
                                                                    val view: checkYourAnswers,
                                                                    featureToggleService: FeatureToggleService
                                                                   )(implicit val executionContext: ExecutionContext)
  extends FrontendBaseController
  with Retrievals with I18nSupport {

  def onPageLoad(mode: Mode, index: Index, srn: SchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData() andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        implicit val userAnswers: UserAnswers = request.userAnswers
        val notNewTrustee = !userAnswers.get(IsTrusteeNewId(index)).getOrElse(true)
        val contactDetailsSection = AnswerSection(
          None,
          PartnershipEmailId(index).row(routes.PartnershipEmailController.onPageLoad(checkMode(mode), index, srn)
            .url, mode) ++
            PartnershipPhoneId(index).row(routes.PartnershipPhoneNumberController.onPageLoad(checkMode(mode), index,
              srn).url, mode)
        )

        val isNew = isNewItem(mode, userAnswers, IsTrusteeNewId(index))

        val title = if (isNew) Message("checkYourAnswers.hs.title") else Message("messages__contactDetailsFor",
          Message("messages__thePartnership"))

        val saveURL = featureToggleService.get(FeatureToggleName.SchemeRegistration).map(_.isEnabled).map { isEnabled =>
          (isEnabled, mode) match {
            case (true, NormalMode) =>
              controllers.register.trustees.routes.PsaSchemeTaskListRegistrationTrusteeController.onPageLoad(index, srn)
            case _ =>
              controllers.routes.PsaSchemeTaskListController.onPageLoad(mode, srn)
          }
        }

        saveURL.flatMap { url =>
          val vm = CYAViewModel(
            answerSections = Seq(contactDetailsSection),
            href = url,
            schemeName = existingSchemeName,
            returnOverview = false,
            hideEditLinks = request.viewOnly || notNewTrustee,
            srn = srn,
            hideSaveAndContinueButton = allowChangeHelper.hideSaveAndContinueButton(request, IsTrusteeNewId(index), mode),
            title = title,
            h1 = headingContactDetails(mode, partnershipName(PartnershipDetailsId(index)), isNew)
          )

          Future.successful(Ok(view(vm)))
        }
    }
}
