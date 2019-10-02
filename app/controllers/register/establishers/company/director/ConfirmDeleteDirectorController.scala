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

import config.{FeatureSwitchManagementService, FrontendAppConfig}
import controllers.Retrievals
import controllers.actions._
import forms.register.establishers.company.director.ConfirmDeleteDirectorFormProvider
import identifiers.register.establishers.company.director.{ConfirmDeleteDirectorId, DirectorNameId}
import javax.inject.Inject
import models.requests.DataRequest
import models.{Index, Mode}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.EstablishersCompanyDirector
import utils.{SectionComplete, UserAnswers}
import views.html.register.establishers.company.director.confirmDeleteDirector

import scala.concurrent.{ExecutionContext, Future}

class ConfirmDeleteDirectorController @Inject()(
                                                 appConfig: FrontendAppConfig,
                                                 override val messagesApi: MessagesApi,
                                                 userAnswersService: UserAnswersService,
                                                 @EstablishersCompanyDirector navigator: Navigator,
                                                 authenticate: AuthAction,
                                                 getData: DataRetrievalAction,
                                                 allowAccess: AllowAccessActionProvider,
                                                 requireData: DataRequiredAction,
                                                 sectionComplete: SectionComplete,
                                                 formProvider: ConfirmDeleteDirectorFormProvider,
                                                 fs: FeatureSwitchManagementService
                                               )(implicit val ec: ExecutionContext) extends FrontendController with I18nSupport with Retrievals {

  private val form: Form[Boolean] = formProvider()

  private def deleteDirector(establisherIndex: Index, directorIndex: Index, mode: Mode, srn: Option[String]
    )(implicit request: DataRequest[AnyContent]): Option[Future[JsValue]] = {
      request.userAnswers.get(DirectorNameId(establisherIndex, directorIndex)).map { director =>
        userAnswersService.save(mode, srn, DirectorNameId(establisherIndex, directorIndex), director.copy(isDeleted = true))
      }
  }

  def onPageLoad(establisherIndex: Index, directorIndex: Index, mode: Mode, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        DirectorNameId(establisherIndex, directorIndex).retrieve.right.map { director =>
            director.isDeleted match {
              case false =>
                Future.successful(
                  Ok(
                    confirmDeleteDirector(
                      appConfig,
                      form,
                      director.fullName,
                      routes.ConfirmDeleteDirectorController.onSubmit(establisherIndex, directorIndex, mode, srn),
                      existingSchemeName
                    )
                  )
                )
              case true =>
                Future.successful(Redirect(routes.AlreadyDeletedController.onPageLoad(establisherIndex, directorIndex, srn)))
            }
        }
    }

  def onSubmit(establisherIndex: Index, directorIndex: Index, mode: Mode, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>

      DirectorNameId(establisherIndex, directorIndex).retrieve.right.map { director =>

          form.bindFromRequest().fold(
            (formWithErrors: Form[_]) =>
              Future.successful(BadRequest(confirmDeleteDirector(
                appConfig,
                formWithErrors,
                director.fullName,
                routes.ConfirmDeleteDirectorController.onSubmit(establisherIndex, directorIndex, mode, srn),
                existingSchemeName
              ))),
            value => {
              if (value) {
                  userAnswersService.save(mode, srn, DirectorNameId(establisherIndex, directorIndex), director.copy(isDeleted = true)).flatMap { jsValue =>
                    Future.successful(Redirect(navigator.nextPage(ConfirmDeleteDirectorId(establisherIndex), mode, UserAnswers(jsValue), srn)))
                  }
              } else {
                Future.successful(Redirect(navigator.nextPage(ConfirmDeleteDirectorId(establisherIndex), mode, request.userAnswers, srn)))
              }
            }

          )
      }
  }
}
