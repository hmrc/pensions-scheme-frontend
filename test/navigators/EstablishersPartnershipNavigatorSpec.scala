/*
 * Copyright 2018 HM Revenue & Customs
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
import connectors.FakeDataCacheConnector
import controllers.register.establishers.partnership.routes
import identifiers.Identifier
import identifiers.register.establishers.partnership._
import models.{AddressYears, CheckMode, Mode, NormalMode}
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor6
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.UserAnswers
//scalastyle:off line.size.limit
class EstablishersPartnershipNavigatorSpec extends SpecBase with NavigatorBehaviour {
  import EstablishersPartnershipNavigatorSpec._

  private def routes: TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id",                                     "User Answers",             "Next Page (Normal Mode)",                  "Save (NM)",   "Next Page (Check Mode)",      "Save (CM)"),
    (PartnershipDetailsId(0),                    emptyAnswers,              partnershipVat(NormalMode),                  true,         Some(checkYourAnswers),         true),
    (PartnershipVatId(0),                        emptyAnswers,              partnershipPaye(NormalMode),                 true,         Some(checkYourAnswers),         true),
    (PartnershipPayeId(0),                       emptyAnswers,              partnershipUtr(NormalMode),                  true,         Some(checkYourAnswers),         true),
    (PartnershipUniqueTaxReferenceID(0),         emptyAnswers,              partnershipPostcodeLookup(NormalMode),       true,         Some(checkYourAnswers),         true),
    (PartnershipPostcodeLookupId(0),             emptyAnswers,              partnershipAddressList(NormalMode),          true,         Some(partnershipAddressList(CheckMode)),true),
    (PartnershipAddressListId(0),                emptyAnswers,              partnershipAddress(NormalMode),              true,         Some(partnershipAddress(CheckMode)),true),
    (PartnershipAddressId(0),                    emptyAnswers,              partnershipAddressYears(NormalMode),         true,         Some(checkYourAnswers),         true),
    (PartnershipAddressYearsId(0),               addressYearsOverAYear,     partnershipContact(NormalMode),              true,         Some(checkYourAnswers),         true),
    (PartnershipAddressYearsId(0),               addressYearsUnderAYear,    partnershipPaPostCodeLookup(NormalMode),     true,         Some(partnershipPaPostCodeLookup(CheckMode)),true),
    (PartnershipAddressYearsId(0),               emptyAnswers,              sessionExpired,                              false,        Some(sessionExpired),           false),
    (PartnershipPreviousAddressPostcodeLookupId(0),emptyAnswers,            partnershipPaList(NormalMode),               true,         Some(partnershipPaList(CheckMode)),true),
    (PartnershipPreviousAddressListId(0),        emptyAnswers,              partnershipPa(NormalMode),                   true,         Some(partnershipPa(CheckMode)), true),
    (PartnershipPreviousAddressId(0),            emptyAnswers,              partnershipContact(NormalMode),              true,         Some(checkYourAnswers),         true),
    (PartnershipContactDetailsId(0),             emptyAnswers,              checkYourAnswers,                            true,         Some(checkYourAnswers),         true),
    (OtherPartnersId(0),                         emptyAnswers,              partnershipReview,                           true,         Some(partnershipReview),         true),
    (PartnershipReviewId(0),                     emptyAnswers,              addEstablisher,                              true,         None,                            true)
  )

  "EstablishersCompanyNavigator when restrict-establisher toggle is off" must {
    appRunning()
    val navigator = new EstablishersPartnershipNavigator(FakeDataCacheConnector, frontendAppConfig)
    behave like navigatorWithRoutes(navigator, FakeDataCacheConnector, routes, dataDescriber)
    behave like nonMatchingNavigator(navigator)
  }

}

object EstablishersPartnershipNavigatorSpec extends OptionValues {
  private val emptyAnswers = UserAnswers(Json.obj())
  private def partnershipVat(mode: Mode) = routes.PartnershipVatController.onPageLoad(mode, 0)
  private def checkYourAnswers = routes.CheckYourAnswersController.onPageLoad(0)
  private def partnershipPaye(mode: Mode) = routes.PartnershipPayeController.onPageLoad(mode, 0)
  private def partnershipUtr(mode: Mode) = routes.PartnershipUniqueTaxReferenceController.onPageLoad(mode, 0)
  private def partnershipPostcodeLookup(mode: Mode) = routes.PartnershipPostcodeLookupController.onPageLoad(mode, 0)
  private def partnershipAddressList(mode: Mode) = routes.PartnershipAddressListController.onPageLoad(mode, 0)
  private def partnershipAddress(mode: Mode) = routes.PartnershipAddressController.onPageLoad(mode, 0)
  private def partnershipAddressYears(mode: Mode) = routes.PartnershipAddressYearsController.onPageLoad(mode, 0)
  private def partnershipContact(mode: Mode) = routes.PartnershipContactDetailsController.onPageLoad(mode, 0)
  private def partnershipPaPostCodeLookup(mode: Mode) = routes.PartnershipPreviousAddressPostcodeLookupController.onPageLoad(mode, 0)
  private def partnershipPaList(mode: Mode) = routes.PartnershipPreviousAddressListController.onPageLoad(mode, 0)
  private def partnershipPa(mode: Mode) = routes.PartnershipPreviousAddressController.onPageLoad(mode, 0)
  private def partnershipReview = routes.PartnershipReviewController.onPageLoad(0)
  private def addPartners = routes.AddPartnersController.onPageLoad(0)
  private def addEstablisher = controllers.register.establishers.routes.AddEstablisherController.onPageLoad(NormalMode)
  private def sessionExpired = controllers.routes.SessionExpiredController.onPageLoad()

  private val addressYearsOverAYear = UserAnswers(Json.obj())
    .set(PartnershipAddressYearsId(0))(AddressYears.OverAYear).asOpt.value
  private val addressYearsUnderAYear = UserAnswers(Json.obj())
    .set(PartnershipAddressYearsId(0))(AddressYears.UnderAYear).asOpt.value

  private def dataDescriber(answers: UserAnswers): String = answers.toString
}
