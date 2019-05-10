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
import identifiers.Identifier
import identifiers.register.establishers.{EstablisherKindId, EstablishersId, IsEstablisherCompleteId}
import identifiers.register.establishers.company._
import identifiers.register.establishers.company.director.{DirectorDetailsId, IsDirectorCompleteId}
import models.Mode.{checkMode, journeyMode}
import models._
import models.person.PersonDetails
import models.register.establishers.EstablisherKind
import models.register.establishers.EstablisherKind.Company
import org.joda.time.LocalDate
import org.scalatest.prop.TableFor6
import org.scalatest.{MustMatchers, OptionValues}
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.{Enumerable, UserAnswers}

//scalastyle:off line.size.limit
//scalastyle:off magic.number

class EstablishersCompanyNavigatorSpec extends SpecBase with MustMatchers with NavigatorBehaviour {

  import EstablishersCompanyNavigatorSpec._

  private def routes(mode: Mode): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id",                                      "User Answers",                   "Next Page (Normal Mode)",                "Save (NM)",    "Next Page (Check Mode)",                 "Save (CM)"),
    (CompanyDetailsId(0),                         emptyAnswers,                     companyVat(mode),                   true,           Some(exitJourney(mode, emptyAnswers)),                   true),
    (CompanyVatId(0),                             emptyAnswers,                     companyPaye(mode),                  true,           Some(exitJourney(mode, emptyAnswers)),                   true),
    (CompanyPayeId(0),                            emptyAnswers,                     companyRegistrationNumber(mode),    true,           Some(exitJourney(mode, emptyAnswers)),                   true),
    (CompanyRegistrationNumberId(0),              emptyAnswers,                     companyUTR(mode),                   true,           Some(exitJourney(mode, emptyAnswers)),                   true),
    (CompanyUniqueTaxReferenceId(0),              emptyAnswers,                     companyPostCodeLookup(mode),        true,           Some(exitJourney(mode, emptyAnswers)),                   true),
    (CompanyPostCodeLookupId(0),                  emptyAnswers,                     companyAddressList(mode),           true,           Some(companyAddressList(checkMode(mode))),      true),
    (CompanyAddressListId(0),                     emptyAnswers,                     companyManualAddress(mode),         true,           Some(companyManualAddress(checkMode(mode))),    true),
    (CompanyAddressId(0),                         emptyAnswers,                     companyAddressYears(mode),          true,           Some(exitJourney(mode, addressYearsOverAYear)),                   true),
    (CompanyAddressYearsId(0),                    addressYearsOverAYear,            companyContactDetails(mode),        true,           Some(exitJourney(mode, addressYearsUnderAYear)),                   true),
    (CompanyAddressYearsId(0),                    addressYearsUnderAYear,           prevAddPostCodeLookup(mode),        true,           Some(prevAddPostCodeLookup(checkMode(mode))),   true),
    (CompanyAddressYearsId(0),                    emptyAnswers,                     sessionExpired,                     false,          Some(sessionExpired),                           false),
    (CompanyPreviousAddressPostcodeLookupId(0),   emptyAnswers,                     companyPaList(mode),                true,           Some(companyPaList(checkMode(mode))),           true),
    (CompanyPreviousAddressListId(0),             emptyAnswers,                     companyPreviousAddress(mode),       true,           Some(companyPreviousAddress(checkMode(mode))),  true),
    (CompanyPreviousAddressId(0),                 emptyAnswers,                     companyContactDetails(mode),        true,           Some(exitJourney(mode, emptyAnswers)),                   true),
    (AddCompanyDirectorsId(0),                    emptyAnswers,                     directorDetails(0, mode),     true,           None,                                           true),
    (AddCompanyDirectorsId(0),                    addCompanyDirectorsTrue,          directorDetails(1, mode),     true,           None,                                           true),
    (AddCompanyDirectorsId(0),                    addCompanyDirectorsFalse,         companyReview(mode),                true,           None,                                           true),
    (AddCompanyDirectorsId(0),                    addOneCompanyDirectors,           sessionExpired,                     false,          None,                                           false),
    (AddCompanyDirectorsId(0),                    addCompanyDirectorsMoreThan10,    otherDirectors(mode),               true,           None,                                           true),
    (OtherDirectorsId(0),                         emptyAnswers,                     companyReview(mode),                true,           Some(companyReview(mode)),                       true),
    (CompanyReviewId(0),                          emptyAnswers,                     addEstablisher(mode),               false,          None,                                           false),
    (CheckYourAnswersId(0),                       emptyAnswers,                     if(mode==UpdateMode) anyMoreChanges else addCompanyDirectors(0, mode), true,           None,                                           false)
  )


  private def normalOnlyRoutes: TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id",                                          "User Answers",               "Next Page (Normal Mode)",                "Save (NM)",  "Next Page (Check Mode)",         "Save (CM)"),
    (CompanyContactDetailsId(0),                  emptyAnswers,                 isDormant(NormalMode),                                true,         Some(checkYourAnswers(NormalMode)),               true),
    (IsCompanyDormantId(0),                       emptyAnswers,                 checkYourAnswers(NormalMode),             true,         Some(checkYourAnswers(NormalMode)),               true)
  )

  private def updateOnlyRoutes: TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id",                                          "User Answers",               "Next Page (Normal Mode)",                "Save (NM)",  "Next Page (Check Mode)",         "Save (CM)"),
    (CompanyContactDetailsId(0),                  emptyAnswers,                 exitJourney(UpdateMode, emptyAnswers),             true,         Some(exitJourney(UpdateMode, emptyAnswers)),               true)
  )

  private def normalRoutes = Table(
    ("Id",                                          "User Answers",               "Next Page (Normal Mode)",                "Save (NM)",  "Next Page (Check Mode)",         "Save (CM)"),
    routes(NormalMode) ++ normalOnlyRoutes: _*
  )

  private def updateRoutes = Table(
    ("Id",                                          "User Answers",               "Next Page (Normal Mode)",                "Save (NM)",  "Next Page (Check Mode)",         "Save (CM)"),
    routes(UpdateMode) ++ updateOnlyRoutes: _*
  )

  private val navigator: EstablishersCompanyNavigator =
    new EstablishersCompanyNavigator(FakeUserAnswersCacheConnector, frontendAppConfig)

  s"${navigator.getClass.getSimpleName}" must {
    appRunning()
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, normalRoutes, dataDescriber)
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, updateRoutes, dataDescriber, UpdateMode)
    behave like nonMatchingNavigator(navigator)
  }
}

