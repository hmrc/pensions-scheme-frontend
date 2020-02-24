/*
 * Copyright 2020 HM Revenue & Customs
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

import connectors.FakeUserAnswersCacheConnector
import controllers.routes.AnyMoreChangesController
import identifiers.{Identifier, TypedIdentifier}
import models.Mode.checkMode
import models.address.{Address, TolerantAddress}
import models.person.PersonName
import models.requests.IdentifiedRequest
import models.{Mode, NormalMode, ReferenceValue}
import org.scalatest.exceptions.TableDrivenPropertyCheckFailedException
import org.scalatest.prop.{TableFor3, TableFor6}
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Writes
import play.api.mvc.Call
import uk.gov.hmrc.http.HeaderCarrier
import utils.UserAnswers

import scala.concurrent.ExecutionContext.Implicits.global

trait NavigatorBehaviour extends ScalaCheckPropertyChecks with OptionValues {
  this: WordSpec with MustMatchers =>

  protected implicit val request: IdentifiedRequest = new IdentifiedRequest {
    override def externalId: String = "test-external-id"
  }

  protected implicit val hc: HeaderCarrier = HeaderCarrier()

  protected val someStringValue = "111111"
  protected val somePersonNameValue = PersonName("abc", "def")
  protected val someRefValue = ReferenceValue(someStringValue)
  protected val someTolerantAddress = TolerantAddress(None, None, None, None, None, None)
  protected val someSeqTolerantAddress = Seq(someTolerantAddress)
  protected val someAddress = Address("line 1", "line 2", None, None, None, "GB")

  protected def row(id: TypedIdentifier.PathDependent)(value: id.Data, call: Call, ua: Option[UserAnswers] = None)
                   (implicit writes: Writes[id.Data]): (id.type, UserAnswers, Call) = {
    val userAnswers = ua.fold(UserAnswers())(identity).set(id)(value).asOpt.value
    Tuple3(id, userAnswers, call)
  }

  protected def anyMoreChangesPage(srn: Option[String] = None): Call = AnyMoreChangesController.onPageLoad(srn)

  protected def navigatorWithRoutesForMode(mode: Mode)(navigator: Navigator,
                                                       routes: TableFor3[Identifier, UserAnswers, Call],
                                                       srn: Option[String]): Unit = {
    forAll(routes) {
      (id: Identifier, userAnswers: UserAnswers, call: Call) =>
        s"move from $id to $call in ${Mode.jsLiteral.to(mode)} with data: ${userAnswers.toString}" in {
          val result = navigator.nextPage(id, mode, userAnswers, srn)
          result mustBe call
        }
    }
  }

  //scalastyle:off method.length
  //scalastyle:off regex
  def navigatorWithRoutes[A <: Identifier, B <: Option[Call]](
                                                               navigator: Navigator,
                                                               dataCacheConnector: FakeUserAnswersCacheConnector,
                                                               routes: TableFor6[A, UserAnswers, Call, Boolean, B, Boolean],
                                                               describer: UserAnswers => String,
                                                               mode: Mode = NormalMode,
                                                               srn: Option[String] = None
                                                             ): Unit = {

    s"behave like a navigator in ${Mode.jsLiteral.to(mode)} journey" when {

      s"navigating in ${Mode.jsLiteral.to(mode)}" must {

        try {
          forAll(routes) {
            (id: Identifier, userAnswers: UserAnswers, call: Call, save: Boolean, _: Option[Call], _: Boolean) =>
              s"move from $id to $call in $mode with data: ${describer(userAnswers)}" in {
                val result = navigator.nextPage(id, mode, userAnswers, srn)
                result mustBe call
              }

              s"move from $id to $call and ${if (!save) "not " else ""}save the page with data: ${describer(userAnswers)} and mode $mode" in {
                dataCacheConnector.reset()
                navigator.nextPage(id, mode, userAnswers, srn)
              }
          }
        }
        catch {
          case e: TableDrivenPropertyCheckFailedException =>
            throw e
        }

      }

      s"navigating in ${Mode.jsLiteral.to(checkMode(mode))}" must {

        try {
          if (routes.nonEmpty) {
            forAll(routes) { (id: Identifier, userAnswers: UserAnswers, _: Call, _: Boolean, editCall: Option[Call], save: Boolean) =>
              if (editCall.isDefined) {
                s"move from $id to ${editCall.value} in mode ${checkMode(mode)} with data: ${describer(userAnswers)}" in {
                  val result = navigator.nextPage(id, checkMode(mode), userAnswers, srn)
                  result mustBe editCall.value
                }

                s"move from $id to $editCall and ${if (!save) "not " else ""} in mode ${checkMode(mode)} save the page with data: ${describer(userAnswers)}" in {
                  dataCacheConnector.reset()
                  navigator.nextPage(id, checkMode(mode), userAnswers, srn)

                }
              }
            }
          }
        }
        catch {
          case e: TableDrivenPropertyCheckFailedException =>
            throw e
        }

      }

    }

  }

  //scalastyle:on method.length
  //scalastyle:on regex

  def nonMatchingNavigator(navigator: Navigator, mode : Mode = NormalMode): Unit = {

    val testId: Identifier = new Identifier {}

    s"behaviour like a navigator without normalAndEditModeRoutes with $mode" when {
      "navigating in NormalMode" must {
        "return a call given a non-configured Id" in {
          navigator.nextPage(testId, mode, UserAnswers()) mustBe a[Call]
        }
      }

      "navigating in CheckMode" must {
        "return a call given a non-configured Id" in {
          navigator.nextPage(testId, checkMode(mode), UserAnswers()) mustBe a[Call]
        }
      }
    }

  }

}