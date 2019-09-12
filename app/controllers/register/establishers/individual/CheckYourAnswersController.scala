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

package controllers.register.establishers.individual

import config.{FeatureSwitchManagementService, FrontendAppConfig}
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import identifiers.register.establishers.individual._
import identifiers.register.establishers.{IsEstablisherCompleteId, IsEstablisherNewId}
import javax.inject.Inject
import models.Mode.checkMode
import models._
import models.requests.DataRequest
import navigators.Navigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.{EstablishersIndividual, NoSuspendedCheck, TaskList}
import utils.checkyouranswers.Ops._
import utils._
import viewmodels.AnswerSection
import views.html.check_your_answers_old

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject()(appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           @NoSuspendedCheck allowAccess: AllowAccessActionProvider,
                                           requiredData: DataRequiredAction,
                                           userAnswersService: UserAnswersService,
                                           implicit val countryOptions: CountryOptions,
                                           @EstablishersIndividual navigator: Navigator,
                                           allowChangeHelper: AllowChangeHelper)(implicit val ec: ExecutionContext)
  extends FrontendController with Retrievals with I18nSupport {

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requiredData).async {
    implicit request =>

      implicit val userAnswers: UserAnswers = request.userAnswers

      val establisherNinoRow = mode match {
        case UpdateMode | CheckUpdateMode if !userAnswers.get(IsEstablisherNewId(index)).getOrElse(false) =>
          EstablisherNewNinoId(index).row(routes.EstablisherNinoNewController.onPageLoad(checkMode(mode), index, srn).url, mode)
        case _ =>
          EstablisherNinoId(index).row(routes.EstablisherNinoController.onPageLoad(checkMode(mode), index, srn).url, mode)
      }

      val sections = Seq(
        AnswerSection(None,
          EstablisherDetailsId(index).row(
            controllers.register.establishers.individual.routes.EstablisherDetailsController.onPageLoad(checkMode(mode), index, srn).url, mode) ++
            establisherNinoRow ++
            UniqueTaxReferenceId(index).row(
              routes.UniqueTaxReferenceController.onPageLoad(checkMode(mode), Index(index), srn).url, mode) ++
            AddressId(index).row(
              controllers.register.establishers.individual.routes.AddressController.onPageLoad(checkMode(mode), index, srn).url, mode) ++
            AddressYearsId(index).row(
              controllers.register.establishers.individual.routes.AddressYearsController.onPageLoad(checkMode(mode), index, srn).url, mode) ++
            PreviousAddressId(index).row(
              controllers.register.establishers.individual.routes.PreviousAddressController.onPageLoad(checkMode(mode), index, srn).url, mode
            ) ++
            ContactDetailsId(index).row(
              controllers.register.establishers.individual.routes.ContactDetailsController.onPageLoad(checkMode(mode), index, srn).url, mode
            )
        )
      )

      Future.successful(
        Ok(
          check_your_answers_old(
            appConfig, sections, routes.CheckYourAnswersController.onSubmit(mode, index, srn),
            existingSchemeName,
            mode = mode,
            hideEditLinks = request.viewOnly || !userAnswers.get(IsEstablisherNewId(index)).getOrElse(true),
            hideSaveAndContinueButton = allowChangeHelper.hideSaveAndContinueButton(request, IsEstablisherNewId(index), mode),
            srn = srn
          )
        )
      )
  }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requiredData).async {
    implicit request =>
      userAnswersService.setCompleteFlag(mode, srn, IsEstablisherCompleteId(index), request.userAnswers, true).map { _ =>
        Redirect(navigator.nextPage(CheckYourAnswersId, mode, request.userAnswers, srn))
      }
  }

}
