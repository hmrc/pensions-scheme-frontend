/*
 * Copyright 2024 HM Revenue & Customs
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

import controllers.Retrievals
import controllers.actions._
import forms.register.establishers.AddEstablisherFormProvider
import identifiers.register.establishers.AddEstablisherId
import models.register.Establisher
import models.requests.DataRequest
import models.{Mode, NormalMode, OptionalSchemeReferenceNumber, SchemeReferenceNumber}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.{Establishers, NoSuspendedCheck}
import views.html.register.establishers.{addEstablisher, addEstablisherOld}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddEstablisherController @Inject()(override val messagesApi: MessagesApi,
                                         @Establishers navigator: Navigator,
                                         authenticate: AuthAction,
                                         getData: DataRetrievalAction,
                                         @NoSuspendedCheck allowAccess: AllowAccessActionProvider,
                                         requireData: DataRequiredAction,
                                         formProvider: AddEstablisherFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         val view: addEstablisher,
                                         val addEstablisherOldview: addEstablisherOld
                                        )(implicit val ec: ExecutionContext)
  extends FrontendBaseController with Retrievals with I18nSupport {

  private def renderPage(
                          establishers: Seq[Establisher[_]],
                          mode: Mode,
                          srn: OptionalSchemeReferenceNumber,
                          form: Form[Option[Boolean]], status: Status)(implicit request: DataRequest[AnyContent]): Future[Result] = {

      mode match {
        case NormalMode =>
          val completeEstablishers = establishers.filter(_.isCompleted)
          val incompleteEstablishers = establishers.filterNot(_.isCompleted)
          Future.successful(status(view(form, mode, completeEstablishers, incompleteEstablishers, existingSchemeName, srn)))
        case _ => Future.successful(status(addEstablisherOldview(form, mode, establishers, existingSchemeName, srn)))
    }
  }

  def onPageLoad(mode: Mode, srn: OptionalSchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        val establishers = request.userAnswers.allEstablishersAfterDelete(mode)
        renderPage(establishers, mode, srn, formProvider(establishers), Ok)
    }

  def onSubmit(mode: Mode, srn: OptionalSchemeReferenceNumber): Action[AnyContent] = (authenticate() andThen getData(mode, srn)
    andThen requireData).async {
    implicit request =>
      val establishers = request.userAnswers.allEstablishersAfterDelete(mode)
      formProvider(establishers).bindFromRequest().fold(
        formWithErrors =>
          renderPage(establishers, mode, srn, formWithErrors, BadRequest),
        value =>
          Future.successful(Redirect(navigator.nextPage(AddEstablisherId(value), mode, request.userAnswers, srn)))
      )
  }
}
