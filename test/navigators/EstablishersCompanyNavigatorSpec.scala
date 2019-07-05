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
import identifiers.register.establishers.company._
import identifiers.register.establishers.company.director.DirectorDetailsId
import identifiers.register.establishers.{EstablishersId, ExistingCurrentAddressId, IsEstablisherNewId}
import identifiers.{EstablishersOrTrusteesChangedId, Identifier}
import models.Mode.{checkMode, journeyMode}
import models._
import models.person.PersonDetails
import org.joda.time.LocalDate
import org.scalatest.prop.TableFor6
import org.scalatest.{MustMatchers, OptionValues}
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.{Enumerable, FakeFeatureSwitchManagementService, UserAnswers}

//scalastyle:off line.size.limit
//scalastyle:off magic.number

class EstablishersCompanyNavigatorSpec extends SpecBase with MustMatchers with NavigatorBehaviour {

  import EstablishersCompanyNavigatorSpec._

  private def routes(mode: Mode, toggled: Boolean = false): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id",                                      "User Answers",                   "Next Page (Normal Mode)",                "Save (NM)",    "Next Page (Check Mode)",                 "Save (CM)"),
    (CompanyDetailsId(0),                         emptyAnswers,                     companyVat(mode),                   true,           Some(exitJourney(mode, emptyAnswers, 0, cya(mode))),                   true),
    (CompanyDetailsId(0),                         newEstablisher,                   companyVat(mode),                   true,           Some(cya(mode)),                                                             true),
    (HasCompanyNumberId(0),                       emptyAnswers,                     sessionExpired,                     true,           Some(exitJourney(mode, emptyAnswers, 0, cyaCompanyDetails(mode))),          true),
    (HasCompanyNumberId(0),                       hasCompanyNumber(true),    companyRegistrationNumberNew(mode), true,           Some(exitJourney(mode, emptyAnswers, 0, cyaCompanyDetails(mode))),          true),
    (HasCompanyNumberId(0),                       hasCompanyNumber(false),   noCompanyRegistrationNumber(mode),  true,           Some(exitJourney(mode, emptyAnswers, 0, cyaCompanyDetails(mode))),          true),
    (HasCompanyVATId(0),                          emptyAnswers,                     sessionExpired,                     true,           Some(exitJourney(mode, emptyAnswers, 0, cyaCompanyDetails(mode))),          true),
    (HasCompanyVATId(0),                          hasCompanyVat(true),       companyVatNew(mode),                true,           Some(exitJourney(mode, emptyAnswers, 0, cyaCompanyDetails(mode))),          true),
    (HasCompanyVATId(0),                          hasCompanyVat(false),      hasCompanyPaye(mode),               true,           Some(exitJourney(mode, emptyAnswers, 0, cyaCompanyDetails(mode))),          true),
    (CompanyVatId(0),                             emptyAnswers,                     companyPaye(mode),                  true,           Some(exitJourney(mode, emptyAnswers, 0, cya(mode))),                   true),
    (CompanyVatId(0),                             newEstablisher,                   companyPaye(mode),                  true,           Some(cya(mode)),                                                             true),
    (CompanyVatVariationsId(0),                   emptyAnswers,                     sessionExpired,                     true,           Some(exitJourney(mode, emptyAnswers, 0, cya(mode))),          true),
    (CompanyVatVariationsId(0),                   establisherEnteredPAYE,           companyHasPaye(mode),               true,           Some(exitJourney(mode, emptyAnswers, 0, cya(mode))),          true),
    (HasCompanyPAYEId(0),                         emptyAnswers,                     sessionExpired,                     true,           Some(exitJourney(mode, emptyAnswers, 0, cya(mode))),          true),
    (HasCompanyPAYEId(0),                         establisherHasPAYE(true),         whatIsPAYE(mode),                   true,           Some(exitJourney(mode, emptyAnswers, 0, cya(mode))),          true),
    (HasCompanyPAYEId(0),                         establisherHasPAYE(false),        cyaCompanyDetails(mode),            true,           Some(exitJourney(mode, emptyAnswers, 0, cya(mode))),          true),
    (CompanyPayeId(0),                            emptyAnswers,                     companyRegistrationNumber(mode),    true,           Some(exitJourney(mode, emptyAnswers, 0, cya(mode))),                   true),
    (CompanyPayeId(0),                            newEstablisher,                   companyRegistrationNumber(mode),    true,           Some(cya(mode)),                                                             true),
    (NoCompanyNumberId(0),                        emptyAnswers,                     hasCompanyUTR(mode),                true,           Some(exitJourney(mode, emptyAnswers, 0, cya(mode))),                   true),
    (NoCompanyNumberId(0),                        newEstablisher,                   hasCompanyUTR(mode),                true,           Some(cya(mode)),                   true),
    (HasCompanyUTRId(0),                          emptyAnswers,                     sessionExpired,                     true,           Some(exitJourney(mode, emptyAnswers, 0, cyaCompanyDetails(mode))),                   true),
    (HasCompanyUTRId(0),                          hasCompanyUtr(true),       companyUTRNew(mode),                true,           Some(exitJourney(mode, emptyAnswers, 0, cyaCompanyDetails(mode))),                   true),
    (HasCompanyUTRId(0),                          hasCompanyUtr(false),      noCompanyUTR(mode),                 true,           Some(exitJourney(mode, emptyAnswers, 0, cyaCompanyDetails(mode))),                   true),
    (CompanyUniqueTaxReferenceId(0),              emptyAnswers,                     companyPostCodeLookup(mode),        true,           Some(exitJourney(mode, emptyAnswers, 0, cya(mode))),                    true),
    (CompanyUniqueTaxReferenceId(0),              newEstablisher,                   companyPostCodeLookup(mode),        true,           Some(cya(mode)),                                                              true),
    (CompanyUTRId(0),                             emptyAnswers,                     hasCompanyVat(mode),                true,           Some(exitJourney(mode, emptyAnswers, 0, cyaCompanyDetails(mode))),                   true),
    (CompanyUTRId(0),                             newEstablisher,                   hasCompanyVat(mode),                true,           Some(cyaCompanyDetails(mode)),                                              true),
    (NoCompanyUTRId(0),                           emptyAnswers,                     hasCompanyVat(mode),                true,           Some(exitJourney(mode, emptyAnswers, 0, cyaCompanyDetails(mode))),                   true),
    (NoCompanyUTRId(0),                           newEstablisher,                   hasCompanyVat(mode),                true,           Some(cyaCompanyDetails(mode)),                                              true),
    (CompanyPostCodeLookupId(0),                  emptyAnswers,                     companyAddressList(mode),           true,           Some(companyAddressList(checkMode(mode))),                                 true),
    (CompanyAddressListId(0),                     emptyAnswers,                     companyManualAddress(mode),         true,           Some(companyManualAddress(checkMode(mode))),                               true),
    (CompanyAddressId(0),                         emptyAnswers,                     companyAddressYears(mode),          true,           if(mode == UpdateMode) Some(companyAddressYears(checkMode(mode))) else Some(cya(mode)),                   true),
    (CompanyAddressId(0),                         newEstablisher,                   companyAddressYears(mode),          true,           Some(cya(mode)),                                                             true),
    (CompanyAddressYearsId(0),                    addressYearsOverAYear,            companyContactDetails(mode),        true,           Some(exitJourney(mode, addressYearsOverAYear, 0, cya(mode))),                   true),
    (CompanyAddressYearsId(0),                    addressYearsOverAYearNew,         companyContactDetails(mode),        true,           Some(exitJourney(mode, addressYearsOverAYearNew, 0, cya(mode))),                   true),
    (CompanyAddressYearsId(0),                    emptyAnswers,                     sessionExpired,                     false,          Some(sessionExpired),                                                      false),
    (CompanyConfirmPreviousAddressId(0),          confirmPreviousAddressYes,        none,                               false,          Some(anyMoreChanges),                                                      false),
    (CompanyConfirmPreviousAddressId(0),          confirmPreviousAddressNo,         none,                               false,          Some(prevAddPostCodeLookup(checkMode(mode))),                              false),
    (CompanyConfirmPreviousAddressId(0),          emptyAnswers,                     none,                               false,          Some(sessionExpired),                                                      false),
    (CompanyPreviousAddressPostcodeLookupId(0),   emptyAnswers,                     companyPaList(mode),                true,           Some(companyPaList(checkMode(mode))),                                      true),
    (CompanyPreviousAddressListId(0),             emptyAnswers,                     companyPreviousAddress(mode),       true,           Some(companyPreviousAddress(checkMode(mode))),                             true),
    (CompanyEmailId(0),                           newEstablisher,                   companyPhoneNumber(mode),           true,           Some(cyaCompanyContactDetails(mode)),                   true),
    (CompanyEmailId(0),                           emptyAnswers,                     companyPhoneNumber(mode),           true,           Some(exitJourney(mode, emptyAnswers, 0, cyaCompanyContactDetails(mode))),                   true),
    (CompanyPreviousAddressId(0),                 emptyAnswers,                     previousAddressRoutes(toggled, mode),true,          Some(previousAddressEditRoutes(toggled, mode, emptyAnswers)),              true),
    (CompanyPreviousAddressId(0),                 newEstablisher,                   previousAddressRoutes(toggled, mode),true,          Some(previousAddressEditRoutes(toggled, mode, newEstablisher)),              true),
    (AddCompanyDirectorsId(0),                    emptyAnswers,                     directorDetails(0, mode),     true,           None,                                                                      true),
    (AddCompanyDirectorsId(0),                    addCompanyDirectorsTrue,          directorDetails(1, mode),     true,           None,                                                                      true),
    (AddCompanyDirectorsId(0),                    addCompanyDirectorsFalse,         if(mode == UpdateMode) taskList else companyReview(mode),                true,           None,                                           true),
    (AddCompanyDirectorsId(0),                    addCompanyDirectorsFalseNewDir,   companyReview(mode),                true,           None,                                                                      true),
    (AddCompanyDirectorsId(0),                    addOneCompanyDirectors,           sessionExpired,                     false,          None,                                                                      false),
    (AddCompanyDirectorsId(0),                    addCompanyDirectorsMoreThan10,    otherDirectors(mode),               true,           None,                                                                      true),
    (OtherDirectorsId(0),                         emptyAnswers,                     if(mode == UpdateMode) anyMoreChanges else companyReview(mode),                true,           Some(companyReview(mode)),                       true),
    (CompanyReviewId(0),                          emptyAnswers,                     if(mode == UpdateMode) anyMoreChanges else addEstablisher(mode),               false,          None,                                           false),
    (CheckYourAnswersId(0),                       emptyAnswers,                     if(mode == UpdateMode) anyMoreChanges else addCompanyDirectors(0, mode), true,           None,                                           false),
    (CheckYourAnswersId(0),                       newEstablisher,                   addCompanyDirectors(0, mode), true,           None,                                                                      false)
  )


  private def normalOnlyRoutes(toggled:Boolean): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id",                                          "User Answers",               "Next Page (Normal Mode)",                "Save (NM)",  "Next Page (Check Mode)",         "Save (CM)"),
