/*
 * Copyright 2018 HM Revenue & Customs
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

package controllers.register.trustees.company

import javax.inject.Inject

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import identifiers.register.trustees.company._
import models.{CheckMode, Index}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.CheckYourAnswers.Ops._
import utils.{CheckYourAnswersFactory, CountryOptions}
import viewmodels.AnswerSection
import views.html.check_your_answers

import scala.concurrent.Future
import scala.language.implicitConversions

class CheckYourAnswersController @Inject()(appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           requiredData: DataRequiredAction,
                                           checkYourAnswersFactory: CheckYourAnswersFactory,
                                           implicit val countryOptions: CountryOptions
                                          ) extends FrontendController with Retrievals with I18nSupport {

  def onPageLoad(index: Index): Action[AnyContent] = (authenticate andThen getData andThen requiredData).async {
    implicit request =>

      val companyDetailsId = CompanyDetailsId(index)

      companyDetailsId.retrieve.right.map{ companyDetails =>

        val companyDetailsRow = companyDetailsId.row(routes.CompanyDetailsController.onPageLoad(CheckMode, index).url)

        val companyRegistrationNumber = CompanyRegistrationNumberId(index).row(
          routes.CompanyRegistrationNumberController.onPageLoad(CheckMode, index).url
        )

        val companyDetailsSection = AnswerSection(
          Some("messages__checkYourAnswers__section__company_details"),
          companyDetailsRow ++ companyRegistrationNumber
        )

        val companyAddress = CompanyAddressId(index).row(
          routes.CompanyAddressController.onPageLoad(CheckMode, index).url
        )

        val companyAddressYears = CompanyAddressYearsId(index).row(
          routes.CompanyAddressYearsController.onPageLoad(CheckMode, index).url
        )

        val companyPreviousAddress = CompanyPreviousAddressId(index).row(
          routes.CompanyPreviousAddressController.onPageLoad(CheckMode, index).url
        )

        val contactDetailsSection = AnswerSection(
          Some("messages__checkYourAnswers__section__contact_details"),
          companyAddress ++ companyAddressYears ++ companyPreviousAddress
        )

        Future.successful(Ok(check_your_answers(
          appConfig,
          Seq(companyDetailsSection, contactDetailsSection),
          Some(companyDetails.companyName),
          routes.CheckYourAnswersController.onSubmit(index)
        )))
      }
  }

  def onSubmit(index: Index): Action[AnyContent] = (authenticate andThen getData andThen requiredData).async {
    implicit request =>
      ???
  }

}
