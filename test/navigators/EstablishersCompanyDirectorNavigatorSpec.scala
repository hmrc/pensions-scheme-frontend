/*
 * Copyright 2024 HM Revenue & Customs
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
import identifiers._
import identifiers.register.establishers.company.director._
import models.Mode.checkMode
import models._
import models.address.Address
import models.person.PersonName
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor3
import play.api.libs.json.{JsString, Json, Writes}
import play.api.mvc.Call
import utils.{Enumerable, UserAnswers}

class EstablishersCompanyDirectorNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import EstablishersCompanyDirectorNavigatorSpec._

  val navigator: Navigator = applicationBuilder(dataRetrievalAction = new FakeDataRetrievalAction(Some(Json.obj()))).build().injector.instanceOf[Navigator]

  "EstablishersCompanyDirectorNavigator" when {

    "in NormalMode" must {
      def navigation: TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          rowNoValue(ConfirmDeleteDirectorId(0))(addCompanyDirectors(NormalMode, EmptyOptionalSchemeReferenceNumber)),
          rowNoValue(CheckYourAnswersId(0, 0))(addCompanyDirectors(NormalMode, EmptyOptionalSchemeReferenceNumber)),
          rowNoValue(DirectorAddressListId(0, 0))(directorAddressYears(NormalMode, EmptyOptionalSchemeReferenceNumber)),
          rowNoValueNewDirector(DirectorAddressListId(0, 0))(directorAddressYears(NormalMode, EmptyOptionalSchemeReferenceNumber)),
          rowNoValue(DirectorAddressId(0, 0))(directorAddressYears(NormalMode, EmptyOptionalSchemeReferenceNumber)),
          rowNoValueNewDirector(DirectorAddressId(0, 0))(directorAddressYears(NormalMode, EmptyOptionalSchemeReferenceNumber)),
          rowNoValue(DirectorNameId(0, 0))(directorDOB(NormalMode, EmptyOptionalSchemeReferenceNumber)),
          rowNoValueNewDirector(DirectorNameId(0, 0))(directorDOB(NormalMode, EmptyOptionalSchemeReferenceNumber)),
          rowNoValue(DirectorDOBId(0, 0))(directorHasNINO(NormalMode, EmptyOptionalSchemeReferenceNumber)),
          rowNoValueNewDirector(DirectorDOBId(0, 0))(directorHasNINO(NormalMode, EmptyOptionalSchemeReferenceNumber)),
          rowNoValue(DirectorEnterNINOId(0, 0))(directorHasUTR(NormalMode, EmptyOptionalSchemeReferenceNumber)),
          rowNoValueNewDirector(DirectorNoUTRReasonId(0, 0))(directorAddressPostcode(NormalMode, EmptyOptionalSchemeReferenceNumber)),
          rowNewDirector(DirectorHasNINOId(0, 0))(true, directorNinoNew(NormalMode, EmptyOptionalSchemeReferenceNumber)),
          rowNewDirector(DirectorHasNINOId(0, 0))(false, directorNinoReason(NormalMode, EmptyOptionalSchemeReferenceNumber)),
          rowNewDirector(DirectorHasUTRId(0, 0))(true, directorWhatIsDirectorUTR(NormalMode, EmptyOptionalSchemeReferenceNumber)),
          rowNewDirector(DirectorHasUTRId(0, 0))(false, directorWhyNoUTR(NormalMode, EmptyOptionalSchemeReferenceNumber)),
          rowNoValue(DirectorAddressPostcodeLookupId(0, 0))(directorAddressList(NormalMode, EmptyOptionalSchemeReferenceNumber)),
          row(DirectorAddressYearsId(0, 0))(AddressYears.OverAYear, directorEmail(NormalMode, EmptyOptionalSchemeReferenceNumber)),
          rowNewDirector(DirectorAddressYearsId(0, 0))(AddressYears.OverAYear, directorEmail(NormalMode, EmptyOptionalSchemeReferenceNumber)),
          row(DirectorAddressYearsId(0, 0))(AddressYears.UnderAYear, directorPreviousAddPostcode(NormalMode, EmptyOptionalSchemeReferenceNumber)),
          rowNoValue(DirectorPreviousAddressPostcodeLookupId(0, 0))(directorPreviousAddList(NormalMode, EmptyOptionalSchemeReferenceNumber)),
          rowNoValue(DirectorPreviousAddressListId(0, 0))(directorEmail(NormalMode, EmptyOptionalSchemeReferenceNumber)),
          rowNoValueNewDirector(DirectorPreviousAddressListId(0, 0))(directorEmail(NormalMode, EmptyOptionalSchemeReferenceNumber)),
          rowNoValue(DirectorPreviousAddressId(0, 0))(directorEmail(NormalMode, EmptyOptionalSchemeReferenceNumber)),
          rowNoValueNewDirector(DirectorPreviousAddressId(0, 0))(directorEmail(NormalMode, EmptyOptionalSchemeReferenceNumber)),
          rowNoValue(DirectorPhoneNumberId(0, 0))(checkYourAnswers(NormalMode, EmptyOptionalSchemeReferenceNumber)),
          rowNoValueNewDirector(DirectorPhoneNumberId(0, 0))(checkYourAnswers(NormalMode, EmptyOptionalSchemeReferenceNumber)),
          rowNoValue(DirectorEmailId(0, 0))(directorPhone(NormalMode, EmptyOptionalSchemeReferenceNumber)),
          rowNoValueNewDirector(DirectorEmailId(0, 0))(directorPhone(NormalMode, EmptyOptionalSchemeReferenceNumber))
        )
      behave like navigatorWithRoutesForMode(NormalMode)(navigator, navigation, EmptyOptionalSchemeReferenceNumber)
    }

    "in CheckMode" must {
      def navigation: TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          rowNoValue(DirectorNameId(0, 0))(exitJourney(NormalMode, emptyAnswers, srn = EmptyOptionalSchemeReferenceNumber)),
          rowNoValueNewDirector(DirectorNameId(0, 0))(exitJourney(NormalMode, newDirector, srn = EmptyOptionalSchemeReferenceNumber)),
          rowNoValue(DirectorDOBId(0, 0))(exitJourney(NormalMode, emptyAnswers, srn = EmptyOptionalSchemeReferenceNumber)),
          rowNoValueNewDirector(DirectorDOBId(0, 0))(exitJourney(NormalMode, newDirector, srn = EmptyOptionalSchemeReferenceNumber)),
          rowNoValue(DirectorEnterNINOId(0, 0))(exitJourney(NormalMode, emptyAnswers, srn = EmptyOptionalSchemeReferenceNumber)),
          rowNoValueNewDirector(DirectorNoUTRReasonId(0, 0))(exitJourney(NormalMode, newDirector, srn = EmptyOptionalSchemeReferenceNumber)),
          rowNewDirector(DirectorHasNINOId(0, 0))(true, directorNinoNew(NormalMode, EmptyOptionalSchemeReferenceNumber)),
          rowNewDirector(DirectorHasNINOId(0, 0))(false, directorNinoReason(NormalMode, EmptyOptionalSchemeReferenceNumber)),
          rowNewDirector(DirectorHasUTRId(0, 0))(true, directorWhatIsDirectorUTR(NormalMode, EmptyOptionalSchemeReferenceNumber)),
          rowNewDirector(DirectorHasUTRId(0, 0))(false, directorWhyNoUTR(NormalMode, EmptyOptionalSchemeReferenceNumber)),
          rowNoValue(DirectorAddressPostcodeLookupId(0, 0))(directorAddressList(NormalMode, EmptyOptionalSchemeReferenceNumber)),
          row(DirectorAddressYearsId(0, 0))(AddressYears.OverAYear, exitJourney(NormalMode, newDirector, srn = EmptyOptionalSchemeReferenceNumber)),
          rowNewDirector(DirectorAddressYearsId(0, 0))(AddressYears.OverAYear, exitJourney(NormalMode, newDirector, srn = EmptyOptionalSchemeReferenceNumber)),
          row(DirectorAddressYearsId(0, 0))(AddressYears.UnderAYear,
                                            addressYearsLessThanTwelveEdit(NormalMode, addressYearsUnderAYear, srn = EmptyOptionalSchemeReferenceNumber)),
          row(DirectorConfirmPreviousAddressId(0, 0))(true, anyMoreChanges(EmptyOptionalSchemeReferenceNumber)),
          row(DirectorConfirmPreviousAddressId(0, 0))(false, directorPreviousAddPostcode(NormalMode, EmptyOptionalSchemeReferenceNumber)),
          rowNoValue(DirectorPreviousAddressPostcodeLookupId(0, 0))(directorPreviousAddList(NormalMode, EmptyOptionalSchemeReferenceNumber)),
          rowNoValue(DirectorPreviousAddressListId(0, 0))(exitJourney(NormalMode, emptyAnswers, srn = EmptyOptionalSchemeReferenceNumber)),
          rowNoValueNewDirector(DirectorPreviousAddressListId(0, 0))(exitJourney(NormalMode, newDirector, srn = EmptyOptionalSchemeReferenceNumber)),
          rowNoValue(DirectorPreviousAddressId(0, 0))(exitJourney(NormalMode, emptyAnswers, srn = EmptyOptionalSchemeReferenceNumber)),
          rowNoValueNewDirector(DirectorPreviousAddressId(0, 0))(exitJourney(NormalMode, newDirector, srn = EmptyOptionalSchemeReferenceNumber)),
          rowNoValue(DirectorPhoneNumberId(0, 0))(exitJourney(NormalMode, emptyAnswers, srn = EmptyOptionalSchemeReferenceNumber)),
          rowNoValueNewDirector(DirectorPhoneNumberId(0, 0))(exitJourney(NormalMode, newDirector, srn = EmptyOptionalSchemeReferenceNumber)),
          rowNoValue(DirectorEmailId(0, 0))(exitJourney(NormalMode, emptyAnswers, srn = EmptyOptionalSchemeReferenceNumber)),
          rowNoValueNewDirector(DirectorEmailId(0, 0))(exitJourney(NormalMode, newDirector, srn = EmptyOptionalSchemeReferenceNumber))
        )
      behave like navigatorWithRoutesForMode(CheckMode)(navigator, navigation, EmptyOptionalSchemeReferenceNumber)
    }

    "in UpdateMode" must {
      def navigation: TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          rowNoValue(ConfirmDeleteDirectorId(0))(anyMoreChanges(OptionalSchemeReferenceNumber(srn))),
          rowNoValue(CheckYourAnswersId(0, 0))(addCompanyDirectors(UpdateMode, OptionalSchemeReferenceNumber(srn))),
          rowNoValue(DirectorAddressYearsId(0, 0))(directorPreviousAddPostcode(UpdateMode, OptionalSchemeReferenceNumber(srn)),
                                                   ua = Some(directorNoExistingCurrentAddress)),
          rowNoValue(DirectorAddressYearsId(0, 0))(directorPreviousAddPostcode(UpdateMode, OptionalSchemeReferenceNumber(srn)), ua = Some(directorExistingCurrentAddress)),
          rowNoValue(DirectorAddressListId(0, 0))(directorAddressYears(UpdateMode, OptionalSchemeReferenceNumber(srn))),
          rowNoValueNewDirector(DirectorAddressListId(0, 0))(directorAddressYears(UpdateMode, OptionalSchemeReferenceNumber(srn))),
          rowNoValue(DirectorAddressId(0, 0))(directorAddressYears(UpdateMode, OptionalSchemeReferenceNumber(srn))),
          rowNoValueNewDirector(DirectorAddressId(0, 0))(directorAddressYears(UpdateMode, OptionalSchemeReferenceNumber(srn))),
          rowNoValue(DirectorNameId(0, 0))(directorDOB(UpdateMode, OptionalSchemeReferenceNumber(srn))),
          rowNoValueNewDirector(DirectorNameId(0, 0))(directorDOB(UpdateMode, OptionalSchemeReferenceNumber(srn))),
          rowNoValue(DirectorDOBId(0, 0))(directorHasNINO(UpdateMode, OptionalSchemeReferenceNumber(srn))),
          rowNoValueNewDirector(DirectorDOBId(0, 0))(directorHasNINO(UpdateMode, OptionalSchemeReferenceNumber(srn))),
          rowNoValue(DirectorEnterNINOId(0, 0))(directorHasUTR(UpdateMode, OptionalSchemeReferenceNumber(srn))),
          rowNoValueNewDirector(DirectorNoUTRReasonId(0, 0))(directorAddressPostcode(UpdateMode, OptionalSchemeReferenceNumber(srn))),
          rowNewDirector(DirectorHasNINOId(0, 0))(true, directorNinoNew(UpdateMode, OptionalSchemeReferenceNumber(srn))),
          rowNewDirector(DirectorHasNINOId(0, 0))(false, directorNinoReason(UpdateMode, OptionalSchemeReferenceNumber(srn))),
          rowNewDirector(DirectorHasUTRId(0, 0))(true, directorWhatIsDirectorUTR(UpdateMode, OptionalSchemeReferenceNumber(srn))),
          rowNewDirector(DirectorHasUTRId(0, 0))(false, directorWhyNoUTR(UpdateMode, OptionalSchemeReferenceNumber(srn))),
          rowNoValue(DirectorAddressPostcodeLookupId(0, 0))(directorAddressList(UpdateMode, OptionalSchemeReferenceNumber(srn))),
          row(DirectorAddressYearsId(0, 0))(AddressYears.OverAYear, directorEmail(UpdateMode, OptionalSchemeReferenceNumber(srn))),
          rowNewDirector(DirectorAddressYearsId(0, 0))(AddressYears.OverAYear, directorEmail(UpdateMode, OptionalSchemeReferenceNumber(srn))),
          row(DirectorAddressYearsId(0, 0))(AddressYears.UnderAYear, directorPreviousAddPostcode(UpdateMode, OptionalSchemeReferenceNumber(srn))),
          rowNoValue(DirectorPreviousAddressPostcodeLookupId(0, 0))(directorPreviousAddList(UpdateMode, OptionalSchemeReferenceNumber(srn))),
          rowNoValue(DirectorPreviousAddressListId(0, 0))(directorEmail(UpdateMode, OptionalSchemeReferenceNumber(srn))),
          rowNoValueNewDirector(DirectorPreviousAddressListId(0, 0))(directorEmail(UpdateMode, OptionalSchemeReferenceNumber(srn))),
          rowNoValue(DirectorPreviousAddressId(0, 0))(directorEmail(UpdateMode, OptionalSchemeReferenceNumber(srn))),
          rowNoValueNewDirector(DirectorPreviousAddressId(0, 0))(directorEmail(UpdateMode, OptionalSchemeReferenceNumber(srn))),
          rowNoValue(DirectorPhoneNumberId(0, 0))(checkYourAnswers(UpdateMode, OptionalSchemeReferenceNumber(srn))),
          rowNoValueNewDirector(DirectorPhoneNumberId(0, 0))(checkYourAnswers(UpdateMode, OptionalSchemeReferenceNumber(srn))),
          rowNoValue(DirectorEmailId(0, 0))(directorPhone(UpdateMode, OptionalSchemeReferenceNumber(srn))),
          rowNoValueNewDirector(DirectorEmailId(0, 0))(directorPhone(UpdateMode, OptionalSchemeReferenceNumber(srn)))
        )
      behave like navigatorWithRoutesForMode(UpdateMode)(navigator, navigation, OptionalSchemeReferenceNumber(srn))
    }

    "in CheckUpdateMode" must {
      def navigation: TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          rowNoValue(DirectorAddressYearsId(0, 0))(addressYearsLessThanTwelveEdit(UpdateMode, directorNoExistingCurrentAddress, OptionalSchemeReferenceNumber(srn)),
                                                   ua = Some(directorNoExistingCurrentAddress)),
          rowNoValue(DirectorAddressYearsId(0, 0))(addressYearsLessThanTwelveEdit(UpdateMode, directorExistingCurrentAddress, OptionalSchemeReferenceNumber(srn)),
                                                   ua = Some(directorExistingCurrentAddress)),
          rowNoValue(DirectorAddressListId(0, 0))(confirmPreviousAddress(OptionalSchemeReferenceNumber(srn))),
          rowNoValueNewDirector(DirectorAddressListId(0, 0))(checkYourAnswers(UpdateMode, OptionalSchemeReferenceNumber(srn))),
          rowNoValue(DirectorAddressId(0, 0))(confirmPreviousAddress(OptionalSchemeReferenceNumber(srn))),
          rowNoValueNewDirector(DirectorAddressId(0, 0))(checkYourAnswers(UpdateMode, OptionalSchemeReferenceNumber(srn))),
          rowNoValue(DirectorNameId(0, 0))(exitJourney(UpdateMode, emptyAnswers, srn =  OptionalSchemeReferenceNumber(srn))),
          rowNoValueNewDirector(DirectorNameId(0, 0))(exitJourney(UpdateMode, newDirector, srn =  OptionalSchemeReferenceNumber(srn))),
          rowNoValue(DirectorDOBId(0, 0))(exitJourney(UpdateMode, emptyAnswers, srn =  OptionalSchemeReferenceNumber(srn))),
          rowNoValueNewDirector(DirectorDOBId(0, 0))(exitJourney(UpdateMode, newDirector, srn =  OptionalSchemeReferenceNumber(srn))),
          rowNoValue(DirectorEnterNINOId(0, 0))(exitJourney(UpdateMode, emptyAnswers, srn =  OptionalSchemeReferenceNumber(srn))),
          rowNoValueNewDirector(DirectorNoUTRReasonId(0, 0))(exitJourney(UpdateMode, newDirector, srn =  OptionalSchemeReferenceNumber(srn))),
          rowNewDirector(DirectorHasNINOId(0, 0))(true, directorNinoNew(CheckUpdateMode, OptionalSchemeReferenceNumber(srn))),
          rowNewDirector(DirectorHasNINOId(0, 0))(false, directorNinoReason(CheckUpdateMode, OptionalSchemeReferenceNumber(srn))),
          rowNewDirector(DirectorHasUTRId(0, 0))(true, directorWhatIsDirectorUTR(CheckUpdateMode, OptionalSchemeReferenceNumber(srn))),
          rowNewDirector(DirectorHasUTRId(0, 0))(false, directorWhyNoUTR(CheckUpdateMode, OptionalSchemeReferenceNumber(srn))),
          rowNoValue(DirectorAddressPostcodeLookupId(0, 0))(directorAddressList(CheckUpdateMode, OptionalSchemeReferenceNumber(srn))),
          row(DirectorAddressYearsId(0, 0))(AddressYears.OverAYear, exitJourney(UpdateMode, emptyAnswers, srn =  OptionalSchemeReferenceNumber(srn))),
          rowNewDirector(DirectorAddressYearsId(0, 0))(AddressYears.OverAYear, exitJourney(UpdateMode, newDirector, srn =  OptionalSchemeReferenceNumber(srn))),
          row(DirectorAddressYearsId(0, 0))(AddressYears.UnderAYear,
                                            addressYearsLessThanTwelveEdit(UpdateMode, addressYearsUnderAYear, srn =  OptionalSchemeReferenceNumber(srn))),
          row(DirectorConfirmPreviousAddressId(0, 0))(true, anyMoreChanges(OptionalSchemeReferenceNumber(srn))),
          row(DirectorConfirmPreviousAddressId(0, 0))(false, directorPreviousAddPostcode(CheckUpdateMode, OptionalSchemeReferenceNumber(srn))),
          rowNoValue(DirectorPreviousAddressPostcodeLookupId(0, 0))(directorPreviousAddList(CheckUpdateMode, OptionalSchemeReferenceNumber(srn))),
          rowNoValue(DirectorPreviousAddressListId(0, 0))(exitJourney(UpdateMode, emptyAnswers, srn =  OptionalSchemeReferenceNumber(srn))),
          rowNoValueNewDirector(DirectorPreviousAddressListId(0, 0))(exitJourney(UpdateMode, newDirector, srn =  OptionalSchemeReferenceNumber(srn))),
          rowNoValue(DirectorPreviousAddressId(0, 0))(exitJourney(UpdateMode, emptyAnswers, srn =  OptionalSchemeReferenceNumber(srn))),
          rowNoValueNewDirector(DirectorPreviousAddressId(0, 0))(exitJourney(UpdateMode, newDirector, srn =  OptionalSchemeReferenceNumber(srn))),
          rowNoValue(DirectorPhoneNumberId(0, 0))(exitJourney(UpdateMode, emptyAnswers, srn =  OptionalSchemeReferenceNumber(srn))),
          rowNoValueNewDirector(DirectorPhoneNumberId(0, 0))(exitJourney(UpdateMode, newDirector, srn =  OptionalSchemeReferenceNumber(srn))),
          rowNoValue(DirectorEmailId(0, 0))(exitJourney(UpdateMode, emptyAnswers, srn =  OptionalSchemeReferenceNumber(srn))),
          rowNoValueNewDirector(DirectorEmailId(0, 0))(exitJourney(UpdateMode, newDirector, srn =  OptionalSchemeReferenceNumber(srn)))
        )
      behave like navigatorWithRoutesForMode(CheckUpdateMode)(navigator, navigation, OptionalSchemeReferenceNumber(srn))
    }
  }
}

object EstablishersCompanyDirectorNavigatorSpec extends SpecBase with OptionValues {
  private implicit def writes[A: Enumerable]: Writes[A] = Writes(value => JsString(value.toString))

  private def rowNoValueNewDirector(id: TypedIdentifier.PathDependent)(call: Call): (id.type, UserAnswers, Call) = Tuple3(id, newDirector, call)

  private def rowNewDirector(id: TypedIdentifier.PathDependent)(value: id.Data, call: Call)(
    implicit writes: Writes[id.Data]): (id.type, UserAnswers, Call) = {
    val userAnswers = newDirector.set(id)(value).asOpt.value
    Tuple3(id, userAnswers, call)
  }

  private val srn = Some(SchemeReferenceNumber("srn"))
  private val emptyAnswers     = UserAnswers(Json.obj())
  private val establisherIndex = Index(0)
  private val directorIndex    = Index(0)
  private val newDirector      = UserAnswers(Json.obj()).set(IsNewDirectorId(establisherIndex, directorIndex))(true).asOpt.value

  private def addressYearsLessThanTwelveEdit(mode: => Mode, userAnswers: => UserAnswers, srn: OptionalSchemeReferenceNumber) =
    (
      userAnswers.get(ExistingCurrentAddressId(establisherIndex, directorIndex)),
      checkMode(mode)
    ) match {
      case (None, CheckUpdateMode) =>
        directorPreviousAddPostcode(checkMode(mode), OptionalSchemeReferenceNumber(srn))
      case (_, CheckUpdateMode) =>
        confirmPreviousAddress(srn)
      case _ =>
        directorPreviousAddPostcode(checkMode(mode), OptionalSchemeReferenceNumber(srn))
    }

  private def confirmPreviousAddress(srn: OptionalSchemeReferenceNumber) = routes.DirectorConfirmPreviousAddressController.onPageLoad(0, 0, OptionalSchemeReferenceNumber(srn))

  private def anyMoreChanges(srn: OptionalSchemeReferenceNumber) = controllers.routes.AnyMoreChangesController.onPageLoad(srn)

  private def exitJourney(mode: Mode, answers: UserAnswers, index: Int = 0, srn: OptionalSchemeReferenceNumber) =
    if (mode == NormalMode) checkYourAnswers(mode, OptionalSchemeReferenceNumber(srn))
    else {
      if (answers.get(IsNewDirectorId(establisherIndex, directorIndex)).getOrElse(false)) checkYourAnswers(mode, OptionalSchemeReferenceNumber(srn))
      else anyMoreChanges(srn)
    }

  private def directorPhone(mode: Mode, srn: OptionalSchemeReferenceNumber) =
    routes.DirectorPhoneNumberController.onPageLoad(mode, directorIndex, establisherIndex, OptionalSchemeReferenceNumber(srn))

  private def directorEmail(mode: Mode, srn: OptionalSchemeReferenceNumber) =
    routes.DirectorEmailController.onPageLoad(mode, directorIndex, establisherIndex, OptionalSchemeReferenceNumber(srn))

  private def directorAddressPostcode(mode: Mode, srn: OptionalSchemeReferenceNumber) =
    routes.DirectorAddressPostcodeLookupController.onPageLoad(mode, directorIndex, establisherIndex, OptionalSchemeReferenceNumber(srn))

  private def directorNinoNew(mode: Mode, srn: OptionalSchemeReferenceNumber) =
    routes.DirectorEnterNINOController.onPageLoad(mode, directorIndex, establisherIndex, OptionalSchemeReferenceNumber(srn))

  private def directorNinoReason(mode: Mode, srn: OptionalSchemeReferenceNumber) =
    routes.DirectorNoNINOReasonController.onPageLoad(mode, directorIndex, establisherIndex, OptionalSchemeReferenceNumber(srn))

  private def directorAddressList(mode: Mode, srn: OptionalSchemeReferenceNumber) =
    routes.DirectorAddressListController.onPageLoad(mode, directorIndex, establisherIndex, OptionalSchemeReferenceNumber(srn))

  private def directorAddressYears(mode: Mode, srn: OptionalSchemeReferenceNumber) =
    routes.DirectorAddressYearsController.onPageLoad(mode, directorIndex, establisherIndex, OptionalSchemeReferenceNumber(srn))

  private def directorPreviousAddPostcode(mode: Mode, srn: OptionalSchemeReferenceNumber) =
    routes.DirectorPreviousAddressPostcodeLookupController.onPageLoad(mode, directorIndex, establisherIndex, OptionalSchemeReferenceNumber(srn))

  private def directorPreviousAddList(mode: Mode, srn: OptionalSchemeReferenceNumber) =
    routes.DirectorPreviousAddressListController.onPageLoad(mode, directorIndex, establisherIndex, OptionalSchemeReferenceNumber(srn))

  private def checkYourAnswers(mode: Mode, srn: OptionalSchemeReferenceNumber) =
    routes.CheckYourAnswersController.onPageLoad(directorIndex, establisherIndex, mode, OptionalSchemeReferenceNumber(srn))

  private def addCompanyDirectors(mode: Mode, srn: OptionalSchemeReferenceNumber) =
    controllers.register.establishers.company.routes.AddCompanyDirectorsController.onPageLoad(mode, OptionalSchemeReferenceNumber(srn), establisherIndex)

  private def directorWhatIsDirectorUTR(mode: Mode, srn: OptionalSchemeReferenceNumber) =
    routes.DirectorEnterUTRController.onPageLoad(mode, directorIndex, establisherIndex, OptionalSchemeReferenceNumber(srn))

  private def directorWhyNoUTR(mode: Mode, srn: OptionalSchemeReferenceNumber) =
    routes.DirectorNoUTRReasonController.onPageLoad(mode, directorIndex, establisherIndex, OptionalSchemeReferenceNumber(srn))

  private def directorHasUTR(mode: Mode, srn: OptionalSchemeReferenceNumber) =
    routes.DirectorHasUTRController.onPageLoad(mode, directorIndex, establisherIndex, OptionalSchemeReferenceNumber(srn))

  private def directorDOB(mode: Mode, srn: OptionalSchemeReferenceNumber) =
    routes.DirectorDOBController.onPageLoad(mode, directorIndex, establisherIndex, OptionalSchemeReferenceNumber(srn))

  private def directorHasNINO(mode: Mode, srn: OptionalSchemeReferenceNumber) =
    routes.DirectorHasNINOController.onPageLoad(mode, directorIndex, establisherIndex, OptionalSchemeReferenceNumber(srn))

  private val addressYearsUnderAYear: UserAnswers = UserAnswers(Json.obj())
    .set(DirectorAddressYearsId(establisherIndex, directorIndex))(AddressYears.UnderAYear)
    .asOpt
    .value

  private val directorNoExistingCurrentAddress: UserAnswers = UserAnswers(Json.obj())
    .set(IsNewDirectorId(establisherIndex, directorIndex))(true)
    .flatMap(_.set(DirectorNameId(establisherIndex, directorIndex))(PersonName("Alan", "Allman", false)))
    .flatMap(_.set(DirectorHasNINOId(establisherIndex, directorIndex))(true))
    .flatMap(_.set(DirectorEnterNINOId(establisherIndex, directorIndex))(ReferenceValue("a")))
    .flatMap(_.set(DirectorHasUTRId(establisherIndex, directorIndex))(true))
    .flatMap(_.set(DirectorEnterUTRId(establisherIndex, directorIndex))(ReferenceValue("a")))
    .flatMap(_.set(DirectorAddressYearsId(establisherIndex, directorIndex))(AddressYears.UnderAYear))
    .flatMap(_.set(DirectorAddressId(establisherIndex, directorIndex))(Address("Line 1", "Line 2", None, None, None, "UK")))
    .asOpt
    .value

  private val directorExistingCurrentAddress: UserAnswers =
    directorNoExistingCurrentAddress
      .set(ExistingCurrentAddressId(establisherIndex, directorIndex))(Address("Line 1", "Line 2", None, None, None, "UK"))
      .asOpt
      .value
}
