/*
 * Copyright 2022 HM Revenue & Customs
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
import controllers.actions.FakeDataRetrievalAction
import controllers.register.establishers.company.director.routes
import identifiers.register.establishers.company._
import identifiers.register.establishers.{EstablishersId, IsEstablisherNewId}
import identifiers.{EstablishersOrTrusteesChangedId, Identifier, TypedIdentifier}
import models.Mode.checkMode
import models._
import models.person.PersonName
import org.scalatest.OptionValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.prop.TableFor3
import play.api.libs.json.{JsObject, Json, Writes}
import play.api.mvc.Call
import utils.{Enumerable, UserAnswers}

//scalastyle:off line.size.limit
//scalastyle:off magic.number

class EstablishersCompanyNavigatorSpec extends SpecBase with Matchers with NavigatorBehaviour {

  import EstablishersCompanyNavigatorSpec._

  val navigator: Navigator = applicationBuilder(dataRetrievalAction = new FakeDataRetrievalAction(Some(Json.obj()))).build().injector.instanceOf[Navigator]

  "EstablishersCompanyNavigator" when {

    "in NormalMode" must {
      def navigation: TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          row(HasCompanyCRNId(0))(true, companyRegistrationNumberNew(NormalMode)),
          row(HasCompanyCRNId(0))(false, noCompanyRegistrationNumber(NormalMode)),
          row(HasCompanyVATId(0))(true, companyVatNew(NormalMode)),
          row(HasCompanyVATId(0))(false, hasCompanyPaye(NormalMode)),
          row(CompanyEnterVATId(0))(someRefValue, companyHasPaye(NormalMode)),
          row(HasCompanyPAYEId(0))(true, whatIsPAYE(NormalMode)),
          row(HasCompanyUTRId(0))(true, companyUTRNew(NormalMode)),
          row(HasCompanyUTRId(0))(false, noCompanyUTR(NormalMode)),
          row(CompanyEnterUTRId(0))(someRefValue, hasCompanyVat(NormalMode)),
          row(CompanyNoUTRReasonId(0))(someStringValue, hasCompanyVat(NormalMode)),
          rowNewEstablisher(CompanyNoUTRReasonId(0))(someStringValue, hasCompanyVat(NormalMode)),
          rowNoValue(CompanyPostCodeLookupId(0))(companyAddressList(NormalMode)),
          row(CompanyAddressYearsId(0))(AddressYears.OverAYear, cyaCompanyAddressDetails(NormalMode)),
          rowNewEstablisher(CompanyAddressYearsId(0))(AddressYears.OverAYear, cyaCompanyAddressDetails(NormalMode)),
          row(CompanyPreviousAddressPostcodeLookupId(0))(someSeqTolerantAddress, companyPaList(NormalMode)),
          rowNoValueNewEstablisher(CompanyEmailId(0))(companyPhoneNumber(NormalMode)),
          rowNoValue(CompanyEmailId(0))(companyPhoneNumber(NormalMode)),
          rowNoValue(CompanyPreviousAddressListId(0))(previousAddressRoutes(NormalMode)),
          rowNoValueNewEstablisher(CompanyPreviousAddressListId(0))(previousAddressRoutes(NormalMode)),
          rowNoValue(CompanyPreviousAddressId(0))(previousAddressRoutes(NormalMode)),
          rowNoValueNewEstablisher(CompanyPreviousAddressId(0))(previousAddressRoutes(NormalMode)),
          rowNoValue(AddCompanyDirectorsId(0))(startDirectorJourney(NormalMode, 0)),
          row(AddCompanyDirectorsId(0))(true, directorName(NormalMode, 0)),
          row(AddCompanyDirectorsId(0))(true, otherDirectors(NormalMode), ua = Some(addCompanyDirectorsMoreThanTen)),
          rowNoValue(OtherDirectorsId(0))(/*if (NormalMode == UpdateMode) anyMoreChanges else */taskList(NormalMode)),
          rowNoValue(CheckYourAnswersId(0))(/*if (NormalMode == UpdateMode) anyMoreChanges else */addCompanyDirectors(0, NormalMode)),
          rowNewEstablisher(CompanyEnterUTRId(0))(someRefValue, hasCompanyVat(NormalMode)),
          rowNoValue(CompanyEnterCRNId(0))(hasCompanyUTR(NormalMode)),
          rowNoValue(CompanyPhoneId(0))(cyaCompanyContactDetails(NormalMode)),
          rowNoValue(IsCompanyDormantId(0))(cyaCompanyDetails(NormalMode)),
          row(HasCompanyPAYEId(0))(false, isDormant(NormalMode)),
          rowNoValue(CompanyNoCRNReasonId(0))(hasCompanyUTR(NormalMode)),
          rowNoValue(CompanyEnterPAYEId(0))(isDormant(NormalMode)),
          rowNoValue(CompanyAddressListId(0))(companyAddressYears(NormalMode)),
          rowNoValueNewEstablisher(CompanyAddressListId(0))(companyAddressYears(NormalMode)),
          row(AddCompanyDirectorsId(0))(false, taskList(NormalMode), ua = Some(addOneCompanyDirectors))
        )
      behave like navigatorWithRoutesForMode(NormalMode)(navigator, navigation, None)
    }

    "in CheckMode" must {
      def navigation: TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          row(HasCompanyCRNId(0))(true, companyRegistrationNumberNew(CheckMode)),
          row(HasCompanyCRNId(0))(false, noCompanyRegistrationNumber(CheckMode)),
          row(HasCompanyVATId(0))(true, companyVatNew(CheckMode)),
          row(HasCompanyVATId(0))(false, cyaCompanyDetails(NormalMode)),
          row(CompanyEnterVATId(0))(someRefValue, exitJourney(NormalMode, establisherEnteredPAYE, 0, cyaCompanyDetails(NormalMode))),
          row(HasCompanyPAYEId(0))(true, whatIsPAYE(CheckMode)),
          row(HasCompanyUTRId(0))(true, companyUTRNew(CheckMode)),
          row(HasCompanyUTRId(0))(false, noCompanyUTR(CheckMode)),
          row(CompanyEnterUTRId(0))(someRefValue, exitJourney(NormalMode, emptyAnswers, 0, cyaCompanyDetails(NormalMode))),
          row(CompanyNoUTRReasonId(0))(someStringValue, exitJourney(NormalMode, emptyAnswers, 0, cyaCompanyDetails(NormalMode))),
          rowNewEstablisher(CompanyNoUTRReasonId(0))(someStringValue, exitJourney(NormalMode, newEstablisher, 0, cyaCompanyDetails(NormalMode))),
          rowNoValue(CompanyPostCodeLookupId(0))(companyAddressList(CheckMode)),
          row(CompanyAddressYearsId(0))(AddressYears.OverAYear, cyaCompanyAddressDetails(NormalMode)),
          rowNewEstablisher(CompanyAddressYearsId(0))(AddressYears.OverAYear, cyaCompanyAddressDetails(NormalMode)),
          row(CompanyConfirmPreviousAddressId(0))(true, anyMoreChanges),
          row(CompanyConfirmPreviousAddressId(0))(false, prevAddPostCodeLookup(CheckMode)),
          row(CompanyPreviousAddressPostcodeLookupId(0))(someSeqTolerantAddress, companyPaList(CheckMode)),
          rowNoValueNewEstablisher(CompanyEmailId(0))(exitJourney(NormalMode, newEstablisher, 0, cyaCompanyContactDetails(NormalMode))),
          rowNoValue(CompanyEmailId(0))(exitJourney(NormalMode, newEstablisher, 0, cyaCompanyContactDetails(NormalMode))),
          rowNoValue(CompanyPreviousAddressListId(0))(exitJourney(NormalMode, emptyAnswers, 0, cyaCompanyAddressDetails(NormalMode))),
          rowNoValueNewEstablisher(CompanyPreviousAddressListId(0))(exitJourney(NormalMode, emptyAnswers, 0, cyaCompanyAddressDetails(NormalMode))),
          rowNoValue(CompanyPreviousAddressId(0))(exitJourney(NormalMode, emptyAnswers, 0, cyaCompanyAddressDetails(NormalMode))),
          rowNoValueNewEstablisher(CompanyPreviousAddressId(0))(exitJourney(NormalMode, emptyAnswers, 0, cyaCompanyAddressDetails(NormalMode))),
          rowNoValue(OtherDirectorsId(0))(taskList(NormalMode)),
          rowNewEstablisher(CompanyEnterUTRId(0))(someRefValue, exitJourney(NormalMode, newEstablisher, 0, cyaCompanyDetails(NormalMode))),
          rowNoValue(CompanyEnterCRNId(0))(cyaCompanyDetails(NormalMode)),
          rowNoValue(CompanyPhoneId(0))(cyaCompanyContactDetails(NormalMode)),
          rowNoValue(IsCompanyDormantId(0))(cyaCompanyDetails(NormalMode)),
          row(HasCompanyPAYEId(0))(false, cyaCompanyDetails(NormalMode)),
          rowNoValue(CompanyNoCRNReasonId(0))(cyaCompanyDetails(NormalMode)),
          rowNoValue(CompanyEnterPAYEId(0))(cyaCompanyDetails(NormalMode)),
          rowNoValue(CompanyAddressListId(0))(getCya(NormalMode, cyaCompanyAddressDetails(NormalMode))),
          rowNoValueNewEstablisher(CompanyAddressListId(0))(getCya(NormalMode, exitJourney(NormalMode, newEstablisher, 0, cyaCompanyAddressDetails(NormalMode))))
        )
      behave like navigatorWithRoutesForMode(CheckMode)(navigator, navigation, None)
    }

    "in UpdateMode" must {
      def navigation: TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          row(HasCompanyCRNId(0))(true, companyRegistrationNumberNew(UpdateMode)),
          row(HasCompanyCRNId(0))(false, noCompanyRegistrationNumber(UpdateMode)),
          row(HasCompanyVATId(0))(true, companyVatNew(UpdateMode)),
          row(HasCompanyVATId(0))(false, hasCompanyPaye(UpdateMode)),
          row(CompanyEnterVATId(0))(someRefValue, companyHasPaye(UpdateMode)),
          row(HasCompanyPAYEId(0))(true, whatIsPAYE(UpdateMode)),
          row(HasCompanyUTRId(0))(true, companyUTRNew(UpdateMode)),
          row(HasCompanyUTRId(0))(false, noCompanyUTR(UpdateMode)),
          row(CompanyEnterUTRId(0))(someRefValue, hasCompanyVat(UpdateMode)),
          row(CompanyNoUTRReasonId(0))(someStringValue, hasCompanyVat(UpdateMode)),
          rowNewEstablisher(CompanyNoUTRReasonId(0))(someStringValue, hasCompanyVat(UpdateMode)),
          rowNoValue(CompanyPostCodeLookupId(0))(companyAddressList(UpdateMode)),
          row(CompanyAddressYearsId(0))(AddressYears.OverAYear, cyaCompanyAddressDetails(UpdateMode)),
          rowNewEstablisher(CompanyAddressYearsId(0))(AddressYears.OverAYear, cyaCompanyAddressDetails(UpdateMode)),
          row(CompanyPreviousAddressPostcodeLookupId(0))(someSeqTolerantAddress, companyPaList(UpdateMode)),
          rowNoValueNewEstablisher(CompanyEmailId(0))(companyPhoneNumber(UpdateMode)),
          rowNoValue(CompanyEmailId(0))(companyPhoneNumber(UpdateMode)),
          rowNoValue(CompanyPreviousAddressListId(0))(previousAddressRoutes(UpdateMode)),
          rowNoValueNewEstablisher(CompanyPreviousAddressListId(0))(previousAddressRoutes(UpdateMode)),
          rowNoValue(CompanyPreviousAddressId(0))(previousAddressRoutes(UpdateMode)),
          rowNoValueNewEstablisher(CompanyPreviousAddressId(0))(previousAddressRoutes(UpdateMode)),
          rowNoValue(AddCompanyDirectorsId(0))(startDirectorJourney(UpdateMode, 0)),
          row(AddCompanyDirectorsId(0))(true, directorName(UpdateMode, 0)),
          row(AddCompanyDirectorsId(0))(true, otherDirectors(UpdateMode), ua = Some(addCompanyDirectorsMoreThanTen)),
          rowNoValue(OtherDirectorsId(0))(anyMoreChanges),
          rowNoValue(CheckYourAnswersId(0))(anyMoreChanges),
          rowNewEstablisher(CompanyEnterUTRId(0))(someRefValue, hasCompanyVat(UpdateMode)),
          row(CompanyAddressYearsId(0))(AddressYears.UnderAYear, hasBeenTrading(UpdateMode)),
          row(CompanyAddressYearsId(0))(AddressYears.UnderAYear, hasBeenTrading(UpdateMode), ua = Some(addressYearsUnderAYearWithExistingCurrentAddress)),
          rowNoValue(CompanyPhoneId(0))(cyaCompanyContactDetails(UpdateMode)),
          rowNoValue(AddCompanyDirectorsId(0))(anyMoreChanges, ua = Some(addCompanyDirectorsFalseWithChanges)),
          rowNoValue(AddCompanyDirectorsId(0))(anyMoreChanges, ua = Some(addCompanyDirectorsFalseNewDir)),
          rowNoValue(CompanyEnterPAYEId(0))(cyaCompanyDetails(UpdateMode)),
          rowNoValue(CompanyEnterCRNId(0))(hasCompanyUTR(UpdateMode)),
          row(HasCompanyPAYEId(0))(false, cyaCompanyDetails(UpdateMode)),
          rowNoValueNewEstablisher(CompanyNoCRNReasonId(0))( hasCompanyUTR(UpdateMode)),
          rowNoValue(CompanyAddressListId(0))( companyAddressYears(UpdateMode)),
          rowNoValueNewEstablisher(CompanyAddressListId(0))( companyAddressYears(UpdateMode))
        )
      behave like navigatorWithRoutesForMode(UpdateMode)(navigator, navigation, None)
    }

    "in CheckUpdateMode" must {
      def navigation: TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          row(HasCompanyCRNId(0))(true, companyRegistrationNumberNew(CheckUpdateMode)),
          row(HasCompanyCRNId(0))(false, noCompanyRegistrationNumber(CheckUpdateMode)),
          row(HasCompanyVATId(0))(true, companyVatNew(CheckUpdateMode)),
          row(HasCompanyVATId(0))(false, cyaCompanyDetails(UpdateMode)),
          row(CompanyEnterVATId(0))(someRefValue, exitJourney(UpdateMode, establisherEnteredPAYE, 0, cyaCompanyDetails(UpdateMode))),
          row(HasCompanyPAYEId(0))(true, whatIsPAYE(CheckUpdateMode)),
          row(HasCompanyUTRId(0))(true, companyUTRNew(CheckUpdateMode)),
          row(HasCompanyUTRId(0))(false, noCompanyUTR(CheckUpdateMode)),
          row(CompanyEnterUTRId(0))(someRefValue, exitJourney(UpdateMode, emptyAnswers, 0, cyaCompanyDetails(UpdateMode))),
          row(CompanyNoUTRReasonId(0))(someStringValue, exitJourney(UpdateMode, emptyAnswers, 0, cyaCompanyDetails(UpdateMode))),
          rowNewEstablisher(CompanyNoUTRReasonId(0))(someStringValue, exitJourney(UpdateMode, newEstablisher, 0, cyaCompanyDetails(UpdateMode))),
          rowNoValue(CompanyPostCodeLookupId(0))(companyAddressList(CheckUpdateMode)),
          row(CompanyAddressYearsId(0))(AddressYears.OverAYear, anyMoreChanges),
          rowNewEstablisher(CompanyAddressYearsId(0))(AddressYears.OverAYear, cyaCompanyAddressDetails(UpdateMode)),
          row(CompanyConfirmPreviousAddressId(0))(true, anyMoreChanges),
          row(CompanyConfirmPreviousAddressId(0))(false, prevAddPostCodeLookup(CheckUpdateMode)),
          row(CompanyPreviousAddressPostcodeLookupId(0))(someSeqTolerantAddress, companyPaList(CheckUpdateMode)),
          rowNoValueNewEstablisher(CompanyEmailId(0))(exitJourney(UpdateMode, newEstablisher, 0, cyaCompanyContactDetails(UpdateMode))),
          rowNoValue(CompanyEmailId(0))(anyMoreChanges),
          rowNoValue(CompanyPreviousAddressListId(0))(exitJourney(UpdateMode, emptyAnswers, 0, cyaCompanyAddressDetails(UpdateMode))),
          rowNoValueNewEstablisher(CompanyPreviousAddressListId(0))(exitJourney(UpdateMode, newEstablisher, 0, cyaCompanyAddressDetails(UpdateMode))),
          rowNoValue(CompanyPreviousAddressId(0))(exitJourney(UpdateMode, emptyAnswers, 0, cyaCompanyAddressDetails(UpdateMode))),
          rowNoValueNewEstablisher(CompanyPreviousAddressId(0))(exitJourney(UpdateMode, newEstablisher, 0, cyaCompanyAddressDetails(UpdateMode))),
          rowNoValue(OtherDirectorsId(0))(taskList(UpdateMode)),
          rowNewEstablisher(CompanyEnterUTRId(0))(someRefValue, exitJourney(UpdateMode, newEstablisher, 0, cyaCompanyDetails(UpdateMode))),
          row(CompanyAddressYearsId(0))(AddressYears.UnderAYear, addressYearsLessThanTwelveEdit(UpdateMode, addressYearsUnderAYear)),
          rowNoValue(CompanyAddressYearsId(0))(addressYearsLessThanTwelveEdit(UpdateMode, addressYearsUnderAYearWithExistingCurrentAddress), ua = Some(addressYearsUnderAYearWithExistingCurrentAddress)),
          rowNoValue(CompanyPhoneId(0))(exitJourney(UpdateMode, emptyAnswers, 0, cyaCompanyContactDetails(UpdateMode))),
          rowNoValue(CompanyEnterPAYEId(0))(exitJourney(UpdateMode, emptyAnswers, 0, cyaCompanyDetails(UpdateMode))),
          rowNoValue(CompanyEnterCRNId(0))(exitJourney(UpdateMode, emptyAnswers, 0, cyaCompanyDetails(UpdateMode))),
          row(HasCompanyPAYEId(0))(false, exitJourney(UpdateMode, establisherHasPAYE(false), 0, cyaCompanyDetails(UpdateMode))),
          rowNoValueNewEstablisher(CompanyNoCRNReasonId(0))(exitJourney(UpdateMode, newEstablisher, 0, cyaCompanyDetails(UpdateMode))),
          rowNoValue(CompanyAddressListId(0))( confirmPreviousAddress),
          rowNoValueNewEstablisher(CompanyAddressListId(0))(exitJourney(UpdateMode, newEstablisher, 0, cyaCompanyAddressDetails(UpdateMode)))
        )
      behave like navigatorWithRoutesForMode(CheckUpdateMode)(navigator, navigation, None)
    }
  }
}

