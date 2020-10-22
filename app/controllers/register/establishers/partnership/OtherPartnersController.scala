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

package controllers.register.establishers.partnership

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import forms.register.establishers.partnership.OtherPartnersFormProvider
import identifiers.register.establishers.partnership.OtherPartnersId
import javax.inject.Inject
import models.{Index, Mode}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.UserAnswers
import views.html.register.establishers.partnership.otherPartners

import scala.concurrent.{ExecutionContext, Future}

class OtherPartnersController @Inject()(
                                         appConfig: FrontendAppConfig,
                                         override val messagesApi: MessagesApi,
                                         userAnswersService: UserAnswersService,
                                         navigator: Navigator,
                                         authenticate: AuthAction,
                                         getData: DataRetrievalAction,
                                         allowAccess: AllowAccessActionProvider,
                                         requireData: DataRequiredAction,
                                         formProvider: OtherPartnersFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         val view: otherPartners
                                       )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController with Retrievals with I18nSupport {

  private val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode, establisherIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        retrievePartnershipName(establisherIndex) { _ =>
          val preparedForm = request.userAnswers.get(OtherPartnersId(establisherIndex)).fold(form)(form.fill)
          val submitUrl = controllers.register.establishers.partnership.routes.OtherPartnersController
            .onSubmit(mode,  establisherIndex, srn)
          Future.successful(Ok(view(preparedForm, mode, establisherIndex, existingSchemeName, submitUrl, srn)))
        }

    }

  def onSubmit(mode: Mode, establisherIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        retrievePartnershipName(establisherIndex) { _ =>
          form.bindFromRequest().fold(
            (formWithErrors: Form[_]) => {
              val submitUrl = controllers.register.establishers.partnership.routes.OtherPartnersController
                .onSubmit(mode, establisherIndex, srn)
              Future.successful(BadRequest(view(formWithErrors, mode, establisherIndex, existingSchemeName,
                submitUrl, srn)))
            },
            value =>
              userAnswersService.save(mode, srn, OtherPartnersId(establisherIndex), value).map(cacheMap =>
                Redirect(navigator.nextPage(OtherPartnersId(establisherIndex), mode, UserAnswers(cacheMap), srn)))
          )
        }
    }
}
