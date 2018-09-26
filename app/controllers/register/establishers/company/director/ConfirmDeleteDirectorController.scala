/*
 * Copyright 2018 HM Revenue & Customs
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
import identifiers.register.establishers.IsEstablisherCompleteId
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.company.director.{ConfirmDeleteDirectorId, DirectorDetailsId}
import javax.inject.Inject
import models.{Index, NormalMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.EstablishersCompanyDirector
import utils.{Navigator, SectionComplete}
import views.html.register.establishers.company.director.confirmDeleteDirector

import scala.concurrent.Future

class ConfirmDeleteDirectorController @Inject()(
                                                 appConfig: FrontendAppConfig,
                                                 override val messagesApi: MessagesApi,
                                                 dataCacheConnector: UserAnswersCacheConnector,
                                                 @EstablishersCompanyDirector navigator: Navigator,
                                                 authenticate: AuthAction,
                                                 getData: DataRetrievalAction,
                                                 requireData: DataRequiredAction,
                                                 sectionComplete: SectionComplete
                                               ) extends FrontendController with I18nSupport with Retrievals {

  def onPageLoad(establisherIndex: Index, directorIndex: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      (CompanyDetailsId(establisherIndex) and DirectorDetailsId(establisherIndex, directorIndex)).retrieve.right.map {
        case company ~ director =>
          director.isDeleted match {
            case false =>
              Future.successful(
                Ok(
                  confirmDeleteDirector(
                    appConfig,
                    company.companyName,
                    director.fullName,
                    routes.ConfirmDeleteDirectorController.onSubmit(establisherIndex, directorIndex),
                    AddCompanyDirectorsController.onPageLoad(NormalMode, establisherIndex)
                  )
                )
              )
            case true =>
              Future.successful(Redirect(routes.AlreadyDeletedController.onPageLoad(establisherIndex, directorIndex)))
          }
      }
  }

  def onSubmit(establisherIndex: Index, directorIndex: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      DirectorDetailsId(establisherIndex, directorIndex).retrieve.right.map {
        case directorDetails =>
          dataCacheConnector.save(DirectorDetailsId(establisherIndex, directorIndex), directorDetails.copy(isDeleted = true)).flatMap {
            userAnswers =>
              if (userAnswers.allDirectorsAfterDelete(establisherIndex).isEmpty) {
                sectionComplete.setCompleteFlag(IsEstablisherCompleteId(establisherIndex), request.userAnswers, false).map { _ =>
                  Redirect(navigator.nextPage(ConfirmDeleteDirectorId(establisherIndex), NormalMode, userAnswers))
                }
              } else {
                Future.successful(Redirect(navigator.nextPage(ConfirmDeleteDirectorId(establisherIndex), NormalMode, userAnswers)))
              }

          }
      }
  }
}
