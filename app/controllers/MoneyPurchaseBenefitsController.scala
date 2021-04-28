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

import controllers.actions._
import forms.MoneyPurchaseBenefitsFormProvider
import identifiers.{MoneyPurchaseBenefitsId, TcmpChangedId}
import models.requests.DataRequest
import models.{Mode, MoneyPurchaseBenefits}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsError, JsResultException, JsSuccess}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.UserAnswers
import utils.annotations.AboutBenefitsAndInsurance
import views.html.moneyPurchaseBenefits

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MoneyPurchaseBenefitsController @Inject()(
                                                 override val messagesApi: MessagesApi,
                                                 userAnswersService: UserAnswersService,
                                                 @AboutBenefitsAndInsurance navigator: Navigator,
                                                 authenticate: AuthAction,
                                                 getData: DataRetrievalAction,
                                                 allowAccess: AllowAccessActionProvider,
                                                 requireData: DataRequiredAction,
                                                 formProvider: MoneyPurchaseBenefitsFormProvider,
                                                 val controllerComponents: MessagesControllerComponents,
                                                 val view: moneyPurchaseBenefits
                                               )(implicit val executionContext: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport
    with Retrievals {

  val postCall: (Mode, Option[String]) => Call =
    routes.MoneyPurchaseBenefitsController.onSubmit

  private def form: Form[MoneyPurchaseBenefits] = formProvider()

  private def convertTcmpArrToStr(implicit request: DataRequest[AnyContent]): Form[MoneyPurchaseBenefits] = {
    request.userAnswers.get(MoneyPurchaseBenefitsId) match {
      case Some(_) =>
        (request.userAnswers.json \ "moneyPurchaseBenefits").validate[Seq[MoneyPurchaseBenefits]] match {
          case JsSuccess(mpb, _) =>
            mpb match {
              case Seq(MoneyPurchaseBenefits.Collective) =>
                form.fill(MoneyPurchaseBenefits.Collective)
              case Seq(MoneyPurchaseBenefits.Other) =>
                form.fill(MoneyPurchaseBenefits.Other)
              case Seq(MoneyPurchaseBenefits.CashBalance) =>
                form.fill(MoneyPurchaseBenefits.CashBalance)
              case Seq(MoneyPurchaseBenefits.Other, MoneyPurchaseBenefits.CashBalance) =>
                form.fill(MoneyPurchaseBenefits.MixtureCashBalanceAndOther)
              case Seq(MoneyPurchaseBenefits.Collective, MoneyPurchaseBenefits.Other, MoneyPurchaseBenefits.CashBalance) =>
                form.fill(MoneyPurchaseBenefits.MixtureCollectiveAndCashBalanceAndOrOther)
              case _ =>
                throw MoneyPurchaseBenefitsUnrecognisedException
            }
          case JsError(err1) =>
            (request.userAnswers.json \ "moneyPurchaseBenefits").validate[MoneyPurchaseBenefits] match {
              case JsSuccess(mpb, _) =>
                form.fill(mpb)
              case JsError(err2) =>
                throw JsResultException(err1 ++ err2)
            }
        }
      case _ =>
        form
    }
  }

  object MoneyPurchaseBenefitsUnrecognisedException extends Exception("Money purchase benefit not recognised")

  def onPageLoad(mode: Mode, srn: Option[String]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        Future.successful(Ok(
          view(
            form = convertTcmpArrToStr,
            mode = mode,
            schemeName = existingSchemeName,
            postCall = postCall(mode, srn),
            srn = srn
          )
        ))
    }

  def onSubmit(mode: Mode, srn: Option[String]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            Future.successful(BadRequest(
              view(
                form = formWithErrors,
                mode = mode,
                schemeName = existingSchemeName,
                postCall = postCall(mode, srn),
                srn = srn
              )
            )),
          value =>
            userAnswersService.save(
              mode = mode,
              srn = srn,
              id = MoneyPurchaseBenefitsId,
              value = value,
              changeId = TcmpChangedId
            ) map { userAnswers =>
              Redirect(navigator.nextPage(MoneyPurchaseBenefitsId, mode, UserAnswers(userAnswers), srn))
            }
        )
    }
}
