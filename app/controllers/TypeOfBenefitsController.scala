/*
 * Copyright 2021 HM Revenue & Customs
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

import config.FrontendAppConfig
import controllers.actions._
import forms.TypeOfBenefitsFormProvider
import identifiers.{SchemeNameId, TypeOfBenefitsId}
import models.FeatureToggle.Enabled
import models.FeatureToggleName.TCMP

import javax.inject.Inject
import models.{CheckMode, Mode, NormalMode, TypeOfBenefits}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.{FeatureToggleService, UserAnswersService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.AboutBenefitsAndInsurance
import utils.{Enumerable, UserAnswers}
import views.html.typeOfBenefits

import scala.concurrent.{ExecutionContext, Future}

class TypeOfBenefitsController @Inject()(appConfig: FrontendAppConfig,
                                         override val messagesApi: MessagesApi,
                                         userAnswersService: UserAnswersService,
                                         @AboutBenefitsAndInsurance navigator: Navigator,
                                         authenticate: AuthAction,
                                         getData: DataRetrievalAction,
                                         allowAccess: AllowAccessActionProvider,
                                         requireData: DataRequiredAction,
                                         formProvider: TypeOfBenefitsFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         val view: typeOfBenefits,
                                         featureToggleService: FeatureToggleService
                                        )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController with I18nSupport with Enumerable.Implicits with Retrievals {

  def onPageLoad(mode: Mode, srn: Option[String]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        SchemeNameId.retrieve.right.map { schemeName =>
          val preparedForm = request.userAnswers.get(TypeOfBenefitsId) match {
            case None => form(schemeName)
            case Some(value) => form(schemeName).fill(value)
          }
          Future.successful(Ok(view(preparedForm, postCall(mode, srn), existingSchemeName)))
        }
    }

  private def form(schemeName: String)(implicit messages: Messages): Form[TypeOfBenefits] = formProvider(schemeName)
  private def postCall: (Mode, Option[String]) => Call = routes.TypeOfBenefitsController.onSubmit

  def onSubmit(mode: Mode, srn: Option[String]): Action[AnyContent] = (authenticate() andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      SchemeNameId.retrieve.right.map { schemeName =>

        form(schemeName).bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            Future.successful(BadRequest(view(formWithErrors, postCall(mode, srn), existingSchemeName))),
          value =>
            userAnswersService.save(mode, srn, TypeOfBenefitsId, value).flatMap(cacheMap =>
              featureToggleService.get(TCMP).map {
                case Enabled(_) => Redirect(navigator.nextPage(TypeOfBenefitsId, mode, UserAnswers(cacheMap), srn))
                case _ => Redirect(toggleOffNavigation(mode, srn))
              }
            )

        )
      }
  }

  private def toggleOffNavigation(mode: Mode, srn: Option[String]): Call = mode match {
    case NormalMode => controllers.routes.BenefitsSecuredByInsuranceController.onPageLoad(mode, srn)
    case CheckMode => controllers.routes.CheckYourAnswersBenefitsAndInsuranceController.onPageLoad(NormalMode, srn)
    case _ => controllers.routes.AnyMoreChangesController.onPageLoad(srn)

  }
}
