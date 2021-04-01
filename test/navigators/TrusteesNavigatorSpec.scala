/*
 * Copyright 2021 HM Revenue & Customs
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
import controllers.actions.FakeDataRetrievalAction
import identifiers.register.trustees._
import identifiers.register.trustees.individual.TrusteeNameId
import identifiers.{EstablishersOrTrusteesChangedId, Identifier}
import models._
import models.person.PersonName
import models.register.trustees.TrusteeKind
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor3
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.{Enumerable, UserAnswers}

//scalastyle:off magic.number

class TrusteesNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import TrusteesNavigatorSpec._

  val navigator: Navigator =
    applicationBuilder(dataRetrievalAction = new FakeDataRetrievalAction(Some(Json.obj()))).build().injector.instanceOf[Navigator]

  "TrusteesNavigator" when {

    "in NormalMode" must {
      def navigation: TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          row(AddTrusteeId)(true, trusteeKind(0, NormalMode, None)),
          row(AddTrusteeId)(true, trusteeKind(1, NormalMode, None), ua = Some(trustees(1))),
          rowNoValue(AddTrusteeId)(trusteeKind(0, NormalMode, None)),
          rowNoValue(AddTrusteeId)(moreThanTenTrustees(NormalMode, None), ua = Some(trustees(10))),
          row(AddTrusteeId)(false, taskList(NormalMode, None), ua = Some(establishersOrTrusteesChanged)),
          rowNoValue(MoreThanTenTrusteesId)(taskList(NormalMode, None)),
          row(TrusteeKindId(0))(TrusteeKind.Company, companyDetails(NormalMode, None)),
          row(TrusteeKindId(0))(TrusteeKind.Individual, trusteeName(NormalMode, None)),
          row(TrusteeKindId(0))(TrusteeKind.Partnership, partnershipDetails(NormalMode, None)),
          row(HaveAnyTrusteesId)(true, trusteeKind(0, NormalMode, None), ua = Some(trustees(0))),
          row(HaveAnyTrusteesId)(true, trusteeKind(1, NormalMode, None), ua = Some(oneDeletedTrustee)),
          row(HaveAnyTrusteesId)(true, addTrustee(NormalMode, None), ua = Some(oneTrustee)),
          row(HaveAnyTrusteesId)(false, taskList(NormalMode, None)),
          rowNoValue(ConfirmDeleteTrusteeId)(addTrustee(NormalMode, None))
        )
      behave like navigatorWithRoutesForMode(NormalMode)(navigator, navigation, None)
    }

    "in UpdateMode" must {
      def navigation: TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          row(AddTrusteeId)(true, trusteeKind(0, UpdateMode, srn)),
          row(AddTrusteeId)(true, trusteeKind(1, UpdateMode, srn), ua = Some(trustees(1))),
          rowNoValue(AddTrusteeId)(trusteeKind(0, UpdateMode, srn)),
          rowNoValue(AddTrusteeId)(moreThanTenTrustees(UpdateMode, srn), ua = Some(trustees(10))),
          row(AddTrusteeId)(false, taskList(UpdateMode, srn), ua = Some(establishersOrTrusteesChanged)),
          row(TrusteeKindId(0))(TrusteeKind.Company, companyDetails(UpdateMode, srn)),
          row(TrusteeKindId(0))(TrusteeKind.Individual, trusteeName(UpdateMode, srn)),
          row(TrusteeKindId(0))(TrusteeKind.Partnership, partnershipDetails(UpdateMode, srn)),
          rowNoValue(MoreThanTenTrusteesId)(controllers.routes.AnyMoreChangesController.onPageLoad(srn)),
          rowNoValue(ConfirmDeleteTrusteeId)(controllers.routes.AnyMoreChangesController.onPageLoad(srn))
        )
      behave like navigatorWithRoutesForMode(UpdateMode)(navigator, navigation, srn)
    }
  }
}

//noinspection MutatorLikeMethodIsParameterless
object TrusteesNavigatorSpec extends OptionValues with Enumerable.Implicits {

  private val emptyAnswers = UserAnswers()
  private val srnValue     = "123"
  private val srn          = Some(srnValue)

  private def establishersOrTrusteesChanged = emptyAnswers.set(EstablishersOrTrusteesChangedId)(true).asOpt.value

  private def oneDeletedTrustee =
    emptyAnswers
      .set(TrusteeNameId(0))(PersonName("first", "last", isDeleted = true))
      .asOpt
      .value
      .set(TrusteeKindId(0))(TrusteeKind.Individual)
      .asOpt
      .value
      .addTrustee(true)
      .trustees(0)

  private def oneTrustee =
    emptyAnswers
      .haveAnyTrustees(true)
      .addTrustee(true)
      .trustees(1)

  private def trustees(howMany: Int) = emptyAnswers.trustees(howMany)

  private def addTrustee(mode: Mode, srn: Option[String]) = controllers.register.trustees.routes.AddTrusteeController.onPageLoad(mode, srn)

  private def companyDetails(mode: Mode, srn: Option[String]) =
    controllers.register.trustees.company.routes.CompanyDetailsController.onPageLoad(mode, 0, srn)

  private def partnershipDetails(mode: Mode, srn: Option[String]) =
    controllers.register.trustees.partnership.routes.PartnershipDetailsController.onPageLoad(mode, 0, srn)

  private def moreThanTenTrustees(mode: Mode, srn: Option[String]) =
    controllers.register.trustees.routes.MoreThanTenTrusteesController.onPageLoad(mode, srn)

  private def trusteeName(mode: Mode, srn: Option[String]) =
    controllers.register.trustees.individual.routes.TrusteeNameController.onPageLoad(mode, 0, srn)

  private def trusteeKind(index: Int, mode: Mode, srn: Option[String]) =
    controllers.register.trustees.routes.TrusteeKindController.onPageLoad(mode, index, srn)

  private def taskList(mode: Mode, srn: Option[String]) = controllers.routes.PsaSchemeTaskListController.onPageLoad(mode, srn)

  implicit class TrusteeUserAnswersOps(answers: UserAnswers) {

    def haveAnyTrustees(haveTrustees: Boolean): UserAnswers = {
      answers.set(HaveAnyTrusteesId)(haveTrustees).asOpt.value
    }

    def addTrustee(addTrustee: Boolean): UserAnswers = {
      answers.set(AddTrusteeId)(addTrustee).asOpt.value
    }

    def trusteeKind(trusteeKind: TrusteeKind): UserAnswers = {
      answers.set(TrusteeKindId(0))(trusteeKind).asOpt.value
    }

    def trustees(howMany: Int): UserAnswers = {
      if (howMany == 0) {
        answers
      }
      else {
        (0 until howMany).foldLeft(answers) {
          case (newAnswers, i) =>
            newAnswers
              .set(TrusteeNameId(i))(person.PersonName("first", "last")).asOpt.value
              .set(TrusteeKindId(i))(TrusteeKind.Individual).asOpt.value
        }
      }
    }
  }

}
