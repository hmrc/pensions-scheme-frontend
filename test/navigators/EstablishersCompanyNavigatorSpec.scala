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
import controllers.register.establishers.company.director.routes
import identifiers.register.establishers.company._
import identifiers.register.establishers.company.director.DirectorNameId
import identifiers.register.establishers.{EstablishersId, IsEstablisherNewId}
import identifiers.{EstablishersOrTrusteesChangedId, Identifier}
import models.Mode.checkMode
import models._
import models.person.PersonName
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
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    (HasCompanyCRNId(0), emptyAnswers, sessionExpired, true, Some(sessionExpired), true),
    (HasCompanyCRNId(0), hasCompanyNumber(true), companyRegistrationNumberNew(mode), true, Some(companyRegistrationNumberNew(checkMode(mode))), true),
    (HasCompanyCRNId(0), hasCompanyNumber(false), noCompanyRegistrationNumber(mode), true, Some(noCompanyRegistrationNumber(checkMode(mode))), true),
    (HasCompanyVATId(0), emptyAnswers, sessionExpired, true, Some(sessionExpired), true),
    (HasCompanyVATId(0), hasCompanyVat(true), companyVatNew(mode), true, Some(companyVatNew(checkMode(mode))), true),
    (HasCompanyVATId(0), hasCompanyVat(false), hasCompanyPaye(mode), true, Some(cyaCompanyDetails(mode)), true),
    (CompanyEnterVATId(0), emptyAnswers, sessionExpired, true, Some(exitJourney(mode, emptyAnswers, 0, cyaCompanyDetails(mode))), true),
    (CompanyEnterVATId(0), establisherEnteredPAYE, companyHasPaye(mode), true, Some(exitJourney(mode, establisherEnteredPAYE, 0, cyaCompanyDetails(mode))), true),
    (HasCompanyPAYEId(0), emptyAnswers, sessionExpired, true, Some(sessionExpired), true),
    (HasCompanyPAYEId(0), establisherHasPAYE(true), whatIsPAYE(mode), true, Some(whatIsPAYE(checkMode(mode))), true),
    (HasCompanyUTRId(0), emptyAnswers, sessionExpired, true, Some(sessionExpired), true),
    (HasCompanyUTRId(0), hasCompanyUtr(true), companyUTRNew(mode), true, Some(companyUTRNew(checkMode(mode))), true),
    (HasCompanyUTRId(0), hasCompanyUtr(false), noCompanyUTR(mode), true, Some(noCompanyUTR(checkMode(mode))), true),
    (CompanyEnterUTRId(0), emptyAnswers, hasCompanyVat(mode), true, Some(exitJourney(mode, emptyAnswers, 0, cyaCompanyDetails(mode))), true),
    (CompanyEnterUTRId(0), newEstablisher, hasCompanyVat(mode), true, Some(exitJourney(mode, newEstablisher, 0, cyaCompanyDetails(mode))), true),
    (CompanyNoUTRReasonId(0), emptyAnswers, hasCompanyVat(mode), true, Some(exitJourney(mode, emptyAnswers, 0, cyaCompanyDetails(mode))), true),
    (CompanyNoUTRReasonId(0), newEstablisher, hasCompanyVat(mode), true, Some(exitJourney(mode, newEstablisher, 0, cyaCompanyDetails(mode))), true),
    (CompanyPostCodeLookupId(0), emptyAnswers, companyAddressList(mode), true, Some(companyAddressList(checkMode(mode))), true),
    (CompanyAddressListId(0), emptyAnswers, companyManualAddress(mode), true, Some(companyManualAddress(checkMode(mode))), true),
    (CompanyAddressYearsId(0), addressYearsOverAYear, cyaCompanyAddressDetails(mode) , true, Some(exitJourney(mode, addressYearsOverAYear, 0, cyaCompanyAddressDetails(mode))), true),
    (CompanyAddressYearsId(0), addressYearsOverAYearNew, cyaCompanyAddressDetails(mode), true, Some(exitJourney(mode, addressYearsOverAYearNew, 0, cyaCompanyAddressDetails(mode))), true),
    (CompanyAddressYearsId(0), emptyAnswers, sessionExpired, false, Some(sessionExpired), false),
    (CompanyConfirmPreviousAddressId(0), confirmPreviousAddressYes, none, false, Some(anyMoreChanges), false),
    (CompanyConfirmPreviousAddressId(0), confirmPreviousAddressNo, none, false, Some(prevAddPostCodeLookup(checkMode(mode))), false),
    (CompanyConfirmPreviousAddressId(0), emptyAnswers, none, false, Some(sessionExpired), false),
    (CompanyPreviousAddressPostcodeLookupId(0), emptyAnswers, companyPaList(mode), true, Some(companyPaList(checkMode(mode))), true),
    (CompanyPreviousAddressListId(0), emptyAnswers, companyPreviousAddress(mode), true, Some(companyPreviousAddress(checkMode(mode))), true),
    (CompanyEmailId(0), newEstablisher, companyPhoneNumber(mode), true, Some(exitJourney(mode, newEstablisher, 0, cyaCompanyContactDetails(mode))), true),
    (CompanyEmailId(0), emptyAnswers, companyPhoneNumber(mode), true, Some(exitJourney(mode, emptyAnswers, 0, cyaCompanyContactDetails(mode))), true),
    (CompanyPreviousAddressId(0), emptyAnswers, previousAddressRoutes( mode), true, Some(exitJourney(mode, emptyAnswers, 0, cyaCompanyAddressDetails(mode))), true),
    (CompanyPreviousAddressId(0), newEstablisher, previousAddressRoutes( mode), true, Some(exitJourney(mode, newEstablisher, 0, cyaCompanyAddressDetails(mode))), true),
    (AddCompanyDirectorsId(0), emptyAnswers, startDirectorJourney( mode, 0), true, None, true),
    (AddCompanyDirectorsId(0), addCompanyDirectorsTrue, directorName(mode) , true, None, true),
    (AddCompanyDirectorsId(0), addCompanyDirectorsFalse, taskList(mode), true, None, true),
    (AddCompanyDirectorsId(0), addCompanyDirectorsFalseNewDir, taskList(mode), true, None, true),
    (AddCompanyDirectorsId(0), addOneCompanyDirectors, sessionExpired, false, None, false),
    (AddCompanyDirectorsId(0), addCompanyDirectorsMoreThan10, otherDirectors(mode), true, None, true),
    (CheckYourAnswersId(0), emptyAnswers, if (mode == UpdateMode) anyMoreChanges else addCompanyDirectors(0, mode), true, None, false),
    (CheckYourAnswersId(0), newEstablisher, addCompanyDirectors(0, mode), true, None, false)
  )


  private def normalOnlyRoutes: TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    (CompanyEnterCRNId(0), emptyAnswers, hasCompanyUTR(NormalMode), true, Some(cyaCompanyDetails(NormalMode)), true),
    (CompanyPhoneId(0), emptyAnswers, cyaCompanyContactDetails(NormalMode), true, Some(cyaCompanyContactDetails(NormalMode)), true),
    (IsCompanyDormantId(0), emptyAnswers, cyaCompanyDetails(NormalMode), true, Some(cyaCompanyDetails(NormalMode)), true),
    (HasCompanyPAYEId(0), establisherHasPAYE(false), isDormant(NormalMode), true, Some(cyaCompanyDetails(NormalMode) ), true),
    (CompanyNoCRNReasonId(0), emptyAnswers, hasCompanyUTR(NormalMode), true, Some(cyaCompanyDetails(NormalMode)), true),
    (CompanyEnterPAYEId(0), emptyAnswers, isDormant(NormalMode), true, Some(cyaCompanyDetails(NormalMode)), true),
    (CompanyAddressId(0), emptyAnswers, companyAddressYears(NormalMode), true, Some(getCya(NormalMode, cyaCompanyAddressDetails(NormalMode))), true),
    (CompanyAddressId(0), newEstablisher, companyAddressYears(NormalMode), true, Some(exitJourney(NormalMode, newEstablisher, 0, cyaCompanyAddressDetails(NormalMode))), true)
  )


  private def updateOnlyRoutes: TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id", "User Answers", "Next Page (UpdateMode Mode)", "Save (NM)", "Next Page (CheckUpdateMode Mode)", "Save (CM)"),
    (CompanyAddressYearsId(0), addressYearsUnderAYear, hasBeenTrading(UpdateMode) , true, addressYearsLessThanTwelveEdit(UpdateMode, addressYearsUnderAYear), true),
    (CompanyAddressYearsId(0), addressYearsUnderAYearWithExistingCurrentAddress, hasBeenTrading(UpdateMode), true, addressYearsLessThanTwelveEdit(UpdateMode, addressYearsUnderAYearWithExistingCurrentAddress), true),
    (CompanyPhoneId(0), emptyAnswers, cyaCompanyContactDetails(UpdateMode), true, Some(exitJourney(UpdateMode, emptyAnswers, 0, cyaCompanyContactDetails(UpdateMode))), true),
    (AddCompanyDirectorsId(0), addCompanyDirectorsFalseWithChanges, taskList(UpdateMode), true, None, true),
    (CompanyEnterPAYEId(0), emptyAnswers, cyaCompanyDetails(UpdateMode), true, Some(exitJourney(UpdateMode, emptyAnswers, 0, cyaCompanyDetails(UpdateMode))), true),
    (CompanyEnterCRNId(0), emptyAnswers, hasCompanyUTR(UpdateMode), true, Some(exitJourney(UpdateMode, emptyAnswers, 0, cyaCompanyDetails(UpdateMode))), true),
    (HasCompanyPAYEId(0), establisherHasPAYE(false), cyaCompanyDetails(UpdateMode), true, Some(exitJourney(UpdateMode, establisherHasPAYE(false), 0, cyaCompanyDetails(UpdateMode))), true),
    (CompanyNoCRNReasonId(0), newEstablisher, hasCompanyUTR(UpdateMode), true, Some(exitJourney(UpdateMode, newEstablisher, 0, cyaCompanyDetails(UpdateMode))), true),
    (CompanyAddressId(0), emptyAnswers, companyAddressYears(UpdateMode), true, Some(confirmPreviousAddress), true),
    (CompanyAddressId(0), newEstablisher, companyAddressYears(UpdateMode), true, Some(exitJourney(UpdateMode, newEstablisher, 0, cyaCompanyAddressDetails(UpdateMode))), true)
  )

  private def normalRoutes = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    routes(NormalMode) ++ normalOnlyRoutes: _*
  )

  private def updateRoutes = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    routes(UpdateMode) ++ updateOnlyRoutes: _*
  )

  s"Establisher Company Navigator" must {
    appRunning()
    val navigator: EstablishersCompanyNavigator =
      new EstablishersCompanyNavigator(FakeUserAnswersCacheConnector, frontendAppConfig)
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, normalRoutes, dataDescriber)
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, updateRoutes, dataDescriber, UpdateMode)
  }
}

