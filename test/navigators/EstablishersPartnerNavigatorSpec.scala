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
import controllers.register.establishers.partnership.partner._
import identifiers.Identifier
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.partnership.partner._
import identifiers.register.establishers.partnership.{AddPartnersId, PartnershipDetailsId}
import models._
import models.person.PersonDetails
import org.joda.time.LocalDate
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor6
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.UserAnswers

class EstablishersPartnerNavigatorSpec extends SpecBase with NavigatorBehaviour {
  //scalastyle:off line.size.limit
  //scalastyle:off magic.number
  import EstablishersPartnerNavigatorSpec._

  private val navigator = new EstablishersPartnerNavigator(FakeUserAnswersCacheConnector, frontendAppConfig)

  private def routes: TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    (AddPartnersId(0), emptyAnswers, partnerDetails(0, NormalMode), true, Some(partnerDetails(0, CheckMode)), true),
    (AddPartnersId(0), addPartnersTrue, partnerDetails(1, NormalMode), true, Some(partnerDetails(1, CheckMode)), true),
    (AddPartnersId(0), addPartnersFalse, partnershipReview, true, Some(partnershipReview), true),
    (AddPartnersId(0), addOnePartner, sessionExpired, false, Some(sessionExpired), false),
    (AddPartnersId(0), addPartnersMoreThan10, otherPartners(NormalMode), true, Some(otherPartners(CheckMode)), true),
    (PartnerDetailsId(0, 0), emptyAnswers, partnerNino(NormalMode), true, Some(checkYourAnswers), true),
    (PartnerNinoId(0, 0), emptyAnswers, partnerUtr(NormalMode), true, Some(checkYourAnswers), true),
    (PartnerUniqueTaxReferenceId(0, 0), emptyAnswers, partnerAddressPostcode(NormalMode), true, Some(checkYourAnswers), true),
    (PartnerAddressPostcodeLookupId(0, 0), emptyAnswers, partnerAddressList(NormalMode), true, Some(partnerAddressList(CheckMode)), true),
    (PartnerAddressListId(0, 0), emptyAnswers, partnerAddress(NormalMode), true, Some(partnerAddress(CheckMode)), true),
    (PartnerAddressId(0, 0), emptyAnswers, partnerAddressYears(NormalMode), true, Some(checkYourAnswers), true),
    (PartnerAddressYearsId(0, 0), addressYearsUnderAYear, partnerPreviousAddPostcode(NormalMode), true, Some(partnerPreviousAddPostcode(CheckMode)), true),
    (PartnerAddressYearsId(0, 0), addressYearsOverAYear, partnerContactDetails(NormalMode), true, Some(checkYourAnswers), true),
    (PartnerAddressYearsId(0, 0), emptyAnswers, sessionExpired, false, Some(sessionExpired), false),
    (PartnerPreviousAddressPostcodeLookupId(0, 0), emptyAnswers, partnerPreviousAddList(NormalMode), true, Some(partnerPreviousAddList(CheckMode)), true),
    (PartnerPreviousAddressListId(0, 0), emptyAnswers, partnerPreviousAddress(NormalMode), true, Some(partnerPreviousAddress(CheckMode)), true),
    (PartnerPreviousAddressId(0, 0), emptyAnswers, partnerContactDetails(NormalMode), true, Some(checkYourAnswers), true),
    (PartnerContactDetailsId(0, 0), emptyAnswers, checkYourAnswers, true, Some(checkYourAnswers), true),
    (ConfirmDeletePartnerId(0), emptyAnswers, addPartners, false, None, false),
    (CheckYourAnswersId(0, 0), emptyAnswers, addPartners, true, None, true)
  )

  navigator.getClass.getSimpleName must {
    appRunning()
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, routes, dataDescriber)
    behave like nonMatchingNavigator(navigator)
  }

}

object EstablishersPartnerNavigatorSpec extends OptionValues {
  private def dataDescriber(answers: UserAnswers): String = answers.toString

