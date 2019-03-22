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

package controllers

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.actions._
import forms.InsuranceCompanyNameFormProvider
import identifiers.InsuranceCompanyNameId
import javax.inject.Inject
import models.Mode
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.{AboutBenefitsAndInsurance, InsuranceService}
import utils.{Navigator, UserAnswers}
import views.html.insuranceCompanyName

import scala.concurrent.{ExecutionContext, Future}

class InsuranceCompanyNameController @Inject()(appConfig: FrontendAppConfig,
                                               override val messagesApi: MessagesApi,
                                               @InsuranceService userAnswersService: UserAnswersService,
                                               @AboutBenefitsAndInsurance navigator: Navigator,
                                               authenticate: AuthAction,
                                               getData: DataRetrievalAction,
                                               requireData: DataRequiredAction,
                                               formProvider: InsuranceCompanyNameFormProvider
                                              )(implicit val ec: ExecutionContext) extends FrontendController with I18nSupport with Retrievals {

  private val form = formProvider()

  def onPageLoad(mode: Mode, srn: Option[String] = None): Action[AnyContent] = (authenticate andThen getData) {
    implicit request =>
      val preparedForm = request.userAnswers.flatMap(_.get(InsuranceCompanyNameId)).fold(form)(v => form.fill(v))
      Ok(insuranceCompanyName(appConfig, preparedForm, mode, existingSchemeName))
  }

  def onSubmit(mode: Mode, srn: Option[String] = None): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(insuranceCompanyName(appConfig, formWithErrors, mode, existingSchemeName))),
        value =>
          userAnswersService.save(mode, srn, InsuranceCompanyNameId, value).map(cacheMap =>
            Redirect(navigator.nextPage(InsuranceCompanyNameId, mode, UserAnswers(cacheMap))))
      )
  }
}
