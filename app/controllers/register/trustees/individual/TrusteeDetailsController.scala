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
import controllers.actions._
import forms.register.PersonDetailsFormProvider
import identifiers.register.trustees.individual.TrusteeDetailsId
import javax.inject.Inject
import models.{Index, Mode}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.TrusteesIndividual
import utils.{Enumerable, UserAnswers}
import views.html.register.trustees.individual.trusteeDetails

import scala.concurrent.{ExecutionContext, Future}

class TrusteeDetailsController @Inject()(
                                          appConfig: FrontendAppConfig,
                                          override val messagesApi: MessagesApi,
                                          userAnswersService: UserAnswersService,
                                          @TrusteesIndividual navigator: Navigator,
                                          authenticate: AuthAction,
                                          getData: DataRetrievalAction,
                                          allowAccess: AllowAccessActionProvider,
                                          requireData: DataRequiredAction,
                                          formProvider: PersonDetailsFormProvider
                                        )(implicit val ec: ExecutionContext) extends FrontendController with Retrievals with I18nSupport with Enumerable.Implicits {

  private val form = formProvider()

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
    implicit request =>
      val submitUrl = controllers.register.trustees.individual.routes.TrusteeDetailsController.onSubmit(mode, index, srn)
      val updatedForm = request.userAnswers.get(TrusteeDetailsId(index)).fold(form)(form.fill)
      Future.successful(Ok(trusteeDetails(appConfig, updatedForm, mode, index, existingSchemeName, submitUrl, srn)))
  }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) => {
          val submitUrl = controllers.register.trustees.individual.routes.TrusteeDetailsController.onSubmit(mode, index, srn)
          Future.successful(BadRequest(trusteeDetails(appConfig, formWithErrors, mode, index, existingSchemeName, submitUrl, srn)))
        },
        value =>
          request.userAnswers.upsert(TrusteeDetailsId(index))(value) {
            answers =>
              userAnswersService.upsert(mode, srn, answers.json).map { cacheMap =>
                Redirect(navigator.nextPage(TrusteeDetailsId(index), mode, new UserAnswers(cacheMap), srn))
              }
          }
      )
  }
}
