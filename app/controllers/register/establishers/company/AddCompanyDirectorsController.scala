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

package controllers.register.establishers.company

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import forms.register.establishers.company.AddCompanyDirectorsFormProvider
import identifiers.register.establishers.company.AddCompanyDirectorsId
import models.FeatureToggleName.SchemeRegistration
import models.requests.DataRequest

import javax.inject.Inject
import models.{FeatureToggleName, Index, Mode, OptionalSchemeReferenceNumber, SchemeReferenceNumber}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.FeatureToggleService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.UserAnswers
import utils.annotations.EstablishersCompany
import views.html.register.establishers.company.addCompanyDirectors

import scala.concurrent.{ExecutionContext, Future}

class AddCompanyDirectorsController @Inject()(
                                               appConfig: FrontendAppConfig,
                                               override val messagesApi: MessagesApi,
                                               @EstablishersCompany navigator: Navigator,
                                               authenticate: AuthAction,
                                               getData: DataRetrievalAction,
                                               requireData: DataRequiredAction,
                                               formProvider: AddCompanyDirectorsFormProvider,
                                               val view: addCompanyDirectors,
                                               val controllerComponents: MessagesControllerComponents,
                                               featureToggleService: FeatureToggleService
                                             )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController with I18nSupport with Retrievals {

  private val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode, srn: OptionalSchemeReferenceNumber, index: Int): Action[AnyContent] = (authenticate() andThen getData
  (mode, srn) andThen requireData).async {
    implicit request =>
      val directors = request.userAnswers.allDirectorsAfterDelete(index)
      Future.successful(Ok(view(form, directors, existingSchemeName, postCall(mode, srn, index), request.viewOnly,
        mode, srn)))
  }

  def onSubmit(mode: Mode, srn: OptionalSchemeReferenceNumber, index: Int): Action[AnyContent] = (authenticate() andThen getData(mode,
    srn) andThen requireData).async {
    implicit request =>
      val directors = request.userAnswers.allDirectorsAfterDelete(index)

      if (directors.isEmpty || directors.lengthCompare(appConfig.maxDirectors) >= 0) {
        Future.successful(Redirect(navigator.nextPage(AddCompanyDirectorsId(index), mode, request.userAnswers, srn)))
      }
      else {
        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            Future.successful(
              BadRequest(
                view(
                  formWithErrors,
                  directors,
                  existingSchemeName,
                  postCall(mode, srn, index),
                  request.viewOnly,
                  mode,
                  srn
                )
              )
            ),
          value => {
            tempToggleAmend(request.userAnswers.set(AddCompanyDirectorsId(index))(value).asOpt.getOrElse(request.userAnswers)).map { updatedUA =>
              Redirect(navigator.nextPage(AddCompanyDirectorsId(index), mode, updatedUA, srn))
            }
          }
        )
      }
  }

  //TODO: Remove whole method once toggle is removed
  private def tempToggleAmend(ua: UserAnswers)(implicit request: DataRequest[AnyContent]): Future[UserAnswers] = {
    featureToggleService.get(FeatureToggleName.SchemeRegistration).map(_.isEnabled).map { toggleValue =>
      val uaAsJsObject = ua.json.as[JsObject]
      val updatedJson = uaAsJsObject ++ Json.obj(SchemeRegistration.asString -> toggleValue)
      UserAnswers(updatedJson)
    }
  }

  private def postCall: (Mode, Option[SchemeReferenceNumber], Index) => Call = routes.AddCompanyDirectorsController.onSubmit _

}