//noinspection MutatorLikeMethodIsParameterless
//scalastyle:off number.of.methods
object EstablishersCompanyNavigatorSpec extends OptionValues with Enumerable.Implicits {
  private def rowNoValueNewEstablisher(id: TypedIdentifier.PathDependent)(call: Call): (id.type, UserAnswers, Call) = Tuple3(id, newEstablisher, call)

  private def rowNewEstablisher(id: TypedIdentifier.PathDependent)(value: id.Data, call: Call)(
    implicit writes: Writes[id.Data]): (id.type, UserAnswers, Call) = {
    val userAnswers = newEstablisher.set(id)(value).asOpt.value
    Tuple3(id, userAnswers, call)
  }

  private val emptyAnswers = UserAnswers(Json.obj())
  private val newEstablisher = UserAnswers(Json.obj()).set(IsEstablisherNewId(0))(true).asOpt.value

  private val establisherIndex = Index(0)

  private def establisherHasPAYE(v: Boolean) = UserAnswers(Json.obj())
    .set(HasCompanyPAYEId(0))(v).asOpt.value

  private def establisherEnteredPAYE = UserAnswers(Json.obj())
    .set(CompanyEnterVATId(0))(ReferenceValue("123456789")).asOpt.value

  private val johnDoe = PersonName("John", "Doe")

