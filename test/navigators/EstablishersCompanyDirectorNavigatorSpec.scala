/*
 * Copyright 2023 HM Revenue & Customs
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
          rowNoValue(ConfirmDeleteDirectorId(0))(addCompanyDirectors(NormalMode, None)),
          rowNoValue(CheckYourAnswersId(0, 0))(addCompanyDirectors(NormalMode, None)),
          rowNoValue(DirectorAddressListId(0, 0))(directorAddressYears(NormalMode, None)),
          rowNoValueNewDirector(DirectorAddressListId(0, 0))(directorAddressYears(NormalMode, None)),
          rowNoValue(DirectorAddressId(0, 0))(directorAddressYears(NormalMode, None)),
          rowNoValueNewDirector(DirectorAddressId(0, 0))(directorAddressYears(NormalMode, None)),
          rowNoValue(DirectorNameId(0, 0))(directorDOB(NormalMode, None)),
          rowNoValueNewDirector(DirectorNameId(0, 0))(directorDOB(NormalMode, None)),
          rowNoValue(DirectorDOBId(0, 0))(directorHasNINO(NormalMode, None)),
          rowNoValueNewDirector(DirectorDOBId(0, 0))(directorHasNINO(NormalMode, None)),
          rowNoValue(DirectorEnterNINOId(0, 0))(directorHasUTR(NormalMode, None)),
          rowNoValueNewDirector(DirectorNoUTRReasonId(0, 0))(directorAddressPostcode(NormalMode, None)),
          rowNewDirector(DirectorHasNINOId(0, 0))(true, directorNinoNew(NormalMode, None)),
          rowNewDirector(DirectorHasNINOId(0, 0))(false, directorNinoReason(NormalMode, None)),
          rowNewDirector(DirectorHasUTRId(0, 0))(true, directorWhatIsDirectorUTR(NormalMode, None)),
          rowNewDirector(DirectorHasUTRId(0, 0))(false, directorWhyNoUTR(NormalMode, None)),
          rowNoValue(DirectorAddressPostcodeLookupId(0, 0))(directorAddressList(NormalMode, None)),
          row(DirectorAddressYearsId(0, 0))(AddressYears.OverAYear, directorEmail(NormalMode, None)),
          rowNewDirector(DirectorAddressYearsId(0, 0))(AddressYears.OverAYear, directorEmail(NormalMode, None)),
          row(DirectorAddressYearsId(0, 0))(AddressYears.UnderAYear, directorPreviousAddPostcode(NormalMode, None)),
          rowNoValue(DirectorPreviousAddressPostcodeLookupId(0, 0))(directorPreviousAddList(NormalMode, None)),
          rowNoValue(DirectorPreviousAddressListId(0, 0))(directorEmail(NormalMode, None)),
          rowNoValueNewDirector(DirectorPreviousAddressListId(0, 0))(directorEmail(NormalMode, None)),
          rowNoValue(DirectorPreviousAddressId(0, 0))(directorEmail(NormalMode, None)),
          rowNoValueNewDirector(DirectorPreviousAddressId(0, 0))(directorEmail(NormalMode, None)),
          rowNoValue(DirectorPhoneNumberId(0, 0))(checkYourAnswers(NormalMode, None)),
          rowNoValueNewDirector(DirectorPhoneNumberId(0, 0))(checkYourAnswers(NormalMode, None)),
          rowNoValue(DirectorEmailId(0, 0))(directorPhone(NormalMode, None)),
          rowNoValueNewDirector(DirectorEmailId(0, 0))(directorPhone(NormalMode, None))
        )
      behave like navigatorWithRoutesForMode(NormalMode)(navigator, navigation, None)
    }

    "in CheckMode" must {
      def navigation: TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          rowNoValue(DirectorNameId(0, 0))(exitJourney(NormalMode, emptyAnswers, srn = None)),
          rowNoValueNewDirector(DirectorNameId(0, 0))(exitJourney(NormalMode, newDirector, srn = None)),
          rowNoValue(DirectorDOBId(0, 0))(exitJourney(NormalMode, emptyAnswers, srn = None)),
          rowNoValueNewDirector(DirectorDOBId(0, 0))(exitJourney(NormalMode, newDirector, srn = None)),
          rowNoValue(DirectorEnterNINOId(0, 0))(exitJourney(NormalMode, emptyAnswers, srn = None)),
          rowNoValueNewDirector(DirectorNoUTRReasonId(0, 0))(exitJourney(NormalMode, newDirector, srn = None)),
          rowNewDirector(DirectorHasNINOId(0, 0))(true, directorNinoNew(CheckMode, None)),
          rowNewDirector(DirectorHasNINOId(0, 0))(false, directorNinoReason(CheckMode, None)),
          rowNewDirector(DirectorHasUTRId(0, 0))(true, directorWhatIsDirectorUTR(CheckMode, None)),
          rowNewDirector(DirectorHasUTRId(0, 0))(false, directorWhyNoUTR(CheckMode, None)),
          rowNoValue(DirectorAddressPostcodeLookupId(0, 0))(directorAddressList(CheckMode, None)),
          row(DirectorAddressYearsId(0, 0))(AddressYears.OverAYear, exitJourney(NormalMode, newDirector, srn = None)),
          rowNewDirector(DirectorAddressYearsId(0, 0))(AddressYears.OverAYear, exitJourney(NormalMode, newDirector, srn = None)),
          row(DirectorAddressYearsId(0, 0))(AddressYears.UnderAYear,
                                            addressYearsLessThanTwelveEdit(NormalMode, addressYearsUnderAYear, srn = None)),
          row(DirectorConfirmPreviousAddressId(0, 0))(true, anyMoreChanges(None)),
          row(DirectorConfirmPreviousAddressId(0, 0))(false, directorPreviousAddPostcode(CheckMode, None)),
          rowNoValue(DirectorPreviousAddressPostcodeLookupId(0, 0))(directorPreviousAddList(CheckMode, None)),
          rowNoValue(DirectorPreviousAddressListId(0, 0))(exitJourney(NormalMode, emptyAnswers, srn = None)),
          rowNoValueNewDirector(DirectorPreviousAddressListId(0, 0))(exitJourney(NormalMode, newDirector, srn = None)),
          rowNoValue(DirectorPreviousAddressId(0, 0))(exitJourney(NormalMode, emptyAnswers, srn = None)),
          rowNoValueNewDirector(DirectorPreviousAddressId(0, 0))(exitJourney(NormalMode, newDirector, srn = None)),
          rowNoValue(DirectorPhoneNumberId(0, 0))(exitJourney(NormalMode, emptyAnswers, srn = None)),
          rowNoValueNewDirector(DirectorPhoneNumberId(0, 0))(exitJourney(NormalMode, newDirector, srn = None)),
          rowNoValue(DirectorEmailId(0, 0))(exitJourney(NormalMode, emptyAnswers, srn = None)),
          rowNoValueNewDirector(DirectorEmailId(0, 0))(exitJourney(NormalMode, newDirector, srn = None))
        )
      behave like navigatorWithRoutesForMode(CheckMode)(navigator, navigation, None)
    }

    "in UpdateMode" must {
      def navigation: TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          rowNoValue(ConfirmDeleteDirectorId(0))(anyMoreChanges(srn)),
          rowNoValue(CheckYourAnswersId(0, 0))(addCompanyDirectors(UpdateMode, srn)),
          rowNoValue(DirectorAddressYearsId(0, 0))(directorPreviousAddPostcode(UpdateMode, srn),
                                                   ua = Some(directorNoExistingCurrentAddress)),
          rowNoValue(DirectorAddressYearsId(0, 0))(directorPreviousAddPostcode(UpdateMode, srn), ua = Some(directorExistingCurrentAddress)),
          rowNoValue(DirectorAddressListId(0, 0))(directorAddressYears(UpdateMode, srn)),
          rowNoValueNewDirector(DirectorAddressListId(0, 0))(directorAddressYears(UpdateMode, srn)),
          rowNoValue(DirectorAddressId(0, 0))(directorAddressYears(UpdateMode, srn)),
          rowNoValueNewDirector(DirectorAddressId(0, 0))(directorAddressYears(UpdateMode, srn)),
          rowNoValue(DirectorNameId(0, 0))(directorDOB(UpdateMode, srn)),
          rowNoValueNewDirector(DirectorNameId(0, 0))(directorDOB(UpdateMode, srn)),
          rowNoValue(DirectorDOBId(0, 0))(directorHasNINO(UpdateMode, srn)),
          rowNoValueNewDirector(DirectorDOBId(0, 0))(directorHasNINO(UpdateMode, srn)),
          rowNoValue(DirectorEnterNINOId(0, 0))(directorHasUTR(UpdateMode, srn)),
          rowNoValueNewDirector(DirectorNoUTRReasonId(0, 0))(directorAddressPostcode(UpdateMode, srn)),
          rowNewDirector(DirectorHasNINOId(0, 0))(true, directorNinoNew(UpdateMode, srn)),
          rowNewDirector(DirectorHasNINOId(0, 0))(false, directorNinoReason(UpdateMode, srn)),
          rowNewDirector(DirectorHasUTRId(0, 0))(true, directorWhatIsDirectorUTR(UpdateMode, srn)),
          rowNewDirector(DirectorHasUTRId(0, 0))(false, directorWhyNoUTR(UpdateMode, srn)),
          rowNoValue(DirectorAddressPostcodeLookupId(0, 0))(directorAddressList(UpdateMode, srn)),
          row(DirectorAddressYearsId(0, 0))(AddressYears.OverAYear, directorEmail(UpdateMode, srn)),
          rowNewDirector(DirectorAddressYearsId(0, 0))(AddressYears.OverAYear, directorEmail(UpdateMode, srn)),
          row(DirectorAddressYearsId(0, 0))(AddressYears.UnderAYear, directorPreviousAddPostcode(UpdateMode, srn)),
          rowNoValue(DirectorPreviousAddressPostcodeLookupId(0, 0))(directorPreviousAddList(UpdateMode, srn)),
          rowNoValue(DirectorPreviousAddressListId(0, 0))(directorEmail(UpdateMode, srn)),
          rowNoValueNewDirector(DirectorPreviousAddressListId(0, 0))(directorEmail(UpdateMode, srn)),
          rowNoValue(DirectorPreviousAddressId(0, 0))(directorEmail(UpdateMode, srn)),
          rowNoValueNewDirector(DirectorPreviousAddressId(0, 0))(directorEmail(UpdateMode, srn)),
          rowNoValue(DirectorPhoneNumberId(0, 0))(checkYourAnswers(UpdateMode, srn)),
          rowNoValueNewDirector(DirectorPhoneNumberId(0, 0))(checkYourAnswers(UpdateMode, srn)),
          rowNoValue(DirectorEmailId(0, 0))(directorPhone(UpdateMode, srn)),
          rowNoValueNewDirector(DirectorEmailId(0, 0))(directorPhone(UpdateMode, srn))
        )
      behave like navigatorWithRoutesForMode(UpdateMode)(navigator, navigation, srn)
    }

    "in CheckUpdateMode" must {
      def navigation: TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          rowNoValue(DirectorAddressYearsId(0, 0))(addressYearsLessThanTwelveEdit(UpdateMode, directorNoExistingCurrentAddress, srn),
                                                   ua = Some(directorNoExistingCurrentAddress)),
          rowNoValue(DirectorAddressYearsId(0, 0))(addressYearsLessThanTwelveEdit(UpdateMode, directorExistingCurrentAddress, srn),
                                                   ua = Some(directorExistingCurrentAddress)),
          rowNoValue(DirectorAddressListId(0, 0))(confirmPreviousAddress(srn)),
          rowNoValueNewDirector(DirectorAddressListId(0, 0))(checkYourAnswers(UpdateMode, srn)),
          rowNoValue(DirectorAddressId(0, 0))(confirmPreviousAddress(srn)),
          rowNoValueNewDirector(DirectorAddressId(0, 0))(checkYourAnswers(UpdateMode, srn)),
          rowNoValue(DirectorNameId(0, 0))(exitJourney(UpdateMode, emptyAnswers, srn = srn)),
          rowNoValueNewDirector(DirectorNameId(0, 0))(exitJourney(UpdateMode, newDirector, srn = srn)),
          rowNoValue(DirectorDOBId(0, 0))(exitJourney(UpdateMode, emptyAnswers, srn = srn)),
          rowNoValueNewDirector(DirectorDOBId(0, 0))(exitJourney(UpdateMode, newDirector, srn = srn)),
          rowNoValue(DirectorEnterNINOId(0, 0))(exitJourney(UpdateMode, emptyAnswers, srn = srn)),
          rowNoValueNewDirector(DirectorNoUTRReasonId(0, 0))(exitJourney(UpdateMode, newDirector, srn = srn)),
          rowNewDirector(DirectorHasNINOId(0, 0))(true, directorNinoNew(CheckUpdateMode, srn)),
          rowNewDirector(DirectorHasNINOId(0, 0))(false, directorNinoReason(CheckUpdateMode, srn)),
          rowNewDirector(DirectorHasUTRId(0, 0))(true, directorWhatIsDirectorUTR(CheckUpdateMode, srn)),
          rowNewDirector(DirectorHasUTRId(0, 0))(false, directorWhyNoUTR(CheckUpdateMode, srn)),
          rowNoValue(DirectorAddressPostcodeLookupId(0, 0))(directorAddressList(CheckUpdateMode, srn)),
          row(DirectorAddressYearsId(0, 0))(AddressYears.OverAYear, exitJourney(UpdateMode, emptyAnswers, srn = srn)),
          rowNewDirector(DirectorAddressYearsId(0, 0))(AddressYears.OverAYear, exitJourney(UpdateMode, newDirector, srn = srn)),
          row(DirectorAddressYearsId(0, 0))(AddressYears.UnderAYear,
                                            addressYearsLessThanTwelveEdit(UpdateMode, addressYearsUnderAYear, srn = srn)),
          row(DirectorConfirmPreviousAddressId(0, 0))(true, anyMoreChanges(srn)),
          row(DirectorConfirmPreviousAddressId(0, 0))(false, directorPreviousAddPostcode(CheckUpdateMode, srn)),
          rowNoValue(DirectorPreviousAddressPostcodeLookupId(0, 0))(directorPreviousAddList(CheckUpdateMode, srn)),
          rowNoValue(DirectorPreviousAddressListId(0, 0))(exitJourney(UpdateMode, emptyAnswers, srn = srn)),
          rowNoValueNewDirector(DirectorPreviousAddressListId(0, 0))(exitJourney(UpdateMode, newDirector, srn = srn)),
          rowNoValue(DirectorPreviousAddressId(0, 0))(exitJourney(UpdateMode, emptyAnswers, srn = srn)),
          rowNoValueNewDirector(DirectorPreviousAddressId(0, 0))(exitJourney(UpdateMode, newDirector, srn = srn)),
          rowNoValue(DirectorPhoneNumberId(0, 0))(exitJourney(UpdateMode, emptyAnswers, srn = srn)),
          rowNoValueNewDirector(DirectorPhoneNumberId(0, 0))(exitJourney(UpdateMode, newDirector, srn = srn)),
          rowNoValue(DirectorEmailId(0, 0))(exitJourney(UpdateMode, emptyAnswers, srn = srn)),
          rowNoValueNewDirector(DirectorEmailId(0, 0))(exitJourney(UpdateMode, newDirector, srn = srn))
        )
      behave like navigatorWithRoutesForMode(CheckUpdateMode)(navigator, navigation, srn)
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

  private val srn = Some("srn")
  private val emptyAnswers     = UserAnswers(Json.obj())
  private val establisherIndex = Index(0)
  private val directorIndex    = Index(0)
  private val newDirector      = UserAnswers(Json.obj()).set(IsNewDirectorId(establisherIndex, directorIndex))(true).asOpt.value

  private def addressYearsLessThanTwelveEdit(mode: => Mode, userAnswers: => UserAnswers, srn: Option[String]) =
    (
      userAnswers.get(ExistingCurrentAddressId(establisherIndex, directorIndex)),
      checkMode(mode)
    ) match {
      case (None, CheckUpdateMode) =>
        directorPreviousAddPostcode(checkMode(mode), srn)
      case (_, CheckUpdateMode) =>
        confirmPreviousAddress(srn)
      case _ =>
        directorPreviousAddPostcode(checkMode(mode), srn)
    }

  private def confirmPreviousAddress(srn: Option[String]) = routes.DirectorConfirmPreviousAddressController.onPageLoad(0, 0, srn)

  private def anyMoreChanges(srn: Option[String]) = controllers.routes.AnyMoreChangesController.onPageLoad(srn)

  private def exitJourney(mode: Mode, answers: UserAnswers, index: Int = 0, srn: Option[String]) =
    if (mode == NormalMode) checkYourAnswers(mode, srn)
    else {
      if (answers.get(IsNewDirectorId(establisherIndex, directorIndex)).getOrElse(false)) checkYourAnswers(mode, srn)
      else anyMoreChanges(srn)
    }

  private def directorPhone(mode: Mode, srn: Option[String]) =
    routes.DirectorPhoneNumberController.onPageLoad(mode, directorIndex, establisherIndex, srn)

  private def directorEmail(mode: Mode, srn: Option[String]) =
    routes.DirectorEmailController.onPageLoad(mode, directorIndex, establisherIndex, srn)

  private def directorAddressPostcode(mode: Mode, srn: Option[String]) =
    routes.DirectorAddressPostcodeLookupController.onPageLoad(mode, directorIndex, establisherIndex, srn)

  private def directorNinoNew(mode: Mode, srn: Option[String]) =
    routes.DirectorEnterNINOController.onPageLoad(mode, directorIndex, establisherIndex, srn)

  private def directorNinoReason(mode: Mode, srn: Option[String]) =
    routes.DirectorNoNINOReasonController.onPageLoad(mode, directorIndex, establisherIndex, srn)

  private def directorAddressList(mode: Mode, srn: Option[String]) =
    routes.DirectorAddressListController.onPageLoad(mode, directorIndex, establisherIndex, srn)

  private def directorAddressYears(mode: Mode, srn: Option[String]) =
    routes.DirectorAddressYearsController.onPageLoad(mode, directorIndex, establisherIndex, srn)

  private def directorPreviousAddPostcode(mode: Mode, srn: Option[String]) =
    routes.DirectorPreviousAddressPostcodeLookupController.onPageLoad(mode, directorIndex, establisherIndex, srn)

  private def directorPreviousAddList(mode: Mode, srn: Option[String]) =
    routes.DirectorPreviousAddressListController.onPageLoad(mode, directorIndex, establisherIndex, srn)

  private def checkYourAnswers(mode: Mode, srn: Option[String]) =
    routes.CheckYourAnswersController.onPageLoad(directorIndex, establisherIndex, mode, srn)

  private def addCompanyDirectors(mode: Mode, srn: Option[String]) =
    controllers.register.establishers.company.routes.AddCompanyDirectorsController.onPageLoad(mode, srn, establisherIndex)

  private def directorWhatIsDirectorUTR(mode: Mode, srn: Option[String]) =
    routes.DirectorEnterUTRController.onPageLoad(mode, directorIndex, establisherIndex, srn)

  private def directorWhyNoUTR(mode: Mode, srn: Option[String]) =
    routes.DirectorNoUTRReasonController.onPageLoad(mode, directorIndex, establisherIndex, srn)

  private def directorHasUTR(mode: Mode, srn: Option[String]) =
    routes.DirectorHasUTRController.onPageLoad(mode, directorIndex, establisherIndex, srn)

  private def directorDOB(mode: Mode, srn: Option[String]) =
    routes.DirectorDOBController.onPageLoad(mode, directorIndex, establisherIndex, srn)

  private def directorHasNINO(mode: Mode, srn: Option[String]) =
    routes.DirectorHasNINOController.onPageLoad(mode, directorIndex, establisherIndex, srn)

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
