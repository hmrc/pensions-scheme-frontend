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
import config.FeatureSwitchManagementServiceTestImpl
import connectors.FakeUserAnswersCacheConnector
import identifiers.register.establishers.company._
import identifiers.register.establishers.company.director.DirectorDetailsId
import identifiers.register.establishers.{EstablishersId, IsEstablisherNewId}
import identifiers.{EstablishersOrTrusteesChangedId, Identifier}
import models.Mode.{checkMode, journeyMode}
import models._
import models.person.PersonDetails
import navigators.EstablishersIndividualNavigatorSpec.{environment, injector}
import org.joda.time.LocalDate
import org.scalatest.prop.TableFor6
import org.scalatest.{MustMatchers, OptionValues}
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.{Enumerable, FakeFeatureSwitchManagementService, UserAnswers}

//scalastyle:off line.size.limit
//scalastyle:off magic.number

class EstablishersCompanyNavigatorSpec extends SpecBase with MustMatchers with NavigatorBehaviour {

  import EstablishersCompanyNavigatorSpec._

  private def routes(mode: Mode, isPrevAddEnabled : Boolean): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id",                                      "User Answers",                   "Next Page (Normal Mode)",                "Save (NM)",    "Next Page (Check Mode)",                 "Save (CM)"),
    (CompanyDetailsId(0),                         emptyAnswers,                     companyVat(mode),                   true,           Some(exitJourney(mode, emptyAnswers)),                   true),
    (CompanyDetailsId(0),                         newEstablisher,                   companyVat(mode),                   true,           Some(checkYourAnswers(mode)),                   true),
    (HasCompanyNumberId(0),                       emptyAnswers,                     sessionExpired,                     true,           Some(exitJourney(mode, emptyAnswers)),          true),
    (HasCompanyNumberId(0),                       hasCompanyNumber(true),    companyRegistrationNumberNew(mode), true,           Some(exitJourney(mode, emptyAnswers)),          true),
    (HasCompanyNumberId(0),                       hasCompanyNumber(false),   noCompanyRegistrationNumber(mode),  true,           Some(exitJourney(mode, emptyAnswers)),          true),
    (HasCompanyVATId(0),                          emptyAnswers,                     sessionExpired,                     true,           Some(exitJourney(mode, emptyAnswers)),          true),
    (HasCompanyVATId(0),                          hasCompanyVat(true),       companyVatNew(mode),                true,           Some(exitJourney(mode, emptyAnswers)),          true),
    (HasCompanyVATId(0),                          hasCompanyVat(false),      hasCompanyPaye(mode),               true,           Some(exitJourney(mode, emptyAnswers)),          true),
    (CompanyVatId(0),                             emptyAnswers,                     companyPaye(mode),                  true,           Some(exitJourney(mode, emptyAnswers)),                   true),
    (CompanyVatId(0),                             newEstablisher,                   companyPaye(mode),                  true,           Some(checkYourAnswers(mode)),                   true),
    (CompanyPayeId(0),                            emptyAnswers,                     companyRegistrationNumber(mode),    true,           Some(exitJourney(mode, emptyAnswers)),                   true),
    (CompanyPayeId(0),                            newEstablisher,                   companyRegistrationNumber(mode),    true,           Some(checkYourAnswers(mode)),                   true),
    (CompanyRegistrationNumberId(0),              emptyAnswers,                     companyUTR(mode),                   true,           Some(exitJourney(mode, emptyAnswers)),                   true),
    (CompanyRegistrationNumberId(0),              newEstablisher,                   companyUTR(mode),                   true,           Some(checkYourAnswers(mode)),                   true),
    (CompanyUniqueTaxReferenceId(0),              emptyAnswers,                     companyPostCodeLookup(mode),        true,           Some(exitJourney(mode, emptyAnswers)),                   true),
    (CompanyUniqueTaxReferenceId(0),              newEstablisher,                   companyPostCodeLookup(mode),        true,           Some(checkYourAnswers(mode)),                   true),
    (NoCompanyUTRId(0),                           emptyAnswers,                     hasCompanyVat(mode),                true,           Some(exitJourney(mode, emptyAnswers)),                   true),
    (NoCompanyUTRId(0),                           newEstablisher,                   hasCompanyVat(mode),                true,           Some(checkYourAnswers(mode)),                   true),
    (CompanyPostCodeLookupId(0),                  emptyAnswers,                     companyAddressList(mode),           true,           Some(companyAddressList(checkMode(mode))),      true),
    (CompanyAddressListId(0),                     emptyAnswers,                     companyManualAddress(mode),         true,           Some(companyManualAddress(checkMode(mode))),    true),
    (CompanyAddressId(0),                         emptyAnswers,                     companyAddressYears(mode),          true,           if(mode == UpdateMode) Some(companyAddressYears(checkMode(mode))) else Some(checkYourAnswers(mode)),                   true),
    (CompanyAddressId(0),                         newEstablisher,                   companyAddressYears(mode),          true,           Some(checkYourAnswers(mode)),                   true),
    (CompanyAddressYearsId(0),                    addressYearsOverAYear,            companyContactDetails(mode),        true,           Some(exitJourney(mode, addressYearsOverAYear)),                   true),
    (CompanyAddressYearsId(0),                    addressYearsOverAYearNew,         companyContactDetails(mode),        true,           Some(exitJourney(mode, addressYearsOverAYearNew)),                   true),
    (CompanyAddressYearsId(0),                    addressYearsUnderAYear,           prevAddPostCodeLookup(mode),        true,           addressYearsLessThanTwelveEdit(mode, isPrevAddEnabled),           true),
    (CompanyAddressYearsId(0),                    emptyAnswers,                     sessionExpired,                     false,          Some(sessionExpired),                           false),
    (CompanyConfirmPreviousAddressId(0),          confirmPreviousAddressYes,        none,                               false,          Some(anyMoreChanges),                           false),
    (CompanyConfirmPreviousAddressId(0),          confirmPreviousAddressNo,         none,                               false,          Some(prevAddPostCodeLookup(checkMode(mode))),   false),
    (CompanyConfirmPreviousAddressId(0),          emptyAnswers,                     none,                               false,          Some(sessionExpired),                           false),
    (CompanyPreviousAddressPostcodeLookupId(0),   emptyAnswers,                     companyPaList(mode),                true,           Some(companyPaList(checkMode(mode))),           true),
    (CompanyPreviousAddressListId(0),             emptyAnswers,                     companyPreviousAddress(mode),       true,           Some(companyPreviousAddress(checkMode(mode))),  true),
    (CompanyPreviousAddressId(0),                 emptyAnswers,                     companyContactDetails(mode),        true,           Some(exitJourney(mode, emptyAnswers)),                   true),
    (CompanyPreviousAddressId(0),                 newEstablisher,                   companyContactDetails(mode),        true,           Some(exitJourney(mode, newEstablisher)),                   true),
    (AddCompanyDirectorsId(0),                    emptyAnswers,                     directorDetails(0, mode),     true,           None,                                           true),
    (AddCompanyDirectorsId(0),                    addCompanyDirectorsTrue,          directorDetails(1, mode),     true,           None,                                           true),
    (AddCompanyDirectorsId(0),                    addCompanyDirectorsFalse,         if(mode == UpdateMode) taskList else companyReview(mode),                true,           None,                                           true),
    (AddCompanyDirectorsId(0),                    addCompanyDirectorsFalseNewDir,   companyReview(mode),                true,           None,                                           true),
    (AddCompanyDirectorsId(0),                    addOneCompanyDirectors,           sessionExpired,                     false,          None,                                           false),
    (AddCompanyDirectorsId(0),                    addCompanyDirectorsMoreThan10,    otherDirectors(mode),               true,           None,                                           true),
    (OtherDirectorsId(0),                         emptyAnswers,                     if(mode == UpdateMode) anyMoreChanges else companyReview(mode),                true,           Some(companyReview(mode)),                       true),
    (CompanyReviewId(0),                          emptyAnswers,                     if(mode == UpdateMode) anyMoreChanges else addEstablisher(mode),               false,          None,                                           false),
    (CheckYourAnswersId(0),                       emptyAnswers,                     if(mode == UpdateMode) anyMoreChanges else addCompanyDirectors(0, mode), true,           None,                                           false),
    (CheckYourAnswersId(0),                       newEstablisher,                   addCompanyDirectors(0, mode), true,           None,                                           false)
  )


  private def normalOnlyRoutes: TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id",                                          "User Answers",               "Next Page (Normal Mode)",                "Save (NM)",  "Next Page (Check Mode)",         "Save (CM)"),
    (CompanyContactDetailsId(0),                  emptyAnswers,                 isDormant(NormalMode),                                true,         Some(checkYourAnswers(journeyMode(CheckMode))),               true),
    (IsCompanyDormantId(0),                       emptyAnswers,                 checkYourAnswers(NormalMode),             true,         Some(checkYourAnswers(journeyMode(CheckMode))),               true)
  )

  private def updateOnlyRoutes: TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id",                                          "User Answers",               "Next Page (UpdateMode Mode)",                "Save (NM)",  "Next Page (Check Mode)",         "Save (CM)"),
    (CompanyContactDetailsId(0),  emptyAnswers,                         checkYourAnswers(UpdateMode),   true,   Some(exitJourney(checkMode(UpdateMode),   emptyAnswers)),       true),
    (AddCompanyDirectorsId(0),    addCompanyDirectorsFalseWithChanges,  anyMoreChanges,                 true,   None,                                                           true),
    (CompanyVatVariationsId(0),   emptyAnswers,                         none,                           true,   Some(exitJourney(checkMode(UpdateMode),   emptyAnswers)),       true),
    (CompanyPayeVariationsId(0),                  emptyAnswers,                  none,    true,           Some(exitJourney(checkMode(UpdateMode), emptyAnswers)),                   true)
  )

  private def normalRoutes(isPrevAddEnabled : Boolean = false) = Table(
    ("Id",                                          "User Answers",               "Next Page (Normal Mode)",                "Save (NM)",  "Next Page (Check Mode)",         "Save (CM)"),
    routes(NormalMode, isPrevAddEnabled) ++ normalOnlyRoutes: _*
  )

  private def updateRoutes(isPrevAddEnabled : Boolean = false) = Table(
    ("Id",                                          "User Answers",               "Next Page (Normal Mode)",                "Save (NM)",  "Next Page (Check Mode)",         "Save (CM)"),
    routes(UpdateMode, isPrevAddEnabled) ++ updateOnlyRoutes: _*
  )

  private val navigator: EstablishersCompanyNavigator =
    new EstablishersCompanyNavigator(FakeUserAnswersCacheConnector, frontendAppConfig, new FakeFeatureSwitchManagementService(false))

  s"${navigator.getClass.getSimpleName}" must {
    appRunning()
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, normalRoutes(), dataDescriber)
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, updateRoutes(), dataDescriber, UpdateMode)
    behave like nonMatchingNavigator(navigator)
  }

  s"when previous address feature is toggled on" must {
    val navigator: EstablishersCompanyNavigator =
      new EstablishersCompanyNavigator(FakeUserAnswersCacheConnector, frontendAppConfig, new FakeFeatureSwitchManagementService(true))
    appRunning()
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, updateRoutes(true), dataDescriber, UpdateMode)
  }
}

