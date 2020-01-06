/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers.register.establishers.company

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import forms.register.establishers.company.OtherDirectorsFormProvider
import identifiers.register.establishers.company.OtherDirectorsId
import javax.inject.Inject
import models.{Index, Mode}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.EstablishersCompany
import utils.UserAnswers
import views.html.register.establishers.company.otherDirectors

import scala.concurrent.{ExecutionContext, Future}

class OtherDirectorsController @Inject()(
                                          appConfig: FrontendAppConfig,
                                          override val messagesApi: MessagesApi,
                                          userAnswersService: UserAnswersService,
                                          @EstablishersCompany navigator: Navigator,
                                          authenticate: AuthAction,
                                          getData: DataRetrievalAction,
                                          allowAccess: AllowAccessActionProvider,
                                          requireData: DataRequiredAction,
                                          formProvider: OtherDirectorsFormProvider
                                        )(implicit val ec: ExecutionContext) extends FrontendController with Retrievals with I18nSupport {

  private val form: Form[Boolean] = formProvider()
  private def postCall: (Mode, Option[String], Index) => Call = routes.OtherDirectorsController.onSubmit _

  def onPageLoad(mode: Mode, srn: Option[String], establisherIndex: Index): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
    implicit request =>
        val redirectResult = request.userAnswers.get(OtherDirectorsId(establisherIndex)) match {
          case None => Ok(otherDirectors(appConfig, form, mode, establisherIndex, existingSchemeName, postCall(mode, srn, establisherIndex), srn))
          case Some(value) => Ok(otherDirectors(appConfig, form.fill(value), mode, establisherIndex, existingSchemeName, postCall(mode, srn, establisherIndex), srn))
        }
        Future.successful(redirectResult)

  }

  def onSubmit(mode: Mode, srn: Option[String], establisherIndex: Index): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
          form.bindFromRequest().fold(
            (formWithErrors: Form[_]) =>
              Future.successful(BadRequest(
                otherDirectors(appConfig, formWithErrors, mode, establisherIndex, existingSchemeName, postCall(mode, srn, establisherIndex), srn))),
            value =>
              userAnswersService.save(mode, srn, OtherDirectorsId(establisherIndex), value).map(cacheMap =>
                Redirect(navigator.nextPage(OtherDirectorsId(establisherIndex), mode, UserAnswers(cacheMap), srn)))
          )
  }
}
