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
import forms.MoneyPurchaseBenefitsFormProvider
import identifiers.MoneyPurchaseBenefitsId
import javax.inject.Inject
import models.{Mode, MoneyPurchaseBenefits}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.UserAnswers
import utils.annotations.{AboutBenefitsAndInsurance, InsuranceService}
import views.html.moneyPurchaseBenefits

import scala.concurrent.{ExecutionContext, Future}

class MoneyPurchaseBenefitsController @Inject()(appConfig: FrontendAppConfig,
                                                override val messagesApi: MessagesApi,
                                                @InsuranceService userAnswersService: UserAnswersService,
                                                @AboutBenefitsAndInsurance navigator: Navigator,
                                                authenticate: AuthAction,
                                                getData: DataRetrievalAction,
                                                allowAccess: AllowAccessActionProvider,
                                                requireData: DataRequiredAction,
                                                formProvider: MoneyPurchaseBenefitsFormProvider,
                                                val controllerComponents: MessagesControllerComponents,
                                                val view: moneyPurchaseBenefits
                                                    )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController with I18nSupport with Retrievals {

  val postCall: (Mode, Option[String]) => Call = routes.MoneyPurchaseBenefitsController.onSubmit

  def onPageLoad(mode: Mode, srn: Option[String]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
          val preparedForm = request.userAnswers.get(MoneyPurchaseBenefitsId) match {
            case None => form
            case Some(value) => form.fill(value)
          }
          Future.successful(Ok(view(preparedForm, mode, existingSchemeName, postCall(mode, srn), srn)))
    }

  private def form: Form[Seq[MoneyPurchaseBenefits]] = formProvider()

  def onSubmit(mode: Mode, srn: Option[String]): Action[AnyContent] = (authenticate() andThen getData(mode, srn)
    andThen requireData).async {
    implicit request =>
      println(s"\n\n  >>>>>>>>>>>>>. ${request.request.body} ")
        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            Future.successful(BadRequest(view(formWithErrors, mode, existingSchemeName, postCall(mode, srn), srn))),
          value =>
            userAnswersService.save(mode, srn, MoneyPurchaseBenefitsId, value).map { userAnswers =>
              Redirect(navigator.nextPage(MoneyPurchaseBenefitsId, mode, UserAnswers(userAnswers), srn))
            }
        )
  }
}
