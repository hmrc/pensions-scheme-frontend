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

package navigators

import base.SpecBase
import controllers.actions.FakeDataRetrievalAction
import controllers.register.establishers
import controllers.register.establishers.company.*
import controllers.register.establishers.{routes as _, *}
import identifiers.Identifier
import identifiers.register.establishers.company.*
import models.*
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatest.prop.TableFor3
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.UserAnswers

class OldEstablishersCompanyNavigatorSpec
  extends SpecBase
    with Matchers
    with NavigatorBehaviour
    with BeforeAndAfterEach
    with MockitoSugar {

  val navigator: Navigator =
    applicationBuilder(new FakeDataRetrievalAction(Some(Json.obj())))
      .build()
      .injector
      .instanceOf[Navigator]

  "EstablishersCompanyNavigator" when {

    "in NormalMode" must {
      def navigation: TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          row(CompanyDetailsId(0))(CompanyDetails("test company name"),
            establishers.routes.PsaSchemeTaskListRegistrationEstablisherController.onPageLoad(0)),
        )

      behave like navigatorWithRoutesForMode(NormalMode)(navigator, navigation, EmptyOptionalSchemeReferenceNumber)
    }

    "in CheckMode" must {
      def navigation: TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          row(CompanyDetailsId(0))(CompanyDetails("test company name"),
            routes.CheckYourAnswersCompanyDetailsController.onPageLoad(NormalMode, EmptyOptionalSchemeReferenceNumber, 0))
        )

      behave like navigatorWithRoutesForMode(CheckMode)(navigator, navigation, EmptyOptionalSchemeReferenceNumber)
    }

    "in UpdateMode" must {
      def navigation: TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          row(CompanyDetailsId(0))(CompanyDetails("test company name"),
            establishers.routes.PsaSchemeTaskListRegistrationEstablisherController.onPageLoad(0))
        )

      behave like navigatorWithRoutesForMode(UpdateMode)(navigator, navigation, EmptyOptionalSchemeReferenceNumber)
    }

    "in CheckUpdateMode" must {
      def navigation: TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          row(CompanyDetailsId(0))(CompanyDetails("test company name"),
            controllers.routes.AnyMoreChangesController.onPageLoad(EmptyOptionalSchemeReferenceNumber))
        )

      behave like navigatorWithRoutesForMode(CheckUpdateMode)(navigator, navigation, EmptyOptionalSchemeReferenceNumber)
    }
  }
}



