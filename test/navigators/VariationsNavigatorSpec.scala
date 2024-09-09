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
import identifiers._
import models.{SchemeReferenceNumber, TypeOfBenefits, UpdateMode}
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor3
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.{Enumerable, UserAnswers}

class VariationsNavigatorSpec
  extends SpecBase
    with NavigatorBehaviour
    with OptionValues
    with Enumerable.Implicits {

  private val srnValue = SchemeReferenceNumber("S123")
  private val srn      = Some(srnValue)

  private def variationsTaskList = controllers.routes.PsaSchemeTaskListController.onPageLoad(UpdateMode, srn)
  private def stillChanges       = controllers.register.routes.StillNeedDetailsController.onPageLoad(srn)
  private def declaration        = controllers.routes.VariationDeclarationController.onPageLoad(srn)

  private val complete = UserAnswers()
    .set(BenefitsSecuredByInsuranceId)(false)
    .asOpt
    .value
    .set(InsuranceDetailsChangedId)(true)
    .asOpt
    .value
    .set(TypeOfBenefitsId)(TypeOfBenefits.Defined)
    .asOpt
    .value

  val navigator: Navigator =
    applicationBuilder(
      dataRetrievalAction = new FakeDataRetrievalAction(Some(Json.obj()))
    ).build().injector.instanceOf[Navigator]

  "RegisterNavigator" when {

    "in UpdateMode" must {
      def navigation: TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          row(AnyMoreChangesId)(true, variationsTaskList),
          row(AnyMoreChangesId)(false, declaration, ua = Some(complete)),
          row(AnyMoreChangesId)(false, stillChanges)
        )
      behave like navigatorWithRoutesForMode(UpdateMode)(navigator, navigation, srn)
    }
  }
}