  private def validData(directors: PersonName*): JsObject = {
    Json.obj(
      EstablishersId.toString -> Json.arr(
        Json.obj(
          CompanyDetailsId.toString -> CompanyDetails("test company name"),
          "director" -> directors.map(d => Json.obj(identifiers.register.establishers.company.director.DirectorNameId.toString -> Json.toJson(d)))
        )
      )
    )
  }

  private def startDirectorJourney(mode: Mode, index: Index) = directorName(mode, index)

  private def directorName(mode: Mode, index: Index) = routes.DirectorNameController.onPageLoad(mode, establisherIndex, index, None)

  private def sessionExpired = controllers.routes.SessionExpiredController.onPageLoad

  private def whatIsPAYE(mode: Mode): Call =
    controllers.register.establishers.company.routes.CompanyEnterPAYEController.onPageLoad(mode, 0, None)

  private def companyRegistrationNumberNew(mode: Mode): Call =
    controllers.register.establishers.company.routes.CompanyEnterCRNController.onPageLoad(mode, None, 0)

  private def noCompanyRegistrationNumber(mode: Mode): Call =
    controllers.register.establishers.company.routes.CompanyNoCRNReasonController.onPageLoad(mode, None, 0)

  private def hasCompanyUTR(mode: Mode): Call =
    controllers.register.establishers.company.routes.HasCompanyUTRController.onPageLoad(mode, None, 0)

