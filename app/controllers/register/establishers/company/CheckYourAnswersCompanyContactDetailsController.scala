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

package controllers.register.establishers.company

import controllers.Retrievals
import controllers.actions._
import controllers.helpers.CheckYourAnswersControllerHelper._
import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.establishers.company.{CompanyDetailsId, CompanyEmailId, CompanyPhoneId}
import models.Mode.checkMode
import models._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.NoSuspendedCheck
import utils.checkyouranswers.Ops._
import utils.{AllowChangeHelper, CountryOptions, UserAnswers}
import viewmodels.{AnswerSection, CYAViewModel, Message}
import views.html.checkYourAnswers

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersCompanyContactDetailsController @Inject()(override val messagesApi: MessagesApi,
                                                                authenticate: AuthAction,
                                                                getData: DataRetrievalAction,
                                                                @NoSuspendedCheck
                                                                allowAccess: AllowAccessActionProvider,
                                                                requireData: DataRequiredAction,
                                                                implicit val countryOptions: CountryOptions,
                                                                allowChangeHelper: AllowChangeHelper,
                                                                val controllerComponents: MessagesControllerComponents,
                                                                val view: checkYourAnswers
                                                               )(implicit val executionContext: ExecutionContext)
  extends FrontendBaseController with I18nSupport with Retrievals {

  def onPageLoad(mode: Mode, srn: OptionalSchemeReferenceNumber = EmptyOptionalSchemeReferenceNumber, index: Index): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        implicit val userAnswers: UserAnswers = request.userAnswers
        val notNewEstablisher = !userAnswers.get(IsEstablisherNewId(index)).getOrElse(true)
        val contactDetails = AnswerSection(
          None,
          CompanyEmailId(index).row(routes.CompanyEmailController.onPageLoad(checkMode(mode), srn, index).url, mode) ++
            CompanyPhoneId(index).row(routes.CompanyPhoneController.onPageLoad(checkMode(mode), srn, index).url, mode)
        )

        val isNew = isNewItem(mode, userAnswers, IsEstablisherNewId(index))

        val title = if (isNew) Message("checkYourAnswers.hs.title") else
          Message("messages__contactDetailsFor", Message("messages__theCompany"))

        val saveURL = mode match {
            case NormalMode =>
              Future.successful(controllers.register.establishers.routes.PsaSchemeTaskListRegistrationEstablisherController.onPageLoad(index))
            case _ =>
              Future.successful(controllers.routes.PsaSchemeTaskListController.onPageLoad(mode, srn))
          }

        saveURL.flatMap { url =>
          val vm = CYAViewModel(
            answerSections = Seq(contactDetails),
            href = url,
            schemeName = existingSchemeName,
            returnOverview = false,
            hideEditLinks = request.viewOnly || notNewEstablisher,
            srn = srn,
            hideSaveAndContinueButton = allowChangeHelper.hideSaveAndContinueButton(request, IsEstablisherNewId(index),
              mode),
            title = title,
            h1 = headingContactDetails(mode, companyName(CompanyDetailsId(index)), isNew)
          )

          Future.successful(Ok(view(vm)))
        }
    }
}