//noinspection MutatorLikeMethodIsParameterless
object EstablishersCompanyNavigatorSpec extends OptionValues with Enumerable.Implicits {
  private val emptyAnswers = UserAnswers(Json.obj())
  private val newEstablisher = UserAnswers(Json.obj()).set(IsEstablisherNewId(0))(true).asOpt.value

  private val establisherIndex = Index(0)
  private val directorIndex = Index(0)
  private val directorIndexNew = Index(1)

  private def establisherHasPAYE(v: Boolean) = UserAnswers(Json.obj())
    .set(HasCompanyPAYEId(0))(v).asOpt.value

  private def establisherEnteredPAYE = UserAnswers(Json.obj())
    .set(CompanyEnterVATId(0))(ReferenceValue("123456789")).asOpt.value

  private def hasCompanyNumber(yesNo: Boolean) = UserAnswers(Json.obj()).set(HasCompanyCRNId(0))(yesNo).asOpt.value

  private def underAYearRoute(mode: Mode) = hasBeenTrading(mode)

  private def hasCompanyUtr(yesNo: Boolean) = UserAnswers(Json.obj()).set(HasCompanyUTRId(0))(yesNo).asOpt.value

  private def hasCompanyVat(yesNo: Boolean) = UserAnswers(Json.obj()).set(HasCompanyVATId(0))(yesNo).flatMap(_.set(IsEstablisherNewId(0))(true)).asOpt.value

