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

import javax.inject.Inject
import models.Mode.checkMode
import models.{FeatureToggleName, Index, Mode, NormalMode}
import navigators.Navigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{FeatureToggleService, UserAnswersService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.NoSuspendedCheck
import utils.checkyouranswers.Ops._
import utils.{AllowChangeHelper, CountryOptions, Enumerable, UserAnswers}
import viewmodels.{AnswerSection, CYAViewModel, Message}
import views.html.checkYourAnswers

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersAddressController @Inject()(val appConfig: FrontendAppConfig,
                                                  override val messagesApi: MessagesApi,
                                                  val userAnswersService: UserAnswersService,
                                                  val navigator: Navigator,
                                                  authenticate: AuthAction,
                                                  getData: DataRetrievalAction,
                                                  @NoSuspendedCheck allowAccess: AllowAccessActionProvider,
                                                  requireData: DataRequiredAction,
                                                  implicit val countryOptions: CountryOptions,
                                                  allowChangeHelper: AllowChangeHelper,
                                                  val view: checkYourAnswers,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  featureToggleService: FeatureToggleService
                                                 )(implicit val ec: ExecutionContext)
  extends FrontendBaseController with Retrievals with I18nSupport with Enumerable.Implicits {

  def onPageLoad(mode: Mode, index: Index, srn: SchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        implicit val userAnswers: UserAnswers = request.userAnswers

        val answerSections = Seq(AnswerSection(
          None,
          AddressId(index).row(routes.AddressController.onPageLoad(checkMode(mode), index, srn).url, mode) ++
            AddressYearsId(index).row(routes.AddressYearsController.onPageLoad(checkMode(mode), index, srn).url, mode) ++
            PreviousAddressId(index).row(routes.PreviousAddressController.onPageLoad(checkMode(mode), index, srn).url, mode)
        ))

        val isNew = isNewItem(mode, userAnswers, IsEstablisherNewId(index))

        val title = if (isNew) Message("checkYourAnswers.hs.title") else
          Message("messages__addressFor", Message("messages__thePerson"))

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
            answerSections = answerSections,
            href = url,
            schemeName = existingSchemeName,
            returnOverview = false,
            hideEditLinks = request.viewOnly || !userAnswers.get(IsEstablisherNewId(index)).forall(identity),
            srn = srn,
            hideSaveAndContinueButton = allowChangeHelper.hideSaveAndContinueButton(request, IsEstablisherNewId(index), mode),
            title = title,
            h1 = headingAddressDetails(mode, personName(EstablisherNameId(index)), isNew)
          )

          Future.successful(Ok(view(vm)))
        }
    }
}
