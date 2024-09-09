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

package controllers.register.trustees.partnership

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import forms.register.PartnershipDetailsFormProvider
import identifiers.register.trustees.partnership.PartnershipDetailsId
import models.FeatureToggleName.SchemeRegistration
import models.requests.DataRequest

import javax.inject.Inject
import models.{FeatureToggleName, Index, Mode}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{FeatureToggleService, UserAnswersService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{Enumerable, UserAnswers}
import views.html.register.trustees.partnership.partnershipDetails

import scala.concurrent.{ExecutionContext, Future}
import models.SchemeReferenceNumber

class PartnershipDetailsController @Inject()(
                                              appConfig: FrontendAppConfig,
                                              override val messagesApi: MessagesApi,
                                              userAnswersService: UserAnswersService,
                                              navigator: Navigator,
                                              authenticate: AuthAction,
                                              getData: DataRetrievalAction,
                                              allowAccess: AllowAccessActionProvider,
                                              requireData: DataRequiredAction,
                                              formProvider: PartnershipDetailsFormProvider,
                                              val controllerComponents: MessagesControllerComponents,
                                              val view: partnershipDetails,
                                              featureToggleService: FeatureToggleService
                                            )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController with Retrievals with I18nSupport with Enumerable.Implicits {
  private val form = formProvider()

  def onPageLoad(mode: Mode, index: Index, srn: Option[SchemeReferenceNumber]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        val submitUrl = controllers.register.trustees.partnership.routes.PartnershipDetailsController.onSubmit(mode,
          index, srn)
        val updatedForm = request.userAnswers.get(PartnershipDetailsId(index)).fold(form)(form.fill)
        Future.successful(Ok(view(updatedForm, mode, index, existingSchemeName, submitUrl, srn)))
    }

  def onSubmit(mode: Mode, index: Index, srn: Option[SchemeReferenceNumber]): Action[AnyContent] = (authenticate() andThen getData
  (mode, srn) andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) => {
          val submitUrl = controllers.register.trustees.partnership.routes.PartnershipDetailsController
            .onSubmit(mode, index, srn)
          Future.successful(BadRequest(view(formWithErrors, mode, index, existingSchemeName, submitUrl, srn)))
        },
        value =>
          request.userAnswers.upsert(PartnershipDetailsId(index))(value) {
            answers =>
              userAnswersService.upsert(mode, srn, answers.json).flatMap { _ =>
                tempToggleAmend(request.userAnswers.setOrException(PartnershipDetailsId(index))(value)).map { updatedUA =>
                  Redirect(navigator.nextPage(PartnershipDetailsId(index), mode, updatedUA, srn))
                }
              }
          }
      )
  }

  //TODO: Remove whole method once toggle is removed
  private def tempToggleAmend(ua: UserAnswers)(implicit request: DataRequest[AnyContent]): Future[UserAnswers] = {
    featureToggleService.get(FeatureToggleName.SchemeRegistration).map(_.isEnabled).map { toggleValue =>
      val uaAsJsObject = ua.json.as[JsObject]
      val updatedJson = uaAsJsObject ++ Json.obj(SchemeRegistration.asString -> toggleValue)
      UserAnswers(updatedJson)
    }
  }
}