  private val johnDoe = PersonName("John", "Doe")



  private def validData(directors: PersonName*) = {
    Json.obj(
      EstablishersId.toString -> Json.arr(
        Json.obj(
          CompanyDetailsId.toString -> CompanyDetails("test company name"),
          "director" -> directors.map(d => Json.obj(DirectorNameId.toString -> Json.toJson(d)))
        )
      )
    )
  }

  private def startDirectorJourney(mode: Mode, index: Index) = directorName(mode, index)

  private def directorName(mode: Mode, index: Index = directorIndexNew) = routes.DirectorNameController.onPageLoad(mode, establisherIndex, index, None)

  private def none: Call = controllers.routes.IndexController.onPageLoad()

  private def sessionExpired = controllers.routes.SessionExpiredController.onPageLoad()

  private def hasCompanyNumber(mode: Mode): Call =
    controllers.register.establishers.company.routes.HasCompanyNumberController.onPageLoad(mode, None, 0)

  private def whatIsPAYE(mode: Mode): Call =
    controllers.register.establishers.company.routes.CompanyPayeVariationsController.onPageLoad(mode, 0, None)

  private def companyRegistrationNumberNew(mode: Mode): Call =
    controllers.register.establishers.company.routes.CompanyRegistrationNumberVariationsController.onPageLoad(mode, None, 0)

