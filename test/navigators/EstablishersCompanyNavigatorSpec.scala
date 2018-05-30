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
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.company._
import identifiers.register.establishers.company.director.DirectorDetailsId
import identifiers.register.trustees.HaveAnyTrusteesId
import models._
import models.register.{SchemeDetails, SchemeType}
import models.register.establishers.company.director.DirectorDetails
import org.joda.time.LocalDate
import org.scalatest.prop.TableFor4
import org.scalatest.{MustMatchers, OptionValues}
import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.UserAnswers

class EstablishersCompanyNavigatorSpec extends SpecBase with MustMatchers with NavigatorBehaviour {

  import EstablishersCompanyNavigatorSpec._

  private def navigator(isEstablisherRestricted: Boolean = false) = {
    val application = new GuiceApplicationBuilder()
      .configure(Configuration("microservice.services.features.restrict-establisher" -> isEstablisherRestricted))
    val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
    new EstablishersCompanyNavigator(appConfig)
  }
  
  private val routesWithNoRestrictedEstablishers: TableFor4[Identifier, UserAnswers, Call, Option[Call]] = Table(
    ("Id",                                     "User Answers",                "Next Page (Normal Mode)",             "Next Page (Check Mode)"),
    (CompanyDetailsId(0),                       emptyAnswers,                  companyRegistrationNumber(NormalMode), Some(checkYourAnswers)),
    (CompanyRegistrationNumberId(0),            emptyAnswers,                  companyUTR(NormalMode),                Some(checkYourAnswers)),
    (CompanyUniqueTaxReferenceId(0),            emptyAnswers,                  companyPostCodeLookup(NormalMode),     Some(checkYourAnswers)),
    (CompanyPostCodeLookupId(0),                emptyAnswers,                  companyAddressList(NormalMode),        Some(companyAddressList(CheckMode))),
    (CompanyAddressListId(0),                   emptyAnswers,                  companyManualAddress(NormalMode),      Some(companyManualAddress(CheckMode))),
    (CompanyAddressId(0),                       emptyAnswers,                  companyAddressYears(NormalMode),       Some(checkYourAnswers)),
    (CompanyAddressYearsId(0),                  addressYearsOverAYear,         companyContactDetails,                 Some(checkYourAnswers)),
    (CompanyAddressYearsId(0),                  addressYearsUnderAYear,        prevAddPostCodeLookup(NormalMode),     Some(prevAddPostCodeLookup(CheckMode))),
    (CompanyPreviousAddressPostcodeLookupId(0), emptyAnswers,                  companyPaList(NormalMode),             Some(companyPaList(CheckMode))),
    (CompanyPreviousAddressListId(0),           emptyAnswers,                  companyPreviousAddress(NormalMode),    Some(companyPreviousAddress(CheckMode))),
    (CompanyPreviousAddressId(0),               emptyAnswers,                  companyContactDetails,                 Some(checkYourAnswers)),
    (CompanyContactDetailsId(0),                emptyAnswers,                  checkYourAnswers,                      Some(checkYourAnswers)),
    (AddCompanyDirectorsId(0),                  emptyAnswers,                  directorDetails(0, NormalMode),        Some(directorDetails(0, CheckMode))),
    (AddCompanyDirectorsId(0),                  addCompanyDirectorsTrue,       directorDetails(1, NormalMode),        Some(directorDetails(1, CheckMode))),
    (AddCompanyDirectorsId(0),                  addCompanyDirectorsFalse,      companyReview,                         Some(companyReview)),
    (AddCompanyDirectorsId(0),                  addOneCompanyDirectors,        sessionExpired,                        Some(sessionExpired)),
    (AddCompanyDirectorsId(0),                  addCompanyDirectorsMoreThan10, otherDirectors(NormalMode),            Some(otherDirectors(CheckMode))),
    (OtherDirectorsId(0),                       emptyAnswers,                  companyReview,                         None),
    (CompanyReviewId(0),                        emptyAnswers,                  addEstablisher,                        None)
  )

  s"${navigator().getClass.getSimpleName} when restrict-establisher toggle is off" must {
    behave like navigatorWithRoutes(navigator(), routesWithNoRestrictedEstablishers, dataDescriber)
  }

  //Delete the test case when the restrict-establisher toggle is removed
  private val routesWithRestrictedEstablishers: TableFor4[Identifier, UserAnswers, Call, Option[Call]] = Table(
    ("Id",                                       "User Answers",                      "Next Page (Normal Mode)",                  "Next Page (Check Mode)"),
    (CompanyReviewId(0),                          schemeBodyCorporate,                 haveAnyTrustees,                             None),
    (CompanyReviewId(0),                          schemeSingleTrust,                   addTrustees,                                 None),
    (CompanyReviewId(0),                          hasTrusteeCompaniesForBodyCorporate, schemeReview,                                None),
    (CompanyReviewId(0),                          noTrusteeCompaniesForBodyCorporate,  schemeReview,                                None)
  )

  s"${navigator(true).getClass.getSimpleName} when restrict-establisher toggle is on" must {
    behave like navigatorWithRoutes(navigator(true), routesWithRestrictedEstablishers, dataDescriber)
  }
}

