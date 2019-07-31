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

  private def companyNoPage(mode:Mode, index: Int, srn:Option[String]): Call = CompanyRegistrationNumberVariationsController.onPageLoad(mode, srn, index)

  private def noCompanyNoPage(mode:Mode, index: Int, srn:Option[String]): Call = NoCompanyNumberController.onPageLoad(mode, index,srn)

  private def utrPage(mode:Mode, index: Int, srn:Option[String]): Call = CompanyUTRController.onPageLoad(mode, srn, index)

  private def noUtrPage(mode:Mode, index: Int, srn:Option[String]): Call = CompanyNoUTRReasonController.onPageLoad(mode, index,srn)

  private def vatPage(mode:Mode, index: Int, srn:Option[String]): Call = CompanyVatVariationsController.onPageLoad(mode, index,srn)

  private def noVatPage(mode:Mode, index: Int, srn:Option[String]): Call = HasCompanyPAYEController.onPageLoad(mode, index,srn)

  private def payePage(mode:Mode, index: Int, srn:Option[String]): Call = CompanyPayeVariationsController.onPageLoad(mode, index,srn)

  private def cyaPage(mode:Mode, index: Int, srn:Option[String]): Call = CheckYourAnswersCompanyDetailsController.onPageLoad(mode, index,srn)


  private def hasCompanyUtrPage(mode:Mode, index: Int, srn:Option[String]):Call = HasCompanyUTRController.onPageLoad(mode, index, srn)

  private def hasCompanyVatPage(mode:Mode, index: Int, srn:Option[String]):Call = HasCompanyVATController.onPageLoad(mode, index, srn)

  private def hasCompanyPayePage(mode:Mode, index: Int, srn:Option[String]):Call = HasCompanyPAYEController.onPageLoad(mode, index, srn)



  private def anyMoreChangesPage(srn: Option[String]): Call = AnyMoreChangesController.onPageLoad(srn)

  private def normalAndUpdateModeRoutes(mode: Mode, ua: UserAnswers, srn: Option[String]): PartialFunction[Identifier, Call] = {
    case id@HasCompanyNumberId(index) => booleanNav(id, ua, mode, index, srn, companyNoPage, noCompanyNoPage)
    case NoCompanyNumberId(index) => hasCompanyUtrPage(mode, index, srn)
    case CompanyRegistrationNumberVariationsId(index) => hasCompanyUtrPage(mode, index, srn)
    case id@HasCompanyUTRId(index) => booleanNav(id, ua, mode, index, srn, utrPage, noUtrPage)
    case CompanyNoUTRReasonId(index) => hasCompanyVatPage(mode, index, srn)
    case CompanyUTRId(index) => hasCompanyVatPage(mode, index, srn)
    case id@HasCompanyVATId(index) => booleanNav(id, ua, mode, index, srn, vatPage, noVatPage)
    case CompanyVatVariationsId(index) => hasCompanyPayePage(mode, index, srn)
    case id@HasCompanyPAYEId(index) => booleanNav(id, ua, mode, index, srn, payePage, cyaPage)
    case CompanyPayeVariationsId(index) => cyaPage(mode, index, srn)
    case _ => controllers.routes.SessionExpiredController.onPageLoad()
  }

  private def checkModeRoutes(mode: Mode, ua: UserAnswers, srn: Option[String]): PartialFunction[Identifier, Call] = {
    case id@HasCompanyNumberId(index) => booleanNav(id, ua, mode, index, srn, companyNoPage, noCompanyNoPage)
    case NoCompanyNumberId(index) => cyaPage(mode, index, srn)
    case CompanyRegistrationNumberVariationsId(index) => cyaPage(mode, index, srn)
    case id@HasCompanyUTRId(index) => booleanNav(id, ua, mode, index, srn, utrPage, noUtrPage)
    case CompanyNoUTRReasonId(index) => cyaPage(mode, index, srn)
    case CompanyUTRId(index) => cyaPage(mode, index, srn)
    case id@HasCompanyVATId(index) => booleanNav(id, ua, mode, index, srn, vatPage, cyaPage)
    case CompanyVatVariationsId(index) => cyaPage(mode, index, srn)
    case id@HasCompanyPAYEId(index) => booleanNav(id, ua, mode, index, srn, payePage, cyaPage)
    case CompanyPayeVariationsId(index) => cyaPage(mode, index, srn)
    case _ => controllers.routes.SessionExpiredController.onPageLoad()
  }

  private def isNewTrustee(ua: UserAnswers, index: Int): Boolean = ua.get(IsTrusteeNewId(index)).getOrElse(false)

  private def checkUpdateModeRoutes(mode: Mode, ua: UserAnswers, srn: Option[String]): PartialFunction[Identifier, Call] = {
    case id@HasCompanyNumberId(index) => booleanNav(id, ua, mode, index, srn, companyNoPage, noCompanyNoPage)
    case NoCompanyNumberId(index) => cyaPage(mode, index, srn)
    case CompanyRegistrationNumberVariationsId(index) if isNewTrustee(ua, index) => cyaPage(mode, index, srn)
    case id@HasCompanyUTRId(index) => booleanNav(id, ua, mode, index, srn, utrPage, noUtrPage)
    case CompanyNoUTRReasonId(index) => cyaPage(mode, index, srn)
    case CompanyUTRId(index) if isNewTrustee(ua, index) => cyaPage(mode, index, srn)
    case id@HasCompanyVATId(index) => booleanNav(id, ua, mode, index, srn, vatPage, cyaPage)
    case CompanyVatVariationsId(index) if isNewTrustee(ua, index) => cyaPage(mode, index, srn)
    case id@HasCompanyPAYEId(index) => booleanNav(id, ua, mode, index, srn, payePage, cyaPage)
    case CompanyPayeVariationsId(index) if isNewTrustee(ua, index) => cyaPage(mode, index, srn)
    case CompanyRegistrationNumberVariationsId(index) => anyMoreChangesPage(srn)
    case CompanyUTRId(index) => anyMoreChangesPage(srn)
    case CompanyVatVariationsId(index) => anyMoreChangesPage(srn)
    case CompanyPayeVariationsId(index) => anyMoreChangesPage(srn)
    case _ => controllers.routes.SessionExpiredController.onPageLoad()
  }

  private def booleanNav(id: TypedIdentifier[Boolean],
                         answers: UserAnswers,
                         mode: Mode,
                         index:Index,
                         srn: Option[String],
                         truePath: (Mode, Int, Option[String]) => Call,
                         falsePath: (Mode, Int, Option[String]) => Call): Call =
    callOrExpired(answers, id,
      if (_: Boolean) {
        truePath(mode, index, srn)
      } else {
        falsePath(mode, index, srn)
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
