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

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import forms.register.establishers.AddEstablisherFormProvider
import identifiers.register.establishers.AddEstablisherId
import models.register.Establisher
import models.requests.DataRequest
import models.{FeatureToggleName, Mode, NormalMode}
import navigators.Navigator
import play.api.Logger
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsResultException
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.FeatureToggleService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.{Establishers, NoSuspendedCheck}
import views.html.register.establishers.{addEstablisher, addEstablisherOld, otherEstablishers}

import javax.inject.Inject
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
                                         val addEstablisherOldView: addEstablisherOld
                                        )(implicit val ec: ExecutionContext)
  extends FrontendBaseController with Retrievals with I18nSupport {

  private val logger = Logger(classOf[AddEstablisherController])

  private val form = formProvider()

  private def renderPage(
                          establishers: Seq[Establisher[_]],
                          mode: Mode,
                          srn: Option[String],
                          form: Form[Boolean], status: Status)(implicit request: DataRequest[AnyContent]): Future[Result] = {

    featureToggleService.get(FeatureToggleName.SchemeRegistration).map(_.isEnabled).map { isEnabled =>
      (isEnabled, mode) match {
        case (true, NormalMode) =>
          val completeEstablishers = establishers.filter(_.isCompleted)
          val incompleteEstablishers = establishers.filterNot(_.isCompleted)
          status(view(form, mode, completeEstablishers, incompleteEstablishers, existingSchemeName, srn))
        case _ => status(addEstablisherOldView(form, mode, establishers, existingSchemeName, srn))
      }
    }
  }

  def onPageLoad(mode: Mode, srn: Option[String]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        val establishers = request.userAnswers.allEstablishersAfterDelete(mode)
        renderPage(establishers, mode, srn, form, Ok)
    }

  def onSubmit(mode: Mode, srn: Option[String]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen requireData).async {
      implicit request =>

        val establishers = request.userAnswers.allEstablishersAfterDelete(mode)

        if (establishers.isEmpty || establishers.lengthCompare(appConfig.maxEstablishers) >= 0)
          Future.successful(Redirect(navigator.nextPage(AddEstablisherId, mode, request.userAnswers, srn)))
        else {
          form.bindFromRequest().fold(
            formWithErrors => {
              renderPage(establishers, mode, srn, formWithErrors, BadRequest)
            },
            value =>
              request.userAnswers.set(AddEstablisherId)(value).fold(
                errors => {
                  logger.error("Unable to set user answer", JsResultException(errors))
                  Future.successful(InternalServerError)
                },
                userAnswers =>
                  Future.successful(Redirect(navigator.nextPage(AddEstablisherId, mode, request.userAnswers, srn)))
              )
      )
    }
}

}