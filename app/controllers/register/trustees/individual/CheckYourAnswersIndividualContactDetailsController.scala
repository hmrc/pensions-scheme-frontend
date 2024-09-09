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

package controllers.register.trustees.individual

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import controllers.helpers.CheckYourAnswersControllerHelper._
import identifiers.register.trustees.IsTrusteeNewId
import identifiers.register.trustees.individual.{TrusteeEmailId, TrusteeNameId, TrusteePhoneId}

import javax.inject.Inject
import models.Mode.checkMode
import models.{FeatureToggleName, Index, Mode, NormalMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.FeatureToggleService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.NoSuspendedCheck
import utils.checkyouranswers.Ops._
import utils.{AllowChangeHelper, CountryOptions, Enumerable, UserAnswers}
import viewmodels.{AnswerSection, CYAViewModel, Message}
import views.html.checkYourAnswers

import scala.concurrent.{ExecutionContext, Future}
import models.SchemeReferenceNumber
class CheckYourAnswersIndividualContactDetailsController @Inject()(val appConfig: FrontendAppConfig,
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
  extends FrontendBaseController with Retrievals with I18nSupport with Enumerable.Implicits {

  def onPageLoad(mode: Mode, index: Index, srn: SchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData() andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        implicit val userAnswers: UserAnswers = request.userAnswers
        val notNewTrustee = !userAnswers.get(IsTrusteeNewId(index)).getOrElse(true)
        val contactDetails = AnswerSection(
          None,
          TrusteeEmailId(index).row(routes.TrusteeEmailController.onPageLoad(checkMode(mode), index, srn).url, mode) ++
            TrusteePhoneId(index).row(routes.TrusteePhoneController.onPageLoad(checkMode(mode), index, srn).url, mode)
        )

        val isNew = isNewItem(mode, userAnswers, IsTrusteeNewId(index))

        val title = if (isNew) Message("checkYourAnswers.hs.title") else Message("messages__contactDetailsFor",
          Message("messages__thePerson"))

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
            answerSections = Seq(contactDetails),
            href = url,
            schemeName = existingSchemeName,
            returnOverview = false,
            hideEditLinks = request.viewOnly || notNewTrustee,
            srn = srn,
            hideSaveAndContinueButton = allowChangeHelper.hideSaveAndContinueButton(request, IsTrusteeNewId(index), mode),
            title = title,
            h1 = headingContactDetails(mode, personName(TrusteeNameId(index)), isNew)
          )

          Future.successful(Ok(view(vm)))
        }
    }
}
