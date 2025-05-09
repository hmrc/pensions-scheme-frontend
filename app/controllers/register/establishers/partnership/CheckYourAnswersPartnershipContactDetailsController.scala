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

import controllers.Retrievals
import controllers.actions.*
import controllers.helpers.CheckYourAnswersControllerHelper.*
import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.establishers.partnership.{PartnershipDetailsId, PartnershipEmailId, PartnershipPhoneNumberId}
import models.Mode.checkMode
import models.{Index, Mode, NormalMode, OptionalSchemeReferenceNumber}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.NoSuspendedCheck
import utils.checkyouranswers.Ops.*
import utils.{AllowChangeHelper, CountryOptions}
import viewmodels.{AnswerSection, CYAViewModel, Message}
import views.html.checkYourAnswers

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersPartnershipContactDetailsController @Inject()(override val messagesApi: MessagesApi,
                                                                    authenticate: AuthAction,
                                                                    getData: DataRetrievalAction,
                                                                    @NoSuspendedCheck
                                                                    allowAccess: AllowAccessActionProvider,
                                                                    requireData: DataRequiredAction,
                                                                    implicit val countryOptions: CountryOptions,
                                                                    allowChangeHelper: AllowChangeHelper,
                                                                    val
                                                                    controllerComponents: MessagesControllerComponents,
                                                                    val view: checkYourAnswers
                                                                   )(implicit val executionContext: ExecutionContext)
  extends FrontendBaseController with Retrievals with I18nSupport {

  def onPageLoad(mode: Mode, index: Index, srn: OptionalSchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        val notNewEstablisher = !request.userAnswers.get(IsEstablisherNewId(index)).getOrElse(true)
        val contactDetailsSection = AnswerSection(
          None,
          PartnershipEmailId(index).row(routes.PartnershipEmailController.onPageLoad(checkMode(mode), index, srn)
            .url, mode) ++
            PartnershipPhoneNumberId(index).row(routes.PartnershipPhoneNumberController.onPageLoad(checkMode(mode),
              index, srn).url, mode)
        )

        val isNew = isNewItem(mode, request.userAnswers, IsEstablisherNewId(index))

        val title = if (isNew) Message("checkYourAnswers.hs.title") else
          Message("messages__contactDetailsFor", Message("messages__thePartnership"))

        val saveURL = mode match {
            case NormalMode =>
              Future.successful(controllers.register.establishers.routes.PsaSchemeTaskListRegistrationEstablisherController.onPageLoad(index))
            case _ =>
              Future.successful(controllers.routes.PsaSchemeTaskListController.onPageLoad(mode, srn))
          }
        saveURL.flatMap { url =>
          val vm = CYAViewModel(
            answerSections = Seq(contactDetailsSection),
            href = url,
            schemeName = existingSchemeName,
            returnOverview = false,
            hideEditLinks = request.viewOnly || notNewEstablisher,
            srn = srn,
            hideSaveAndContinueButton = allowChangeHelper.hideSaveAndContinueButton(request, IsEstablisherNewId(index),
              mode),
            title = title,
            h1 = headingContactDetails(mode, partnershipName(PartnershipDetailsId(index)), isNew)
          )

          Future.successful(Ok(view(vm)))
        }
    }
}