//noinspection MutatorLikeMethodIsParameterless
object EstablishersCompanyNavigatorSpec extends OptionValues with Enumerable.Implicits {
  private val emptyAnswers = UserAnswers(Json.obj())
  private val newEstablisher = UserAnswers(Json.obj()).set(IsEstablisherNewId(0))(true).asOpt.value
  private def hasCompanyNumber(yesNo : Boolean) = UserAnswers(Json.obj()).set(HasCompanyNumberId(0))(yesNo).asOpt.value
  private def hasCompanyVat(yesNo : Boolean) = UserAnswers(Json.obj()).set(HasCompanyVATId(0))(yesNo).asOpt.value
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

  private def none: Call = controllers.routes.IndexController.onPageLoad()
  private def sessionExpired = controllers.routes.SessionExpiredController.onPageLoad()

  private def companyRegistrationNumber(mode: Mode): Call =
    controllers.register.establishers.company.routes.CompanyRegistrationNumberController.onPageLoad(mode, None, 0)

  private def companyRegistrationNumberNew(mode: Mode): Call =
    controllers.register.establishers.company.routes.CompanyRegistrationNumberVariationsController.onPageLoad(mode, None, 0)

  private def noCompanyRegistrationNumber(mode: Mode): Call =
    controllers.register.establishers.company.routes.NoCompanyNumberController.onPageLoad(Index(0))

