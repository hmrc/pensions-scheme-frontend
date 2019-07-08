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
import forms.register.PersonNameFormProvider
import identifiers.register.establishers.IsEstablisherCompleteId
import identifiers.register.establishers.company.director.{DirectorNameId, IsNewDirectorId}
import javax.inject.Inject
import models.person.PersonName
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.EstablishersCompanyDirector
import utils.{Enumerable, Navigator, SectionComplete, UserAnswers}
import views.html.register.establishers.company.director.directorName

import scala.concurrent.{ExecutionContext, Future}

/* JSON:-
{
                        "directorName" : {
                            "firstName" : "Martin",
                            "lastName" : "Smart",
                        }
                    }


                    message key = directorName

 */

class DirectorNameController @Inject()(
                                        appConfig: FrontendAppConfig,
                                        override val messagesApi: MessagesApi,
                                        userAnswersService: UserAnswersService,
                                        @EstablishersCompanyDirector navigator: Navigator,
                                        authenticate: AuthAction,
                                        getData: DataRetrievalAction,
                                        allowAccess: AllowAccessActionProvider,
                                        requireData: DataRequiredAction,
                                        formProvider: PersonNameFormProvider,
                                        sectionComplete: SectionComplete
                                      )(implicit val ec: ExecutionContext) extends FrontendController with Retrievals with I18nSupport with Enumerable.Implicits {

  private val form = formProvider()

  private def postCall: (Mode, Index, Index, Option[String]) => Call = routes.DirectorNameController.onSubmit _

  def onPageLoad(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        val preparedForm = request.userAnswers.get[PersonName](DirectorNameId(establisherIndex, directorIndex)) match {
          case None => form
          case Some(value) => form.fill(value)
        }
        Future.successful(Ok(directorName(
          appConfig, preparedForm, mode, establisherIndex, directorIndex, existingSchemeName, postCall(mode, establisherIndex, directorIndex, srn), srn)))
    }

  def onSubmit(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            Future.successful(BadRequest(directorName(
              appConfig, formWithErrors, mode, establisherIndex, directorIndex, existingSchemeName, postCall(mode, establisherIndex, directorIndex, srn), srn)))
          ,
          value => {
            val answers = request.userAnswers.set(IsNewDirectorId(establisherIndex, directorIndex))(true).flatMap(
              _.set(DirectorNameId(establisherIndex, directorIndex))(value)).asOpt.getOrElse(request.userAnswers)

            userAnswersService.upsert(mode, srn, answers.json).flatMap {
              cacheMap =>
                val userAnswers = UserAnswers(cacheMap)
                val allDirectors = userAnswers.allDirectorsAfterDelete(establisherIndex)
                val allDirectorsCompleted = allDirectors.count(_.isCompleted) == allDirectors.size

                if (allDirectorsCompleted) {
                  Future.successful(Redirect(navigator.nextPage(DirectorNameId(establisherIndex, directorIndex), mode, userAnswers, srn)))
                } else {
                  userAnswers.upsert(IsEstablisherCompleteId(establisherIndex))(false) { answers =>
                    userAnswersService.upsert(mode, srn, answers.json).map { json =>
                      Redirect(navigator.nextPage(DirectorNameId(establisherIndex, directorIndex), mode, UserAnswers(json), srn))
                    }
                  }
                }
            }
          }
        )
    }
}
