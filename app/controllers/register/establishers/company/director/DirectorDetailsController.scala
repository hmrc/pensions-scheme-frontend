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
import forms.register.PersonDetailsFormProvider
import identifiers.register.establishers.IsEstablisherCompleteId
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.company.director.DirectorDetailsId
import javax.inject.Inject
import models.person.PersonDetails
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.EstablishersCompanyDirector
import utils.{Navigator, SectionComplete, UserAnswers}
import views.html.register.establishers.company.director.directorDetails

import scala.concurrent.{ExecutionContext, Future}

class DirectorDetailsController @Inject()(
                                           appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           dataCacheConnector: UserAnswersCacheConnector,
                                           @EstablishersCompanyDirector navigator: Navigator,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           formProvider: PersonDetailsFormProvider,
                                           sectionComplete: SectionComplete
                                         )(implicit val ec: ExecutionContext) extends FrontendController with Retrievals with I18nSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode, establisherIndex: Index, directorIndex: Index): Action[AnyContent] =
    (authenticate andThen getData andThen requireData).async {
      implicit request =>
        CompanyDetailsId(establisherIndex).retrieve.right.map { companyDetails =>
          val preparedForm = request.userAnswers.get[PersonDetails](DirectorDetailsId(establisherIndex, directorIndex)) match {
            case None => form
            case Some(value) => form.fill(value)
          }
          Future.successful(Ok(directorDetails(appConfig, preparedForm, mode, establisherIndex, directorIndex)))
        }
    }

  def onSubmit(mode: Mode, establisherIndex: Index, directorIndex: Index): Action[AnyContent] =
    (authenticate andThen getData andThen requireData).async {
      implicit request =>
        CompanyDetailsId(establisherIndex).retrieve.right.map { companyDetails =>
          form.bindFromRequest().fold(
            (formWithErrors: Form[_]) =>
              Future.successful(BadRequest(directorDetails(appConfig, formWithErrors, mode, establisherIndex, directorIndex)))
            ,
            value =>
              dataCacheConnector.save(request.externalId, DirectorDetailsId(establisherIndex, directorIndex), value).flatMap {
                cacheMap =>
                  val userAnswers = UserAnswers(cacheMap)
                  val allDirectors = userAnswers.allDirectorsAfterDelete(establisherIndex)
                  val allDirectorsCompleted = allDirectors.count(_.isCompleted) == allDirectors.size

                  if (allDirectorsCompleted) {
                    Future.successful(Redirect(navigator.nextPage(DirectorDetailsId(establisherIndex, directorIndex), mode, userAnswers)))
                  } else {
                    sectionComplete.setCompleteFlag(request.externalId, IsEstablisherCompleteId(establisherIndex), userAnswers, value = false).map { _ =>
                      Redirect(navigator.nextPage(DirectorDetailsId(establisherIndex, directorIndex), mode, userAnswers))
                    }
                  }
              }
          )
        }
    }
}

