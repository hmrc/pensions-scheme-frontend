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
import connectors.FakeUserAnswersCacheConnector
import identifiers.register.trustees._
import identifiers.register.trustees.individual.TrusteeNameId
import identifiers.{EstablishersOrTrusteesChangedId, Identifier}
import models.person.PersonName
import models.register.trustees.TrusteeKind
import models.{Mode, NormalMode, UpdateMode, person}
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor6
import play.api.mvc.Call
import utils.{Enumerable, FakeFeatureSwitchManagementService, UserAnswers}

//scalastyle:off magic.number

class TrusteesNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import TrusteesNavigatorSpec._

  private def routes(mode: Mode, srn: Option[String]): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    (AddTrusteeId, addTrusteeTrue(0), trusteeKind(0, mode, srn), true, None, true),
    (AddTrusteeId, addTrusteeTrue(1), trusteeKind(1, mode, srn), true, None, true),
    (AddTrusteeId, emptyAnswers, trusteeKind(0, mode, srn), true, None, true),
    (AddTrusteeId, trustees(10), moreThanTenTrustees(mode, srn), true, None, true),
    (AddTrusteeId, addTrusteeFalseWithChanges, taskList(mode, srn), false, None, false),
    (MoreThanTenTrusteesId, emptyAnswers,
      if (mode == UpdateMode) controllers.routes.AnyMoreChangesController.onPageLoad(srn) else taskList(mode, srn), false, None, false),
    (TrusteeKindId(0), trusteeKindCompany, companyDetails(mode, srn), true, None, false),
    (TrusteeKindId(0), trusteeKindIndividual, trusteeName(mode, srn) , true, None, false),
    (TrusteeKindId(0), trusteeKindPartnership, partnershipDetails(mode, srn), true, None, false),
    (TrusteeKindId(0), emptyAnswers, sessionExpired, false, None, false),
    (ConfirmDeleteTrusteeId, emptyAnswers, if (mode == UpdateMode) controllers.routes.AnyMoreChangesController.onPageLoad(srn) else addTrustee(mode, srn), true, None, false)
  )

  private def normalOnlyRoutes: TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    (HaveAnyTrusteesId, haveAnyTrusteesTrueWithNoTrustees, trusteeKind(0, NormalMode, None), false, None, false),
    (HaveAnyTrusteesId, haveAnyTrusteesTrueWithOneDeletedTrustee, trusteeKind(1, NormalMode, None), false, None, false),
    (HaveAnyTrusteesId, haveAnyTrusteesTrueWithTrustees, addTrustee(NormalMode, None), true, None, false),
    (HaveAnyTrusteesId, haveAnyTrusteesFalse, taskList(NormalMode, None), false, None, false),
    (HaveAnyTrusteesId, emptyAnswers, sessionExpired, false, None, false)
  )

  private def normalRoutes: TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    normalOnlyRoutes ++ routes(NormalMode, None): _*
  )

  private def updateRoutes = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    routes(UpdateMode, srn): _*
  )

  s"Trustees navigations " must {
    appRunning()
    val navigator: TrusteesNavigator =
      new TrusteesNavigator(FakeUserAnswersCacheConnector, frontendAppConfig, new FakeFeatureSwitchManagementService(true))
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, normalRoutes, dataDescriber)
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, updateRoutes, dataDescriber, UpdateMode, srn)
    behave like nonMatchingNavigator(navigator)
    behave like nonMatchingNavigator(navigator, UpdateMode)
  }
}

//noinspection MutatorLikeMethodIsParameterless
object TrusteesNavigatorSpec extends OptionValues with Enumerable.Implicits {

  private val emptyAnswers = UserAnswers()
  private val srnValue = "123"
  private val srn = Some(srnValue)

  private def addTrusteeFalse = emptyAnswers.addTrustee(false)

  private def addTrusteeFalseWithChanges = emptyAnswers.set(EstablishersOrTrusteesChangedId)(true).asOpt.value.addTrustee(false)

  private def addTrusteeTrue(howMany: Int) = emptyAnswers.addTrustee(true).trustees(howMany)

  private def haveAnyTrusteesTrueWithNoTrustees = emptyAnswers.haveAnyTrustees(true)
    .addTrustee(true).trustees(0)

  private def haveAnyTrusteesTrueWithOneDeletedTrustee = emptyAnswers
    .set(TrusteeNameId(0))(PersonName("first", "last", isDeleted = true)).asOpt.value.
    haveAnyTrustees(true)
    .addTrustee(true).trustees(0)

  private def haveAnyTrusteesTrueWithTrustees = emptyAnswers.haveAnyTrustees(true)
    .addTrustee(true).trustees(1)

  private def haveAnyTrusteesFalse = emptyAnswers.haveAnyTrustees(false)

  private def trustees(howMany: Int) = emptyAnswers.trustees(howMany)

  private def trusteeKindPartnership = emptyAnswers.trusteeKind(TrusteeKind.Partnership)

  private def trusteeKindCompany = emptyAnswers.trusteeKind(TrusteeKind.Company)

  private def trusteeKindIndividual = emptyAnswers.trusteeKind(TrusteeKind.Individual)

  private def addTrustee(mode: Mode, srn: Option[String]) = controllers.register.trustees.routes.AddTrusteeController.onPageLoad(mode, srn)

  private def companyDetails(mode: Mode, srn: Option[String]) = controllers.register.trustees.company.routes.CompanyDetailsController.onPageLoad(mode, 0, srn)

  private def partnershipDetails(mode: Mode, srn: Option[String]) = controllers.register.trustees.partnership.routes.TrusteeDetailsController.onPageLoad(mode, 0, srn)

  private def moreThanTenTrustees(mode: Mode, srn: Option[String]) = controllers.register.trustees.routes.MoreThanTenTrusteesController.onPageLoad(mode, srn)

  private def trusteeName(mode: Mode, srn: Option[String]) =
    controllers.register.trustees.individual.routes.TrusteeNameController.onPageLoad(mode, 0, srn)

  private def trusteeKind(index: Int, mode: Mode, srn: Option[String]) = controllers.register.trustees.routes.TrusteeKindController.onPageLoad(mode, index, srn)

  private def sessionExpired = controllers.routes.SessionExpiredController.onPageLoad()

  private def taskList(mode: Mode, srn: Option[String]) = controllers.routes.SchemeTaskListController.onPageLoad(mode, srn)

  private def dataDescriber(answers: UserAnswers): String = {
    val haveAnyTrustees = answers.get(HaveAnyTrusteesId) map { value =>
      s"haveAnyTrustees: $value"
    }

    val addTrustee = answers.get(AddTrusteeId) map { value =>
      s"addTrustee: $value"
    }

    val trusteeKind = answers.get(TrusteeKindId(0)) map { value =>
      s"trusteeKind: $value"
    }

    val trustees = answers.allTrustees.length match {
      case n if n > 0 => Some(s"trustees: $n")
      case _ => None
    }

    Seq(haveAnyTrustees, addTrustee, trusteeKind, trustees).flatten.mkString(", ")
  }

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
        (0 until howMany).foldLeft(answers) { case (newAnswers, i) =>
          newAnswers.set(TrusteeNameId(i))(person.PersonName("first", "last")).asOpt.value
        }
      }
    }
  }

}
