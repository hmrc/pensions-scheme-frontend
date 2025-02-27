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
        row(AddPartnersId(index))(true, otherPartnersPage(NormalMode, EmptyOptionalSchemeReferenceNumber), Some(addPartnersMoreThan10)),
        row(AddPartnersId(index))(true, partnerNamePage(NormalMode, EmptyOptionalSchemeReferenceNumber, partnerIndex = 1), Some(lessThan10Partners)),
        row(PartnerNameId(Index(0), index))(somePersonNameValue, dobPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerDOBId(Index(0), index))(LocalDate.now, hasNinoPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerHasNINOId(Index(0), index))(true, ninoPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerHasNINOId(Index(0), index))(false, whyNoNinoPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerEnterNINOId(Index(0), index))(someRefValue, hasUtrPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerNoNINOReasonId(Index(0), index))(someStringValue, hasUtrPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerHasUTRId(Index(0), index))(true, utrPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerHasUTRId(Index(0), index))(false, whyNoUtrPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerEnterUTRId(Index(0), index))(someRefValue, postcodeLookupPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerNoUTRReasonId(Index(0), index))(someStringValue, postcodeLookupPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerAddressPostcodeLookupId(Index(0), index))(Seq(someTolerantAddress), addressListPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerAddressListId(Index(0), index))(someTolerantAddress, addressYearsPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerAddressId(Index(0), index))(someAddress, addressYearsPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerAddressYearsId(Index(0), index))(value = AddressYears.UnderAYear, paPostcodeLookupPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerAddressYearsId(Index(0), index))(value = AddressYears.OverAYear, emailPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerPreviousAddressPostcodeLookupId(Index(0), index))(Seq(someTolerantAddress), paAddressListPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerPreviousAddressListId(Index(0), index))(someTolerantAddress, emailPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerPreviousAddressId(Index(0), index))(someAddress, emailPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerEmailId(Index(0), index))(someStringValue, phonePage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerPhoneId(Index(0), index))(someStringValue, cyaPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(OtherPartnersId(index))(false, taskListPage(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(OtherPartnersId(index))(true, taskListPage(NormalMode, EmptyOptionalSchemeReferenceNumber))
      )

    behave like navigatorWithRoutesForMode(NormalMode)(navigator, normalModeRoutes, EmptyOptionalSchemeReferenceNumber)
  }

  "CheckMode" must {
    val checkModeRoutes: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Next Page"),
        row(PartnerNameId(Index(0), index))(somePersonNameValue, cyaPage(CheckMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerDOBId(Index(0), index))(LocalDate.now, cyaPage(CheckMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerHasNINOId(Index(0), index))(true, ninoPage(CheckMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerHasNINOId(Index(0), index))(false, whyNoNinoPage(CheckMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerEnterNINOId(Index(0), index))(someRefValue, cyaPage(CheckMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerNoNINOReasonId(Index(0), index))(someStringValue, cyaPage(CheckMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerHasUTRId(Index(0), index))(true, utrPage(CheckMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerHasUTRId(Index(0), index))(false, whyNoUtrPage(CheckMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerEnterUTRId(Index(0), index))(someRefValue, cyaPage(CheckMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerNoUTRReasonId(Index(0), index))(someStringValue, cyaPage(CheckMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerAddressId(Index(0), index))(someAddress, cyaPage(CheckMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerAddressYearsId(Index(0), index))(value = AddressYears.UnderAYear, paPostcodeLookupPage(CheckMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerAddressYearsId(Index(0), index))(value = AddressYears.OverAYear, cyaPage(CheckMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerPreviousAddressId(Index(0), index))(someAddress, cyaPage(CheckMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerEmailId(Index(0), index))(someStringValue, cyaPage(CheckMode, EmptyOptionalSchemeReferenceNumber)),
        row(PartnerPhoneId(Index(0), index))(someStringValue, cyaPage(CheckMode, EmptyOptionalSchemeReferenceNumber))
      )

    behave like navigatorWithRoutesForMode(CheckMode)(navigator, checkModeRoutes, EmptyOptionalSchemeReferenceNumber)
  }

  "UpdateMode" must {
    val updateModeRoutes: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Next page"),
        row(AddPartnersId(index))(false, anyMoreChangesPage(OptionalSchemeReferenceNumber(Some(SchemeReferenceNumber(srn.get))))),
        row(AddPartnersId(index))(true, otherPartnersPage(UpdateMode, OptionalSchemeReferenceNumber(srn)), Some(addPartnersMoreThan10)),
        row(AddPartnersId(index))(true, partnerNamePage(UpdateMode, OptionalSchemeReferenceNumber(srn), partnerIndex = 1), Some(lessThan10Partners)),
        row(PartnerNameId(Index(0), index))(somePersonNameValue, dobPage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnerDOBId(Index(0), index))(LocalDate.now, hasNinoPage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnerHasNINOId(Index(0), index))(true, ninoPage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnerHasNINOId(Index(0), index))(false, whyNoNinoPage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnerEnterNINOId(Index(0), index))(someRefValue, hasUtrPage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnerNoNINOReasonId(Index(0), index))(someStringValue, hasUtrPage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnerHasUTRId(Index(0), index))(true, utrPage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnerHasUTRId(Index(0), index))(false, whyNoUtrPage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnerEnterUTRId(Index(0), index))(someRefValue, postcodeLookupPage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnerNoUTRReasonId(Index(0), index))(someStringValue, postcodeLookupPage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnerAddressPostcodeLookupId(Index(0), index))(Seq(someTolerantAddress), addressListPage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnerAddressListId(Index(0), index))(someTolerantAddress, addressYearsPage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnerAddressId(Index(0), index))(someAddress, addressYearsPage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnerAddressYearsId(Index(0), index))(value = AddressYears.UnderAYear, paPostcodeLookupPage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnerAddressYearsId(Index(0), index))(value = AddressYears.OverAYear, emailPage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnerPreviousAddressPostcodeLookupId(Index(0), index))(Seq(someTolerantAddress), paAddressListPage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnerPreviousAddressListId(Index(0), index))(someTolerantAddress, emailPage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnerPreviousAddressId(Index(0), index))(someAddress, emailPage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnerEmailId(Index(0), index))(someStringValue, phonePage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnerPhoneId(Index(0), index))(someStringValue, cyaPage(UpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(OtherPartnersId(index))(false, anyMoreChangesPage(OptionalSchemeReferenceNumber(Some(SchemeReferenceNumber(srn.get))))),
        row(OtherPartnersId(index))(true, anyMoreChangesPage(OptionalSchemeReferenceNumber(Some(SchemeReferenceNumber(srn.get)))))
      )

    behave like navigatorWithRoutesForMode(UpdateMode)(navigator, updateModeRoutes, OptionalSchemeReferenceNumber(srn))
  }

  "CheckUpdateMode" must {
    val checkUpdateModeRoutes: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Next page"),
        row(PartnerNameId(Index(0), index))(somePersonNameValue, cyaPage(CheckUpdateMode, OptionalSchemeReferenceNumber(srn)), Some(newPartnerUserAnswers)),
        row(PartnerDOBId(Index(0), index))(LocalDate.now, cyaPage(CheckUpdateMode, OptionalSchemeReferenceNumber(srn)), Some(newPartnerUserAnswers)),
        row(PartnerHasNINOId(Index(0), index))(true, ninoPage(CheckUpdateMode, OptionalSchemeReferenceNumber(srn)), Some(newPartnerUserAnswers)),
        row(PartnerHasNINOId(Index(0), index))(false, whyNoNinoPage(CheckUpdateMode, OptionalSchemeReferenceNumber(srn)), Some(newPartnerUserAnswers)),
        row(PartnerEnterNINOId(Index(0), index))(someRefValue, cyaPage(CheckUpdateMode, OptionalSchemeReferenceNumber(srn)), Some(newPartnerUserAnswers)),
        row(PartnerEnterNINOId(Index(0), index))(someRefValue, anyMoreChangesPage(OptionalSchemeReferenceNumber(Some(SchemeReferenceNumber(srn.get))))),
        row(PartnerNoNINOReasonId(Index(0), index))(someStringValue, cyaPage(CheckUpdateMode, OptionalSchemeReferenceNumber(srn)), Some(newPartnerUserAnswers)),
        row(PartnerHasUTRId(Index(0), index))(true, utrPage(CheckUpdateMode, OptionalSchemeReferenceNumber(srn)), Some(newPartnerUserAnswers)),
        row(PartnerHasUTRId(Index(0), index))(false, whyNoUtrPage(CheckUpdateMode, OptionalSchemeReferenceNumber(srn)), Some(newPartnerUserAnswers)),
        row(PartnerEnterUTRId(Index(0), index))(someRefValue, cyaPage(CheckUpdateMode, OptionalSchemeReferenceNumber(srn)), Some(newPartnerUserAnswers)),
        row(PartnerEnterUTRId(Index(0), index))(someRefValue, anyMoreChangesPage(OptionalSchemeReferenceNumber(Some(SchemeReferenceNumber(srn.get))))),
        row(PartnerNoUTRReasonId(Index(0), index))(someStringValue, cyaPage(CheckUpdateMode, OptionalSchemeReferenceNumber(srn)), Some(newPartnerUserAnswers)),
        row(PartnerAddressId(Index(0), index))(someAddress, cyaPage(CheckUpdateMode, OptionalSchemeReferenceNumber(srn)), Some(newPartnerUserAnswers)),
        row(PartnerAddressId(Index(0), index))(someAddress, isThisPaPage(CheckUpdateMode, OptionalSchemeReferenceNumber(srn))),
        row(PartnerAddressYearsId(Index(0), index))(value = AddressYears.UnderAYear, paPostcodeLookupPage(CheckUpdateMode, OptionalSchemeReferenceNumber(srn)), Some(newPartnerUserAnswers)),
        row(PartnerAddressYearsId(Index(0), index))(value = AddressYears.OverAYear, emailPage(CheckUpdateMode, OptionalSchemeReferenceNumber(srn)), Some(newPartnerUserAnswers)),
        row(PartnerPreviousAddressId(Index(0), index))(someAddress, cyaPage(CheckUpdateMode, OptionalSchemeReferenceNumber(srn)), Some(newPartnerUserAnswers)),
        row(PartnerPreviousAddressId(Index(0), index))(someAddress, anyMoreChangesPage(OptionalSchemeReferenceNumber(Some(SchemeReferenceNumber(srn.get))))),
        row(PartnerEmailId(Index(0), index))(someStringValue, cyaPage(CheckUpdateMode, OptionalSchemeReferenceNumber(srn)), Some(newPartnerUserAnswers)),
        row(PartnerEmailId(Index(0), index))(someStringValue, anyMoreChangesPage(OptionalSchemeReferenceNumber(Some(SchemeReferenceNumber(srn.get))))),
        row(PartnerPhoneId(Index(0), index))(someStringValue, cyaPage(CheckUpdateMode, OptionalSchemeReferenceNumber(srn)), Some(newPartnerUserAnswers)),
        row(PartnerPhoneId(Index(0), index))(someStringValue, anyMoreChangesPage(OptionalSchemeReferenceNumber(Some(SchemeReferenceNumber(srn.get)))))
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
  private val newPartnerUserAnswers: UserAnswers = UserAnswers().set(IsNewPartnerId(Index(0), index))(value = true).asOpt.value

  private def isThisPaPage(mode: Mode,  srn: OptionalSchemeReferenceNumber): Call =
    PartnerConfirmPreviousAddressController.onPageLoad(Index(0), index, OptionalSchemeReferenceNumber(srn))

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








