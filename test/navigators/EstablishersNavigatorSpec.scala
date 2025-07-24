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
import identifiers.Identifier
import identifiers.register.establishers.{AddEstablisherId, ConfirmDeleteEstablisherId, EstablisherKindId}
import models.register.establishers.EstablisherKind
import models.{EmptyOptionalSchemeReferenceNumber, Mode, NormalMode, UpdateMode}
import org.scalatest.OptionValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.prop.TableFor3
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.{Enumerable, UserAnswers}

class EstablishersNavigatorSpec extends SpecBase with Matchers with NavigatorBehaviour {

  import EstablishersNavigatorSpec._

  val navigator: Navigator = applicationBuilder(dataRetrievalAction = new FakeDataRetrievalAction(Some(Json.obj()))).build().injector.instanceOf[Navigator]

  "EstablishersNavigator" when {

    "in NormalMode" must {
      def navigation: TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          rowNoValue(AddEstablisherId(None))(establisherKind(NormalMode)),
          rowNoValue(AddEstablisherId(Some(true)))(establisherKind(NormalMode), ua = Some(addEstablishersTrue)),
          rowNoValue(AddEstablisherId(Some(false)))(taskList(NormalMode), ua = Some(addEstablishersFalse)),
          row(EstablisherKindId(0))(EstablisherKind.Company, companyDetails(NormalMode)),
          row(EstablisherKindId(0))(EstablisherKind.Individual, individualName(NormalMode)),
          row(EstablisherKindId(0))(EstablisherKind.Partnership, partnershipDetails(NormalMode)),

          rowNoValue(ConfirmDeleteEstablisherId)(addEstablisher(NormalMode))
        )
      behave like navigatorWithRoutesForMode(NormalMode)(navigator, navigation, EmptyOptionalSchemeReferenceNumber)
    }

    "in UpdateMode" must {
      def navigation: TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          rowNoValue(AddEstablisherId(None))(establisherKind(UpdateMode)),
          rowNoValue(AddEstablisherId(Some(true)))(establisherKind(UpdateMode), ua = Some(addEstablishersTrue)),
          rowNoValue(AddEstablisherId(Some(false)))(taskList(UpdateMode), ua = Some(addEstablishersFalse)),
          row(EstablisherKindId(0))(EstablisherKind.Company, companyDetails(UpdateMode)),
          row(EstablisherKindId(0))(EstablisherKind.Individual, individualName(UpdateMode)),
          row(EstablisherKindId(0))(EstablisherKind.Partnership, partnershipDetails(UpdateMode)),

          rowNoValue(ConfirmDeleteEstablisherId)(controllers.routes.AnyMoreChangesController.onPageLoad(EmptyOptionalSchemeReferenceNumber))
        )
      behave like navigatorWithRoutesForMode(UpdateMode)(navigator, navigation, EmptyOptionalSchemeReferenceNumber)
    }
  }
}

//noinspection MutatorLikeMethodIsParameterless
object EstablishersNavigatorSpec extends OptionValues with Enumerable.Implicits {

  private val addEstablishersTrue = UserAnswers(Json.obj(AddEstablisherId.toString -> "true"))
  private val addEstablishersFalse = UserAnswers(Json.obj(AddEstablisherId.toString -> "false"))

  private def companyDetails(mode: Mode) = controllers.register.establishers.company.routes.CompanyDetailsController.onPageLoad(mode, EmptyOptionalSchemeReferenceNumber, 0)

  private def individualName(mode: Mode) = controllers.register.establishers.individual.routes.EstablisherNameController.onPageLoad(mode, 0, EmptyOptionalSchemeReferenceNumber)

  private def partnershipDetails(mode: Mode) = controllers.register.establishers.partnership.routes.PartnershipDetailsController.onPageLoad(mode, 0, EmptyOptionalSchemeReferenceNumber)

  private def establisherKind(mode: Mode) = controllers.register.establishers.routes.EstablisherKindController.onPageLoad(mode, 0, EmptyOptionalSchemeReferenceNumber)

  private def addEstablisher(mode: Mode) = controllers.register.establishers.routes.AddEstablisherController.onPageLoad(mode, EmptyOptionalSchemeReferenceNumber)

  private def taskList(mode: Mode) = controllers.routes.PsaSchemeTaskListController.onPageLoad(mode, EmptyOptionalSchemeReferenceNumber)
}
