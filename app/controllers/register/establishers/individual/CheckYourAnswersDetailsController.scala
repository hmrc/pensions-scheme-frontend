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

package controllers.register.establishers.individual

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.helpers.CheckYourAnswersControllerHelper._
import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.establishers.individual._
import models.Mode._
import models.{Index, Mode, NormalMode, OptionalSchemeReferenceNumber}
import navigators.Navigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.NoSuspendedCheck
import utils.checkyouranswers.Ops._
import utils.{AllowChangeHelper, CountryOptions, Enumerable, UserAnswers}
import viewmodels.{AnswerSection, CYAViewModel, Message}
import views.html.checkYourAnswers

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class CheckYourAnswersDetailsController @Inject()(val appConfig: FrontendAppConfig,
                                                  override val messagesApi: MessagesApi,
                                                  val userAnswersService: UserAnswersService,
                                                  val navigator: Navigator,
                                                  authenticate: AuthAction,
                                                  getData: DataRetrievalAction,
                                                  @NoSuspendedCheck allowAccess: AllowAccessActionProvider,
                                                  allowChangeHelper: AllowChangeHelper,
                                                  requireData: DataRequiredAction,
                                                  implicit val countryOptions: CountryOptions,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  val view: checkYourAnswers
                                                 )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController with Retrievals with I18nSupport with Enumerable.Implicits {

  def onPageLoad(mode: Mode, index: Index, srn: OptionalSchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        implicit val userAnswers: UserAnswers = request.userAnswers
        val establisherIndividualDetails = Seq(AnswerSection(
          None,
          EstablisherDOBId(index).row(routes.EstablisherDOBController.onPageLoad(checkMode(mode), index, srn).url,
            mode) ++
            EstablisherHasNINOId(index).row(routes.EstablisherHasNINOController.onPageLoad(checkMode(mode), index,
              srn).url, mode) ++
            EstablisherEnterNINOId(index).row(routes.EstablisherEnterNINOController.onPageLoad(checkMode(mode),
              index, srn).url, mode) ++
            EstablisherNoNINOReasonId(index).row(routes.EstablisherNoNINOReasonController.onPageLoad(checkMode(mode),
              index, srn).url, mode) ++
            EstablisherHasUTRId(index).row(routes.EstablisherHasUTRController.onPageLoad(checkMode(mode), index, srn)
              .url, mode) ++
            EstablisherNoUTRReasonId(index).row(routes.EstablisherNoUTRReasonController.onPageLoad(checkMode(mode),
              index, srn).url, mode) ++
            EstablisherUTRId(index).row(routes.EstablisherEnterUTRController.onPageLoad(checkMode(mode), index, srn)
              .url, mode)
        ))

        val isNew = isNewItem(mode, userAnswers, IsEstablisherNewId(index))

        val title = if (isNew) Message("checkYourAnswers.hs.title") else
          Message("messages__detailsFor", Message("messages__thePerson"))

        val saveURL = mode match {
            case NormalMode =>
              Future.successful(controllers.register.establishers.routes.PsaSchemeTaskListRegistrationEstablisherController.onPageLoad(index))
            case _ =>
              Future.successful(controllers.routes.PsaSchemeTaskListController.onPageLoad(mode, srn))
          }

        saveURL.flatMap { url =>
        val vm = CYAViewModel(
          answerSections = establisherIndividualDetails,
          href = url,
          schemeName = existingSchemeName,
          returnOverview = false,
          hideEditLinks = request.viewOnly || !userAnswers.get(IsEstablisherNewId(index)).forall(identity),
          srn = srn,
          hideSaveAndContinueButton = allowChangeHelper.hideSaveAndContinueButton(request, IsEstablisherNewId(index),
            mode),
          title = title,
          h1 = headingDetails(mode, personName(EstablisherNameId(index)), isNew)
        )

        Future.successful(Ok(view(vm)))
        }
    }
}