  private def companyPaye(mode: Mode): Call =
    controllers.register.establishers.company.routes.CompanyPayeController.onPageLoad(mode, 0, None)

  private def companyVat(mode: Mode): Call =
    controllers.register.establishers.company.routes.CompanyVatController.onPageLoad(mode, 0, None)

  private def companyVatNew(mode: Mode): Call =
    controllers.register.establishers.company.routes.CompanyVatVariationsController.onPageLoad(mode, 0, None)

  private def companyUTR(mode: Mode): Call =
    controllers.register.establishers.company.routes.CompanyUniqueTaxReferenceController.onPageLoad(mode, None, 0)

  private def hasCompanyVat(mode: Mode): Call =
    controllers.register.establishers.company.routes.HasCompanyVATController.onPageLoad(mode, None, 0)

  private def hasCompanyPaye(mode: Mode): Call =
    controllers.register.establishers.company.routes.HasCompanyPAYEController.onPageLoad(0)

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

  private def exitJourney(mode: Mode, answers:UserAnswers, index:Int = 0) = if (mode == NormalMode) checkYourAnswers(mode) else {
    if(answers.get(IsEstablisherNewId(index)).getOrElse(false)) checkYourAnswers(mode)
    else anyMoreChanges
  }

  private def confirmPreviousAddress = controllers.register.establishers.company.routes.CompanyConfirmPreviousAddressController.onPageLoad(0, None)


