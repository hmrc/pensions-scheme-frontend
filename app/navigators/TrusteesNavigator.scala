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
import identifiers.register.trustees._
import models.{Mode, NormalMode}
import models.register.{SchemeDetails, SchemeType}
import models.register.trustees.TrusteeKind
import play.api.mvc.Call
import utils.{Enumerable, Navigator, UserAnswers}

class TrusteesNavigator @Inject()(appConfig: FrontendAppConfig) extends Navigator with Enumerable.Implicits {

  override protected def routeMap: PartialFunction[Identifier, UserAnswers => Call] = {
    case HaveAnyTrusteesId =>
      haveAnyTrusteesRoutes()
    case AddTrusteeId =>
      addTrusteeRoutes()
    case MoreThanTenTrusteesId =>
      _ => controllers.register.routes.SchemeReviewController.onPageLoad()
    case TrusteeKindId(index) =>
      trusteeKindRoutes(index)
    case ConfirmDeleteTrusteeId =>
      _ => controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode)
  }

  override protected def editRouteMap: PartialFunction[Identifier, UserAnswers => Call] = {
    case AddTrusteeId =>
      addTrusteeRoutes()
  }

  private def haveAnyTrusteesRoutes()(answers: UserAnswers): Call = {
    answers.get(HaveAnyTrusteesId) match {
      case Some(true) =>
        controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode)
      case Some(false) =>
        controllers.register.routes.SchemeReviewController.onPageLoad()
      case None =>
        controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

  private def addTrusteeRoutes()(answers: UserAnswers): Call = {
    import controllers.register.trustees.routes._
    val trusteesLengthCompare = answers.allTrustees.lengthCompare(appConfig.maxTrustees)

    answers.get(AddTrusteeId) match {
      case Some(false) =>
        controllers.register.routes.SchemeReviewController.onPageLoad()
      case Some(true) =>
        TrusteeKindController.onPageLoad(NormalMode, answers.allTrustees.length)
      case None if trusteesLengthCompare >= 0 =>
        MoreThanTenTrusteesController.onPageLoad(NormalMode)
      case None =>
        TrusteeKindController.onPageLoad(NormalMode, answers.allTrustees.length)
    }
  }

  private def trusteeKindRoutes(index: Int)(answers: UserAnswers): Call = {
    answers.get(TrusteeKindId(index)) match {
      case Some(TrusteeKind.Company) =>
        controllers.register.trustees.company.routes.CompanyDetailsController.onPageLoad(NormalMode, index)
      case Some(TrusteeKind.Individual) =>
        controllers.register.trustees.individual.routes.TrusteeDetailsController.onPageLoad(NormalMode, index)
      case _ =>
        controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

}

object TrusteesNavigator {

  def trusteeEntryRoutes()(answers: UserAnswers): Call = {
    answers.get(SchemeDetailsId) match {
      case Some(SchemeDetails(_, schemeType)) if schemeType == SchemeType.SingleTrust =>
        controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode)
      case Some(SchemeDetails(_, _)) =>
        controllers.register.trustees.routes.HaveAnyTrusteesController.onPageLoad(NormalMode)
      case None =>
        controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

}
