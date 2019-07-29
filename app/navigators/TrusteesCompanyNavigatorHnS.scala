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

package navigators

import com.google.inject.Inject
import connectors.UserAnswersCacheConnector
import controllers.register.trustees.company.routes._
import identifiers.{Identifier, TypedIdentifier}
import identifiers.register.trustees.IsTrusteeNewId
import identifiers.register.trustees.company._
import models.{Mode, NormalMode, UpdateMode}
import play.api.libs.json.Reads
import play.api.mvc.Call
import utils.UserAnswers

class TrusteesCompanyNavigatorHnS @Inject()(val dataCacheConnector: UserAnswersCacheConnector) extends AbstractNavigator {

  protected def callOrExpired[A](answers: UserAnswers,
                                 id: => TypedIdentifier[A],
                                 destination: A => Call)(implicit reads: Reads[A]): Call =
    answers.get(id).fold(controllers.routes.SessionExpiredController.onPageLoad())(destination(_))

  protected def routes(from: NavigateFrom, mode: Mode, srn: Option[String]): Call = {
    sharedRoutes(mode, from.userAnswers, srn)(from.id)
  }

  private def sharedRoutes(mode: Mode, ua: UserAnswers, srn: Option[String]): PartialFunction[Identifier, Call] = {
    case HasCompanyNumberId(id) => hasCompanyNumberId(id, ua)
    case NoCompanyNumberId(id) => HasCompanyUTRController.onPageLoad(mode, id, srn)
    case CompanyRegistrationNumberVariationsId(id) => HasCompanyUTRController.onPageLoad(mode, id, srn)
    case HasCompanyUTRId(id) => hasCompanyUTRId(id, ua)
    case CompanyNoUTRReasonId(id) => HasCompanyVATController.onPageLoad(NormalMode, id, srn)
    case CompanyUTRId(id) => HasCompanyVATController.onPageLoad(NormalMode, id, srn)
    case HasCompanyVATId(id) => hasCompanyVatId(id, ua)
    case CompanyVatVariationsId(id) => HasCompanyPAYEController.onPageLoad(NormalMode, id, None)
    case HasCompanyPAYEId(id) => hasCompanyPayeId(id, ua)
    case CompanyPayeVariationsId(id) => CheckYourAnswersCompanyDetailsController.onPageLoad(NormalMode, id, None)
    case _ => controllers.routes.SessionExpiredController.onPageLoad()
  }


  private def hasCompanyPayeId(id: Int, answers: UserAnswers): Call =
    callOrExpired(answers, HasCompanyPAYEId(id),
      if (_: Boolean) {
        CompanyPayeVariationsController.onPageLoad(NormalMode, id, None)
      } else {
        CheckYourAnswersCompanyDetailsController.onPageLoad(NormalMode, id, None)
      }
    )

  private def hasCompanyVatId(id: Int, answers: UserAnswers): Call =
    callOrExpired(answers, HasCompanyVATId(id),
      if (_: Boolean) {
        CompanyVatVariationsController.onPageLoad(NormalMode, id, None)
      } else {
        HasCompanyPAYEController.onPageLoad(NormalMode, id, None)
      }
    )

  private def hasCompanyNumberId(id: Int, answers: UserAnswers): Call =
    callOrExpired(answers, HasCompanyNumberId(id),
      if (_: Boolean) {
        CompanyRegistrationNumberVariationsController.onPageLoad(NormalMode, None, id)
      } else {
        NoCompanyNumberController.onPageLoad(NormalMode, id, None)
      }
    )

  private def hasCompanyUTRId(id: Int, answers: UserAnswers): Call =
    callOrExpired(answers, HasCompanyUTRId(id),
      if (_: Boolean) {
        CompanyUTRController.onPageLoad(NormalMode, None, id)
      } else {
        CompanyNoUTRReasonController.onPageLoad(NormalMode, id, None)
      }
    )

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = NavigateTo.dontSave(routes(from, NormalMode, None))

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] = ???

  private def ook(id:Identifier) = {

    id match {
      case TypedIdentifier[Boolean]
    }

  }

  override protected def updateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = {
    NavigateTo.dontSave(sharedRoutes(UpdateMode, from.userAnswers, srn)(from.id))
  }

  override protected def checkUpdateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = ???
}
