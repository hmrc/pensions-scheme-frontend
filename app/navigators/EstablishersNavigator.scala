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

package navigators

import com.google.inject.Inject
import config.FrontendAppConfig
import identifiers.Identifier
import identifiers.register.SchemeDetailsId
import identifiers.register.establishers.{AddEstablisherId, ConfirmDeleteEstablisherId, EstablisherKindId}
import identifiers.register.trustees.HaveAnyTrusteesId
import models.NormalMode
import models.register.{SchemeDetails, SchemeType}
import models.register.establishers.EstablisherKind
import play.api.mvc.Call
import utils.{Enumerable, Navigator, UserAnswers}

class EstablishersNavigator @Inject()(config: FrontendAppConfig) extends Navigator with Enumerable.Implicits {

  override protected def routeMap: PartialFunction[Identifier, UserAnswers => Call] = {
    case AddEstablisherId(value) => addEstablisherRoutes(value)
    case EstablisherKindId(index) => establisherKindRoutes(index)
    case ConfirmDeleteEstablisherId =>
      _ => controllers.register.establishers.routes.AddEstablisherController.onPageLoad(NormalMode)
  }

  private def establisherKindRoutes(index: Int)(answers: UserAnswers): Call = {
    answers.get(EstablisherKindId(index)) match {
      case Some(EstablisherKind.Company) =>
        controllers.register.establishers.company.routes.CompanyDetailsController.onPageLoad(NormalMode, index)
      case Some(EstablisherKind.Indivdual) =>
        controllers.register.establishers.individual.routes.EstablisherDetailsController.onPageLoad(NormalMode, index)
      case _ =>
        controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

  private def addEstablisherRoutes(value: Option[Boolean])(answers: UserAnswers): Call = {
    value match {
      case Some(false) =>
        if (answers.allTrustees.nonEmpty) {
          controllers.register.routes.SchemeReviewController.onPageLoad()
        } else {
          navigateBasedOnSchemeDetails(answers)
        }
      case _ =>
        controllers.register.establishers.routes.EstablisherKindController.onPageLoad(NormalMode, answers.allEstablishers.length)
    }
  }

  private def navigateBasedOnSchemeDetails(answers: UserAnswers) = {
    answers.get(SchemeDetailsId) match {
      case Some(SchemeDetails(_, schemeType)) if schemeType == SchemeType.SingleTrust =>
        controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode)
      case Some(SchemeDetails(_, _)) =>
        answers.get(HaveAnyTrusteesId) match {
          case None =>
            controllers.register.trustees.routes.HaveAnyTrusteesController.onPageLoad(NormalMode)
          case _ =>
            controllers.register.routes.SchemeReviewController.onPageLoad()
        }
      case None =>
        controllers.routes.SessionExpiredController.onPageLoad()
    }
  }
}
