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

package controllers.register.establishers.partnership

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import forms.register.PartnershipDetailsFormProvider
import identifiers.register.establishers.partnership.PartnershipDetailsId
import models.{FeatureToggleName, Index, Mode, NormalMode, OptionalSchemeReferenceNumber, SchemeReferenceNumber}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{FeatureToggleService, UserAnswersService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.{EstablishersPartnership, OldEstablishersPartnership}
import utils.{Enumerable, UserAnswers}
import views.html.register.establishers.partnership.partnershipDetails

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PartnershipDetailsController @Inject()(
                                              appConfig: FrontendAppConfig,
                                              override val messagesApi: MessagesApi,
                                              userAnswersService: UserAnswersService,
                                              @EstablishersPartnership navigator: Navigator,
                                              @OldEstablishersPartnership oldNavigator: Navigator,
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

  def onPageLoad(mode: Mode, index: Index, srn: OptionalSchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        val formWithData = request.userAnswers.get(PartnershipDetailsId(index)).fold(form)(form.fill)
        val submitUrl = controllers.register.establishers.partnership.routes.PartnershipDetailsController
          .onSubmit(mode, index, srn)
        Future.successful(Ok(view(formWithData, mode, index, existingSchemeName, submitUrl, srn)))
    }

  def onSubmit(mode: Mode, index: Index, srn: OptionalSchemeReferenceNumber): Action[AnyContent] = (authenticate() andThen getData
  (mode, srn) andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) => {
          val submitUrl = controllers.register.establishers.partnership.routes.PartnershipDetailsController
            .onSubmit(mode, index, srn)
          Future.successful(BadRequest(view(formWithErrors, mode, index, existingSchemeName, submitUrl, srn)))
        },
        value =>
          userAnswersService.save(mode, srn, PartnershipDetailsId(index), value
          ).flatMap {
            json =>
              featureToggleService.get(FeatureToggleName.SchemeRegistration).map(_.isEnabled).map { isEnabled =>
                (isEnabled, mode) match {
                  case (true, NormalMode) => Redirect(navigator.nextPage(PartnershipDetailsId(index), mode, UserAnswers(json), srn))
                  case _ => Redirect(oldNavigator.nextPage(PartnershipDetailsId(index), mode, UserAnswers(json), srn))
                }
              }
          }
      )
  }
}