  private val emptyAnswers = UserAnswers(Json.obj())
  val establisherIndex = Index(0)
  val partnerIndex = Index(0)
  private val johnDoe = PersonDetails("John", None, "Doe", LocalDate.now())

  val addressYearsOverAYear = UserAnswers(Json.obj())
    .set(PartnerAddressYearsId(establisherIndex, partnerIndex))(AddressYears.OverAYear).asOpt.value
  val addressYearsUnderAYear = UserAnswers(Json.obj())
    .set(PartnerAddressYearsId(establisherIndex, partnerIndex))(AddressYears.UnderAYear).asOpt.value

  private def validData(partners: PersonDetails*) = {
    Json.obj(
      EstablishersId.toString -> Json.arr(
        Json.obj(
          PartnershipDetailsId.toString -> PartnershipDetails("test partnership name", false),
          "partner" -> partners.map(d => Json.obj(PartnerDetailsId.toString -> Json.toJson(d)))
        )
      )
    )
  }

  private val addPartnersTrue = UserAnswers(validData(johnDoe)).set(AddPartnersId(0))(true).asOpt.value
  private val addPartnersFalse = UserAnswers(validData(johnDoe)).set(AddPartnersId(0))(false).asOpt.value
  private val addPartnersMoreThan10 = UserAnswers(validData(Seq.fill(10)(johnDoe): _*))
  private val addOnePartner = UserAnswers(validData(johnDoe))

  private def partnerNino(mode: Mode) = routes.PartnerNinoController.onPageLoad(mode, establisherIndex, partnerIndex, None)

  private def partnerDetails(partnerIndex: Index = Index(0), mode: Mode) = routes.PartnerDetailsController.onPageLoad(mode, 0, partnerIndex, None)

  private def partnerUtr(mode: Mode) = routes.PartnerUniqueTaxReferenceController.onPageLoad(mode, partnerIndex, establisherIndex, None)

  private def partnerContactDetails(mode: Mode) = routes.PartnerContactDetailsController.onPageLoad(mode, partnerIndex, establisherIndex, None)

  private def partnerAddressPostcode(mode: Mode) = routes.PartnerAddressPostcodeLookupController.onPageLoad(mode, partnerIndex, establisherIndex, None)

  private def partnerAddressList(mode: Mode) = routes.PartnerAddressListController.onPageLoad(mode, partnerIndex, establisherIndex, None)

  private def partnerAddress(mode: Mode) = routes.PartnerAddressController.onPageLoad(mode, partnerIndex, establisherIndex, None)

  private def partnerAddressYears(mode: Mode) = routes.PartnerAddressYearsController.onPageLoad(mode, partnerIndex, establisherIndex, None)

  private def partnerPreviousAddPostcode(mode: Mode) = routes.PartnerPreviousAddressPostcodeLookupController.onPageLoad(mode, partnerIndex, establisherIndex, None)

  private def partnerPreviousAddList(mode: Mode) = routes.PartnerPreviousAddressListController.onPageLoad(mode, partnerIndex, establisherIndex, None)

  private def partnerPreviousAddress(mode: Mode) = routes.PartnerPreviousAddressController.onPageLoad(mode, partnerIndex, establisherIndex, None)

  private def checkYourAnswers = routes.CheckYourAnswersController.onPageLoad(NormalMode, establisherIndex, partnerIndex, None)

  private def sessionExpired = controllers.routes.SessionExpiredController.onPageLoad()

  private def addPartners = controllers.register.establishers.partnership.routes.AddPartnersController.onPageLoad(NormalMode, establisherIndex, None)

  private def partnershipReview = controllers.register.establishers.partnership.routes.PartnershipReviewController.onPageLoad(NormalMode, establisherIndex, None)

  private def otherPartners(mode: Mode) = controllers.register.establishers.partnership.routes.OtherPartnersController.onPageLoad(mode, 0, None)
}
