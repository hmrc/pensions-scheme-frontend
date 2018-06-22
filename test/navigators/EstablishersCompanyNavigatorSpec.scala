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
import identifiers.Identifier
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.company._
import identifiers.register.establishers.company.director.DirectorDetailsId
import models._
import models.register.establishers.company.director.DirectorDetails
import org.joda.time.LocalDate
import org.scalatest.prop.TableFor6
import org.scalatest.{MustMatchers, OptionValues}
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.UserAnswers

//scalastyle:off line.size.limit
//scalastyle:off magic.number

class EstablishersCompanyNavigatorSpec extends SpecBase with MustMatchers with NavigatorBehaviour {

  import EstablishersCompanyNavigatorSpec._

  private def routesWithNoRestrictedEstablishers: TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id",                                     "User Answers",                 "Next Page (Normal Mode)",             "Save (NM)",  "Next Page (Check Mode)",                 "Save (CM)"),
    (CompanyDetailsId(0),                       emptyAnswers,                  companyRegistrationNumber(NormalMode), true,         Some(checkYourAnswers),                   true),
    (CompanyRegistrationNumberId(0),            emptyAnswers,                  companyUTR(NormalMode),                true,         Some(checkYourAnswers),                   true),
    (CompanyUniqueTaxReferenceId(0),            emptyAnswers,                  companyPostCodeLookup(NormalMode),     true,         Some(checkYourAnswers),                   true),
    (CompanyPostCodeLookupId(0),                emptyAnswers,                  companyAddressList(NormalMode),        true,         Some(companyAddressList(CheckMode)),      true),
    (CompanyAddressListId(0),                   emptyAnswers,                  companyManualAddress(NormalMode),      true,         Some(companyManualAddress(CheckMode)),    true),
    (CompanyAddressId(0),                       emptyAnswers,                  companyAddressYears(NormalMode),       true,         Some(checkYourAnswers),                   true),
    (CompanyAddressYearsId(0),                  addressYearsOverAYear,         companyContactDetails,                 true,         Some(checkYourAnswers),                   true),
    (CompanyAddressYearsId(0),                  addressYearsUnderAYear,        prevAddPostCodeLookup(NormalMode),     true,         Some(prevAddPostCodeLookup(CheckMode)),   true),
    (CompanyAddressYearsId(0),                  emptyAnswers,                  sessionExpired,                        false,        Some(sessionExpired),                     false ),
    (CompanyPreviousAddressPostcodeLookupId(0), emptyAnswers,                  companyPaList(NormalMode),             true,         Some(companyPaList(CheckMode)),           true),
    (CompanyPreviousAddressListId(0),           emptyAnswers,                  companyPreviousAddress(NormalMode),    true,         Some(companyPreviousAddress(CheckMode)),  true),
    (CompanyPreviousAddressId(0),               emptyAnswers,                  companyContactDetails,                 true,         Some(checkYourAnswers),                   true),
    (CompanyContactDetailsId(0),                emptyAnswers,                  checkYourAnswers,                      true,         Some(checkYourAnswers),                   true),
    (AddCompanyDirectorsId(0),                  emptyAnswers,                  directorDetails(0, NormalMode),        true,         Some(directorDetails(0, CheckMode)),      true),
    (AddCompanyDirectorsId(0),                  addCompanyDirectorsTrue,       directorDetails(1, NormalMode),        true,         Some(directorDetails(1, CheckMode)),      true),
    (AddCompanyDirectorsId(0),                  addCompanyDirectorsFalse,      companyReview,                         true,         Some(companyReview),                      true),
    (AddCompanyDirectorsId(0),                  addOneCompanyDirectors,        sessionExpired,                        false,        Some(sessionExpired),                     false),
    (AddCompanyDirectorsId(0),                  addCompanyDirectorsMoreThan10, otherDirectors(NormalMode),            true,         Some(otherDirectors(CheckMode)),          true),
    (OtherDirectorsId(0),                       emptyAnswers,                  companyReview,                         true,         Some(companyReview),                      true),
    (CompanyReviewId(0),                        emptyAnswers,                  addEstablisher,                        true,         None,                                     false),
    (CheckYourAnswersId(0),                     emptyAnswers,                  addCompanyDirectors(0, NormalMode),    true,         None,                                     false)
  )

  "EstablishersCompanyNavigator when restrict-establisher toggle is off" must {
    appRunning()
    val navigator = new EstablishersCompanyNavigator(FakeDataCacheConnector, frontendAppConfig)
    behave like navigatorWithRoutes(navigator, FakeDataCacheConnector, routesWithNoRestrictedEstablishers, dataDescriber)
    behave like nonMatchingNavigator(navigator)
  }

}

//noinspection MutatorLikeMethodIsParameterless
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

  private def sessionExpired = controllers.routes.SessionExpiredController.onPageLoad()

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

  private def companyReview = controllers.register.establishers.company.routes.CompanyReviewController.onPageLoad(0)

  private def otherDirectors(mode: Mode) = controllers.register.establishers.company.routes.OtherDirectorsController.onPageLoad(mode, 0)

  private def checkYourAnswers = controllers.register.establishers.company.routes.CheckYourAnswersController.onPageLoad(0)

  private def addEstablisher = controllers.register.establishers.routes.AddEstablisherController.onPageLoad(NormalMode)

  private def addCompanyDirectors(index: Int, mode: Mode) = controllers.register.establishers.company.routes.AddCompanyDirectorsController.onPageLoad(mode, index)

  private val emptyAnswers = UserAnswers(Json.obj())
  private val addressYearsOverAYear = UserAnswers(Json.obj())
    .set(CompanyAddressYearsId(0))(AddressYears.OverAYear).asOpt.value
  private val addressYearsUnderAYear = UserAnswers(Json.obj())
    .set(CompanyAddressYearsId(0))(AddressYears.UnderAYear).asOpt.value

  private val addCompanyDirectorsTrue = UserAnswers(validData(johnDoe)).set(AddCompanyDirectorsId(0))(true).asOpt.value
  private val addCompanyDirectorsFalse = UserAnswers(validData(johnDoe)).set(AddCompanyDirectorsId(0))(false).asOpt.value
  private val addCompanyDirectorsMoreThan10 = UserAnswers(validData(Seq.fill(10)(johnDoe): _*))
  private val addOneCompanyDirectors = UserAnswers(validData(johnDoe))

  private def dataDescriber(answers: UserAnswers): String = answers.toString

}