object EstablishersCompanyNavigatorSpec extends OptionValues {
  private val johnDoe = DirectorDetails("John", None, "Doe", new LocalDate(1862, 6, 9))

  private def validData(directors: DirectorDetails*) = {
    Json.obj(
      EstablishersId.toString -> Json.arr(
        Json.obj(
          CompanyDetailsId.toString -> CompanyDetails("test company name", None, None),
          "director" -> directors.map(d => Json.obj(DirectorDetailsId.toString -> Json.toJson(d)))
        )
      )
    )
  }

  private val sessionExpired = controllers.routes.SessionExpiredController.onPageLoad()

  private def companyRegistrationNumber(mode: Mode): Call =
    controllers.register.establishers.company.routes.CompanyRegistrationNumberController.onPageLoad(mode, 0)

  private def companyUTR(mode: Mode): Call =
    controllers.register.establishers.company.routes.CompanyUniqueTaxReferenceController.onPageLoad(mode, 0)

  private def companyPostCodeLookup(mode: Mode) = controllers.register.establishers.company.routes.CompanyPostCodeLookupController.onPageLoad(mode, 0)

  private def companyAddressList(mode: Mode) = controllers.register.establishers.company.routes.CompanyAddressListController.onPageLoad(mode, 0)

  private def companyManualAddress(mode: Mode) = controllers.register.establishers.company.routes.CompanyAddressController.onPageLoad(mode, 0)

  private def companyAddressYears(mode: Mode) = controllers.register.establishers.company.routes.CompanyAddressYearsController.onPageLoad(mode, 0)

  private def prevAddPostCodeLookup(mode: Mode) =
    controllers.register.establishers.company.routes.CompanyPreviousAddressPostcodeLookupController.onPageLoad(mode, 0)

  private def companyPaList(mode: Mode) =
    controllers.register.establishers.company.routes.CompanyPreviousAddressListController.onPageLoad(mode, 0)

  private def companyPreviousAddress(mode: Mode) =
    controllers.register.establishers.company.routes.CompanyPreviousAddressController.onPageLoad(mode, 0)

  private def companyContactDetails = controllers.register.establishers.company.routes.CompanyContactDetailsController.onPageLoad(NormalMode, 0)

  private def directorDetails(index: Index, mode: Mode) =
    controllers.register.establishers.company.director.routes.DirectorDetailsController.onPageLoad(mode, 0, index)

  private val companyReview = controllers.register.establishers.company.routes.CompanyReviewController.onPageLoad(0)

  private def otherDirectors(mode: Mode) = controllers.register.establishers.company.routes.OtherDirectorsController.onPageLoad(mode, 0)

  private val haveAnyTrustees = controllers.register.trustees.routes.HaveAnyTrusteesController.onPageLoad(NormalMode)
  private val addTrustees = controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode)
  private val schemeReview = controllers.register.routes.SchemeReviewController.onPageLoad()

  private def checkYourAnswers = controllers.register.establishers.company.routes.CheckYourAnswersController.onPageLoad(0)

  private val addEstablisher = controllers.register.establishers.routes.AddEstablisherController.onPageLoad(NormalMode)

  private val emptyAnswers = UserAnswers(Json.obj())
  private val addressYearsOverAYear = UserAnswers(Json.obj())
    .set(CompanyAddressYearsId(0))(AddressYears.OverAYear).asOpt.value
  private val addressYearsUnderAYear = UserAnswers(Json.obj())
    .set(CompanyAddressYearsId(0))(AddressYears.UnderAYear).asOpt.value
  private val schemeBodyCorporate = UserAnswers(Json.obj()).set(SchemeDetailsId)(SchemeDetails("test-scheme-name",
    SchemeType.BodyCorporate)).asOpt.value
  private val schemeSingleTrust = UserAnswers(Json.obj()).set(SchemeDetailsId)(SchemeDetails("test-scheme-name",
    SchemeType.SingleTrust)).asOpt.value

  private val addCompanyDirectorsTrue = UserAnswers(validData(johnDoe)).set(AddCompanyDirectorsId(0))(true).asOpt.value
  private val addCompanyDirectorsFalse = UserAnswers(validData(johnDoe)).set(AddCompanyDirectorsId(0))(false).asOpt.value
  private val addCompanyDirectorsMoreThan10 = UserAnswers(validData(Seq.fill(10)(johnDoe): _*))
  private val addOneCompanyDirectors = UserAnswers(validData(johnDoe))
  private val hasTrusteeCompaniesForBodyCorporate = UserAnswers().trusteesCompanyDetails(0, CompanyDetails("test-company-name", None, None)).
    schemeDetails(SchemeDetails("test-scheme-name", SchemeType.BodyCorporate))
  private val noTrusteeCompaniesForBodyCorporate = UserAnswers().schemeDetails(SchemeDetails("test-scheme-name", SchemeType.BodyCorporate)).set(
    HaveAnyTrusteesId)(false).asOpt.value

  private def dataDescriber(answers: UserAnswers): String = answers.toString
}
