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
import models.{FeatureToggleName, Mode}
import models.register.Establisher
import models.requests.DataRequest
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.FeatureToggleService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.{Establishers, NoSuspendedCheck}
import views.html.register.establishers.{addEstablisher, addEstablisherOld}

import scala.concurrent.{ExecutionContext, Future}

class AddEstablisherController @Inject()(appConfig: FrontendAppConfig,
                                         override val messagesApi: MessagesApi,
                                         @Establishers navigator: Navigator,
                                         authenticate: AuthAction,
                                         getData: DataRetrievalAction,
                                         @NoSuspendedCheck allowAccess: AllowAccessActionProvider,
                                         requireData: DataRequiredAction,
                                         formProvider: AddEstablisherFormProvider,
                                         featureToggleService: FeatureToggleService,
                                         val controllerComponents: MessagesControllerComponents,
                                         val view: addEstablisher,
                                         val addEstablisherOldview: addEstablisherOld
                                        )(implicit val ec: ExecutionContext)
  extends FrontendBaseController with Retrievals with I18nSupport {

  private def renderPage(establishers: Seq[Establisher[_]], mode: Mode, srn: Option[String], form:Form[Option[Boolean]], status: Status)(implicit request:  DataRequest[AnyContent]): Future[Result] ={
      featureToggleService.get(FeatureToggleName.SchemeRegistration).map(_.isEnabled).map {
        case true => val x = establishers.map{ result =>
          SummaryListRow(
            key = Key(
              content = Text("Event"),
              classes = "govuk-visually-hidden"
            ),
            value = Value(
              content = Text("Event 18: Scheme chargeable payment"),
              classes = "govuk-!-font-weight-bold govuk-!-width-full"
            ),
            actions = Some(Actions(
              items = Seq(
                ActionItem(
                  href = "/pension-scheme-event-reporting-frontend/new-report/event-18-confirmation?waypoints=event-18-check-answers",
                  content = Text("Change"),
                  visuallyHiddenText = Some("Event 18: Scheme chargeable payment")
                )
              )
            ))
          )
          status(view(form, mode, x, existingSchemeName, srn))
        case _ => status(addEstablisherOldview(form, mode, establishers, existingSchemeName, srn))
      }
  }

  def onPageLoad(mode: Mode, srn: Option[String]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        val establishers = request.userAnswers.allEstablishersAfterDelete(mode)
        renderPage(establishers, mode, srn, formProvider(establishers), Ok)
    }

  def onSubmit(mode: Mode, srn: Option[String]): Action[AnyContent] = (authenticate() andThen getData(mode, srn)
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
