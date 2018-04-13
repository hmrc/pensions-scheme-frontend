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

package controllers.register.trustees.individual

import javax.inject.Inject

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import identifiers.register.SchemeDetailsId
import identifiers.register.trustees.individual._
import models.{CheckMode, Index, NormalMode}
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
                                           implicit val countryOptions: CountryOptions
                                          ) extends FrontendController with Retrievals with I18nSupport {

  def onPageLoad(index: Index): Action[AnyContent] = (authenticate andThen getData andThen requiredData).async {
    implicit request =>

      val schemeDetailsId = SchemeDetailsId

      schemeDetailsId.retrieve.right.map { schemeDetails =>

        val trusteeDetailsRow = TrusteeDetailsId(index).row(routes.TrusteeDetailsController.onPageLoad(CheckMode, index).url)
        val trusteeNinoRow = TrusteeNinoId(index).row(routes.TrusteeNinoController.onPageLoad(CheckMode, index).url)
        val trusteeUtrRow = UniqueTaxReferenceId(index).row(routes.UniqueTaxReferenceController.onPageLoad(CheckMode, index).url)
        val trusteeAddressRow = TrusteeAddressId(index).row(routes.TrusteeAddressController.onPageLoad(CheckMode, index).url)
        val trusteeAddressYearsRow = TrusteeAddressYearsId(index).row(
          routes.TrusteeAddressYearsController.onPageLoad(CheckMode, index).url)
        val trusteePreviousAddressRow = TrusteePreviousAddressId(index).row(routes.TrusteePreviousAddressController.onPageLoad(CheckMode,
          index).url)
        val trusteeContactDetails = TrusteeContactDetailsId(index).row(routes.TrusteeContactDetailsController.onPageLoad(CheckMode, index).url)

        val trusteeDetailsSection = AnswerSection(None,
          trusteeDetailsRow ++ trusteeNinoRow ++ trusteeUtrRow
        )
        val contactDetailsSection = AnswerSection(
          Some("messages__checkYourAnswers__section__contact_details"),
          trusteeAddressRow ++ trusteeAddressYearsRow ++ trusteePreviousAddressRow ++ trusteeContactDetails
        )

        Future.successful(Ok(check_your_answers(
          appConfig,
          Seq(trusteeDetailsSection, contactDetailsSection),
          Some(s"Trustees for ${schemeDetails.schemeName}"),
          routes.CheckYourAnswersController.onSubmit(index)
        )))
      }
  }

  def onSubmit(index: Index): Action[AnyContent] = (authenticate andThen getData andThen requiredData) {
    implicit request =>
      Redirect(controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode))
  }
}
