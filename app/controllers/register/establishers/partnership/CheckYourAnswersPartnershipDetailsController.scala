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

package controllers.register.establishers.partnership

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import controllers.helpers.CheckYourAnswersControllerHelper._
import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.establishers.partnership._

import javax.inject.Inject
import models.Mode.checkMode
import models.{FeatureToggleName, Index, Mode, NormalMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.FeatureToggleService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.NoSuspendedCheck
import utils.checkyouranswers.Ops._
import utils.{AllowChangeHelper, CountryOptions, Enumerable}
import viewmodels.{AnswerSection, CYAViewModel, Message}
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
                                                              allowChangeHelper: AllowChangeHelper,
                                                              val controllerComponents: MessagesControllerComponents,
                                                              val view: checkYourAnswers,
                                                              featureToggleService: FeatureToggleService
                                                            )(implicit val executionContext: ExecutionContext)
  extends FrontendBaseController
    with Retrievals with I18nSupport with Enumerable.Implicits {

  def onPageLoad(mode: Mode, index: Index, srn: SchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        val partnershipDetails = Seq(AnswerSection(
          None,
          PartnershipHasUTRId(index).row(routes.PartnershipHasUTRController.onPageLoad(checkMode(mode), index, srn)
            .url, mode) ++
            PartnershipEnterUTRId(index).row(routes.PartnershipEnterUTRController.onPageLoad(checkMode(mode), index,
              srn).url, mode) ++
            PartnershipNoUTRReasonId(index).row(routes.PartnershipNoUTRReasonController.onPageLoad(checkMode(mode),
              index, srn).url, mode) ++
            PartnershipHasVATId(index).row(routes.PartnershipHasVATController.onPageLoad(checkMode(mode), index, srn)
              .url, mode) ++
            PartnershipEnterVATId(index).row(routes.PartnershipEnterVATController.onPageLoad(checkMode(mode), index,
              srn).url, mode) ++
            PartnershipHasPAYEId(index).row(routes.PartnershipHasPAYEController.onPageLoad(checkMode(mode), index,
              srn).url, mode) ++
            PartnershipEnterPAYEId(index).row(routes.PartnershipEnterPAYEController.onPageLoad(checkMode(mode),
              index, srn).url, mode)
        ))

        val isNew = isNewItem(mode, request.userAnswers, IsEstablisherNewId(index))

        val title = if (isNew) Message("checkYourAnswers.hs.title") else
          Message("messages__detailsFor", Message("messages__thePartnership"))

        val saveURL = featureToggleService.get(FeatureToggleName.SchemeRegistration).map(_.isEnabled).map { isEnabled =>
          (isEnabled, mode) match {
            case (true, NormalMode) =>
              controllers.register.establishers.routes.PsaSchemeTaskListRegistrationEstablisherController.onPageLoad(index)
            case _ =>
              controllers.routes.PsaSchemeTaskListController.onPageLoad(mode, srn)
          }
        }
        saveURL.flatMap { url =>
          val vm = CYAViewModel(
            answerSections = partnershipDetails,
            href = url,
            schemeName = existingSchemeName,
            returnOverview = false,
            hideEditLinks = request.viewOnly || !request.userAnswers.get(IsEstablisherNewId(index)).forall(identity),
            srn = srn,
            hideSaveAndContinueButton = allowChangeHelper.hideSaveAndContinueButton(request, IsEstablisherNewId(index),
              mode),
            title = title,
            h1 = headingDetails(mode, partnershipName(PartnershipDetailsId(index)), isNew)
          )

          Future.successful(Ok(view(vm)))
        }
    }
}