  private def companyUTRNew(mode: Mode): Call =
    controllers.register.establishers.company.routes.CompanyEnterUTRController.onPageLoad(mode, None, 0)

  private def noCompanyUTR(mode: Mode): Call =
    controllers.register.establishers.company.routes.CompanyNoUTRReasonController.onPageLoad(mode, None, 0)

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

  private def companyAddressList(mode: Mode) = controllers.register.establishers.company.routes.CompanyAddressListController.onPageLoad(mode, None, 0)

  private def companyAddressYears(mode: Mode) = controllers.register.establishers.company.routes.CompanyAddressYearsController.onPageLoad(mode, None, 0)

  private def prevAddPostCodeLookup(mode: Mode) =
    controllers.register.establishers.company.routes.CompanyPreviousAddressPostcodeLookupController.onPageLoad(mode, None, 0)

  private def companyPaList(mode: Mode) =
    controllers.register.establishers.company.routes.CompanyPreviousAddressListController.onPageLoad(mode, None, 0)

  private def companyPhoneNumber(mode: Mode) = controllers.register.establishers.company.routes.CompanyPhoneController.onPageLoad(mode, None, 0)

  private def otherDirectors(mode: Mode) = controllers.register.establishers.company.routes.OtherDirectorsController.onPageLoad(mode, None, 0)