  private def noCompanyRegistrationNumber(mode: Mode): Call =
    controllers.register.establishers.company.routes.NoCompanyNumberController.onPageLoad(mode, None, 0)

  private def companyVatNumberNew(mode: Mode): Call =
    controllers.register.establishers.company.routes.CompanyEnterVATController.onPageLoad(mode, 0, None)

  private def hasCompanyUTR(mode: Mode): Call =
    controllers.register.establishers.company.routes.HasCompanyUTRController.onPageLoad(mode, None, 0)

  private def companyUTRNew(mode: Mode): Call =
    controllers.register.establishers.company.routes.CompanyUTRController.onPageLoad(mode, None, 0)

  private def noCompanyUTR(mode: Mode): Call =
    controllers.register.establishers.company.routes.NoCompanyUTRController.onPageLoad(mode, None, 0)

  private def companyHasPaye(mode: Mode): Call =
    controllers.register.establishers.company.routes.HasCompanyPAYEController.onPageLoad(mode, None, 0)

  private def companyVatNew(mode: Mode): Call =
    controllers.register.establishers.company.routes.CompanyEnterVATController.onPageLoad(mode, 0, None)

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

  private def companyPhoneNumber(mode: Mode) = controllers.register.establishers.company.routes.CompanyPhoneController.onPageLoad(mode, None, 0)

  private def otherDirectors(mode: Mode) = controllers.register.establishers.company.routes.OtherDirectorsController.onPageLoad(mode, None, 0)

  private def cyaCompanyDetails(mode: Mode) =
    controllers.register.establishers.company.routes.CheckYourAnswersCompanyDetailsController.onPageLoad(mode, None, 0)

  private def cyaCompanyContactDetails(mode: Mode) =
    controllers.register.establishers.company.routes.CheckYourAnswersCompanyContactDetailsController.onPageLoad(mode, None, 0)

  private def cyaCompanyAddressDetails(mode: Mode) =
    controllers.register.establishers.company.routes.CheckYourAnswersCompanyAddressController.onPageLoad(mode, None, 0)

  private def anyMoreChanges = controllers.routes.AnyMoreChangesController.onPageLoad(None)

