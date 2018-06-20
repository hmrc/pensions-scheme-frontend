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
import connectors.FakeDataCacheConnector
import identifiers.Identifier
import identifiers.register.SchemeDetailsId
import identifiers.register.establishers.individual._
import identifiers.register.trustees.HaveAnyTrusteesId
import models._
import models.register.{SchemeDetails, SchemeType}
import org.scalatest.{MustMatchers, OptionValues}
import org.scalatest.prop.TableFor6
import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.UserAnswers

class EstablishersIndividualNavigator2Spec extends SpecBase with MustMatchers with NavigatorBehaviour2 {
  import EstablishersIndividualNavigator2Spec._

  private val navigator = new EstablishersIndividualNavigator2(frontendAppConfig, FakeDataCacheConnector)

  private def routes: TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id",                          "User Answers",             "Next Page (Normal Mode)",                  "Save (NM)",  "Next Page (Check Mode)",             "Save (CM)"),
    (EstablisherDetailsId(0),        emptyAnswers,               establisherNino(NormalMode),                 true, Some(checkYourAnswers), true),
    (EstablisherNinoId(0),           emptyAnswers,               establisherUtr(NormalMode),                  true, Some(checkYourAnswers), true),
    (UniqueTaxReferenceId(0),        emptyAnswers,               postCodeLookup(NormalMode),                  true, Some(checkYourAnswers), true),
    (PostCodeLookupId(0),            emptyAnswers,               addressList(NormalMode),                     true, Some(addressList(CheckMode)), true),
    (AddressListId(0),               emptyAnswers,               address(NormalMode),                         true, Some(address(CheckMode)), true),
    (AddressId(0),                   emptyAnswers,               addressYears(NormalMode),                    true, Some(checkYourAnswers), true),
    (AddressYearsId(0),              addressYearsOverAYear,      contactDetails,                              true, Some(checkYourAnswers), true),
    (AddressYearsId(0),              addressYearsUnderAYear,     previousAddressPostCodeLookup(NormalMode),   true, Some(previousAddressPostCodeLookup(CheckMode)), true),
    (AddressYearsId(0),              emptyAnswers,               sessionExpired,                              false, Some(sessionExpired),  false),
    (PreviousPostCodeLookupId(0),    emptyAnswers,               previousAddressAddressList(NormalMode),      true, Some(previousAddressAddressList(CheckMode)), true),
    (PreviousAddressListId(0),       emptyAnswers,               previousAddress(NormalMode),                 true, Some(previousAddress(CheckMode)), true),
    (PreviousAddressId(0),           emptyAnswers,               contactDetails,                              true, Some(checkYourAnswers), true),
    (CheckYourAnswersId,             emptyAnswers,               addEstablisher,                              true, None, true)
  )

  s"${navigator.getClass.getSimpleName}" must {
    appRunning()
    behave like navigatorWithRoutes(navigator, FakeDataCacheConnector, routes, dataDescriber)
  }
}

object EstablishersIndividualNavigator2Spec extends OptionValues {
  private val emptyAnswers = UserAnswers(Json.obj())
  private val addressYearsOverAYear = UserAnswers(Json.obj())
    .set(AddressYearsId(0))(AddressYears.OverAYear).asOpt.value
  private val addressYearsUnderAYear = UserAnswers(Json.obj())
    .set(AddressYearsId(0))(AddressYears.UnderAYear).asOpt.value
  private val schemeBodyCorporate = UserAnswers().set(SchemeDetailsId)(SchemeDetails("test-scheme-name", SchemeType.BodyCorporate)).asOpt.value
  private val schemeSingleTrust = UserAnswers().set(SchemeDetailsId)(SchemeDetails("test-scheme-name", SchemeType.SingleTrust)).asOpt.value
  private val hasTrusteeCompanies = UserAnswers().trusteesCompanyDetails(0, CompanyDetails("test-company-name", None, None))
  private val bodyCorporateWithNoTrustees =
    UserAnswers().schemeDetails(SchemeDetails("test-scheme-name", SchemeType.BodyCorporate)).set(HaveAnyTrusteesId)(false).asOpt.value

  private def establisherNino(mode: Mode) = controllers.register.establishers.individual.routes.EstablisherNinoController.onPageLoad(mode, 0)
  private def establisherUtr(mode: Mode) = controllers.register.establishers.individual.routes.UniqueTaxReferenceController.onPageLoad(mode, 0)
  private def postCodeLookup(mode: Mode) = controllers.register.establishers.individual.routes.PostCodeLookupController.onPageLoad(mode, 0)
  private def addressList(mode: Mode) = controllers.register.establishers.individual.routes.AddressListController.onPageLoad(mode, 0)
  private def address(mode: Mode) = controllers.register.establishers.individual.routes.AddressController.onPageLoad(mode, 0)
  private def addressYears(mode: Mode) = controllers.register.establishers.individual.routes.AddressYearsController.onPageLoad(mode, 0)
  private def checkYourAnswers = controllers.register.establishers.individual.routes.CheckYourAnswersController.onPageLoad(0)
  private def contactDetails = controllers.register.establishers.individual.routes.ContactDetailsController.onPageLoad(NormalMode, 0)
  private def previousAddressPostCodeLookup(mode: Mode) =
    controllers.register.establishers.individual.routes.PreviousAddressPostCodeLookupController.onPageLoad(mode, 0)
  private def previousAddressAddressList(mode: Mode) = controllers.register.establishers.individual.routes.PreviousAddressListController.onPageLoad(mode, 0)
  private def previousAddress(mode: Mode) = controllers.register.establishers.individual.routes.PreviousAddressController.onPageLoad(mode, 0)
  private def haveAnyTrustees = controllers.register.trustees.routes.HaveAnyTrusteesController.onPageLoad(NormalMode)
  private def addTrustees = controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode)
  private def schemeReview = controllers.register.routes.SchemeReviewController.onPageLoad()
  private def addEstablisher = controllers.register.establishers.routes.AddEstablisherController.onPageLoad(NormalMode)
  private def dataDescriber(answers: UserAnswers): String = answers.toString
  private def sessionExpired = controllers.routes.SessionExpiredController.onPageLoad()
}


