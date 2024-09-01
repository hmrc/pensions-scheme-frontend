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

package controllers

import controllers.actions._
import forms.TypeOfBenefitsFormProvider
import identifiers.{MoneyPurchaseBenefitsId, SchemeNameId, TcmpChangedId, TypeOfBenefitsId}
import models.{Mode, SchemeReferenceNumber, TypeOfBenefits}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.AboutBenefitsAndInsurance
import utils.{Enumerable, UserAnswers}
import views.html.typeOfBenefits

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TypeOfBenefitsController @Inject()(
                                          override val messagesApi: MessagesApi,
                                          userAnswersService: UserAnswersService,
                                          @AboutBenefitsAndInsurance navigator: Navigator,
                                          authenticate: AuthAction,
                                          getData: DataRetrievalAction,
                                          allowAccess: AllowAccessActionProvider,
                                          requireData: DataRequiredAction,
                                          formProvider: TypeOfBenefitsFormProvider,
                                          val controllerComponents: MessagesControllerComponents,
                                          val view: typeOfBenefits
                                        )(implicit val executionContext: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport
    with Enumerable.Implicits
    with Retrievals {

  private def form(schemeName: String)
                  (implicit messages: Messages): Form[TypeOfBenefits] = formProvider(schemeName)

  def onPageLoad(mode: Mode, srn: SchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData() andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        SchemeNameId.retrieve.map {
          schemeName =>
            val preparedForm =
              request.userAnswers.get(TypeOfBenefitsId) match {
                case None =>
                  form(schemeName)
                case Some(value) =>
                  form(schemeName).fill(value)
              }
            Future.successful(Ok(view(
              form = preparedForm,
              postCall = routes.TypeOfBenefitsController.onSubmit(mode, srn),
              schemeName = existingSchemeName, srn
            )))
        }
    }

  def onSubmit(mode: Mode, srn: SchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData() andThen requireData).async {
      implicit request =>
        SchemeNameId.retrieve.map {
          schemeName =>
            form(schemeName).bindFromRequest().fold(
              (formWithErrors: Form[_]) =>
                Future.successful(BadRequest(view(
                  form = formWithErrors,
                  postCall = routes.TypeOfBenefitsController.onSubmit(mode, srn),
                  schemeName = existingSchemeName, srn
                ))),
              value =>
                (if(value == TypeOfBenefits.Defined && request.userAnswers.get(MoneyPurchaseBenefitsId).isDefined)
                  userAnswersService.save(mode, srn, TypeOfBenefitsId, value, TcmpChangedId)
                else
                  userAnswersService.save(mode, srn, TypeOfBenefitsId, value)
                ).map(cacheMap =>
                   Redirect(navigator.nextPage(TypeOfBenefitsId, mode, UserAnswers(cacheMap), srn))
                )

            )
        }
    }
}