  private def exitJourney(mode: Mode, answers: UserAnswers, index: Int = 0, cyaPage: Call) = {
    if (mode == NormalMode)
      cyaPage
    else if (answers.get(IsEstablisherNewId(index)).getOrElse(false))
      cyaPage
    else
      anyMoreChanges
  }

  private def confirmPreviousAddress = controllers.register.establishers.company.routes.CompanyConfirmPreviousAddressController.onPageLoad(0, None)

  private def addressYearsLessThanTwelveEdit(mode: Mode,
                                             userAnswers: UserAnswers) =
    (
      userAnswers.get(CompanyAddressYearsId(0)),
      userAnswers.get(IsEstablisherNewId(0)).getOrElse(false)
    ) match {
      case (Some(AddressYears.UnderAYear), false) =>
        Some(confirmPreviousAddress)
      case (Some(AddressYears.UnderAYear), _) =>
        Some(hasBeenTrading(checkMode(mode)))
      case (Some(AddressYears.OverAYear), _) =>
        Some(exitJourney(mode, userAnswers, 0, cyaCompanyAddressDetails(mode)))
      case _ =>
        Some(sessionExpired)
    }


  private def addEstablisher(mode: Mode) = controllers.register.establishers.routes.AddEstablisherController.onPageLoad(mode, None)

  private def companyNameRouting(mode: Mode) = addEstablisher(mode)

  private def addCompanyDirectors(index: Int, mode: Mode) = controllers.register.establishers.company.routes.AddCompanyDirectorsController.onPageLoad(mode, None, index)

  private def isDormant(mode: Mode) = controllers.register.establishers.company.routes.IsCompanyDormantController.onPageLoad(mode, None, 0)

  private def taskList(mode: Mode): Call = controllers.routes.SchemeTaskListController.onPageLoad(mode, None)

  private val addressYearsOverAYearNew = UserAnswers(Json.obj())
    .set(CompanyAddressYearsId(0))(AddressYears.OverAYear).flatMap(_.set(IsEstablisherNewId(0))(true)).asOpt.value
  private val addressYearsOverAYear = UserAnswers(Json.obj())
    .set(CompanyAddressYearsId(0))(AddressYears.OverAYear).asOpt.value
  private val addressYearsUnderAYear = UserAnswers(Json.obj())
    .set(CompanyAddressYearsId(0))(AddressYears.UnderAYear).asOpt.value
  private val addressYearsUnderAYearWithExistingCurrentAddress = UserAnswers(Json.obj())
    .set(CompanyAddressYearsId(0))(AddressYears.UnderAYear).flatMap(
    _.set(IsEstablisherNewId(0))(true)).asOpt.value

  private def addCompanyDirectorsTrue = addOneCompanyDirectors.set(AddCompanyDirectorsId(0))(true).asOpt.value

  private def addCompanyDirectorsFalse = addOneCompanyDirectors.set(AddCompanyDirectorsId(0))(false).asOpt.value

  private def addCompanyDirectorsFalseWithChanges = addOneCompanyDirectors.set(AddCompanyDirectorsId(0))(false).flatMap(
    _.set(EstablishersOrTrusteesChangedId)(true)).asOpt.value

  private def addCompanyDirectorsFalseNewDir = addOneCompanyDirectors.set(AddCompanyDirectorsId(0))(false)
    .flatMap(_.set(IsEstablisherNewId(0))(true)).asOpt.value

  private def addCompanyDirectorsMoreThan10 =
    UserAnswers(validData(Seq.fill(10)(johnDoe): _*))

  private def addOneCompanyDirectors =
    UserAnswers(validData(johnDoe))

  private val confirmPreviousAddressYes = UserAnswers(Json.obj())
    .set(CompanyConfirmPreviousAddressId(0))(true).asOpt.value
  private val confirmPreviousAddressNo = UserAnswers(Json.obj())
    .set(CompanyConfirmPreviousAddressId(0))(false).asOpt.value

  private def dataDescriber(answers: UserAnswers): String = answers.toString

  private def getCya(mode: Mode, cyaPage: Call) =  cyaPage

  private def previousAddressRoutes(mode: Mode) =
      cyaCompanyAddressDetails(mode)
}