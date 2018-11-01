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

package controllers

import connectors._
import controllers.actions.{DataRetrievalAction, _}
import models.details.transformation.{SchemeDetailsMasterSection, SchemeDetailsStubData}
import org.mockito.Matchers
import org.mockito.Mockito.{reset, when}
import org.scalatest.mockito.MockitoSugar
import play.api.test.Helpers.{contentAsString, _}
import views.html.psa_scheme_details

import scala.concurrent.Future

class PSASchemeDetailsControllerSpec extends ControllerSpecBase {

  import PSASchemeDetailsControllerSpec._

  "SchemeDetailsController" must {

    "return OK and the correct view for a GET" in {

      reset(fakeSchemeDetailsConnector)
      when(fakeSchemeDetailsConnector.getSchemeDetails(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(psaSchemeDetailsSample))
      when(fakeSchemeTransformer.transformMasterSection(Matchers.any())).thenReturn(masterSections)

      val result = controller().onPageLoad(srn)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

  }
}

private object PSASchemeDetailsControllerSpec extends ControllerSpecBase with MockitoSugar with SchemeDetailsStubData {

  val fakeSchemeDetailsConnector: SchemeDetailsConnector = mock[SchemeDetailsConnector]
  val fakeSchemeTransformer: SchemeDetailsMasterSection = mock[SchemeDetailsMasterSection]

  def controller(dataRetrievalAction: DataRetrievalAction = dontGetAnyData): PSASchemeDetailsController =
    new PSASchemeDetailsController(frontendAppConfig,
      messagesApi,
      fakeSchemeDetailsConnector,
      fakeSchemeTransformer,
      FakeAuthAction)

  val masterSections = Seq(individualMasterSection)
  val srn = "S1000000456"

  def viewAsString(): String =
    psa_scheme_details(
      frontendAppConfig, masterSections, psaSchemeDetailsSample.schemeDetails.name, srn
    )(fakeRequest, messages).toString()
}