  private def addressYearsLessThanTwelveEdit(mode: Mode, isPrevAddEnabled : Boolean = false) =
    if (checkMode(mode) == CheckUpdateMode && isPrevAddEnabled) Some(confirmPreviousAddress)
    else Some(prevAddPostCodeLookup(checkMode(mode)))

  private def addEstablisher(mode: Mode) = controllers.register.establishers.routes.AddEstablisherController.onPageLoad(mode, None)

  private def addCompanyDirectors(index: Int, mode: Mode) = controllers.register.establishers.company.routes.AddCompanyDirectorsController.onPageLoad(mode, None, index)

  private def isDormant(mode: Mode) = controllers.register.establishers.company.routes.IsCompanyDormantController.onPageLoad(mode, None, 0)

  private def taskList: Call = controllers.routes.SchemeTaskListController.onPageLoad(UpdateMode, None)

  private val addressYearsOverAYearNew = UserAnswers(Json.obj())
    .set(CompanyAddressYearsId(0))(AddressYears.OverAYear).flatMap(_.set(IsEstablisherNewId(0))(true)).asOpt.value
  private val addressYearsOverAYear = UserAnswers(Json.obj())
    .set(CompanyAddressYearsId(0))(AddressYears.OverAYear).asOpt.value
  private val addressYearsUnderAYear = UserAnswers(Json.obj())
    .set(CompanyAddressYearsId(0))(AddressYears.UnderAYear).asOpt.value

  private val addCompanyDirectorsTrue = UserAnswers(validData(johnDoe)).set(AddCompanyDirectorsId(0))(true).asOpt.value
  private val addCompanyDirectorsFalse = UserAnswers(validData(johnDoe)).set(AddCompanyDirectorsId(0))(false).asOpt.value
  private val addCompanyDirectorsFalseWithChanges = UserAnswers(validData(johnDoe)).set(AddCompanyDirectorsId(0))(false).flatMap(
    _.set(EstablishersOrTrusteesChangedId)(true)).asOpt.value

  private val addCompanyDirectorsFalseNewDir = UserAnswers(validData(johnDoe)).set(AddCompanyDirectorsId(0))(false)
    .flatMap(_.set(IsEstablisherNewId(0))(true)).asOpt.value
  private val addCompanyDirectorsMoreThan10 = UserAnswers(validData(Seq.fill(10)(johnDoe): _*))
  private val addOneCompanyDirectors = UserAnswers(validData(johnDoe))

  private val confirmPreviousAddressYes = UserAnswers(Json.obj())
    .set(CompanyConfirmPreviousAddressId(0))(true).asOpt.value
  private val confirmPreviousAddressNo = UserAnswers(Json.obj())
    .set(CompanyConfirmPreviousAddressId(0))(false).asOpt.value

  private def dataDescriber(answers: UserAnswers): String = answers.toString
}
