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
import identifiers.Identifier
import identifiers.register.SchemeDetailsId
import identifiers.register.establishers.individual._
import identifiers.register.trustees.HaveAnyTrusteesId
import models.register.{SchemeDetails, SchemeType}
import models._
import org.scalatest.prop.TableFor4
import org.scalatest.{MustMatchers, OptionValues}
import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.UserAnswers

class EstablishersIndividualNavigatorSpec extends SpecBase with MustMatchers with NavigatorBehaviour {
  import EstablishersIndividualNavigatorSpec._

  private def navigator(isEstablisherRestricted: Boolean = false) = {
    val application = new GuiceApplicationBuilder()
      .configure(Configuration("microservice.services.features.restrict-establisher" -> isEstablisherRestricted))
    val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
    new EstablishersIndividualNavigator(appConfig)
  }

  private val routesWithNoRestrictedEstablishers: TableFor4[Identifier, UserAnswers, Call, Option[Call]] = Table(
    ("Id",                          "User Answers",             "Next Page (Normal Mode)",                  "Next Page (Check Mode)"),
    (EstablisherDetailsId(0),        emptyAnswers,               establisherNino(NormalMode),                 Some(checkYourAnswers)),
    (EstablisherNinoId(0),           emptyAnswers,               establisherUtr(NormalMode),                  Some(checkYourAnswers)),
    (UniqueTaxReferenceId(0),        emptyAnswers,               postCodeLookup(NormalMode),                  Some(checkYourAnswers)),
    (PostCodeLookupId(0),            emptyAnswers,               addressList(NormalMode),                     Some(addressList(CheckMode))),
    (AddressListId(0),               emptyAnswers,               address(NormalMode),                         Some(address(CheckMode))),
    (AddressId(0),                   emptyAnswers,               addressYears(NormalMode),                    Some(checkYourAnswers)),
    (AddressYearsId(0),              addressYearsOverAYear,      contactDetails,                              Some(checkYourAnswers)),
    (AddressYearsId(0),              addressYearsUnderAYear,     previousAddressPostCodeLookup(NormalMode),   Some(previousAddressPostCodeLookup(CheckMode))),
    (PreviousPostCodeLookupId(0),    emptyAnswers,               previousAddressAddressList(NormalMode),      Some(previousAddressAddressList(CheckMode))),
    (PreviousAddressListId(0),       emptyAnswers,               previousAddress(NormalMode),                 Some(previousAddress(CheckMode))),
    (PreviousAddressId(0),           emptyAnswers,               contactDetails,                              Some(checkYourAnswers)),
    (CheckYourAnswersId,             emptyAnswers,               addEstablisher,                              None)
  )

  s"${navigator().getClass.getSimpleName}" must {
    behave like navigatorWithRoutes(navigator(), routesWithNoRestrictedEstablishers, dataDescriber)
  }
}

object EstablishersIndividualNavigatorSpec extends OptionValues {
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
  private val haveAnyTrustees = controllers.register.trustees.routes.HaveAnyTrusteesController.onPageLoad(NormalMode)
  private val addTrustees = controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode)
  private val schemeReview = controllers.register.routes.SchemeReviewController.onPageLoad()
  private val addEstablisher = controllers.register.establishers.routes.AddEstablisherController.onPageLoad(NormalMode)
  private def dataDescriber(answers: UserAnswers): String = answers.toString
}
