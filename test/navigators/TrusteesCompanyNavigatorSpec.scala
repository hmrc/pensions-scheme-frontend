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

import base.SpecBase
import controllers.register.trustees.company.routes._
import identifiers.Identifier
import identifiers.register.trustees.IsTrusteeNewId
import identifiers.register.trustees.company._
import models.Mode._
import models._
import org.scalatest.MustMatchers
import org.scalatest.prop.TableFor3
import play.api.mvc.Call
import utils.UserAnswers

class TrusteesCompanyNavigatorSpec extends SpecBase with MustMatchers with NavigatorBehaviour {

  import TrusteesCompanyNavigatorSpec._

  "TrusteesCompanyNavigatorOld" must {

    behave like navigatorWithRoutesForMode(NormalMode)(navigator, normalAndUpdateModeRoutes(NormalMode))
    behave like navigatorWithRoutesForMode(CheckMode)(navigator, routesCheckMode(CheckMode))

    behave like navigatorWithRoutesForMode(UpdateMode)(navigator, normalAndUpdateModeRoutes(UpdateMode))

    behave like navigatorWithRoutesForMode(CheckUpdateMode)(navigator, routesCheckUpdateMode(CheckUpdateMode))
    behave like navigatorWithRoutesForMode(CheckUpdateMode)(navigator, setNewTrusteeIdentifier(routesCheckMode(CheckUpdateMode)))
  }
}

object TrusteesCompanyNavigatorSpec extends SpecBase with NavigatorBehaviour {
  private def setNewTrusteeIdentifier(table: TableFor3[Identifier, UserAnswers, Call]): TableFor3[Identifier, UserAnswers, Call] = table.map(tuple =>
    (tuple._1, tuple._2.set(IsTrusteeNewId(0))(true).asOpt.value, tuple._3)
  )

  private def companyNoPage(mode: Mode): Call = CompanyRegistrationNumberVariationsController.onPageLoad(mode, None, 0)

  private def noCompanyNoPage(mode: Mode): Call = NoCompanyNumberController.onPageLoad(mode, 0, None)

  private def hasCompanyUtrPage(mode: Mode): Call = HasCompanyUTRController.onPageLoad(mode, 0, None)

  private def hasCompanyVatPage(mode: Mode): Call = HasCompanyVATController.onPageLoad(mode, 0, None)

  private def hasCompanyPayePage(mode: Mode): Call = HasCompanyPAYEController.onPageLoad(mode, 0, None)

  private def utrPage(mode: Mode): Call = CompanyUTRController.onPageLoad(mode, None, 0)

  private def noUtrPage(mode: Mode): Call = CompanyNoUTRReasonController.onPageLoad(mode, 0, None)

  private def vatPage(mode: Mode): Call = CompanyVatVariationsController.onPageLoad(mode, 0, None)

  private def payePage(mode: Mode): Call = CompanyPayeVariationsController.onPageLoad(mode, 0, None)

  private def cyaPage(mode: Mode): Call = CheckYourAnswersCompanyDetailsController.onPageLoad(journeyMode(mode), 0, None)


  def normalAndUpdateModeRoutes(mode: Mode): TableFor3[Identifier, UserAnswers, Call] =
    Table(
      ("Id", "UserAnswers", "Next Page"),
      row(HasCompanyNumberId(0))(true, companyNoPage(mode)),
      row(HasCompanyNumberId(0))(false, noCompanyNoPage(mode)),
      row(NoCompanyNumberId(0))(someStringValue, hasCompanyUtrPage(mode)),
      row(CompanyRegistrationNumberVariationsId(0))(someRefValue, hasCompanyUtrPage(mode)),
      row(HasCompanyUTRId(0))(true, utrPage(mode)),
      row(HasCompanyUTRId(0))(false, noUtrPage(mode)),
      row(CompanyNoUTRReasonId(0))(someStringValue, hasCompanyVatPage(mode)),
      row(CompanyUTRId(0))(someStringValue, hasCompanyVatPage(mode)),
      row(HasCompanyVATId(0))(true, vatPage(mode)),
      row(HasCompanyVATId(0))(false, hasCompanyPayePage(mode)),
      row(CompanyVatVariationsId(0))(someRefValue, hasCompanyPayePage(mode)),
      row(HasCompanyPAYEId(0))(true, payePage(mode)),
      row(HasCompanyPAYEId(0))(false, cyaPage(mode)),
      row(CompanyPayeVariationsId(0))(someRefValue, cyaPage(mode))
    )

  def routesCheckMode(mode: Mode): TableFor3[Identifier, UserAnswers, Call] =
    Table(
      ("Id", "UserAnswers", "Next Page"),
      row(HasCompanyNumberId(0))(true, companyNoPage(mode)),
      row(HasCompanyNumberId(0))(false, noCompanyNoPage(mode)),
      row(NoCompanyNumberId(0))(someStringValue, cyaPage(mode)),
      row(CompanyRegistrationNumberVariationsId(0))(someRefValue, cyaPage(mode)),
      row(HasCompanyUTRId(0))(true, utrPage(mode)),
      row(HasCompanyUTRId(0))(false, noUtrPage(mode)),
      row(CompanyNoUTRReasonId(0))(someStringValue, cyaPage(mode)),
      row(CompanyUTRId(0))(someStringValue, cyaPage(mode)),
      row(HasCompanyVATId(0))(true, vatPage(mode)),
      row(HasCompanyVATId(0))(false, cyaPage(mode)),
      row(CompanyVatVariationsId(0))(someRefValue, cyaPage(mode)),
      row(HasCompanyPAYEId(0))(true, payePage(mode)),
      row(HasCompanyPAYEId(0))(false, cyaPage(mode)),
      row(CompanyPayeVariationsId(0))(someRefValue, cyaPage(mode))
    )

  def routesCheckUpdateMode(mode: Mode): TableFor3[Identifier, UserAnswers, Call] = {
    Table(
      ("Id", "UserAnswers", "Next Page"),
      row(CompanyRegistrationNumberVariationsId(0))(someRefValue, anyMoreChangesPage),
      row(CompanyUTRId(0))(someStringValue, anyMoreChangesPage),
      row(CompanyVatVariationsId(0))(someRefValue, anyMoreChangesPage),
      row(CompanyPayeVariationsId(0))(someRefValue, anyMoreChangesPage)
    )
  }

  val navigator: Navigator = injector.instanceOf[TrusteesCompanyNavigator]
}
