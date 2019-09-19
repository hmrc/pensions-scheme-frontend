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

package controllers.register

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import forms.CompanyRegistrationNumberFormProvider
import identifiers.TypedIdentifier
import javax.inject.Inject
import models.requests.DataRequest
import models.{CompanyRegistrationNumber, Index, Mode}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call}
import play.twirl.api.Html
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils._

import scala.concurrent.{ExecutionContext, Future}

abstract class CompanyRegistrationNumberBaseController @Inject()(
                                                           appConfig: FrontendAppConfig,
                                                           override val messagesApi: MessagesApi,
                                                           userAnswersService: UserAnswersService,
                                                           navigator: Navigator,
                                                           authenticate: AuthAction,
                                                           getData: DataRetrievalAction,
                                                           allowAccess: AllowAccessActionProvider,
                                                           requireData: DataRequiredAction,
                                                           formProvider: CompanyRegistrationNumberFormProvider
                                                         )(implicit val ec: ExecutionContext) extends FrontendController with Retrievals with I18nSupport with Enumerable.Implicits with MapFormats {

  val form = formProvider()

  def addView(mode: Mode, index: Index, srn: Option[String])(implicit request: DataRequest[AnyContent]): Html

  def errorView(mode: Mode, index: Index, srn: Option[String], form: Form[_])(implicit request: DataRequest[AnyContent]): Html

  def updateView(mode: Mode, index: Index, srn: Option[String], value: CompanyRegistrationNumber)(implicit request: DataRequest[AnyContent]): Html

  def id(index: Index): TypedIdentifier[CompanyRegistrationNumber]

  def onPageLoad(mode: Mode, srn: Option[String], index: Index): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        val redirectResult = request.userAnswers.get(id(index)) match {
          case None =>
            Ok(addView(mode, index, srn))
          case Some(value) =>
            Ok(updateView(mode, index, srn, value))
        }

        Future.successful(redirectResult)
    }

  def onSubmit(mode: Mode, srn: Option[String], index: Index): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(errorView(mode, index, srn, formWithErrors))),
        value =>
          userAnswersService.save(mode, srn, id(index), value).map(cacheMap =>
            Redirect(navigator.nextPage(id(index), mode, UserAnswers(cacheMap), srn)))
      )
  }

  protected def postCall: (Mode, Option[String], Index) => Call

}