<<<<<<< HEAD
    (CompanyAddressYearsId(0),                    addressYearsUnderAYear,       underAYearRouteWithToggle(NormalMode, toggled),        true,         Some(underAYearRouteWithToggle(CheckMode, toggled)),                          true),
=======
    (CompanyAddressYearsId(0),                    addressYearsUnderAYear,       if(toggled) hasBeenTrading(UpdateMode) else prevAddPostCodeLookup(NormalMode),        true,         addressYearsLessThanTwelveEdit(journeyMode(CheckMode), toggled, addressYearsUnderAYear),                          true),
>>>>>>> 49cf5a67857cdc10198a13a79657a410677a6c13
    (CompanyRegistrationNumberId(0),              emptyAnswers,                 companyUTR(NormalMode),                   true,         Some(exitJourney(journeyMode(CheckMode), emptyAnswers, 0, cya(NormalMode))),                   true),
    (CompanyRegistrationNumberId(0),              newEstablisher,               companyUTR(NormalMode),                   true,         Some(cya(journeyMode(CheckMode))),                   true),
    (CompanyContactDetailsId(0),                  emptyAnswers,                 isDormant(NormalMode),                                true,         Some(cya(journeyMode(CheckMode))),               true),
    (CompanyPhoneId(0),                           emptyAnswers,                 cyaCompanyContactDetails(NormalMode),                         true,         Some(cyaCompanyContactDetails(journeyMode(CheckMode))),               true),
    (IsCompanyDormantId(0),                       emptyAnswers,                 cya(NormalMode),             true,         Some(cya(journeyMode(CheckMode))),               true)
  )

  private def updateOnlyRoutes(toggled:Boolean): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id",                                          "User Answers",               "Next Page (UpdateMode Mode)",                "Save (NM)",  "Next Page (Check Mode)",         "Save (CM)"),