//noinspection MutatorLikeMethodIsParameterless
object EstablishersCompanyNavigatorSpec extends OptionValues with Enumerable.Implicits {

  private val johnDoe = PersonDetails("John", None, "Doe", new LocalDate(1862, 6, 9))

  private def validData(directors: PersonDetails*) = {
    Json.obj(
      EstablishersId.toString -> Json.arr(
        Json.obj(
          CompanyDetailsId.toString -> CompanyDetails("test company name"),
          "director" -> directors.map(d => Json.obj(DirectorDetailsId.toString -> Json.toJson(d)))
        )
      )
    )
  }

  private def sessionExpired = controllers.routes.SessionExpiredController.onPageLoad()

  private def companyRegistrationNumber(mode: Mode): Call =
    controllers.register.establishers.company.routes.CompanyRegistrationNumberController.onPageLoad(mode, None, 0)

  private def companyPaye(mode: Mode): Call =
    controllers.register.establishers.company.routes.CompanyPayeController.onPageLoad(mode, 0, None)

  private def companyVat(mode: Mode): Call =
    controllers.register.establishers.company.routes.CompanyVatController.onPageLoad(mode, 0, None)

  private def companyUTR(mode: Mode): Call =
    controllers.register.establishers.company.routes.CompanyUniqueTaxReferenceController.onPageLoad(mode, None, 0)

  private def companyPostCodeLookup(mode: Mode) = controllers.register.establishers.company.routes.CompanyPostCodeLookupController.onPageLoad(mode, None, 0)

  private def companyAddressList(mode: Mode) = controllers.register.establishers.company.routes.CompanyAddressListController.onPageLoad(mode, None, 0)

