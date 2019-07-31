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
import controllers.routes.AnyMoreChangesController
import identifiers.register.trustees.IsTrusteeNewId
import identifiers.register.trustees.company._
import identifiers.{Identifier, TypedIdentifier}
import models._
import play.api.libs.json.Reads
import play.api.mvc.Call
import utils.UserAnswers

class TrusteesCompanyNavigatorHnS @Inject()(val dataCacheConnector: UserAnswersCacheConnector) extends AbstractNavigator {

  protected def callOrExpired[A](answers: UserAnswers, id: => TypedIdentifier[A], destination: A => Call)(implicit reads: Reads[A]): Call =
    answers.get(id).fold(controllers.routes.SessionExpiredController.onPageLoad())(destination(_))

  protected def routes(from: NavigateFrom, mode: Mode, srn: Option[String]): Call = {
    normalAndUpdateModeRoutes(mode, from.userAnswers, srn)(from.id)
  }

  private def companyNoPage(index: Int): (Mode, Option[String]) => Call = CompanyRegistrationNumberVariationsController.onPageLoad(_: Mode, _: Option[String], index)

  private def noCompanyNoPage(index: Int): (Mode, Option[String]) => Call = NoCompanyNumberController.onPageLoad(_: Mode, index, _: Option[String])

  private def utrPage(index: Int): (Mode, Option[String]) => Call = CompanyUTRController.onPageLoad(_: Mode, _: Option[String], index)

  private def noUtrPage(index: Int): (Mode, Option[String]) => Call = CompanyNoUTRReasonController.onPageLoad(_: Mode, index, _: Option[String])

  private def vatPage(index: Int): (Mode, Option[String]) => Call = CompanyVatVariationsController.onPageLoad(_: Mode, index, _: Option[String])

  private def noVatPage(index: Int): (Mode, Option[String]) => Call = HasCompanyPAYEController.onPageLoad(_: Mode, index, _: Option[String])

  private def payePage(index: Int): (Mode, Option[String]) => Call = CompanyPayeVariationsController.onPageLoad(_: Mode, index, _: Option[String])

  private def cyaPage(index: Int): (Mode, Option[String]) => Call = CheckYourAnswersCompanyDetailsController.onPageLoad(_: Mode, index, _: Option[String])

  private def anyMoreChangesPage(srn: Option[String]): Call = AnyMoreChangesController.onPageLoad(srn)

  private def normalAndUpdateModeRoutes(mode: Mode, ua: UserAnswers, srn: Option[String]): PartialFunction[Identifier, Call] = {
    case id@HasCompanyNumberId(index) => booleanNav(id, ua, mode, srn, companyNoPage(index), noCompanyNoPage(index))
    case NoCompanyNumberId(index) => HasCompanyUTRController.onPageLoad(mode, index, srn)
    case CompanyRegistrationNumberVariationsId(index) => HasCompanyUTRController.onPageLoad(mode, index, srn)
    case id@HasCompanyUTRId(index) => booleanNav(id, ua, mode, srn, utrPage(index), noUtrPage(index))
    case CompanyNoUTRReasonId(index) => HasCompanyVATController.onPageLoad(mode, index, srn)
    case CompanyUTRId(index) => HasCompanyVATController.onPageLoad(mode, index, srn)
    case id@HasCompanyVATId(index) => booleanNav(id, ua, mode, srn, vatPage(index), noVatPage(index))
    case CompanyVatVariationsId(index) => HasCompanyPAYEController.onPageLoad(mode, index, None)
    case id@HasCompanyPAYEId(index) => booleanNav(id, ua, mode, srn, payePage(index), cyaPage(index))
    case CompanyPayeVariationsId(index) => CheckYourAnswersCompanyDetailsController.onPageLoad(mode, index, None)
    case _ => controllers.routes.SessionExpiredController.onPageLoad()
  }

