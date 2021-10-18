/*
 * Copyright 2021 HM Revenue & Customs
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
        row(AddPartnersId(index))(false, taskListPage(NormalMode, None)),
        row(AddPartnersId(index))(true, otherPartnersPage(NormalMode, None), Some(addPartnersMoreThan10)),
        row(AddPartnersId(index))(true, partnerNamePage(NormalMode, None, partnerIndex = 1), Some(lessThan10Partners)),
        row(PartnerNameId(index, index))(somePersonNameValue, dobPage(NormalMode, None)),
        row(PartnerDOBId(index, index))(LocalDate.now, hasNinoPage(NormalMode, None)),
        row(PartnerHasNINOId(index, index))(true, ninoPage(NormalMode, None)),
        row(PartnerHasNINOId(index, index))(false, whyNoNinoPage(NormalMode, None)),
        row(PartnerEnterNINOId(index, index))(someRefValue, hasUtrPage(NormalMode, None)),
        row(PartnerNoNINOReasonId(index, index))(someStringValue, hasUtrPage(NormalMode, None)),
        row(PartnerHasUTRId(index, index))(true, utrPage(NormalMode, None)),
        row(PartnerHasUTRId(index, index))(false, whyNoUtrPage(NormalMode, None)),
        row(PartnerEnterUTRId(index, index))(someRefValue, postcodeLookupPage(NormalMode, None)),
        row(PartnerNoUTRReasonId(index, index))(someStringValue, postcodeLookupPage(NormalMode, None)),
        row(PartnerAddressPostcodeLookupId(index, index))(Seq(someTolerantAddress), addressListPage(NormalMode, None)),
        row(PartnerAddressListId(index, index))(someTolerantAddress, addressYearsPage(NormalMode, None)),
        row(PartnerAddressId(index, index))(someAddress, addressYearsPage(NormalMode, None)),
        row(PartnerAddressYearsId(index, index))(value = AddressYears.UnderAYear, paPostcodeLookupPage(NormalMode, None)),
        row(PartnerAddressYearsId(index, index))(value = AddressYears.OverAYear, emailPage(NormalMode, None)),
        row(PartnerPreviousAddressPostcodeLookupId(index, index))(Seq(someTolerantAddress), paAddressListPage(NormalMode, None)),
        row(PartnerPreviousAddressListId(index, index))(someTolerantAddress, emailPage(NormalMode, None)),
        row(PartnerPreviousAddressId(index, index))(someAddress, emailPage(NormalMode, None)),
        row(PartnerEmailId(index, index))(someStringValue, phonePage(NormalMode, None)),
        row(PartnerPhoneId(index, index))(someStringValue, cyaPage(NormalMode, None)),
        row(OtherPartnersId(index))(false, taskListPage(NormalMode, None)),
        row(OtherPartnersId(index))(true, taskListPage(NormalMode, None))
      )

    behave like navigatorWithRoutesForMode(NormalMode)(navigator, normalModeRoutes, None)
  }

  "CheckMode" must {
    val checkModeRoutes: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Next Page"),
        row(PartnerNameId(index, index))(somePersonNameValue, cyaPage(CheckMode, None)),
        row(PartnerDOBId(index, index))(LocalDate.now, cyaPage(CheckMode, None)),
        row(PartnerHasNINOId(index, index))(true, ninoPage(CheckMode, None)),
        row(PartnerHasNINOId(index, index))(false, whyNoNinoPage(CheckMode, None)),
        row(PartnerEnterNINOId(index, index))(someRefValue, cyaPage(CheckMode, None)),
        row(PartnerNoNINOReasonId(index, index))(someStringValue, cyaPage(CheckMode, None)),
        row(PartnerHasUTRId(index, index))(true, utrPage(CheckMode, None)),
        row(PartnerHasUTRId(index, index))(false, whyNoUtrPage(CheckMode, None)),
        row(PartnerEnterUTRId(index, index))(someRefValue, cyaPage(CheckMode, None)),
        row(PartnerNoUTRReasonId(index, index))(someStringValue, cyaPage(CheckMode, None)),
        row(PartnerAddressId(index, index))(someAddress, cyaPage(CheckMode, None)),
        row(PartnerAddressYearsId(index, index))(value = AddressYears.UnderAYear, paPostcodeLookupPage(CheckMode, None)),
        row(PartnerAddressYearsId(index, index))(value = AddressYears.OverAYear, cyaPage(CheckMode, None)),
        row(PartnerPreviousAddressId(index, index))(someAddress, cyaPage(CheckMode, None)),
        row(PartnerEmailId(index, index))(someStringValue, cyaPage(CheckMode, None)),
        row(PartnerPhoneId(index, index))(someStringValue, cyaPage(CheckMode, None))
      )

    behave like navigatorWithRoutesForMode(CheckMode)(navigator, checkModeRoutes, None)
  }

  "UpdateMode" must {
    val updateModeRoutes: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Next page"),
        row(AddPartnersId(index))(false, anyMoreChangesPage(srn)),
        row(AddPartnersId(index))(true, otherPartnersPage(UpdateMode, srn), Some(addPartnersMoreThan10)),
        row(AddPartnersId(index))(true, partnerNamePage(UpdateMode, srn, partnerIndex = 1), Some(lessThan10Partners)),
        row(PartnerNameId(index, index))(somePersonNameValue, dobPage(UpdateMode, srn)),
        row(PartnerDOBId(index, index))(LocalDate.now, hasNinoPage(UpdateMode, srn)),
        row(PartnerHasNINOId(index, index))(true, ninoPage(UpdateMode, srn)),
        row(PartnerHasNINOId(index, index))(false, whyNoNinoPage(UpdateMode, srn)),
        row(PartnerEnterNINOId(index, index))(someRefValue, hasUtrPage(UpdateMode, srn)),
        row(PartnerNoNINOReasonId(index, index))(someStringValue, hasUtrPage(UpdateMode, srn)),
        row(PartnerHasUTRId(index, index))(true, utrPage(UpdateMode, srn)),
        row(PartnerHasUTRId(index, index))(false, whyNoUtrPage(UpdateMode, srn)),
        row(PartnerEnterUTRId(index, index))(someRefValue, postcodeLookupPage(UpdateMode, srn)),
        row(PartnerNoUTRReasonId(index, index))(someStringValue, postcodeLookupPage(UpdateMode, srn)),
        row(PartnerAddressPostcodeLookupId(index, index))(Seq(someTolerantAddress), addressListPage(UpdateMode, srn)),
        row(PartnerAddressListId(index, index))(someTolerantAddress, addressYearsPage(UpdateMode, srn)),
        row(PartnerAddressId(index, index))(someAddress, addressYearsPage(UpdateMode, srn)),
        row(PartnerAddressYearsId(index, index))(value = AddressYears.UnderAYear, paPostcodeLookupPage(UpdateMode, srn)),
        row(PartnerAddressYearsId(index, index))(value = AddressYears.OverAYear, emailPage(UpdateMode, srn)),
        row(PartnerPreviousAddressPostcodeLookupId(index, index))(Seq(someTolerantAddress), paAddressListPage(UpdateMode, srn)),
        row(PartnerPreviousAddressListId(index, index))(someTolerantAddress, emailPage(UpdateMode, srn)),
        row(PartnerPreviousAddressId(index, index))(someAddress, emailPage(UpdateMode, srn)),
        row(PartnerEmailId(index, index))(someStringValue, phonePage(UpdateMode, srn)),
        row(PartnerPhoneId(index, index))(someStringValue, cyaPage(UpdateMode, srn)),
        row(OtherPartnersId(index))(false, anyMoreChangesPage(srn)),
        row(OtherPartnersId(index))(true, anyMoreChangesPage(srn))
      )

    behave like navigatorWithRoutesForMode(UpdateMode)(navigator, updateModeRoutes, srn)
  }

  "CheckUpdateMode" must {
    val checkUpdateModeRoutes: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Next page"),
        row(PartnerNameId(index, index))(somePersonNameValue, cyaPage(CheckUpdateMode, srn), Some(newPartnerUserAnswers)),
        row(PartnerDOBId(index, index))(LocalDate.now, cyaPage(CheckUpdateMode, srn), Some(newPartnerUserAnswers)),
        row(PartnerHasNINOId(index, index))(true, ninoPage(CheckUpdateMode, srn), Some(newPartnerUserAnswers)),
        row(PartnerHasNINOId(index, index))(false, whyNoNinoPage(CheckUpdateMode, srn), Some(newPartnerUserAnswers)),
        row(PartnerEnterNINOId(index, index))(someRefValue, cyaPage(CheckUpdateMode, srn), Some(newPartnerUserAnswers)),
        row(PartnerEnterNINOId(index, index))(someRefValue, anyMoreChangesPage(srn)),
        row(PartnerNoNINOReasonId(index, index))(someStringValue, cyaPage(CheckUpdateMode, srn), Some(newPartnerUserAnswers)),
        row(PartnerHasUTRId(index, index))(true, utrPage(CheckUpdateMode, srn), Some(newPartnerUserAnswers)),
        row(PartnerHasUTRId(index, index))(false, whyNoUtrPage(CheckUpdateMode, srn), Some(newPartnerUserAnswers)),
        row(PartnerEnterUTRId(index, index))(someRefValue, cyaPage(CheckUpdateMode, srn), Some(newPartnerUserAnswers)),
        row(PartnerEnterUTRId(index, index))(someRefValue, anyMoreChangesPage(srn)),
        row(PartnerNoUTRReasonId(index, index))(someStringValue, cyaPage(CheckUpdateMode, srn), Some(newPartnerUserAnswers)),
        row(PartnerAddressId(index, index))(someAddress, cyaPage(CheckUpdateMode, srn), Some(newPartnerUserAnswers)),
        row(PartnerAddressId(index, index))(someAddress, isThisPaPage(CheckUpdateMode, srn)),
        row(PartnerAddressYearsId(index, index))(value = AddressYears.UnderAYear, paPostcodeLookupPage(CheckUpdateMode, srn), Some(newPartnerUserAnswers)),
        row(PartnerAddressYearsId(index, index))(value = AddressYears.OverAYear, emailPage(CheckUpdateMode, srn), Some(newPartnerUserAnswers)),
        row(PartnerPreviousAddressId(index, index))(someAddress, cyaPage(CheckUpdateMode, srn), Some(newPartnerUserAnswers)),
        row(PartnerPreviousAddressId(index, index))(someAddress, anyMoreChangesPage(srn)),
        row(PartnerEmailId(index, index))(someStringValue, cyaPage(CheckUpdateMode, srn), Some(newPartnerUserAnswers)),
        row(PartnerEmailId(index, index))(someStringValue, anyMoreChangesPage(srn)),
        row(PartnerPhoneId(index, index))(someStringValue, cyaPage(CheckUpdateMode, srn), Some(newPartnerUserAnswers)),
        row(PartnerPhoneId(index, index))(someStringValue, anyMoreChangesPage(srn))
      )

    behave like navigatorWithRoutesForMode(CheckUpdateMode)(navigator, checkUpdateModeRoutes, srn)
  }
}


object PartnerNavigatorSpec extends SpecBase with Matchers with NavigatorBehaviour with Generators {
  private lazy val index = 0
  private val srn = Some("srn")
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

  private def isThisPaPage(mode: Mode,  srn: Option[String]): Call =
    PartnerConfirmPreviousAddressController.onPageLoad(index, index, srn)

  private def otherPartnersPage(mode: Mode,  srn: Option[String]): Call =
    controllers.register.establishers.partnership.routes.OtherPartnersController.onPageLoad(mode, index, srn)

  private def taskListPage(mode: Mode,  srn: Option[String]): Call =
    controllers.routes.PsaSchemeTaskListController.onPageLoad(mode, srn)

  private def partnerNamePage(mode: Mode, srn: Option[String], partnerIndex: Int): Call =
    controllers.register.establishers.partnership.partner.routes.PartnerNameController.onPageLoad(mode, index, partnerIndex, srn)

  private def dobPage(mode: Mode, srn: Option[String]): Call =
    PartnerDOBController.onPageLoad(mode, index, index, srn)

  private def hasNinoPage(mode: Mode,  srn: Option[String]): Call =
    PartnerHasNINOController.onPageLoad(mode, index, index, srn)

  private def ninoPage(mode: Mode,  srn: Option[String]): Call =
    PartnerEnterNINOController.onPageLoad(mode, index, index, srn)

  private def whyNoNinoPage(mode: Mode,  srn: Option[String]): Call =
    PartnerNoNINOReasonController.onPageLoad(mode, index, index, srn)

  private def hasUtrPage(mode: Mode,  srn: Option[String]): Call =
    PartnerHasUTRController.onPageLoad(mode, index, index, srn)

  private def utrPage(mode: Mode,  srn: Option[String]): Call =
    PartnerEnterUTRController.onPageLoad(mode, index, index, srn)

  private def whyNoUtrPage(mode: Mode,  srn: Option[String]): Call =
    PartnerNoUTRReasonController.onPageLoad(mode, index, index, srn)

  private def cyaPage(mode: Mode,  srn: Option[String]): Call =
    CheckYourAnswersController.onPageLoad(journeyMode(mode), index, index, srn)

  private def postcodeLookupPage(mode: Mode,  srn: Option[String]): Call =
    PartnerAddressPostcodeLookupController.onPageLoad(mode, index, index, srn)

  private def addressListPage(mode: Mode,  srn: Option[String]): Call =
    PartnerAddressListController.onPageLoad(mode, index, index, srn)

  private def paPostcodeLookupPage(mode: Mode,  srn: Option[String]): Call =
    PartnerPreviousAddressPostcodeLookupController.onPageLoad(mode, index, index, srn)

  private def paAddressListPage(mode: Mode,  srn: Option[String]): Call =
    PartnerPreviousAddressListController.onPageLoad(mode, index, index, srn)

  private def addressYearsPage(mode: Mode,  srn: Option[String]): Call =
    PartnerAddressYearsController.onPageLoad(mode, index, index, srn)

  private def emailPage(mode: Mode,  srn: Option[String]): Call =
    PartnerEmailController.onPageLoad(mode, index, index, srn)

  private def phonePage(mode: Mode,  srn: Option[String]): Call =
    PartnerPhoneController.onPageLoad(mode, index, index, srn)
}








