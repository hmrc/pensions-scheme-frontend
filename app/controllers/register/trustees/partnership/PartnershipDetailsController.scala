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

import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import forms.register.PartnershipDetailsFormProvider
import identifiers.register.trustees.partnership.PartnershipDetailsId
import models.{Index, Mode, OptionalSchemeReferenceNumber}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Enumerable
import views.html.register.trustees.partnership.partnershipDetails

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PartnershipDetailsController @Inject()(
                                              override val messagesApi: MessagesApi,
                                              userAnswersService: UserAnswersService,
                                              navigator: Navigator,
                                              authenticate: AuthAction,
                                              getData: DataRetrievalAction,
                                              allowAccess: AllowAccessActionProvider,
                                              requireData: DataRequiredAction,
                                              formProvider: PartnershipDetailsFormProvider,
                                              val controllerComponents: MessagesControllerComponents,
                                              val view: partnershipDetails
                                            )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController with Retrievals with I18nSupport with Enumerable.Implicits {
  private val form = formProvider()

  def onPageLoad(mode: Mode, index: Index, srn: OptionalSchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        val submitUrl = controllers.register.trustees.partnership.routes.PartnershipDetailsController.onSubmit(mode,
          index, srn)
        val updatedForm = request.userAnswers.get(PartnershipDetailsId(index)).fold(form)(form.fill)
        Future.successful(Ok(view(updatedForm, mode, index, existingSchemeName, submitUrl, srn)))
    }

  def onSubmit(mode: Mode, index: Index, srn: OptionalSchemeReferenceNumber): Action[AnyContent] = (authenticate() andThen getData
  (mode, srn) andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[?]) => {
          val submitUrl = controllers.register.trustees.partnership.routes.PartnershipDetailsController
            .onSubmit(mode, index, srn)
          Future.successful(BadRequest(view(formWithErrors, mode, index, existingSchemeName, submitUrl, srn)))
        },
        value =>
          request.userAnswers.upsert(PartnershipDetailsId(index))(value) {
            answers =>
              userAnswersService.upsert(mode, srn, answers.json).flatMap { _ =>
                Future.successful(request.userAnswers.setOrException(PartnershipDetailsId(index))(value)).map { updatedUA =>
                  Redirect(navigator.nextPage(PartnershipDetailsId(index), mode, updatedUA, srn))
                }
              }
          }
      )
  }
}
