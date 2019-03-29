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
import connectors.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions._
import controllers.register.establishers.company.routes.AddCompanyDirectorsController
import forms.register.establishers.company.director.ConfirmDeleteDirectorFormProvider
import identifiers.register.establishers.IsEstablisherCompleteId
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.company.director.{ConfirmDeleteDirectorId, DirectorDetailsId}
import javax.inject.Inject
import models.{Index, Mode, NormalMode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.EstablishersCompanyDirector
import utils.{Navigator, SectionComplete}
import views.html.register.establishers.company.director.confirmDeleteDirector

import scala.concurrent.{ExecutionContext, Future}

class ConfirmDeleteDirectorController @Inject()(
                                                 appConfig: FrontendAppConfig,
                                                 override val messagesApi: MessagesApi,
                                                 dataCacheConnector: UserAnswersCacheConnector,
                                                 @EstablishersCompanyDirector navigator: Navigator,
                                                 authenticate: AuthAction,
                                                 getData: DataRetrievalAction,
                                                 requireData: DataRequiredAction,
                                                 sectionComplete: SectionComplete,
                                                 formProvider: ConfirmDeleteDirectorFormProvider
                                               ) (implicit val ec: ExecutionContext) extends FrontendController with I18nSupport with Retrievals {

  private val form: Form[Boolean] = formProvider()

  def onPageLoad(establisherIndex: Index, directorIndex: Index, mode: Mode, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData() andThen requireData).async {
    implicit request =>
      (CompanyDetailsId(establisherIndex) and DirectorDetailsId(establisherIndex, directorIndex)).retrieve.right.map {
        case company ~ director =>
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

  def onSubmit(establisherIndex: Index, directorIndex: Index, mode: Mode, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData() andThen requireData).async {
    implicit request =>

      (DirectorDetailsId(establisherIndex, directorIndex)).retrieve.right.map {
        case directorDetails  =>

          form.bindFromRequest().fold(
            (formWithErrors: Form[_]) =>
              Future.successful(BadRequest(confirmDeleteDirector(
                appConfig,
                formWithErrors,
                directorDetails.fullName,
                routes.ConfirmDeleteDirectorController.onSubmit(establisherIndex, directorIndex, mode, srn),
                existingSchemeName
              ))),
            value => {
              val deletionResult = if (value) {
                dataCacheConnector.save(DirectorDetailsId(establisherIndex, directorIndex), directorDetails.copy(isDeleted = true))
              } else {
                Future.successful(request.userAnswers)
              }
              deletionResult.flatMap {
                userAnswers =>
                  if (userAnswers.allDirectorsAfterDelete(establisherIndex).isEmpty) {
                    sectionComplete.setCompleteFlag(request.externalId, IsEstablisherCompleteId(establisherIndex), request.userAnswers, false).map { _ =>
                      Redirect(navigator.nextPage(ConfirmDeleteDirectorId(establisherIndex), NormalMode, userAnswers))
                    }
                  } else {
                    Future.successful(Redirect(navigator.nextPage(ConfirmDeleteDirectorId(establisherIndex), NormalMode, userAnswers)))
                  }
              }
            }

          )
      }
  }
}
