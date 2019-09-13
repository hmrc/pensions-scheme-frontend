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

package controllers.register.establishers.company.director

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import controllers.dateOfBirth.DateOfBirthController
import forms.DOBFormProvider
import identifiers.register.establishers.company.director.{DirectorDOBId, DirectorNameId}
import javax.inject.Inject
import models.{Index, Mode}
import navigators.Navigator
import org.joda.time.LocalDate
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.EstablishersCompanyDirector
import utils.{Enumerable, UserAnswers}
import viewmodels.Message
import viewmodels.dateOfBirth.DateOfBirthViewModel
import views.html.register.DOB

import scala.concurrent.{ExecutionContext, Future}

class DirectorDOBController @Inject()(
                                       appConfig: FrontendAppConfig,
                                       override val messagesApi: MessagesApi,
                                       userAnswersService: UserAnswersService,
                                       @EstablishersCompanyDirector navigator: Navigator,
                                       authenticate: AuthAction,
                                       getData: DataRetrievalAction,
                                       allowAccess: AllowAccessActionProvider,
                                       requireData: DataRequiredAction,
                                       formProvider: DOBFormProvider
                                     )(implicit val ec: ExecutionContext) extends DateOfBirthController {

  private val form: Form[LocalDate] = formProvider()

  private def postCall: (Mode, Index, Index, Option[String]) => Call = routes.DirectorDOBController.onSubmit

  private def viewModel(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String], token: String): DateOfBirthViewModel = {
    DateOfBirthViewModel(
      postCall = postCall(mode, establisherIndex, directorIndex, srn),
      srn = srn,
      token = token
    )
  }

  def onPageLoad(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        get(DirectorDOBId(establisherIndex, directorIndex), DirectorNameId(establisherIndex, directorIndex), viewModel(mode, establisherIndex, directorIndex, srn, Message("messages__theDirector").resolve), mode)
    }

  def onSubmit(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        form.bindFromRequest().fold(
          formWithErrors =>
            DirectorNameId(establisherIndex, directorIndex).retrieve.right.map(
              personName =>
                Future.successful(BadRequest(
                  DOB(appConfig, formWithErrors, mode, existingSchemeName, postCall(mode, establisherIndex, directorIndex, srn), srn, personName.fullName, Message("messages__theDirector").resolve))
                )),

          value =>
            userAnswersService.save(mode, srn, DirectorDOBId(establisherIndex, directorIndex), value).map {
              cacheMap =>
                Redirect(navigator.nextPage(DirectorDOBId(establisherIndex, directorIndex), mode, UserAnswers(cacheMap), srn))
            }
        )
    }
}
