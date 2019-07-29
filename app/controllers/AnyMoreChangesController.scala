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

package controllers

import config.FrontendAppConfig
import controllers.actions._
import forms.AnyMoreChangesFormProvider
import identifiers.AnyMoreChangesId
import javax.inject.Inject
import models.UpdateMode
import navigators.Navigator
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.Variations
import views.html.anyMoreChanges

import scala.concurrent.{ExecutionContext, Future}

class AnyMoreChangesController @Inject()(appConfig: FrontendAppConfig,
                                         override val messagesApi: MessagesApi,
                                         @Variations navigator: Navigator,
                                         authenticate: AuthAction,
                                         allowAccess: AllowAccessActionProvider,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: AnyMoreChangesFormProvider)(implicit val ec: ExecutionContext)
  extends FrontendController with Retrievals with I18nSupport {

  private val form: Form[Boolean] = formProvider()
  private val postCall = controllers.routes.AnyMoreChangesController.onSubmit _

  def onPageLoad(srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(UpdateMode, srn) andThen allowAccess(srn) andThen requireData).async {
    implicit request =>
      Future.successful(Ok(anyMoreChanges(appConfig, form, existingSchemeName, dateToCompleteDeclaration, postCall(srn), srn)))
  }

  def onSubmit(srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(UpdateMode, srn) andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(anyMoreChanges(appConfig, formWithErrors, existingSchemeName, dateToCompleteDeclaration, postCall(srn), srn))),
        value => {
          val ua = request.userAnswers.set(AnyMoreChangesId)(value).asOpt.getOrElse(request.userAnswers)
          Future.successful(Redirect(navigator.nextPage(AnyMoreChangesId, UpdateMode, ua, srn)))
        }
      )
  }

  private def dateToCompleteDeclaration: String = LocalDate.now().plusDays(appConfig.daysDataSaved).toString(DateTimeFormat.forPattern("dd MMMM YYYY"))
}

