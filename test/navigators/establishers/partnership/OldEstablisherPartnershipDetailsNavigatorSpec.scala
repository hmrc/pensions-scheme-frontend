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

package navigators.establishers.partnership

import base.SpecBase
import controllers.register.establishers.partnership.routes._
import controllers.register.establishers.routes.AddEstablisherController
import identifiers.Identifier
import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.establishers.partnership._
import models.FeatureToggleName.SchemeRegistration
import models._
import navigators.{Navigator, NavigatorBehaviour}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.prop._
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import play.api.mvc.Call
import services.FeatureToggleService
import utils.UserAnswers

import scala.concurrent.Future

class OldEstablisherPartnershipDetailsNavigatorSpec extends SpecBase with NavigatorBehaviour with BeforeAndAfterEach with MockitoSugar {

  import OldEstablisherPartnershipDetailsNavigatorSpec._

  private val mockFeatureToggleService = mock[FeatureToggleService]


  override protected def beforeEach(): Unit = {
    reset(mockFeatureToggleService)
    when(mockFeatureToggleService.get(any())(any(), any()))
      .thenReturn(Future.successful(FeatureToggle(SchemeRegistration, false)))
  }

  val navigator: Navigator =
    applicationBuilder(dataRetrievalAction = UserAnswers().dataRetrievalAction).build().injector
      .instanceOf[OldEstablisherPartnershipDetailsNavigator]

  "OldEstablishersPartnershipDetailsNavigator" when {
    "in NormalMode" must {
      def navigationForEstablisherPartnership: TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          row(PartnershipDetailsId(index))(partnershipDetails, addEstablisherPage(NormalMode, None)),
          row(PartnershipHasUTRId(index))(value = true, PartnershipEnterUTRController.onPageLoad(NormalMode, index, None)),
          row(PartnershipHasUTRId(index))(value = false, PartnershipNoUTRReasonController.onPageLoad(NormalMode, index, None)),
          row(PartnershipNoUTRReasonId(index))(someStringValue, hasVatPage(NormalMode, index, None)),
          row(PartnershipEnterUTRId(index))(someRefValue, hasVatPage(NormalMode, index, None)),
          row(PartnershipHasVATId(index))(value = true, PartnershipEnterVATController.onPageLoad(NormalMode, index, None)),
          row(PartnershipHasVATId(index))(value = false, PartnershipHasPAYEController.onPageLoad(NormalMode, index, None)),
          row(PartnershipEnterVATId(index))(someRefValue, PartnershipHasPAYEController.onPageLoad(NormalMode, index, None)),
          row(PartnershipHasPAYEId(index))(value = true, PartnershipEnterPAYEController.onPageLoad(NormalMode, index, None)),
          row(PartnershipHasPAYEId(index))(value = false, cyaPartnershipDetailsPage(NormalMode, index, None)),
          row(PartnershipEnterPAYEId(index))(someRefValue, cyaPartnershipDetailsPage(NormalMode, index, None))
        )

      behave like navigatorWithRoutesForMode(NormalMode)(navigator, navigationForEstablisherPartnership, None)
    }

    "CheckMode" must {
      val navigationForCheckModeEstablisherPartnership: TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Expected next page"),
          row(PartnershipHasUTRId(index))(value = true, PartnershipEnterUTRController.onPageLoad(CheckMode, index, None)),
          row(PartnershipHasUTRId(index))(value = false, PartnershipNoUTRReasonController.onPageLoad(CheckMode, index, None)),
          row(PartnershipNoUTRReasonId(index))(someStringValue, cyaPartnershipDetailsPage(CheckMode, index, None)),
          row(PartnershipEnterUTRId(index))(someRefValue, cyaPartnershipDetailsPage(CheckMode, index, None)),
          row(PartnershipHasVATId(index))(value = true, PartnershipEnterVATController.onPageLoad(CheckMode, index, None)),
          row(PartnershipHasVATId(index))(value = false, cyaPartnershipDetailsPage(CheckMode, index, None)),
          row(PartnershipEnterVATId(index))(someRefValue, cyaPartnershipDetailsPage(CheckMode, index, None)),
          row(PartnershipHasPAYEId(index))(value = true, PartnershipEnterPAYEController.onPageLoad(CheckMode, index, None)),
          row(PartnershipHasPAYEId(index))(value = false, cyaPartnershipDetailsPage(CheckMode, index, None)),
          row(PartnershipEnterPAYEId(index))(someRefValue, cyaPartnershipDetailsPage(CheckMode, index, None))
        )