  private def cyaCompanyDetails(mode: Mode) =
    controllers.register.establishers.company.routes.CheckYourAnswersCompanyDetailsController.onPageLoad(mode, None, 0)

  private def cyaCompanyContactDetails(mode: Mode) =
    controllers.register.establishers.company.routes.CheckYourAnswersCompanyContactDetailsController.onPageLoad(mode, None, 0)

  private def cyaCompanyAddressDetails(mode: Mode) =
    controllers.register.establishers.company.routes.CheckYourAnswersCompanyAddressController.onPageLoad(mode, None, 0)

  private def anyMoreChanges = controllers.routes.AnyMoreChangesController.onPageLoad(None)

  private def exitJourney(mode: Mode, answers: UserAnswers, index: Int , cyaPage: Call) = {
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
        confirmPreviousAddress
      case (Some(AddressYears.UnderAYear), _) =>
        hasBeenTrading(checkMode(mode))
      case (Some(AddressYears.OverAYear), _) =>
        exitJourney(mode, userAnswers, 0, cyaCompanyAddressDetails(mode))
      case _ =>
        sessionExpired
    }


  private def addCompanyDirectors(index: Int, mode: Mode) = controllers.register.establishers.company.routes.AddCompanyDirectorsController.onPageLoad(mode, None, index)

