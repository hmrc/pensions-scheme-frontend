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
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import forms.register.PersonNameFormProvider
import identifiers.register.establishers.individual.EstablisherNameId
import javax.inject.Inject
import models.person.PersonName
import models.{Index, Mode}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.UserAnswers
import utils.annotations.EstablishersIndividual
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.personName

import scala.concurrent.{ExecutionContext, Future}

class EstablisherNameController @Inject()(
                                           val appConfig: FrontendAppConfig,
                                           val messagesApi: MessagesApi,
                                           val userAnswersService: UserAnswersService,
                                           val navigator: Navigator,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           allowAccess: AllowAccessActionProvider,
                                           requireData: DataRequiredAction,
                                           formProvider: PersonNameFormProvider
                                         )(implicit val ec: ExecutionContext) extends FrontendController with I18nSupport with Retrievals {

  private val form = formProvider("messages__error__establisher")

  def viewmodel(mode: Mode, index: Index, srn: Option[String]) = CommonFormWithHintViewModel(
    postCall = routes.EstablisherNameController.onSubmit(mode, index, srn),
    title = Message("messages__establisherName__title"),
    heading = Message("messages__establisherName__heading"),
    srn = srn
  )

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData) {
      implicit request =>
        val preparedForm = request.userAnswers.get[PersonName](EstablisherNameId(index)) match {
          case None => form
          case Some(value) => form.fill(value)
        }
        Ok(personName(
          appConfig, preparedForm, viewmodel(mode, index, srn), existingSchemeName))
    }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(personName(
            appConfig, formWithErrors, viewmodel(mode, index, srn), existingSchemeName))),
        value => {
          userAnswersService.save(mode, srn, EstablisherNameId(index), value).map {
            cacheMap =>
              Redirect(navigator.nextPage(EstablisherNameId(index), mode, UserAnswers(cacheMap), srn))
          }
        }
      )
  }
}
