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
import controllers.routes.AnyMoreChangesController
import identifiers.register.trustees.IsTrusteeNewId
import identifiers.register.trustees.company._
import identifiers.{Identifier, TypedIdentifier}
import models._
import org.scalatest.MustMatchers
import org.scalatest.prop.TableFor3
import play.api.libs.json.Writes
import play.api.mvc.Call
import utils.UserAnswers

import scala.concurrent.ExecutionContext.Implicits.global

class TrusteesCompanyNavigatorHnSSpec extends SpecBase with MustMatchers with NavigatorBehaviour {
  private val indexZero = Index(0)
  private val stringValue = "111111"
  private val refValue = ReferenceValue(stringValue)

  private def row(id: TypedIdentifier.PathDependent)(value: id.Data, call: Call, ua: Option[UserAnswers] = None)
                 (implicit writes: Writes[id.Data]): (id.type, UserAnswers, Call) = {
    val userAnswers = ua.fold(UserAnswers())(identity).set(id)(value).asOpt.value
    Tuple3(id, userAnswers, call)
  }

  private def setNewTrusteeIdentifier(table: TableFor3[Identifier, UserAnswers, Call]): TableFor3[Identifier, UserAnswers, Call] = table.map(tuple =>
    (tuple._1, tuple._2.set(IsTrusteeNewId(indexZero))(true).asOpt.value, tuple._3)
  )

  private def anyMoreChangesPage: Call = AnyMoreChangesController.onPageLoad(None)

  private def companyNoPage(mode:Mode):Call = CompanyRegistrationNumberVariationsController.onPageLoad(mode, None, indexZero)

  private def noCompanyNoPage(mode:Mode):Call = NoCompanyNumberController.onPageLoad(mode, indexZero, None)

  private def hasCompanyUtrPage(mode:Mode):Call = HasCompanyUTRController.onPageLoad(mode, indexZero, None)

  private def hasCompanyVatPage(mode:Mode):Call = HasCompanyVATController.onPageLoad(mode, indexZero, None)

  private def hasCompanyPayePage(mode:Mode):Call = HasCompanyPAYEController.onPageLoad(mode, indexZero, None)

  private def utrPage(mode:Mode):Call = CompanyUTRController.onPageLoad(mode, None, indexZero)

  private def noUtrPage(mode:Mode):Call = CompanyNoUTRReasonController.onPageLoad(mode, indexZero, None)

  private def vatPage(mode:Mode):Call = CompanyVatVariationsController.onPageLoad(mode, indexZero, None)

  private def noVatPage(mode:Mode):Call = HasCompanyPAYEController.onPageLoad(mode, indexZero, None)

  private def payePage(mode:Mode):Call = CompanyPayeVariationsController.onPageLoad(mode, indexZero, None)

  private def cyaPage(mode:Mode):Call = CheckYourAnswersCompanyDetailsController.onPageLoad(mode, indexZero, None)

  "For Scheme Subscription (Normal Mode)" should {
    def routes(mode: Mode): TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Next Page"),
        row(HasCompanyNumberId(indexZero))(true, companyNoPage(mode)),
        row(HasCompanyNumberId(indexZero))(false, noCompanyNoPage(mode)),
        row(NoCompanyNumberId(indexZero))(stringValue, hasCompanyUtrPage(mode)),
        row(CompanyRegistrationNumberVariationsId(indexZero))(refValue, hasCompanyUtrPage(mode)),
        row(HasCompanyUTRId(indexZero))(true, utrPage(mode)),
        row(HasCompanyUTRId(indexZero))(false, noUtrPage(mode)),
        row(CompanyNoUTRReasonId(indexZero))(stringValue, hasCompanyVatPage(mode)),
        row(CompanyUTRId(indexZero))(stringValue, hasCompanyVatPage(mode)),
        row(HasCompanyVATId(indexZero))(true, vatPage(mode)),
        row(HasCompanyVATId(indexZero))(false, hasCompanyPayePage(mode)),
        row(CompanyVatVariationsId(indexZero))(refValue, hasCompanyPayePage(mode)),
        row(HasCompanyPAYEId(indexZero))(true, payePage(mode)),
        row(HasCompanyPAYEId(indexZero))(false, cyaPage(mode)),
        row(CompanyPayeVariationsId(indexZero))(refValue, cyaPage(mode))
      )

    def routesCheckMode(mode: Mode): TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Next Page"),
        row(HasCompanyNumberId(indexZero))(true, companyNoPage(mode)),
        row(HasCompanyNumberId(indexZero))(false, noCompanyNoPage(mode)),
        row(NoCompanyNumberId(indexZero))(stringValue, cyaPage(mode)),
        row(CompanyRegistrationNumberVariationsId(indexZero))(refValue, cyaPage(mode)),
        row(HasCompanyUTRId(indexZero))(true, utrPage(mode)),
        row(HasCompanyUTRId(indexZero))(false, noUtrPage(mode)),
        row(CompanyNoUTRReasonId(indexZero))(stringValue, cyaPage(mode)),
        row(CompanyUTRId(indexZero))(stringValue, cyaPage(mode)),
        row(HasCompanyVATId(indexZero))(true, vatPage(mode)),
        row(HasCompanyVATId(indexZero))(false, cyaPage(mode)),
        row(CompanyVatVariationsId(indexZero))(refValue, cyaPage(mode)),
        row(HasCompanyPAYEId(indexZero))(true, payePage(mode)),
        row(HasCompanyPAYEId(indexZero))(false, cyaPage(mode)),
        row(CompanyPayeVariationsId(indexZero))(refValue, cyaPage(mode))
      )

    def routesCheckUpdateMode(mode: Mode): TableFor3[Identifier, UserAnswers, Call] = {
      Table(
        ("Id", "UserAnswers", "Next Page"),
        row(CompanyRegistrationNumberVariationsId(indexZero))(refValue, anyMoreChangesPage),
        row(CompanyUTRId(indexZero))(stringValue, anyMoreChangesPage),
        row(CompanyVatVariationsId(indexZero))(refValue, anyMoreChangesPage),
        row(CompanyPayeVariationsId(indexZero))(refValue, anyMoreChangesPage)
      )
    }

    val navigator: Navigator = injector.instanceOf[TrusteesCompanyNavigatorHnS]

    behave like navigatorWithRoutesForMode(NormalMode)(navigator, routes(NormalMode))
    behave like navigatorWithRoutesForMode(CheckMode)(navigator, routesCheckMode(CheckMode))

    behave like navigatorWithRoutesForMode(UpdateMode)(navigator, routes(UpdateMode))

    behave like navigatorWithRoutesForMode(CheckUpdateMode)(navigator, routesCheckUpdateMode(CheckUpdateMode))
    behave like navigatorWithRoutesForMode(CheckUpdateMode)(navigator, setNewTrusteeIdentifier(routesCheckMode(CheckUpdateMode)))
  }

  def navigatorWithRoutesForMode(mode: Mode)(navigator: Navigator,
                                             routes: TableFor3[Identifier, UserAnswers, Call],
                                             srn: Option[String] = None): Unit = {
    s"behave like a navigator in ${Mode.jsLiteral.to(mode)} journey" when {

      s"navigating in ${Mode.jsLiteral.to(mode)}" must {

        forAll(routes) {
          (id: Identifier, userAnswers: UserAnswers, call: Call) =>

            s"move from $id to $call in $mode with data: ${userAnswers.toString}" in {
              val result = navigator.nextPage(id, mode, userAnswers, srn)
              result mustBe call
            }

        }

      }
    }
  }
}