  private def isDormant(mode: Mode) = controllers.register.establishers.company.routes.IsCompanyDormantController.onPageLoad(mode, None, 0)

  private def taskList(mode: Mode): Call = controllers.routes.PsaSchemeTaskListController.onPageLoad(mode, None)

  private val addressYearsUnderAYear = UserAnswers(Json.obj())
    .set(CompanyAddressYearsId(0))(AddressYears.UnderAYear).asOpt.value
  private val addressYearsUnderAYearWithExistingCurrentAddress = UserAnswers(Json.obj())
    .set(CompanyAddressYearsId(0))(AddressYears.UnderAYear).flatMap(
    _.set(IsEstablisherNewId(0))(true)).asOpt.value

  private def addCompanyDirectorsFalseWithChanges = addOneCompanyDirectors.set(AddCompanyDirectorsId(0))(false).flatMap(
    _.set(EstablishersOrTrusteesChangedId)(true)).asOpt.value

  private def addCompanyDirectorsFalseNewDir = addOneCompanyDirectors.set(AddCompanyDirectorsId(0))(false)
    .flatMap(_.set(IsEstablisherNewId(0))(true)).asOpt.value

  private def addCompanyDirectorsMoreThanTen =
    UserAnswers(validData(Seq.fill(10)(johnDoe): _*))

  private def addOneCompanyDirectors =
    UserAnswers(validData(johnDoe))

  private def getCya(mode: Mode, cyaPage: Call) =  cyaPage

  private def previousAddressRoutes(mode: Mode) =
      cyaCompanyAddressDetails(mode)
}
