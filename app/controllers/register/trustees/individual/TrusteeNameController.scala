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

package controllers.register.trustees.individual

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.register.trustees.individual.routes._
import forms.register.PersonNameFormProvider
import identifiers.register.trustees.individual.TrusteeNameId
import models.FeatureToggleName.SchemeRegistration

import javax.inject.Inject
import models.person.PersonName
import models.requests.DataRequest
import models.{FeatureToggleName, Index, Mode, OptionalSchemeReferenceNumber, SchemeReferenceNumber}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{FeatureToggleService, UserAnswersService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{Enumerable, UserAnswers}
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.personName

import scala.concurrent.{ExecutionContext, Future}

class TrusteeNameController @Inject()(appConfig: FrontendAppConfig,
                                      override val messagesApi: MessagesApi,
                                      userAnswersService: UserAnswersService,
                                      navigator: Navigator,
                                      authenticate: AuthAction,
                                      getData: DataRetrievalAction,
                                      allowAccess: AllowAccessActionProvider,
                                      requireData: DataRequiredAction,
                                      formProvider: PersonNameFormProvider,
                                      val controllerComponents: MessagesControllerComponents,
                                      featureToggleService: FeatureToggleService,
                                      val view: personName
                                     )(implicit val executionContext: ExecutionContext) extends FrontendBaseController
  with Retrievals with I18nSupport with Enumerable.Implicits {

  def onPageLoad(mode: Mode, index: Index, srn: OptionalSchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        val updatedForm = request.userAnswers.get(TrusteeNameId(index)).fold(form)(form.fill)
        Future.successful(Ok(view(updatedForm, viewmodel(mode, index, srn), existingSchemeName)))
    }

  private def form(implicit request: DataRequest[AnyContent]): Form[PersonName] =
    formProvider("messages__error__trustees")

  private def viewmodel(mode: Mode, index: Index, srn: OptionalSchemeReferenceNumber) = CommonFormWithHintViewModel(
    TrusteeNameController.onSubmit(mode, index, srn),
    Message("messages__trusteeName__title"),
    Message("messages__trusteeName__heading")
  )


  def onSubmit(mode: Mode, index: Index, srn: OptionalSchemeReferenceNumber): Action[AnyContent] = (authenticate() andThen getData
  (mode, srn) andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, viewmodel(mode, index, srn), existingSchemeName))),
        value =>
          userAnswersService.save(mode, srn, TrusteeNameId(index), value).flatMap { _ =>
            tempToggleAmend(request.userAnswers.set(TrusteeNameId(index))(value).asOpt.getOrElse(request.userAnswers)).map{ updatedUA =>
            Redirect(navigator.nextPage(TrusteeNameId(index), mode, updatedUA, srn))
            }
          }
      )
  }

  //TODO: Remove whole method once toggle is removed
  private def tempToggleAmend(ua: UserAnswers)(implicit  request: DataRequest[AnyContent]): Future[UserAnswers] = {
    featureToggleService.get(FeatureToggleName.SchemeRegistration).map(_.isEnabled).map { toggleValue =>
      val uaAsJsObject = ua.json.as[JsObject]
      val updatedJson = uaAsJsObject ++ Json.obj(SchemeRegistration.asString -> toggleValue)
      UserAnswers(updatedJson)
    }
  }
}
