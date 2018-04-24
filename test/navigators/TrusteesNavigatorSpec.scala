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
import identifiers.register.SchemeDetailsId
import identifiers.register.trustees.individual.TrusteeDetailsId
import identifiers.register.trustees._
import models.NormalMode
import models.person.PersonDetails
import models.register.{SchemeDetails, SchemeType}
import models.register.trustees.TrusteeKind
import org.joda.time.LocalDate
import org.scalatest.OptionValues
import play.api.mvc.Call
import utils.{Enumerable, UserAnswers}

// scalastyle:off magic.number
class TrusteesNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import TrusteesNavigatorSpec._

  private val navigator = new TrusteesNavigator(frontendAppConfig)

  private val routes = Table(
    ("Id",                          "User Answers",             "Next Page (Normal Mode)",  "Next Page (Check Mode)"),
    (HaveAnyTrusteesId,             haveAnyTrusteesTrue,        addTrustee,                 None),
    (HaveAnyTrusteesId,             haveAnyTrusteesFalse,       schemeReview,               None),
    (HaveAnyTrusteesId,             emptyAnswers,               sessionExpired,             None),
    (AddTrusteeId,                  addTrusteeTrue(0),          trusteeKind(0),             Some(trusteeKind(0))),
    (AddTrusteeId,                  addTrusteeTrue(1),          trusteeKind(1),             Some(trusteeKind(1))),
    (AddTrusteeId,                  emptyAnswers,               trusteeKind(0),             Some(trusteeKind(0))),
    (AddTrusteeId,                  trustees(10),               moreThanTenTrustees,        Some(moreThanTenTrustees)),
    (AddTrusteeId,                  addTrusteeFalse,            schemeReview,               Some(schemeReview)),
    (MoreThanTenTrusteesId,         emptyAnswers,               schemeReview,               None),
    (TrusteeKindId(0),              trusteeKindCompany,         companyDetails,             None),
    (TrusteeKindId(0),              trusteeKindIndividual,      trusteeDetails,             None),
    (TrusteeKindId(0),              emptyAnswers,               sessionExpired,             None),
    (ConfirmDeleteTrusteeId,        emptyAnswers,               addTrustee,                 None)
  )

  navigator.getClass.getSimpleName must {
    behave like navigatorWithRoutes(navigator, routes, dataDescriber)
  }

  "trusteeEntryRoutes" must {
    "navigate to HaveAnyTrustees when SchemeType is Group life/death" in {
      val result = TrusteesNavigator.trusteeEntryRoutes()(emptyAnswers.schemeType(SchemeType.GroupLifeDeath))
      result mustBe controllers.register.trustees.routes.HaveAnyTrusteesController.onPageLoad(NormalMode)
    }

    "navigate to HaveAnyTrustees when SchemeType is Body corporate" in {
      val result = TrusteesNavigator.trusteeEntryRoutes()(emptyAnswers.schemeType(SchemeType.BodyCorporate))
      result mustBe controllers.register.trustees.routes.HaveAnyTrusteesController.onPageLoad(NormalMode)
    }

    "navigate to HaveAnyTrustees when SchemeType is Other" in {
      val result = TrusteesNavigator.trusteeEntryRoutes()(emptyAnswers.schemeType(SchemeType.Other("test")))
      result mustBe controllers.register.trustees.routes.HaveAnyTrusteesController.onPageLoad(NormalMode)
    }

    "navigate to AddTrustee when SchemeType is Single trust" in {
      val result = TrusteesNavigator.trusteeEntryRoutes()(emptyAnswers.schemeType(SchemeType.SingleTrust))
      result mustBe controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode)
    }

    "navigate to SessionExpired when there are no SchemeDetails" in {
      val result = TrusteesNavigator.trusteeEntryRoutes()(emptyAnswers)
      result mustBe controllers.routes.SessionExpiredController.onPageLoad()
    }
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
