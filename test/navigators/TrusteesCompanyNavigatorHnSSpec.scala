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

  "For Scheme Subscription (Normal Mode)" should {
    lazy val testForNormalMode: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Next Page"),
        row(HasCompanyNumberId(indexZero))(true, CompanyRegistrationNumberVariationsController.onPageLoad(NormalMode, None, indexZero)),
        row(HasCompanyNumberId(indexZero))(false, NoCompanyNumberController.onPageLoad(NormalMode, indexZero, None)),
        row(NoCompanyNumberId(indexZero))(stringValue, HasCompanyUTRController.onPageLoad(NormalMode, indexZero, None)),
        row(CompanyRegistrationNumberVariationsId(indexZero))(refValue, HasCompanyUTRController.onPageLoad(NormalMode, indexZero, None)),
        row(HasCompanyUTRId(indexZero))(true, CompanyUTRController.onPageLoad(NormalMode, None, indexZero)),
        row(HasCompanyUTRId(indexZero))(false, CompanyNoUTRReasonController.onPageLoad(NormalMode, indexZero, None)),
        row(CompanyNoUTRReasonId(indexZero))(stringValue, HasCompanyVATController.onPageLoad(NormalMode, indexZero, None)),
        row(CompanyUTRId(indexZero))(stringValue, HasCompanyVATController.onPageLoad(NormalMode, indexZero, None)),
        row(HasCompanyVATId(indexZero))(true, CompanyVatVariationsController.onPageLoad(NormalMode, indexZero, None)),
        row(HasCompanyVATId(indexZero))(false, HasCompanyPAYEController.onPageLoad(NormalMode, indexZero, None)),
        row(CompanyVatVariationsId(indexZero))(refValue, HasCompanyPAYEController.onPageLoad(NormalMode, indexZero, None)),
        row(HasCompanyPAYEId(indexZero))(true, CompanyPayeVariationsController.onPageLoad(NormalMode, indexZero, None)),
        row(HasCompanyPAYEId(indexZero))(false, CheckYourAnswersCompanyDetailsController.onPageLoad(NormalMode, indexZero, None)),
        row(CompanyPayeVariationsId(indexZero))(refValue, CheckYourAnswersCompanyDetailsController.onPageLoad(NormalMode, indexZero, None))
      )


    val navigator: Navigator = injector.instanceOf[TrusteesCompanyNavigatorHnS]

    behave like navigatorWithRoutesForMode(NormalMode)(navigator, testForNormalMode)
    //behave like navigatorWithRoutesForMode(UpdateMode)(navigator, testForNormalMode)

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