<<<<<<< HEAD
    (CompanyAddressYearsId(0),                    addressYearsUnderAYearNew,        underAYearRouteWithToggle(UpdateMode, toggled),        true,         Some(underAYearRouteWithToggle(CheckUpdateMode, toggled)),                          true),
=======
>>>>>>> 49cf5a67857cdc10198a13a79657a410677a6c13
    (CompanyAddressYearsId(0),                    addressYearsUnderAYear,         if(toggled) hasBeenTrading(UpdateMode) else prevAddPostCodeLookup(UpdateMode),        true,         addressYearsLessThanTwelveEdit(UpdateMode, toggled, addressYearsUnderAYear),                          true),
    (CompanyRegistrationNumberId(0),              emptyAnswers,                     if(toggled)hasCompanyUTR(UpdateMode) else companyUTR(UpdateMode),                   true,           Some(exitJourney(UpdateMode, emptyAnswers, 0, cya(UpdateMode))),                   true),
    (CompanyRegistrationNumberId(0),              newEstablisher,                   if(toggled)hasCompanyUTR(UpdateMode) else companyUTR(UpdateMode),                   true,           Some(cya(UpdateMode)),                   true),
    (CompanyContactDetailsId(0),  emptyAnswers,                         cya(UpdateMode),                        true,   Some(exitJourney(checkMode(UpdateMode),   emptyAnswers, 0, cya(UpdateMode))),       true),
    (CompanyPhoneId(0),           emptyAnswers,                         cyaCompanyContactDetails(UpdateMode),   true,   Some(exitJourney(checkMode(UpdateMode),   emptyAnswers, 0, cyaCompanyContactDetails(UpdateMode))),       true),
    (AddCompanyDirectorsId(0),    addCompanyDirectorsFalseWithChanges,  anyMoreChanges,                         true,   None,                                                           true),
    (CompanyPayeVariationsId(0),                  emptyAnswers,         none,                                   true,   Some(exitJourney(checkMode(UpdateMode), emptyAnswers, 0, cya(UpdateMode))),                   true),
    (CompanyRegistrationNumberVariationsId(0),                  emptyAnswers,                  none,    true,           Some(exitJourney(checkMode(UpdateMode), emptyAnswers, 0, cya(UpdateMode))),                   true)
  )

  private def normalRoutes(toggled : Boolean = false) = Table(
    ("Id",                                          "User Answers",               "Next Page (Normal Mode)",                "Save (NM)",  "Next Page (Check Mode)",         "Save (CM)"),
    routes(NormalMode, toggled) ++ normalOnlyRoutes(toggled): _*
  )

  private def updateRoutes(toggled : Boolean = false) = Table(
    ("Id",                                          "User Answers",               "Next Page (Normal Mode)",                "Save (NM)",  "Next Page (Check Mode)",         "Save (CM)"),
    routes(UpdateMode, toggled) ++ updateOnlyRoutes(toggled): _*
  )

  private val navigator: EstablishersCompanyNavigator =
    new EstablishersCompanyNavigator(FakeUserAnswersCacheConnector, frontendAppConfig, new FakeFeatureSwitchManagementService(false))

  s"${navigator.getClass.getSimpleName}" must {
    appRunning()
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, normalRoutes(), dataDescriber)
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, updateRoutes(), dataDescriber, UpdateMode)
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
  private def establisherHasPAYE(v:Boolean) = UserAnswers(Json.obj())
    .set(HasCompanyPAYEId(0))(v).asOpt.value

  private def establisherEnteredPAYE = UserAnswers(Json.obj())
    .set(CompanyVatVariationsId(0))(ReferenceValue("123456789")).asOpt.value

  private def hasCompanyNumber(yesNo : Boolean) = UserAnswers(Json.obj()).set(HasCompanyNumberId(0))(yesNo).asOpt.value
  private def hasCompanyUtr(yesNo : Boolean) = UserAnswers(Json.obj()).set(HasCompanyUTRId(0))(yesNo).asOpt.value
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

  private def whatIsPAYE(mode:Mode):Call =
    controllers.register.establishers.company.routes.CompanyPayeVariationsController.onPageLoad (mode, 0, None)

  private def companyRegistrationNumberNew(mode: Mode): Call =
    controllers.register.establishers.company.routes.CompanyRegistrationNumberVariationsController.onPageLoad(mode, None, 0)

  private def noCompanyRegistrationNumber(mode: Mode): Call =
    controllers.register.establishers.company.routes.NoCompanyNumberController.onPageLoad(mode, None, 0)

  private def companyVatNumberNew(mode: Mode): Call =
    controllers.register.establishers.company.routes.CompanyVatVariationsController.onPageLoad(mode, 0, None)

  private def companyUTR(mode: Mode): Call =
    controllers.register.establishers.company.routes.CompanyUniqueTaxReferenceController.onPageLoad(mode, None, 0)

  private def hasCompanyUTR(mode: Mode): Call =
    controllers.register.establishers.company.routes.HasCompanyUTRController.onPageLoad(mode, None, 0)

  private def companyUTRNew(mode: Mode): Call =
    controllers.register.establishers.company.routes.CompanyUTRController.onPageLoad(mode, None, 0)

  private def noCompanyUTR(mode: Mode): Call =
    controllers.register.establishers.company.routes.NoCompanyUTRController.onPageLoad(mode, None, 0)

  private def companyPaye(mode: Mode): Call =
    controllers.register.establishers.company.routes.CompanyPayeController.onPageLoad(mode, 0, None)

  private def companyHasPaye(mode: Mode): Call =
    controllers.register.establishers.company.routes.HasCompanyPAYEController.onPageLoad(mode, None, 0)

  private def companyVat(mode: Mode): Call =
    controllers.register.establishers.company.routes.CompanyVatController.onPageLoad(mode, 0, None)

  private def companyVatNew(mode: Mode): Call =
    controllers.register.establishers.company.routes.CompanyVatVariationsController.onPageLoad(mode, 0, None)

  private def hasCompanyVat(mode: Mode): Call =
    controllers.register.establishers.company.routes.HasCompanyVATController.onPageLoad(mode, None, 0)

  private def hasCompanyPaye(mode: Mode): Call =
    controllers.register.establishers.company.routes.HasCompanyPAYEController.onPageLoad(mode, None, 0)

  private def hasBeenTrading(mode: Mode): Call =
    controllers.register.establishers.company.routes.HasBeenTradingCompanyController.onPageLoad(mode, None, 0)


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

  private def companyPhoneNumber(mode: Mode) = controllers.register.establishers.company.routes.CompanyPhoneController.onPageLoad(mode, None, 0)

  private def directorDetails(index: Index, mode: Mode) =
    controllers.register.establishers.company.director.routes.DirectorDetailsController.onPageLoad(mode, 0, index, None)

  private def companyReview(mode: Mode) = controllers.register.establishers.company.routes.CompanyReviewController.onPageLoad(mode, None, 0)

  private def otherDirectors(mode: Mode) = controllers.register.establishers.company.routes.OtherDirectorsController.onPageLoad(mode, None, 0)

  private def cya(mode: Mode): Call = controllers.register.establishers.company.routes.CheckYourAnswersController.onPageLoad(mode, None, 0)
  private def cyaCompanyDetails(mode: Mode) =
    controllers.register.establishers.company.routes.CheckYourAnswersCompanyDetailsController.onPageLoad(mode, None, 0)
  private def cyaCompanyContactDetails(mode: Mode) =
    controllers.register.establishers.company.routes.CheckYourAnswersCompanyContactDetailsController.onPageLoad(mode, None, 0)
  private def cyaCompanyAddressDetails(mode: Mode) =
    controllers.register.establishers.company.routes.CheckYourAnswersCompanyAddressController.onPageLoad(mode, None, 0)

  private def anyMoreChanges = controllers.routes.AnyMoreChangesController.onPageLoad(None)

  private def exitJourney(mode: Mode, answers:UserAnswers, index:Int = 0, cyaPage: Call) =
    if (mode == NormalMode)
      cyaPage
    else if (answers.get(IsEstablisherNewId(index)).getOrElse(false))
      cyaPage
    else
      anyMoreChanges

  private def confirmPreviousAddress = controllers.register.establishers.company.routes.CompanyConfirmPreviousAddressController.onPageLoad(0, None)

  private def addressYearsLessThanTwelveEdit(mode: Mode,
                                             isEstablisherCompanyHnSEnabled: Boolean = false,
                                             userAnswers: UserAnswers) =
    (
      userAnswers.get(CompanyAddressYearsId(0)),
      isEstablisherCompanyHnSEnabled,
      userAnswers.get(ExistingCurrentAddressId(0))
    ) match {
      case (Some(AddressYears.UnderAYear), false, Some(_)) =>
        Some(confirmPreviousAddress)
      case (Some(AddressYears.UnderAYear), true, _) =>
        Some(hasBeenTrading(checkMode(mode)))
      case (Some(AddressYears.UnderAYear), false, _) =>
        Some(prevAddPostCodeLookup(checkMode(mode)))
      case (Some(AddressYears.OverAYear), true, _) =>
        Some(exitJourney(mode, userAnswers, 0, cyaCompanyDetails(mode)))
      case (Some(AddressYears.OverAYear), false, _) =>
        Some(exitJourney(mode, userAnswers, 0, cya(mode)))
      case _ =>
        Some(sessionExpired)
    }

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

  private def previousAddressRoutes(toggled: Boolean, mode: Mode) =
    if (toggled)
      cyaCompanyAddressDetails(mode)
    else
      companyContactDetails(mode)

  private def previousAddressEditRoutes(toggled: Boolean, mode: Mode, userAnswers: UserAnswers) =
    if (toggled)
      exitJourney(mode, userAnswers, 0, cyaCompanyAddressDetails(mode))
    else
      exitJourney(mode, userAnswers, 0, cya(mode))


}
