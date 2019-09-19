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
import forms.register.establishers.company.director.DirectorNinoFormProvider
import identifiers.register.establishers.company.director.{DirectorNameId, DirectorNinoId}
import javax.inject.Inject
import models.{Index, Mode, Nino}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.EstablishersCompanyDirector
import utils.{Enumerable, UserAnswers}
import views.html.register.establishers.company.director.directorNino

import scala.concurrent.{ExecutionContext, Future}

class DirectorNinoController @Inject()(
                                        appConfig: FrontendAppConfig,
                                        override val messagesApi: MessagesApi,
                                        userAnswersService: UserAnswersService,
                                        @EstablishersCompanyDirector navigator: Navigator,
                                        authenticate: AuthAction,
                                        getData: DataRetrievalAction,
                                        allowAccess: AllowAccessActionProvider,
                                        requireData: DataRequiredAction,
                                        formProvider: DirectorNinoFormProvider
                                      )(implicit val ec: ExecutionContext) extends FrontendController with Retrievals with I18nSupport with Enumerable.Implicits {

  private val form: Form[Nino] = formProvider()
  private def postCall: (Mode, Index, Index, Option[String]) => Call = routes.DirectorNinoController.onSubmit _

  def onPageLoad(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
    implicit request =>
      DirectorNameId(establisherIndex, directorIndex).retrieve.right.flatMap { director =>
        DirectorNinoId(establisherIndex, directorIndex).retrieve.right.map { value =>
          Future.successful(Ok(directorNino(
            appConfig, form.fill(value), mode, establisherIndex, directorIndex, existingSchemeName, postCall(mode, establisherIndex, directorIndex, srn), srn)))
        }.left.map { _ =>
          Future.successful(Ok(directorNino(
            appConfig, form, mode, establisherIndex, directorIndex, existingSchemeName, postCall(mode, establisherIndex, directorIndex, srn), srn)))
        }
      }
  }


  def onSubmit(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      DirectorNameId(establisherIndex, directorIndex).retrieve.right.map { director =>
        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            Future.successful(BadRequest(directorNino(
              appConfig, formWithErrors, mode, establisherIndex, directorIndex, existingSchemeName, postCall(mode, establisherIndex, directorIndex, srn), srn))),
          (value) =>
            userAnswersService.save(
              mode,
              srn,
              DirectorNinoId(establisherIndex, directorIndex),
              value
            ) map { json =>
              Redirect(navigator.nextPage(DirectorNinoId(establisherIndex, directorIndex), mode, UserAnswers(json), srn))
            }
        )
      }
  }

}