  private def checkModeRoutes(mode: Mode, ua: UserAnswers, srn: Option[String]): PartialFunction[Identifier, Call] = {
    case id@HasCompanyNumberId(index) => booleanNav(id, ua, mode, srn, companyNoPage(index), noCompanyNoPage(index))
    case NoCompanyNumberId(index) => CheckYourAnswersCompanyDetailsController.onPageLoad(mode, index, None)
    case CompanyRegistrationNumberVariationsId(index) => CheckYourAnswersCompanyDetailsController.onPageLoad(mode, index, None)
    case id@HasCompanyUTRId(index) => booleanNav(id, ua, mode, srn, utrPage(index), noUtrPage(index))
    case CompanyNoUTRReasonId(index) => CheckYourAnswersCompanyDetailsController.onPageLoad(mode, index, None)
    case CompanyUTRId(index) => CheckYourAnswersCompanyDetailsController.onPageLoad(mode, index, None)
    case id@HasCompanyVATId(index) => booleanNav(id, ua, mode, srn, vatPage(index), cyaPage(index))
    case CompanyVatVariationsId(index) => CheckYourAnswersCompanyDetailsController.onPageLoad(mode, index, None)
    case id@HasCompanyPAYEId(index) => booleanNav(id, ua, mode, srn, payePage(index), cyaPage(index))
    case CompanyPayeVariationsId(index) => CheckYourAnswersCompanyDetailsController.onPageLoad(mode, index, None)
    case _ => controllers.routes.SessionExpiredController.onPageLoad()
  }

  private def isNewTrustee(ua: UserAnswers, index: Int): Boolean = ua.get(IsTrusteeNewId(index)).getOrElse(false)

  private def checkUpdateModeRoutes(mode: Mode, ua: UserAnswers, srn: Option[String]): PartialFunction[Identifier, Call] = {
    case id@HasCompanyNumberId(index) => booleanNav(id, ua, mode, srn, companyNoPage(index), noCompanyNoPage(index))
    case NoCompanyNumberId(index) => CheckYourAnswersCompanyDetailsController.onPageLoad(mode, index, None)
    case CompanyRegistrationNumberVariationsId(index) if isNewTrustee(ua, index) => CheckYourAnswersCompanyDetailsController.onPageLoad(mode, index, None)
    case id@HasCompanyUTRId(index) => booleanNav(id, ua, mode, srn, utrPage(index), noUtrPage(index))
    case CompanyNoUTRReasonId(index) => CheckYourAnswersCompanyDetailsController.onPageLoad(mode, index, None)
    case CompanyUTRId(index) if isNewTrustee(ua, index) => CheckYourAnswersCompanyDetailsController.onPageLoad(mode, index, None)
    case id@HasCompanyVATId(index) => booleanNav(id, ua, mode, srn, vatPage(index), cyaPage(index))
    case CompanyVatVariationsId(index) if isNewTrustee(ua, index) => CheckYourAnswersCompanyDetailsController.onPageLoad(mode, index, None)
    case id@HasCompanyPAYEId(index) => booleanNav(id, ua, mode, srn, payePage(index), cyaPage(index))
    case CompanyPayeVariationsId(index) if isNewTrustee(ua, index) => CheckYourAnswersCompanyDetailsController.onPageLoad(mode, index, None)
    case CompanyRegistrationNumberVariationsId(index) => anyMoreChangesPage(srn)
    case CompanyUTRId(index) => anyMoreChangesPage(srn)
    case CompanyVatVariationsId(index) => anyMoreChangesPage(srn)
    case CompanyPayeVariationsId(index) => anyMoreChangesPage(srn)
    case _ => controllers.routes.SessionExpiredController.onPageLoad()
  }

  private def booleanNav(id: TypedIdentifier[Boolean],
                         answers: UserAnswers,
                         mode: Mode,
                         srn: Option[String],
                         truePath: (Mode, Option[String]) => Call,
                         falsePath: (Mode, Option[String]) => Call): Call =
    callOrExpired(answers, id,
      if (_: Boolean) {
        truePath(mode, srn)
      } else {
        falsePath(mode, srn)
      }
    )

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] =
    NavigateTo.dontSave(routes(from, NormalMode, None))

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] =
    NavigateTo.dontSave(checkModeRoutes(CheckMode, from.userAnswers, None)(from.id))

  override protected def updateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] =
    NavigateTo.dontSave(normalAndUpdateModeRoutes(UpdateMode, from.userAnswers, srn)(from.id))

  override protected def checkUpdateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] =
    NavigateTo.dontSave(checkUpdateModeRoutes(CheckUpdateMode, from.userAnswers, srn)(from.id))
}
