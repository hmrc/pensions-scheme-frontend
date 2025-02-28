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
import identifiers.register.establishers.company._
import models.Mode.checkMode
import models.{Index, Mode, NormalMode, OptionalSchemeReferenceNumber}
import navigators.Navigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils._
import utils.annotations.{EstablishersCompany, NoSuspendedCheck}
import utils.checkyouranswers.Ops._
import viewmodels.{AnswerSection, CYAViewModel, Message}
import views.html.checkYourAnswers

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersCompanyAddressController @Inject()(override val messagesApi: MessagesApi,
                                                         authenticate: AuthAction,
                                                         getData: DataRetrievalAction,
                                                         @NoSuspendedCheck allowAccess: AllowAccessActionProvider,
                                                         requireData: DataRequiredAction,
                                                         implicit val countryOptions: CountryOptions,
                                                         @EstablishersCompany navigator: Navigator,
                                                         userAnswersService: UserAnswersService,
                                                         allowChangeHelper: AllowChangeHelper,
                                                         val controllerComponents: MessagesControllerComponents,
                                                         val view: checkYourAnswers
                                                        )(implicit val executionContext: ExecutionContext) extends FrontendBaseController
  with Retrievals with I18nSupport with Enumerable.Implicits {

  def onPageLoad(mode: Mode, srn: OptionalSchemeReferenceNumber, index: Index): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        val answerSections = Seq(AnswerSection(
          None,
          CompanyAddressId(index).row(routes.CompanyAddressController.onPageLoad(checkMode(mode), srn, index).url, mode) ++
            CompanyAddressYearsId(index).row(routes.CompanyAddressYearsController.onPageLoad(checkMode(mode), srn, index).url, mode) ++
            HasBeenTradingCompanyId(index).row(routes.HasBeenTradingCompanyController.onPageLoad(checkMode(mode), srn, index).url, mode) ++
            CompanyPreviousAddressId(index).row(routes.CompanyPreviousAddressController.onPageLoad(checkMode(mode), srn, index).url, mode)
        ))

        val isNew = isNewItem(mode, request.userAnswers, IsEstablisherNewId(index))

        val title = if (isNew) Message("checkYourAnswers.hs.title") else
          Message("messages__addressFor", Message("messages__theCompany"))

        val saveURL = mode match {
            case NormalMode =>
              Future.successful(controllers.register.establishers.routes.PsaSchemeTaskListRegistrationEstablisherController.onPageLoad(index))
            case _ =>
              Future.successful(controllers.routes.PsaSchemeTaskListController.onPageLoad(mode, srn))
          }

        saveURL.flatMap { url =>
          val vm = CYAViewModel(
            answerSections = answerSections,
            href = url,
            schemeName = existingSchemeName,
            returnOverview = false,
            hideEditLinks = request.viewOnly || !request.userAnswers.get(IsEstablisherNewId(index)).forall(identity),
            srn = srn,
            hideSaveAndContinueButton = allowChangeHelper.hideSaveAndContinueButton(request, IsEstablisherNewId(index), mode),
            title = title,
            h1 = headingAddressDetails(mode, companyName(CompanyDetailsId(index)), isNew)
          )

          Future.successful(Ok(view(vm)))
        }
    }
}