      behave like navigatorWithRoutesForMode(CheckMode)(navigator, navigationForCheckModeEstablisherPartnership, None)
    }

    "in UpdateMode" must {
      def navigationForUpdateModeEstablisherPartnership: TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Expected next page"),
          row(PartnershipDetailsId(index))(partnershipDetails, addEstablisherPage(UpdateMode, srn), Some(newEstablisherUserAnswers)),
          row(PartnershipHasUTRId(index))(value = true, PartnershipEnterUTRController.onPageLoad(UpdateMode, index, srn), Some(newEstablisherUserAnswers)),
          row(PartnershipHasUTRId(index))(value = false, PartnershipNoUTRReasonController.onPageLoad(UpdateMode, index, srn), Some(newEstablisherUserAnswers)),
          row(PartnershipNoUTRReasonId(index))(someStringValue, hasVatPage(UpdateMode, index, srn), Some(newEstablisherUserAnswers)),
          row(PartnershipEnterUTRId(index))(someRefValue, hasVatPage(UpdateMode, index, srn), Some(newEstablisherUserAnswers)),
          row(PartnershipHasVATId(index))(value = true, PartnershipEnterVATController.onPageLoad(UpdateMode, index, srn)),
          row(PartnershipHasVATId(index))(value = false, PartnershipHasPAYEController.onPageLoad(UpdateMode, index, srn)),
          row(PartnershipEnterVATId(index))(someRefValue, PartnershipHasPAYEController.onPageLoad(UpdateMode, index, srn)),
          row(PartnershipHasPAYEId(index))(value = true, PartnershipEnterPAYEController.onPageLoad(UpdateMode, index, srn)),
          row(PartnershipHasPAYEId(index))(value = false, cyaPartnershipDetailsPage(UpdateMode, index, srn)),
          row(PartnershipEnterPAYEId(index))(someRefValue, cyaPartnershipDetailsPage(UpdateMode, index, srn))
        )

      behave like navigatorWithRoutesForMode(UpdateMode)(navigator, navigationForUpdateModeEstablisherPartnership, srn)
    }

    "CheckUpdateMode" must {
      val navigationForCheckUpdateEstablisherPartnership: TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Expected next page"),
          row(PartnershipHasUTRId(index))(value = true, PartnershipEnterUTRController.onPageLoad(CheckUpdateMode, index, srn), Some(newEstablisherUserAnswers)),
          row(PartnershipHasUTRId(index))(value = false, PartnershipNoUTRReasonController.onPageLoad(CheckUpdateMode, index, srn), Some(newEstablisherUserAnswers)),
          row(PartnershipEnterUTRId(index))(someRefValue, cyaPartnershipDetailsPage(CheckUpdateMode, index, srn), Some(newEstablisherUserAnswers)),
          row(PartnershipEnterUTRId(index))(someRefValue, anyMoreChangesPage(srn)),
          row(PartnershipNoUTRReasonId(index))(someStringValue, cyaPartnershipDetailsPage(CheckUpdateMode, index, srn), Some(newEstablisherUserAnswers)),
          row(PartnershipHasVATId(index))(value = true, PartnershipEnterVATController.onPageLoad(CheckUpdateMode, index, srn)),
          row(PartnershipHasVATId(index))(value = false, cyaPartnershipDetailsPage(CheckUpdateMode, index, srn)),
          row(PartnershipEnterVATId(index))(someRefValue, cyaPartnershipDetailsPage(CheckUpdateMode, index, srn), Some(newEstablisherUserAnswers)),
          row(PartnershipEnterVATId(index))(someRefValue, anyMoreChangesPage(srn)),
          row(PartnershipHasPAYEId(index))(value = true, PartnershipEnterPAYEController.onPageLoad(CheckUpdateMode, index, srn)),
          row(PartnershipHasPAYEId(index))(value = false, cyaPartnershipDetailsPage(CheckUpdateMode, index, srn)),
          row(PartnershipEnterPAYEId(index))(someRefValue, anyMoreChangesPage(srn)),
          row(PartnershipEnterPAYEId(index))(someRefValue, cyaPartnershipDetailsPage(CheckUpdateMode, index, srn), Some(newEstablisherUserAnswers))
        )

      behave like navigatorWithRoutesForMode(CheckUpdateMode)(navigator, navigationForCheckUpdateEstablisherPartnership, srn)
    }
  }

}

object OldEstablisherPartnershipDetailsNavigatorSpec extends OptionValues {
  private lazy val index = 0
  private val srn = Some("srn")
  private val newEstablisherUserAnswers = UserAnswers().set(IsEstablisherNewId(index))(value = true).asOpt.value
  private val partnershipDetails = PartnershipDetails("test partnership")

  private def addEstablisherPage(mode: Mode, srn: Option[String]): Call =
    AddEstablisherController.onPageLoad(Mode.journeyMode(mode), srn)

  private def hasVatPage(mode: Mode, index: Index, srn: Option[String]): Call =
    PartnershipHasVATController.onPageLoad(Mode.journeyMode(mode), index, srn)

  private def cyaPartnershipDetailsPage(mode: Mode, index: Index, srn: Option[String]): Call =
    CheckYourAnswersPartnershipDetailsController.onPageLoad(Mode.journeyMode(mode), index, srn)
}



