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

package controllers.register.trustees.individual

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.register.trustees.individual.routes._
import forms.register.PersonNameFormProvider
import identifiers.register.trustees.IsTrusteeNewId
import identifiers.register.trustees.individual.TrusteeNameId
import javax.inject.Inject
import models.{Index, Mode}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsError, JsResult, JsSuccess, JsValue}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.TrusteesIndividual
import utils.{Enumerable, UserAnswers}
import views.html.register.trustees.individual.trusteeName

import scala.concurrent.{ExecutionContext, Future}

class TrusteeNameController @Inject()(appConfig: FrontendAppConfig,
                                      override val messagesApi: MessagesApi,
                                      userAnswersService: UserAnswersService,
                                      navigator: Navigator,
                                      authenticate: AuthAction,
                                      getData: DataRetrievalAction,
                                      allowAccess: AllowAccessActionProvider,
                                      requireData: DataRequiredAction,
                                      formProvider: PersonNameFormProvider
                                     )(implicit val ec: ExecutionContext) extends FrontendController with Retrievals with I18nSupport with Enumerable.Implicits {

  private val form = formProvider("messages__error__trustees")

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        val submitUrl = TrusteeNameController.onSubmit(mode, index, srn)
        val updatedForm = request.userAnswers.get(TrusteeNameId(index)).fold(form)(form.fill)
        Future.successful(Ok(trusteeName(appConfig, updatedForm, mode, index, existingSchemeName, submitUrl, srn)))
    }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) => {
          val submitUrl = TrusteeNameController.onSubmit(mode, index, srn)
          Future.successful(BadRequest(trusteeName(appConfig, formWithErrors, mode, index, existingSchemeName, submitUrl, srn)))
        },
        value => {
          val answers = request.userAnswers.set(IsTrusteeNewId(index))(true).flatMap(
            _.set(TrusteeNameId(index))(value)).asOpt.getOrElse(request.userAnswers)

          userAnswersService.upsert(mode, srn, answers.json).map { cacheMap =>
            Redirect(navigator.nextPage(TrusteeNameId(index), mode, new UserAnswers(cacheMap), srn))
          }
        }
      )
  }
}
