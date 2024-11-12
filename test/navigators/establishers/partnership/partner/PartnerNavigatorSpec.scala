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

package navigators.establishers.partnership.partner

import base.SpecBase
import controllers.register.establishers.partnership.partner.routes._
import generators.Generators
import identifiers.Identifier
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.partnership._
import identifiers.register.establishers.partnership.partner._
import models.Mode.journeyMode
import models._
import models.person.PersonName
import navigators.{Navigator, NavigatorBehaviour}
import org.scalatest.matchers.must.Matchers
import org.scalatest.prop._
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.UserAnswers

import java.time.LocalDate

class PartnerNavigatorSpec extends SpecBase with Matchers with NavigatorBehaviour with Generators {

  import PartnerNavigatorSpec._

  val navigator: Navigator = injector.instanceOf[PartnerNavigator]

  "NormalMode" must {
    val normalModeRoutes: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Next Page"),
        row(AddPartnersId(index))(false, taskListPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(AddPartnersId(index))(true, otherPartnersPage(NormalMode, EmptyOptionalSchemeReferenceNumber), Some(addPartnersMoreThan10)),
        row(AddPartnersId(index))(true, partnerNamePage(NormalMode, EmptyOptionalSchemeReferenceNumber, partnerIndex = 1), Some(lessThan10Partners)),
        row(PartnerNameId(index, index))(somePersonNameValue, dobPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerDOBId(index, index))(LocalDate.now, hasNinoPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerHasNINOId(index, index))(true, ninoPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerHasNINOId(index, index))(false, whyNoNinoPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerEnterNINOId(index, index))(someRefValue, hasUtrPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerNoNINOReasonId(index, index))(someStringValue, hasUtrPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerHasUTRId(index, index))(true, utrPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerHasUTRId(index, index))(false, whyNoUtrPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerEnterUTRId(index, index))(someRefValue, postcodeLookupPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerNoUTRReasonId(index, index))(someStringValue, postcodeLookupPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerAddressPostcodeLookupId(index, index))(Seq(someTolerantAddress), addressListPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerAddressListId(index, index))(someTolerantAddress, addressYearsPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerAddressId(index, index))(someAddress, addressYearsPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerAddressYearsId(index, index))(value = AddressYears.UnderAYear, paPostcodeLookupPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerAddressYearsId(index, index))(value = AddressYears.OverAYear, emailPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerPreviousAddressPostcodeLookupId(index, index))(Seq(someTolerantAddress), paAddressListPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerPreviousAddressListId(index, index))(someTolerantAddress, emailPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerPreviousAddressId(index, index))(someAddress, emailPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerEmailId(index, index))(someStringValue, phonePage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerPhoneId(index, index))(someStringValue, cyaPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(OtherPartnersId(index))(false, taskListPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(OtherPartnersId(index))(true, taskListPage(NormalMode, EmptyOptionalSchemeReferenceNumber))
      )

    behave like navigatorWithRoutesForMode(NormalMode)(navigator, normalModeRoutes, EmptyOptionalSchemeReferenceNumber)
  }

  "CheckMode" must {
    val checkModeRoutes: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Next Page"),
        row(PartnerNameId(index, index))(somePersonNameValue, cyaPage(CheckMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerDOBId(index, index))(LocalDate.now, cyaPage(CheckMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerHasNINOId(index, index))(true, ninoPage(CheckMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerHasNINOId(index, index))(false, whyNoNinoPage(CheckMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerEnterNINOId(index, index))(someRefValue, cyaPage(CheckMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerNoNINOReasonId(index, index))(someStringValue, cyaPage(CheckMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerHasUTRId(index, index))(true, utrPage(CheckMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerHasUTRId(index, index))(false, whyNoUtrPage(CheckMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerEnterUTRId(index, index))(someRefValue, cyaPage(CheckMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerNoUTRReasonId(index, index))(someStringValue, cyaPage(CheckMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerAddressId(index, index))(someAddress, cyaPage(CheckMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerAddressYearsId(index, index))(value = AddressYears.UnderAYear, paPostcodeLookupPage(CheckMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerAddressYearsId(index, index))(value = AddressYears.OverAYear, cyaPage(CheckMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerPreviousAddressId(index, index))(someAddress, cyaPage(CheckMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerEmailId(index, index))(someStringValue, cyaPage(CheckMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerPhoneId(index, index))(someStringValue, cyaPage(CheckMode, EmptyOptionalSchemeReferenceNumber))
      )

    behave like navigatorWithRoutesForMode(CheckMode)(navigator, checkModeRoutes, EmptyOptionalSchemeReferenceNumber)
  }

  "UpdateMode" must {
    val updateModeRoutes: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Next page"),
        row(AddPartnersId(index))(false, anyMoreChangesPage(srn)),
        row(AddPartnersId(index))(true, otherPartnersPage(UpdateMode, OptionalSchemeReferenceNumber(srn)), Some(addPartnersMoreThan10)),
        row(AddPartnersId(index))(true, partnerNamePage(UpdateMode, OptionalSchemeReferenceNumber(srn), partnerIndex = 1), Some(lessThan10Partners)),
        row(PartnerNameId(index, index))(somePersonNameValue, dobPage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnerDOBId(index, index))(LocalDate.now, hasNinoPage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnerHasNINOId(index, index))(true, ninoPage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnerHasNINOId(index, index))(false, whyNoNinoPage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnerEnterNINOId(index, index))(someRefValue, hasUtrPage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnerNoNINOReasonId(index, index))(someStringValue, hasUtrPage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnerHasUTRId(index, index))(true, utrPage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnerHasUTRId(index, index))(false, whyNoUtrPage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnerEnterUTRId(index, index))(someRefValue, postcodeLookupPage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnerNoUTRReasonId(index, index))(someStringValue, postcodeLookupPage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnerAddressPostcodeLookupId(index, index))(Seq(someTolerantAddress), addressListPage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnerAddressListId(index, index))(someTolerantAddress, addressYearsPage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnerAddressId(index, index))(someAddress, addressYearsPage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnerAddressYearsId(index, index))(value = AddressYears.UnderAYear, paPostcodeLookupPage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnerAddressYearsId(index, index))(value = AddressYears.OverAYear, emailPage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnerPreviousAddressPostcodeLookupId(index, index))(Seq(someTolerantAddress), paAddressListPage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnerPreviousAddressListId(index, index))(someTolerantAddress, emailPage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnerPreviousAddressId(index, index))(someAddress, emailPage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnerEmailId(index, index))(someStringValue, phonePage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnerPhoneId(index, index))(someStringValue, cyaPage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(OtherPartnersId(index))(false, anyMoreChangesPage(srn)),
        row(OtherPartnersId(index))(true, anyMoreChangesPage(srn))
      )

    behave like navigatorWithRoutesForMode(UpdateMode)(navigator, updateModeRoutes, OptionalSchemeReferenceNumber(srn))
  }

  "CheckUpdateMode" must {
    val checkUpdateModeRoutes: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Next page"),
        row(PartnerNameId(index, index))(somePersonNameValue, cyaPage(CheckUpdateMode, OptionalSchemeReferenceNumber(srn)), Some(newPartnerUserAnswers)),
        row(PartnerDOBId(index, index))(LocalDate.now, cyaPage(CheckUpdateMode, OptionalSchemeReferenceNumber(srn)), Some(newPartnerUserAnswers)),
        row(PartnerHasNINOId(index, index))(true, ninoPage(CheckUpdateMode, OptionalSchemeReferenceNumber(srn)), Some(newPartnerUserAnswers)),
        row(PartnerHasNINOId(index, index))(false, whyNoNinoPage(CheckUpdateMode, OptionalSchemeReferenceNumber(srn)), Some(newPartnerUserAnswers)),
        row(PartnerEnterNINOId(index, index))(someRefValue, cyaPage(CheckUpdateMode, OptionalSchemeReferenceNumber(srn)), Some(newPartnerUserAnswers)),
        row(PartnerEnterNINOId(index, index))(someRefValue, anyMoreChangesPage(srn)),
        row(PartnerNoNINOReasonId(index, index))(someStringValue, cyaPage(CheckUpdateMode, OptionalSchemeReferenceNumber(srn)), Some(newPartnerUserAnswers)),
        row(PartnerHasUTRId(index, index))(true, utrPage(CheckUpdateMode, OptionalSchemeReferenceNumber(srn)), Some(newPartnerUserAnswers)),
        row(PartnerHasUTRId(index, index))(false, whyNoUtrPage(CheckUpdateMode, OptionalSchemeReferenceNumber(srn)), Some(newPartnerUserAnswers)),
        row(PartnerEnterUTRId(index, index))(someRefValue, cyaPage(CheckUpdateMode, OptionalSchemeReferenceNumber(srn)), Some(newPartnerUserAnswers)),
        row(PartnerEnterUTRId(index, index))(someRefValue, anyMoreChangesPage(srn)),
        row(PartnerNoUTRReasonId(index, index))(someStringValue, cyaPage(CheckUpdateMode, OptionalSchemeReferenceNumber(srn)), Some(newPartnerUserAnswers)),
        row(PartnerAddressId(index, index))(someAddress, cyaPage(CheckUpdateMode, OptionalSchemeReferenceNumber(srn)), Some(newPartnerUserAnswers)),
        row(PartnerAddressId(index, index))(someAddress, isThisPaPage(CheckUpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnerAddressYearsId(index, index))(value = AddressYears.UnderAYear, paPostcodeLookupPage(CheckUpdateMode, OptionalSchemeReferenceNumber(srn)), Some(newPartnerUserAnswers)),
        row(PartnerAddressYearsId(index, index))(value = AddressYears.OverAYear, emailPage(CheckUpdateMode, OptionalSchemeReferenceNumber(srn)), Some(newPartnerUserAnswers)),
        row(PartnerPreviousAddressId(index, index))(someAddress, cyaPage(CheckUpdateMode, OptionalSchemeReferenceNumber(srn)), Some(newPartnerUserAnswers)),
        row(PartnerPreviousAddressId(index, index))(someAddress, anyMoreChangesPage(srn)),
        row(PartnerEmailId(index, index))(someStringValue, cyaPage(CheckUpdateMode, OptionalSchemeReferenceNumber(srn)), Some(newPartnerUserAnswers)),
        row(PartnerEmailId(index, index))(someStringValue, anyMoreChangesPage(srn)),
        row(PartnerPhoneId(index, index))(someStringValue, cyaPage(CheckUpdateMode, OptionalSchemeReferenceNumber(srn)), Some(newPartnerUserAnswers)),
        row(PartnerPhoneId(index, index))(someStringValue, anyMoreChangesPage(srn))
      )

    behave like navigatorWithRoutesForMode(CheckUpdateMode)(navigator, checkUpdateModeRoutes, OptionalSchemeReferenceNumber(srn))
  }
}


object PartnerNavigatorSpec extends SpecBase with Matchers with NavigatorBehaviour with Generators {
  private lazy val index = 0
  private val srn = Some(SchemeReferenceNumber("srn"))
  private val johnDoe = PersonName("first", "last")
  private def validData(partners: PersonName*) = {
    Json.obj(
      EstablishersId.toString -> Json.arr(
        Json.obj(
          PartnershipDetailsId.toString -> PartnershipDetails("test partnership name", false),
          "partner" -> partners.map(d => Json.obj(PartnerNameId.toString -> Json.toJson(d)))
        )
      )
    )
  }

  private val lessThan10Partners = UserAnswers(validData(johnDoe))
  private val addPartnersMoreThan10 = UserAnswers(validData(Seq.fill(10)(johnDoe): _*))
  private val newPartnerUserAnswers: UserAnswers = UserAnswers().set(IsNewPartnerId(index, index))(value = true).asOpt.value

  private def isThisPaPage(mode: Mode,  srn: OptionalSchemeReferenceNumber): Call =
    PartnerConfirmPreviousAddressController.onPageLoad(index, index, OptionalSchemeReferenceNumber(srn))

  private def otherPartnersPage(mode: Mode,  srn: OptionalSchemeReferenceNumber): Call =
    controllers.register.establishers.partnership.routes.OtherPartnersController.onPageLoad(mode, index, OptionalSchemeReferenceNumber(srn))

  private def taskListPage(mode: Mode,  srn: OptionalSchemeReferenceNumber): Call =
    controllers.routes.PsaSchemeTaskListController.onPageLoad(mode, OptionalSchemeReferenceNumber(srn))

  private def partnerNamePage(mode: Mode, srn: OptionalSchemeReferenceNumber, partnerIndex: Int): Call =
    controllers.register.establishers.partnership.partner.routes.PartnerNameController.onPageLoad(mode, index, partnerIndex, OptionalSchemeReferenceNumber(srn))

  private def dobPage(mode: Mode, srn: OptionalSchemeReferenceNumber): Call =
    PartnerDOBController.onPageLoad(mode, index, index, OptionalSchemeReferenceNumber(srn))

  private def hasNinoPage(mode: Mode,  srn: OptionalSchemeReferenceNumber): Call =
    PartnerHasNINOController.onPageLoad(mode, index, index, OptionalSchemeReferenceNumber(srn))

  private def ninoPage(mode: Mode,  srn: OptionalSchemeReferenceNumber): Call =
    PartnerEnterNINOController.onPageLoad(mode, index, index, OptionalSchemeReferenceNumber(srn))

  private def whyNoNinoPage(mode: Mode,  srn: OptionalSchemeReferenceNumber): Call =
    PartnerNoNINOReasonController.onPageLoad(mode, index, index, OptionalSchemeReferenceNumber(srn))

  private def hasUtrPage(mode: Mode,  srn: OptionalSchemeReferenceNumber): Call =
    PartnerHasUTRController.onPageLoad(mode, index, index, OptionalSchemeReferenceNumber(srn))

  private def utrPage(mode: Mode,  srn: OptionalSchemeReferenceNumber): Call =
    PartnerEnterUTRController.onPageLoad(mode, index, index, OptionalSchemeReferenceNumber(srn))

  private def whyNoUtrPage(mode: Mode,  srn: OptionalSchemeReferenceNumber): Call =
    PartnerNoUTRReasonController.onPageLoad(mode, index, index, OptionalSchemeReferenceNumber(srn))

  private def cyaPage(mode: Mode,  srn: OptionalSchemeReferenceNumber): Call =
    CheckYourAnswersController.onPageLoad(journeyMode(mode), index, index, OptionalSchemeReferenceNumber(srn))

  private def postcodeLookupPage(mode: Mode,  srn: OptionalSchemeReferenceNumber): Call =
    PartnerAddressPostcodeLookupController.onPageLoad(mode, index, index, OptionalSchemeReferenceNumber(srn))

  private def addressListPage(mode: Mode,  srn: OptionalSchemeReferenceNumber): Call =
    PartnerAddressListController.onPageLoad(mode, index, index, OptionalSchemeReferenceNumber(srn))

  private def paPostcodeLookupPage(mode: Mode,  srn: OptionalSchemeReferenceNumber): Call =
    PartnerPreviousAddressPostcodeLookupController.onPageLoad(mode, index, index, OptionalSchemeReferenceNumber(srn))

  private def paAddressListPage(mode: Mode,  srn: OptionalSchemeReferenceNumber): Call =
    PartnerPreviousAddressListController.onPageLoad(mode, index, index, OptionalSchemeReferenceNumber(srn))

  private def addressYearsPage(mode: Mode,  srn: OptionalSchemeReferenceNumber): Call =
    PartnerAddressYearsController.onPageLoad(mode, index, index, OptionalSchemeReferenceNumber(srn))

  private def emailPage(mode: Mode,  srn: OptionalSchemeReferenceNumber): Call =
    PartnerEmailController.onPageLoad(mode, index, index, OptionalSchemeReferenceNumber(srn))

  private def phonePage(mode: Mode,  srn: OptionalSchemeReferenceNumber): Call =
    PartnerPhoneController.onPageLoad(mode, index, index, OptionalSchemeReferenceNumber(srn))
}








