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

package navigators.establishers.individual

import base.SpecBase
import controllers.register.establishers.individual.routes._
import controllers.register.establishers.routes._
import generators.Generators
import identifiers.Identifier
import identifiers.register.establishers.individual._
import models.FeatureToggleName.SchemeRegistration
import models.Mode._
import models._
import navigators.{Navigator, NavigatorBehaviour}
import identifiers.register.establishers.IsEstablisherNewId
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatest.prop._
import play.api.mvc.Call
import services.FeatureToggleService
import utils.UserAnswers
import java.time.LocalDate

import scala.concurrent.Future

class OldEstablishersIndividualDetailsNavigatorSpec extends SpecBase
  with Matchers with NavigatorBehaviour with Generators with BeforeAndAfterEach with MockitoSugar {

  import OldEstablishersIndividualDetailsNavigatorSpec._

  val navigator: Navigator = injector.instanceOf[OldEstablishersIndividualDetailsNavigator]
  val mockFeatureToggle = mock[FeatureToggleService]

  override protected def beforeEach(): Unit = {
    reset(mockFeatureToggle)
    when(mockFeatureToggle.get(any())(any(), any()))
      .thenReturn(Future.successful(FeatureToggle(SchemeRegistration, false)))
  }


  "NormalMode" must {
    val normalModeRoutes: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Next Page"),
        row(EstablisherNameId(index))(somePersonNameValue, AddEstablisherController.onPageLoad(NormalMode, EmptyOptionalSchemeReferenceNumber)),
        row(EstablisherDOBId(index))(someDate, EstablisherHasNINOController.onPageLoad( NormalMode, index, EmptyOptionalSchemeReferenceNumber)),
        row(EstablisherHasNINOId(index))(true, EstablisherEnterNINOController.onPageLoad( NormalMode, index, EmptyOptionalSchemeReferenceNumber)),
        row(EstablisherHasNINOId(index))(false, EstablisherNoNINOReasonController.onPageLoad( NormalMode, index, EmptyOptionalSchemeReferenceNumber)),
        row(EstablisherEnterNINOId(index))(someRefValue, EstablisherHasUTRController.onPageLoad( NormalMode, index, EmptyOptionalSchemeReferenceNumber)),
        row(EstablisherNoNINOReasonId(index))(someStringValue, EstablisherHasUTRController.onPageLoad( NormalMode, index, EmptyOptionalSchemeReferenceNumber)),
        row(EstablisherHasUTRId(index))(true, EstablisherEnterUTRController.onPageLoad( NormalMode, index, EmptyOptionalSchemeReferenceNumber)),
        row(EstablisherHasUTRId(index))(false, EstablisherNoUTRReasonController.onPageLoad( NormalMode, index, EmptyOptionalSchemeReferenceNumber)),
        row(EstablisherNoUTRReasonId(index))(someStringValue, CheckYourAnswersDetailsController.onPageLoad(journeyMode(NormalMode), Index, EmptyOptionalSchemeReferenceNumber)),
        row(EstablisherUTRId(index))(someRefValue, CheckYourAnswersDetailsController.onPageLoad(journeyMode(NormalMode), Index, EmptyOptionalSchemeReferenceNumber))
      )

    behave like navigatorWithRoutesForMode(NormalMode)(navigator, normalModeRoutes, None)
  }

  "CheckMode" must {
    val checkModeRoutes: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Expected next page"),
        row(EstablisherDOBId(index))(someDate, CheckYourAnswersDetailsController.onPageLoad(journeyMode(CheckMode), Index, EmptyOptionalSchemeReferenceNumber)),
        row(EstablisherHasNINOId(index))(true, EstablisherEnterNINOController.onPageLoad(CheckMode, Index, EmptyOptionalSchemeReferenceNumber)),
        row(EstablisherEnterNINOId(index))(someRefValue, CheckYourAnswersDetailsController.onPageLoad(journeyMode(CheckMode), Index, EmptyOptionalSchemeReferenceNumber)),
        row(EstablisherHasNINOId(index))(false, EstablisherNoNINOReasonController.onPageLoad(CheckMode, Index, EmptyOptionalSchemeReferenceNumber)),
        row(EstablisherNoNINOReasonId(index))(someStringValue, CheckYourAnswersDetailsController.onPageLoad(journeyMode(CheckMode), Index, EmptyOptionalSchemeReferenceNumber)),
        row(EstablisherHasUTRId(index))(true, EstablisherEnterUTRController.onPageLoad(CheckMode, Index, EmptyOptionalSchemeReferenceNumber)),
        row(EstablisherUTRId(index))(someRefValue, CheckYourAnswersDetailsController.onPageLoad(journeyMode(CheckMode), Index, EmptyOptionalSchemeReferenceNumber)),
        row(EstablisherHasUTRId(index))(false, EstablisherNoUTRReasonController.onPageLoad(CheckMode, Index, EmptyOptionalSchemeReferenceNumber)),
        row(EstablisherNoUTRReasonId(index))(someStringValue, CheckYourAnswersDetailsController.onPageLoad(journeyMode(CheckMode), Index, EmptyOptionalSchemeReferenceNumber))
      )

    behave like navigatorWithRoutesForMode(CheckMode)(navigator, checkModeRoutes, None)
  }

  "UpdateMode" must {
    val updateModeRoutes: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Expected next page"),
        row(EstablisherNameId(index))(somePersonNameValue, AddEstablisherController.onPageLoad(UpdateMode, OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers)),
        row(EstablisherDOBId(index))(someDate, EstablisherHasNINOController.onPageLoad(UpdateMode, index, OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers)),
        row(EstablisherHasNINOId(index))(true, EstablisherEnterNINOController.onPageLoad(UpdateMode, index, OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers)),
        row(EstablisherHasNINOId(index))(false, EstablisherNoNINOReasonController.onPageLoad(UpdateMode, index, OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers)),
        row(EstablisherEnterNINOId(index))(someRefValue, EstablisherHasUTRController.onPageLoad(UpdateMode, index, OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers)),
        row(EstablisherNoNINOReasonId(index))(someStringValue, EstablisherHasUTRController.onPageLoad(UpdateMode, index, OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers)),
        row(EstablisherHasUTRId(index))(true, EstablisherEnterUTRController.onPageLoad(UpdateMode, index, OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers)),
        row(EstablisherHasUTRId(index))(false, EstablisherNoUTRReasonController.onPageLoad(UpdateMode, index, OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers)),
        row(EstablisherNoUTRReasonId(index))(someStringValue, CheckYourAnswersDetailsController.onPageLoad(journeyMode(UpdateMode), index, OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers)),
        row(EstablisherUTRId(index))(someRefValue, CheckYourAnswersDetailsController.onPageLoad(journeyMode(UpdateMode), index, OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers))
      )

    behave like navigatorWithRoutesForMode(UpdateMode)(navigator, updateModeRoutes, OptionalSchemeReferenceNumber(srn))
  }

  "CheckUpdateMode" must {
    val checkUpdateModeRoutes: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Expected next page"),
        row(EstablisherDOBId(index))(someDate, CheckYourAnswersDetailsController.onPageLoad(journeyMode(CheckUpdateMode), index, OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers)),
        row(EstablisherHasNINOId(index))(true, EstablisherEnterNINOController.onPageLoad(CheckUpdateMode, index, OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers)),
        row(EstablisherEnterNINOId(index))(someRefValue, CheckYourAnswersDetailsController.onPageLoad(journeyMode(CheckUpdateMode), index, OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers)),
        row(EstablisherEnterNINOId(index))(someRefValue, anyMoreChangesPage(srn), Some(existingEstablisherUserAnswers)),
        row(EstablisherHasNINOId(index))(false, EstablisherNoNINOReasonController.onPageLoad(CheckUpdateMode, index, OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers)),
        row(EstablisherNoNINOReasonId(index))(someStringValue, CheckYourAnswersDetailsController.onPageLoad(journeyMode(CheckUpdateMode), index, OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers)),
        row(EstablisherHasUTRId(index))(true, EstablisherEnterUTRController.onPageLoad(CheckUpdateMode, index, OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers)),
        row(EstablisherUTRId(index))(someRefValue, CheckYourAnswersDetailsController.onPageLoad(journeyMode(CheckUpdateMode), index, OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers)),
        row(EstablisherUTRId(index))(someRefValue, anyMoreChangesPage(srn), Some(existingEstablisherUserAnswers)),
        row(EstablisherHasUTRId(index))(false, EstablisherNoUTRReasonController.onPageLoad(CheckUpdateMode, index, OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers)),
        row(EstablisherNoUTRReasonId(index))(someStringValue, CheckYourAnswersDetailsController.onPageLoad(journeyMode(CheckUpdateMode), index, OptionalSchemeReferenceNumber(srn)), Some(newEstablisherUserAnswers))
      )

    behave like navigatorWithRoutesForMode(CheckUpdateMode)(navigator, checkUpdateModeRoutes, OptionalSchemeReferenceNumber(srn))
  }
}

object OldEstablishersIndividualDetailsNavigatorSpec extends SpecBase with Matchers with NavigatorBehaviour with Generators {
  private lazy val index = 0
  private val newEstablisherUserAnswers = UserAnswers().set(IsEstablisherNewId(index))(true).asOpt.value
  private val existingEstablisherUserAnswers = UserAnswers().set(IsEstablisherNewId(index))(false).asOpt.value
  private val srn = Some(SchemeReferenceNumber("srn"))
  private val someDate = LocalDate.now()
}


