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

package controllers.register.establishers.company

import javax.inject.Inject

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import identifiers.register.SchemeDetailsId
import identifiers.register.establishers.company.CompanyDetailsId
import models.{CheckMode, Index, NormalMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.CheckYourAnswersFactory
import viewmodels.AnswerSection
import views.html.check_your_answers
import utils.CheckYourAnswers.Ops._

import scala.concurrent.Future

class CheckYourAnswersController @Inject()(appConfig: FrontendAppConfig,
                                         override val messagesApi: MessagesApi,
                                         authenticate: AuthAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         checkYourAnswersFactory: CheckYourAnswersFactory)
                                          extends FrontendController with Retrievals with I18nSupport {

  def onPageLoad(index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      SchemeDetailsId.retrieve.right.map { schemeDetails =>
        val checkYourAnswersHelper = checkYourAnswersFactory.checkYourAnswersHelper(request.userAnswers)

        val companyDetails = AnswerSection(
          Some("messages__common__company_details__title"),
          CompanyDetailsId(index).row(routes.CompanyDetailsController.onPageLoad(CheckMode, index).url) ++
            checkYourAnswersHelper.companyRegistrationNumber(index.id) ++
            checkYourAnswersHelper.companyUniqueTaxReference(index.id)
        )

        val companyContactDetails = AnswerSection(
          Some("messages__establisher_company_contact_details__title"),
          checkYourAnswersHelper.companyAddress(index.id) ++
            checkYourAnswersHelper.companyAddressYears(index.id) ++
            checkYourAnswersHelper.companyPreviousAddress(index.id) ++
            checkYourAnswersHelper.companyContactDetails(index.id)
        )

        Future.successful(Ok(check_your_answers(
          appConfig,
          Seq(companyDetails,companyContactDetails),
          Some(schemeDetails.schemeName),
          routes.CheckYourAnswersController.onSubmit(index)))
        )
      }
  }

  def onSubmit(index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>
      Redirect(controllers.register.establishers.company.routes.AddCompanyDirectorsController.onPageLoad(NormalMode, index))
  }
}