  private def companyManualAddress(mode: Mode) = controllers.register.establishers.company.routes.CompanyAddressController.onPageLoad(mode, None, 0)

  private def companyAddressYears(mode: Mode) = controllers.register.establishers.company.routes.CompanyAddressYearsController.onPageLoad(mode, None, 0)

  private def prevAddPostCodeLookup(mode: Mode) =
    controllers.register.establishers.company.routes.CompanyPreviousAddressPostcodeLookupController.onPageLoad(mode, None, 0)

  private def companyPaList(mode: Mode) =
    controllers.register.establishers.company.routes.CompanyPreviousAddressListController.onPageLoad(mode, None, 0)

  private def companyPreviousAddress(mode: Mode) =
    controllers.register.establishers.company.routes.CompanyPreviousAddressController.onPageLoad(mode, None, 0)

  private def companyContactDetails(mode: Mode) = controllers.register.establishers.company.routes.CompanyContactDetailsController.onPageLoad(mode, None, 0)

  private def directorDetails(index: Index, mode: Mode) =
    controllers.register.establishers.company.director.routes.DirectorDetailsController.onPageLoad(mode, 0, index, None)

  private def companyReview(mode: Mode) = controllers.register.establishers.company.routes.CompanyReviewController.onPageLoad(mode, None, 0)

  private def otherDirectors(mode: Mode) = controllers.register.establishers.company.routes.OtherDirectorsController.onPageLoad(mode, None, 0)

  private def checkYourAnswers(mode: Mode) = controllers.register.establishers.company.routes.CheckYourAnswersController.onPageLoad(mode, None, 0)

  private def anyMoreChanges = controllers.routes.AnyMoreChangesController.onPageLoad(None)

  private def exitJourney(mode: Mode, answers:UserAnswers) = if (mode == NormalMode) checkYourAnswers(mode) else {
    if(answers.allEstablishersAfterDelete.nonEmpty && answers.allEstablishersCompleted) anyMoreChanges
    else checkYourAnswers(mode)
  }

  private def addEstablisher(mode: Mode) = controllers.register.establishers.routes.AddEstablisherController.onPageLoad(mode, None)

  private def addCompanyDirectors(index: Int, mode: Mode) = controllers.register.establishers.company.routes.AddCompanyDirectorsController.onPageLoad(mode, None, index)

  private def isDormant(mode: Mode) = controllers.register.establishers.company.routes.IsCompanyDormantController.onPageLoad(mode, None, 0)

  private def taskList: Call = controllers.routes.SchemeTaskListController.onPageLoad(NormalMode, None)

  private val emptyAnswers = UserAnswers(Json.obj())
  private val addressYearsOverAYear = UserAnswers(Json.obj())
    .set(CompanyAddressYearsId(0))(AddressYears.OverAYear).flatMap(
    _.set(EstablisherKindId(0))(EstablisherKind.Company).flatMap(
    _.set(AddCompanyDirectorsId(0))(true)).flatMap(
    _.set(IsEstablisherCompleteId(0))(true)).flatMap(
    _.set(IsDirectorCompleteId(0, 0))(true))).asOpt.value
  private val addressYearsUnderAYear = UserAnswers(Json.obj())
    .set(CompanyAddressYearsId(0))(AddressYears.UnderAYear).flatMap(
    _.set(EstablisherKindId(0))(EstablisherKind.Company).flatMap(
    _.set(AddCompanyDirectorsId(0))(true)).flatMap(
    _.set(IsEstablisherCompleteId(0))(true)).flatMap(
    _.set(IsDirectorCompleteId(0, 0))(true))).asOpt.value

  private val addCompanyDirectorsTrue = UserAnswers(validData(johnDoe)).set(AddCompanyDirectorsId(0))(true).asOpt.value
  private val addCompanyDirectorsFalse = UserAnswers(validData(johnDoe)).set(AddCompanyDirectorsId(0))(false).asOpt.value
  private val addCompanyDirectorsMoreThan10 = UserAnswers(validData(Seq.fill(10)(johnDoe): _*))
  private val addOneCompanyDirectors = UserAnswers(validData(johnDoe))

  private def dataDescriber(answers: UserAnswers): String = answers.toString
}
