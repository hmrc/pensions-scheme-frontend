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
import identifiers.register.trustees.company._
import models.{Index, Mode, NormalMode}
import org.scalatest.MustMatchers
import org.scalatest.prop.TableFor3
import play.api.mvc.Call


class TrusteesCompanyNavigatorHnSSpec extends SpecBase with MustMatchers with NavigatorBehaviour {
  val indexZero = Index(0)

  "For Scheme Subscription (Normal Mode)" should {

    lazy val testForNormalMode: TableFor3[Identifier, Map[Identifier, Boolean], Call] = Table(
      ("Id", "User Answers", "Next Page (Normal Mode)"),
      (HasCompanyNumberId(indexZero), Map(HasCompanyNumberId(indexZero) -> true), CompanyRegistrationNumberVariationsController.onPageLoad(NormalMode, None, indexZero)),
      (HasCompanyNumberId(indexZero), Map(HasCompanyNumberId(indexZero) -> false), NoCompanyNumberController.onPageLoad(NormalMode, indexZero, None))
    )

    val navigator: Navigator = injector.instanceOf[TrusteesCompanyNavigatorHnS]

    behave like navigatorWithRoutesForMode(NormalMode)(navigator, testForNormalMode)

  }

  def navigatorWithRoutesForMode(mode: Mode)(navigator: Navigator,
                                             routes: TableFor3[Identifier, Map[Identifier, Boolean], Call],
                                             srn: Option[String] = None): Unit = {

    s"behave like a navigator in ${Mode.jsLiteral.to(mode)} journey" when {

      s"navigating in ${Mode.jsLiteral.to(mode)}" must {

        forAll(routes) {
          (id: Identifier, userAnswers: Map[Identifier, Boolean], call: Call) =>

            s"move from $id to $call in $mode with data: ${userAnswers.toString}" in {
              val result = navigator.nextPage(id, mode, userAnswers, srn)
              result mustBe call
            }

        }

      }

    }
  }

}
