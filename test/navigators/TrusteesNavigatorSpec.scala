/*
 * Copyright 2018 HM Revenue & Customs
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
import config.FrontendAppConfig
import connectors.FakeUserAnswersCacheConnector
import identifiers.Identifier
import identifiers.register.SchemeDetailsId
import identifiers.register.trustees._
import identifiers.register.trustees.individual.TrusteeDetailsId
import models.NormalMode
import models.person.PersonDetails
import models.register.trustees.TrusteeKind
import models.register.{SchemeDetails, SchemeType}
import org.joda.time.LocalDate
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor6
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Call
import utils.{Enumerable, UserAnswers}

//scalastyle:off magic.number

class TrusteesNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import TrusteesNavigatorSpec._

  def appConfig(isHubEnabled: Boolean): FrontendAppConfig = new GuiceApplicationBuilder().configure(
    "features.is-hub-enabled" -> isHubEnabled
  ).build().injector.instanceOf[FrontendAppConfig]

  private def routesWithHubEnabled: TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id",                  "User Answers",               "Next Page (Normal Mode)",     "Save (NM)",           "Next Page (Check Mode)",       "Save (CM)"),
    (HaveAnyTrusteesId,       haveAnyTrusteesTrue,          addTrustee,                   true,                   None,                           false),
    (HaveAnyTrusteesId,       haveAnyTrusteesFalse,         taskList,                     false,                   None,                           false),
    (HaveAnyTrusteesId,       emptyAnswers,                 sessionExpired,               false,                  None,                           false),
    (AddTrusteeId,            addTrusteeTrue(0),  trusteeKind(0),         true,                   Some(trusteeKind(0)),    true),
    (AddTrusteeId,            addTrusteeTrue(1),  trusteeKind(1),         true,                   Some(trusteeKind(1)),    true),
    (AddTrusteeId,            emptyAnswers,                 trusteeKind(0),        true,                   Some(trusteeKind(0)),    true),
    (AddTrusteeId,            trustees(10),       moreThanTenTrustees,          true,                   Some(moreThanTenTrustees),      true),
    (AddTrusteeId,            addTrusteeFalse,              taskList,                     false,                  Some(taskList),                 false),
    (MoreThanTenTrusteesId,   emptyAnswers,                 taskList,                     false,                  None,                           false),
    (TrusteeKindId(0),        trusteeKindCompany,           companyDetails,               true,                   None,                           false),
    (TrusteeKindId(0),        trusteeKindIndividual,        trusteeDetails,               true,                   None,                           false),
    (TrusteeKindId(0),        emptyAnswers,                 sessionExpired,               false,                  None,                           false),
    (ConfirmDeleteTrusteeId,  emptyAnswers,                 addTrustee,                   true,                   None,                           false)
  )

  private def routesWithHubDisabled: TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id",                  "User Answers",               "Next Page (Normal Mode)",     "Save (NM)",           "Next Page (Check Mode)",       "Save (CM)"),
    (MoreThanTenTrusteesId, emptyAnswers,                   schemeReview,                 true,                   None,                           false),
    (AddTrusteeId,          addTrusteeFalse,                schemeReview,                 true,                   Some(schemeReview),             true),
    (HaveAnyTrusteesId,     haveAnyTrusteesFalse,          schemeReview,                  true,                   None,                           false)
  )

  private def navigator(isHubEnabled: Boolean = true) = new TrusteesNavigator(FakeUserAnswersCacheConnector, appConfig(isHubEnabled))

  s"${navigator().getClass.getSimpleName} when isHubEnabled toggle is on" must {
    appRunning()
    behave like navigatorWithRoutes(navigator(), FakeUserAnswersCacheConnector, routesWithHubEnabled, dataDescriber)
    behave like nonMatchingNavigator(navigator())
  }

  s"${navigator(false).getClass.getSimpleName} when isHubEnabled toggle is off" must {
    appRunning()
    behave like navigatorWithRoutes(navigator(false), FakeUserAnswersCacheConnector, routesWithHubDisabled, dataDescriber)
    behave like nonMatchingNavigator(navigator(false))
  }
}

//noinspection MutatorLikeMethodIsParameterless
object TrusteesNavigatorSpec extends OptionValues with Enumerable.Implicits {

  private val emptyAnswers = UserAnswers()

  private def addTrusteeFalse = emptyAnswers.addTrustee(false)

  private def addTrusteeTrue(howMany: Int) = emptyAnswers.addTrustee(true).trustees(howMany)

  private def haveAnyTrusteesTrue = emptyAnswers.haveAnyTrustees(true)

  private def haveAnyTrusteesFalse = emptyAnswers.haveAnyTrustees(false)

  private def trustees(howMany: Int) = emptyAnswers.trustees(howMany)

  private def trusteeKindCompany = emptyAnswers.trusteeKind(TrusteeKind.Company)

  private def trusteeKindIndividual = emptyAnswers.trusteeKind(TrusteeKind.Individual)

  private def addTrustee = controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode)

  private def companyDetails = controllers.register.trustees.company.routes.CompanyDetailsController.onPageLoad(NormalMode, 0)

  private def moreThanTenTrustees = controllers.register.trustees.routes.MoreThanTenTrusteesController.onPageLoad(NormalMode)

  private def schemeReview = controllers.register.routes.SchemeReviewController.onPageLoad()

  private def trusteeDetails = controllers.register.trustees.individual.routes.TrusteeDetailsController.onPageLoad(NormalMode, 0)

  private def trusteeKind(index: Int) = controllers.register.trustees.routes.TrusteeKindController.onPageLoad(NormalMode, index)

  private def sessionExpired = controllers.routes.SessionExpiredController.onPageLoad()

  private def taskList = controllers.register.routes.SchemeTaskListController.onPageLoad()

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
          newAnswers.set(TrusteeDetailsId(i))(PersonDetails("first", None, "last", LocalDate.now)).asOpt.value
        }
      }
    }

    def schemeType(schemeType: SchemeType): UserAnswers = {
      answers.set(SchemeDetailsId)(SchemeDetails("test-scheme-name", schemeType)).asOpt.value
    }

  }

}
