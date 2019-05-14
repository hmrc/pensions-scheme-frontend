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
import controllers.register.trustees.partnership.routes
import identifiers.Identifier
import identifiers.register.trustees.IsTrusteeNewId
import identifiers.register.trustees.partnership._
import models.Mode.checkMode
import models._
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor6
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.UserAnswers

//scalastyle:off line.size.limit
class TrusteesPartnershipNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import TrusteesPartnershipNavigatorSpec._

  private def routes(mode: Mode): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    (PartnershipDetailsId(0), emptyAnswers, partnershipVat(mode), true, Some(exitJourney(mode, emptyAnswers)), true),
    (PartnershipDetailsId(0), newTrustee, partnershipVat(mode), true, Some(exitJourney(mode, newTrustee)), true),
    (PartnershipVatId(0), emptyAnswers, partnershipPaye(mode), true, Some(exitJourney(mode,emptyAnswers)), true),
    (PartnershipVatId(0), newTrustee, partnershipPaye(mode), true, Some(exitJourney(mode,newTrustee)), true),
    (PartnershipPayeId(0), emptyAnswers, partnershipUtr(mode), true, Some(exitJourney(mode,emptyAnswers)), true),
    (PartnershipPayeId(0), newTrustee, partnershipUtr(mode), true, Some(exitJourney(mode,newTrustee)), true),
    (PartnershipUniqueTaxReferenceId(0), emptyAnswers, partnershipPostcodeLookup(mode), true, Some(exitJourney(mode,emptyAnswers)), true),
    (PartnershipUniqueTaxReferenceId(0), newTrustee, partnershipPostcodeLookup(mode), true, Some(exitJourney(mode,newTrustee)), true),
    (PartnershipPostcodeLookupId(0), emptyAnswers, partnershipAddressList(mode), true, Some(partnershipAddressList(checkMode((mode)))), true),
    (PartnershipAddressListId(0), emptyAnswers, partnershipAddress(mode), true, Some(partnershipAddress(checkMode((mode)))), true),
    (PartnershipAddressId(0), emptyAnswers, partnershipAddressYears(mode), true,
      if(mode == UpdateMode) Some(partnershipAddressYears(checkMode(mode))) else Some(checkYourAnswers(mode)) , true),
    (PartnershipAddressId(0), newTrustee, partnershipAddressYears(mode), true, Some(checkYourAnswers(mode)) , true),
    (PartnershipAddressYearsId(0), addressYearsOverAYear, partnershipContact(mode), true, Some(exitJourney(mode,emptyAnswers)), true),
    (PartnershipAddressYearsId(0), addressYearsUnderAYear, partnershipPaPostCodeLookup(mode), true, Some(partnershipPaPostCodeLookup(checkMode((mode)))), true),
    (PartnershipAddressYearsId(0), emptyAnswers, sessionExpired, false, Some(sessionExpired), false),
    (PartnershipPreviousAddressPostcodeLookupId(0), emptyAnswers, partnershipPaList(mode), true, Some(partnershipPaList(checkMode((mode)))), true),
    (PartnershipPreviousAddressListId(0), emptyAnswers, partnershipPa(mode), true, Some(partnershipPa(checkMode((mode)))), true),
    (PartnershipPreviousAddressId(0), emptyAnswers, partnershipContact(mode), true, Some(exitJourney(mode,emptyAnswers)), true),
    (PartnershipPreviousAddressId(0), newTrustee, partnershipContact(mode), true, Some(exitJourney(mode,newTrustee)), true),
    (PartnershipContactDetailsId(0), emptyAnswers, checkYourAnswers(mode), true, Some(exitJourney(mode,emptyAnswers)), true),
    (PartnershipContactDetailsId(0), newTrustee, checkYourAnswers(mode), true, Some(exitJourney(mode,newTrustee)), true),
    (CheckYourAnswersId(0), emptyAnswers, if(mode==UpdateMode) anyMoreChanges else addTrustee(mode), false, None, true)
  )

  private val navigator: TrusteesPartnershipNavigator =
    new TrusteesPartnershipNavigator(FakeUserAnswersCacheConnector, frontendAppConfig)

  s"${navigator.getClass.getSimpleName}" must {
    appRunning()
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, routes(NormalMode), dataDescriber)
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, routes(UpdateMode), dataDescriber, UpdateMode)
    behave like nonMatchingNavigator(navigator)
  }
}

object TrusteesPartnershipNavigatorSpec extends OptionValues {

  private val newTrustee = UserAnswers(Json.obj()).set(IsTrusteeNewId(0))(true).asOpt.value

  private def taskList: Call = controllers.routes.SchemeTaskListController.onPageLoad(NormalMode, None)

  private val emptyAnswers = UserAnswers(Json.obj())

  private def partnershipVat(mode: Mode) = routes.PartnershipVatController.onPageLoad(mode, 0, None)

  private def checkYourAnswers(mode: Mode) = routes.CheckYourAnswersController.onPageLoad(mode, 0, None)

  private def partnershipPaye(mode: Mode) = routes.PartnershipPayeController.onPageLoad(mode, 0, None)

  private def partnershipUtr(mode: Mode) = routes.PartnershipUniqueTaxReferenceController.onPageLoad(mode, 0, None)

  private def partnershipPostcodeLookup(mode: Mode) = routes.PartnershipPostcodeLookupController.onPageLoad(mode, 0, None)

  private def partnershipAddressList(mode: Mode) = routes.PartnershipAddressListController.onPageLoad(mode, 0, None)

  private def partnershipAddress(mode: Mode) = routes.PartnershipAddressController.onPageLoad(mode, 0, None)

  private def partnershipAddressYears(mode: Mode) = routes.PartnershipAddressYearsController.onPageLoad(mode, 0, None)

  private def partnershipContact(mode: Mode) = routes.PartnershipContactDetailsController.onPageLoad(mode, 0, None)

  private def partnershipPaPostCodeLookup(mode: Mode) = routes.PartnershipPreviousAddressPostcodeLookupController.onPageLoad(mode, 0, None)

  private def partnershipPaList(mode: Mode) = routes.PartnershipPreviousAddressListController.onPageLoad(mode, 0, None)

  private def partnershipPa(mode: Mode) = routes.PartnershipPreviousAddressController.onPageLoad(mode, 0, None)

  private def addTrustee(mode: Mode) = controllers.register.trustees.routes.AddTrusteeController.onPageLoad(mode, None)

  private def sessionExpired = controllers.routes.SessionExpiredController.onPageLoad()

  private val addressYearsOverAYear = UserAnswers(Json.obj())
    .set(PartnershipAddressYearsId(0))(AddressYears.OverAYear).asOpt.value
  private val addressYearsUnderAYear = UserAnswers(Json.obj())
    .set(PartnershipAddressYearsId(0))(AddressYears.UnderAYear).asOpt.value

  private def dataDescriber(answers: UserAnswers): String = answers.toString

  private def anyMoreChanges = controllers.routes.AnyMoreChangesController.onPageLoad(None)

  private def exitJourney(mode: Mode, answers:UserAnswers, index:Int = 0) = if(mode == CheckMode || mode == NormalMode) checkYourAnswers(mode)
  else {
    if(answers.get(IsTrusteeNewId(index)).getOrElse(false)) checkYourAnswers(mode)
    else anyMoreChanges
  }
}


