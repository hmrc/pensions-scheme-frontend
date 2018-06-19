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
import connectors.FakeDataCacheConnector
import identifiers.register.SchemeDetailsId
import identifiers.register.establishers.{AddEstablisherId, ConfirmDeleteEstablisherId, EstablisherKindId}
import identifiers.register.trustees.HaveAnyTrusteesId
import identifiers.register.trustees.individual.TrusteeDetailsId
import models.NormalMode
import models.person.PersonDetails
import models.register.establishers.EstablisherKind
import models.register.{SchemeDetails, SchemeType}
import org.joda.time.LocalDate
import org.scalatest.{MustMatchers, OptionValues}
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.{Enumerable, UserAnswers}

class EstablishersNavigator2Spec extends SpecBase with MustMatchers with NavigatorBehaviour2 {

  import EstablishersNavigator2Spec._

  private def routes = Table(
    ("Id",                          "User Answers",                     "Next Page (Normal Mode)",  "Save (NM)",   "Next Page (Check Mode)",  "Save (CM)"),
    (AddEstablisherId(None),        emptyAnswers,                         establisherKind,           true,          None: Option[Call],       false),
    (AddEstablisherId(Some(true)),  addEstablishersTrue,                  establisherKind,           true,          None: Option[Call],       false),
    (AddEstablisherId(Some(false)), haveTrustee,                          schemeReview,              true,          None: Option[Call],       false),
    (AddEstablisherId(Some(false)), addEstablishersFalseWithSingleTrust,  addTrustee,                true,          None: Option[Call],       false),
    (AddEstablisherId(Some(false)), addEstablishersFalseWithBodyCorporate,haveAnyTrustee,            true,          None: Option[Call],       false),
    (AddEstablisherId(Some(false)), addEstablishersFalseHaveTrusteeTrue,  schemeReview,              true,          None: Option[Call],       false),
    (AddEstablisherId(Some(false)), addEstablishersFalseWithNoScheme,     expired,                   false,         None: Option[Call],       false),
    (EstablisherKindId(0),          company,                              companyDetails,            true,          None: Option[Call],       false),
    (EstablisherKindId(0),          individual,                           individualDetails,         true,          None,                     false),
    (EstablisherKindId(0),          emptyAnswers,                         expired,                   false,         None,                     false),
    (ConfirmDeleteEstablisherId,    emptyAnswers,                         addEstablisher,            true,          None,                     false)
  )

  private val navigator = new EstablishersNavigator2(FakeDataCacheConnector, frontendAppConfig)

  navigator.getClass.getSimpleName must {
    appRunning()
    behave like navigatorWithRoutes(navigator, FakeDataCacheConnector, routes, dataDescriber)
    behave like nonMatchingNavigator(navigator)
  }

}

//noinspection MutatorLikeMethodIsParameterless
object EstablishersNavigator2Spec extends OptionValues with Enumerable.Implicits {

  private val emptyAnswers = UserAnswers(Json.obj())
  private val haveTrustee = UserAnswers(Json.obj(AddEstablisherId.toString -> "true")).
    set(TrusteeDetailsId(0))(PersonDetails("first", None, "last", LocalDate.now)).asOpt.value
  private val addEstablishersFalseWithSingleTrust = UserAnswers(Json.obj(AddEstablisherId.toString -> "false")).
    set(SchemeDetailsId)(SchemeDetails("test scheme", SchemeType.SingleTrust)).asOpt.value
  private val addEstablishersFalseWithBodyCorporate = UserAnswers(Json.obj(AddEstablisherId.toString -> "false")).
    set(SchemeDetailsId)(SchemeDetails("test scheme", SchemeType.BodyCorporate)).asOpt.value
  private val addEstablishersFalseHaveTrusteeTrue = UserAnswers(Json.obj(AddEstablisherId.toString -> "false")).
    set(SchemeDetailsId)(SchemeDetails("test scheme", SchemeType.BodyCorporate)).asOpt.value.set(HaveAnyTrusteesId)(true).asOpt.value
  private val company = UserAnswers().set(EstablisherKindId(0))(EstablisherKind.Company).asOpt.value
  private val individual = UserAnswers().set(EstablisherKindId(0))(EstablisherKind.Indivdual).asOpt.value
  private val addEstablishersTrue = UserAnswers(Json.obj(AddEstablisherId.toString -> "true"))
  private val addEstablishersFalseWithNoScheme = UserAnswers(Json.obj(AddEstablisherId.toString -> "false"))

  private def schemeReview = controllers.register.routes.SchemeReviewController.onPageLoad()

  private def companyDetails = controllers.register.establishers.company.routes.CompanyDetailsController.onPageLoad(NormalMode, 0)

  private def individualDetails = controllers.register.establishers.individual.routes.EstablisherDetailsController.onPageLoad(NormalMode, 0)

  private def establisherKind = controllers.register.establishers.routes.EstablisherKindController.onPageLoad(NormalMode, 0)
  private def addTrustee = controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode)
  private def haveAnyTrustee = controllers.register.trustees.routes.HaveAnyTrusteesController.onPageLoad(NormalMode)
  private def addEstablisher = controllers.register.establishers.routes.AddEstablisherController.onPageLoad(NormalMode)

  private def expired = controllers.routes.SessionExpiredController.onPageLoad()

  private def dataDescriber(answers: UserAnswers): String = answers.toString

}
