/*
 * Copyright 2019 HM Revenue & Customs
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
import identifiers.register.establishers.company.CompanyRegistrationNumberId
import models.requests.DataRequest
import models.{CompanyRegistrationNumber, Index, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, Call, Result}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils._
import views.html.register.companyRegistrationNumberVariations

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait CompanyRegistrationNumberVariationsController extends FrontendController with Retrievals with I18nSupport {

  protected implicit val ec = play.api.libs.concurrent.Execution.defaultContext

  protected def appConfig: FrontendAppConfig

  protected def userAnswersService: UserAnswersService

  protected val form: Form[String]

  protected def navigator: Navigator

  def postCall: (Mode, Option[String], Index) => Call = routes.CompanyRegistrationNumberController.onSubmit _

  def get(mode: Mode, srn: Option[String], index: Index)
         (implicit request: DataRequest[AnyContent]): Future[Result] = {

    val preparedForm =
      request.userAnswers.get(CompanyRegistrationNumberId(index)) match {
        case Some(CompanyRegistrationNumber.Yes(crnNumber)) => form.fill(crnNumber)
        case _ => form
      }

    val view = companyRegistrationNumberVariations(appConfig, preparedForm, mode, index, existingSchemeName, postCall(mode, srn, index), srn)

    Future.successful(Ok(view))
  }

  def post(mode: Mode, srn: Option[String], index: Index)
          (implicit request: DataRequest[AnyContent]): Future[Result] = {
    form.bindFromRequest().fold(
      (formWithErrors: Form[_]) =>

        Future.successful(BadRequest(
          companyRegistrationNumberVariations(appConfig, formWithErrors, mode, index, existingSchemeName, postCall(mode, srn, index), srn))),

      crnNumber => {
        val updatedUserAnswers = request.userAnswers
          .set(CompanyRegistrationNumberId(index))(CompanyRegistrationNumber.Yes(crnNumber)).asOpt.getOrElse(request.userAnswers)
        userAnswersService.upsert(mode, srn, updatedUserAnswers.json).map(cacheMap =>
          Redirect(navigator.nextPage(CompanyRegistrationNumberId(index), mode, UserAnswers(cacheMap), srn)))
      }
    )
  }
}

