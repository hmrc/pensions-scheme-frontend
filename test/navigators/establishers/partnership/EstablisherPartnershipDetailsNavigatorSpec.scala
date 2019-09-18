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

package navigators.establishers.partnership

import base.SpecBase
import generators.Generators
import identifiers.Identifier
import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.establishers.partnership._
import models._
import navigators.establishers.individual.EstablishersIndividualDetailsNavigatorSpec.index
import navigators.{Navigator, NavigatorBehaviour}
import org.scalatest.{MustMatchers, OptionValues}
import org.scalatest.prop._
import play.api.mvc.Call
import utils.UserAnswers

class EstablisherPartnershipDetailsNavigatorSpec extends SpecBase with MustMatchers with NavigatorBehaviour with Generators {

  import EstablisherPartnershipDetailsNavigatorSpec._

  val navigator: Navigator =
    applicationBuilder(dataRetrievalAction = UserAnswers().dataRetrievalAction, featureSwitchEnabled = true).build().injector.instanceOf[Navigator]

  "EstablisherPartnershipDetailsNavigator" when {

    "in NormalMode" must {
      def normalModeRoutes(): TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          row(PartnershipDetailsId(index))(partnershipDetails, addEstablisherPage(NormalMode, None)),
          row(PartnershipUniqueTaxReferenceID(index))(UniqueTaxReference.Yes(someStringValue), vatPage(NormalMode, None)),
          row(PartnershipVatId(index))(Vat.Yes(someStringValue), payePage(NormalMode, None)),
          row(PartnershipPayeId(index))(Paye.Yes(someStringValue), cyaPage(NormalMode, None))
        )

      behave like navigatorWithRoutesForMode(NormalMode)(navigator, normalModeRoutes(), None)
    }

    "in CheckMode" must {
      def checkModeRoutes(): TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          row(PartnershipUniqueTaxReferenceID(index))(UniqueTaxReference.Yes(someStringValue), cyaPage(CheckMode, None)),
          row(PartnershipVatId(index))(Vat.Yes(someStringValue), cyaPage(CheckMode, None)),
          row(PartnershipPayeId(index))(Paye.Yes(someStringValue), cyaPage(CheckMode, None))
        )

      behave like navigatorWithRoutesForMode(CheckMode)(navigator, checkModeRoutes(), None)
    }

    "in UpdateMode" must {
      def updateModeRoutes(): TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          row(PartnershipDetailsId(index))(partnershipDetails, addEstablisherPage(UpdateMode, srn)),
          row(PartnershipUniqueTaxReferenceID(index))(UniqueTaxReference.Yes(someStringValue), vatPage(UpdateMode, srn)),
          row(PartnershipVatId(index))(Vat.Yes(someStringValue), payePage(UpdateMode, srn)),
          row(PartnershipPayeId(index))(Paye.Yes(someStringValue), cyaPage(UpdateMode, srn))
        )

      behave like navigatorWithRoutesForMode(UpdateMode)(navigator, updateModeRoutes(), srn)
    }

    "in CheckUpdateMode" must {
      def checkUpdateModeRoutes(): TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          row(PartnershipUniqueTaxReferenceID(index))(UniqueTaxReference.Yes(someStringValue), cyaPage(CheckUpdateMode, srn), Some(uaNewEstablisher)),
          row(PartnershipUniqueTaxReferenceID(index))(UniqueTaxReference.Yes(someStringValue), anyMoreChangesPage(srn)),
          row(PartnershipVatId(index))(Vat.Yes(someStringValue), cyaPage(CheckUpdateMode, srn)),
          row(PartnershipEnterVATId(index))(someRefValue, anyMoreChangesPage(srn)),
          row(PartnershipPayeId(index))(Paye.Yes(someStringValue), cyaPage(CheckUpdateMode, srn)),
          row(PartnershipPayeVariationsId(index))(someRefValue, anyMoreChangesPage(srn))
        )

      behave like navigatorWithRoutesForMode(CheckUpdateMode)(navigator, checkUpdateModeRoutes(), srn)
    }
  }
}

object EstablisherPartnershipDetailsNavigatorSpec extends OptionValues {
  private val index = 0
  private val srn = Some("test-srn")
  private val partnershipDetails = PartnershipDetails("test partnership")
  private val uaNewEstablisher = UserAnswers().set(IsEstablisherNewId(index))(true).asOpt.value

  private def addEstablisherPage(mode: Mode, srn: Option[String]): Call =
    controllers.register.establishers.routes.AddEstablisherController.onPageLoad(mode, srn)

  private def vatPage(mode: Mode, srn: Option[String]): Call =
    controllers.register.establishers.partnership.routes.PartnershipVatController.onPageLoad(mode, index, srn)

  private def payePage(mode: Mode, srn: Option[String]): Call =
    controllers.register.establishers.partnership.routes.PartnershipPayeController.onPageLoad(mode, index, srn)

  private def cyaPage(mode: Mode, srn: Option[String]): Call =
    controllers.register.establishers.partnership.routes.CheckYourAnswersPartnershipDetailsController.onPageLoad(mode, index, srn)
}



