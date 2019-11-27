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

import config.FrontendAppConfig
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.{CheckYourAnswersControllerCommon, Retrievals}
import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.establishers.individual.{EstablisherEmailId, EstablisherNameId, EstablisherPhoneId}
import javax.inject.Inject
import models.Mode.checkMode
import models.{Index, Mode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import utils.annotations.NoSuspendedCheck
import utils.checkyouranswers.Ops._
import utils.{AllowChangeHelper, CountryOptions, Enumerable}
import viewmodels.{AnswerSection, CYAViewModel}
import views.html.checkYourAnswers

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersContactDetailsController @Inject()(val appConfig: FrontendAppConfig,
                                                         val messagesApi: MessagesApi,
                                                         authenticate: AuthAction,
                                                         getData: DataRetrievalAction,
                                                         @NoSuspendedCheck allowAccess: AllowAccessActionProvider,
                                                         requireData: DataRequiredAction,
                                                         implicit val countryOptions: CountryOptions,
                                                         allowChangeHelper: AllowChangeHelper
                                                        )(implicit val ec: ExecutionContext) extends CheckYourAnswersControllerCommon with Retrievals with I18nSupport with Enumerable.Implicits{

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        val notNewEstablisher = !request.userAnswers.get(IsEstablisherNewId(index)).getOrElse(true)
        val contactDetails = AnswerSection(
          None,
          EstablisherEmailId(index).row(routes.EstablisherEmailController.onPageLoad(checkMode(mode), index, srn).url, mode) ++
            EstablisherPhoneId(index).row(routes.EstablisherPhoneController.onPageLoad(checkMode(mode), index, srn).url, mode)
        )

        val isNew = isNewItem(mode, request.userAnswers, IsEstablisherNewId(index))

        val vm = CYAViewModel(
          answerSections = Seq(contactDetails),
          href = controllers.routes.SchemeTaskListController.onPageLoad(mode, srn),
          schemeName = existingSchemeName,
          returnOverview = false,
          hideEditLinks = request.viewOnly || notNewEstablisher,
          srn = srn,
          hideSaveAndContinueButton = allowChangeHelper.hideSaveAndContinueButton(request, IsEstablisherNewId(index), mode),
          title = titleIndividualContactDetails(mode, isNew),
          h1 =  headingContactDetails(mode, personName(EstablisherNameId(index)), isNew)
        )

        Future.successful(Ok(checkYourAnswers(appConfig,vm)))
    }
}
