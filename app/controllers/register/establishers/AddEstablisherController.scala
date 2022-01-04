/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.register.establishers

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import forms.register.establishers.AddEstablisherFormProvider
import identifiers.register.establishers.AddEstablisherId
import javax.inject.Inject
import models.Mode
import navigators.Navigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.{Establishers, NoSuspendedCheck}
import views.html.register.establishers.addEstablisher

import scala.concurrent.{ExecutionContext, Future}

class AddEstablisherController @Inject()(appConfig: FrontendAppConfig,
                                         override val messagesApi: MessagesApi,
                                         @Establishers navigator: Navigator,
                                         authenticate: AuthAction,
                                         getData: DataRetrievalAction,
                                         @NoSuspendedCheck allowAccess: AllowAccessActionProvider,
                                         requireData: DataRequiredAction,
                                         formProvider: AddEstablisherFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         val view: addEstablisher
                                        )(implicit val ec: ExecutionContext)
  extends FrontendBaseController with Retrievals with I18nSupport {

  def onPageLoad(mode: Mode, srn: Option[String]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        val establishers = request.userAnswers.allEstablishersAfterDelete(mode)
        Future.successful(Ok(view(formProvider(establishers), mode,
          establishers, existingSchemeName, srn)))
    }

  def onSubmit(mode: Mode, srn: Option[String]): Action[AnyContent] = (authenticate() andThen getData(mode, srn)
    andThen requireData).async {
    implicit request =>
      val establishers = request.userAnswers.allEstablishersAfterDelete(mode)
      formProvider(establishers).bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode,
            establishers, existingSchemeName, srn))),
        value =>
          Future.successful(Redirect(navigator.nextPage(AddEstablisherId(value), mode, request.userAnswers, srn)))
      )
  }
}
