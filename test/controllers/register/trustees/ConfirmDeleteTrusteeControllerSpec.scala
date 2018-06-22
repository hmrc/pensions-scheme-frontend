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

package controllers.register.trustees

import connectors.FakeDataCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import identifiers.TypedIdentifier
import identifiers.register.SchemeDetailsId
import identifiers.register.trustees.TrusteesId
import identifiers.register.trustees.company.CompanyDetailsId
import identifiers.register.trustees.individual.TrusteeDetailsId
import models.CompanyDetails
import models.person.PersonDetails
import models.register.trustees.TrusteeKind
import models.register.trustees.TrusteeKind.{Company, Individual}
import models.register.{SchemeDetails, SchemeType}
import org.joda.time.LocalDate
import play.api.libs.json.Writes
import play.api.test.Helpers._
import utils.{FakeNavigator, UserAnswers}
import views.html.register.trustees.confirmDeleteTrustee

class ConfirmDeleteTrusteeControllerSpec extends ControllerSpecBase {

  import ConfirmDeleteTrusteeControllerSpec._

  "ConfirmDeleteTrustee Controller" must {

    "return OK and the correct view for a GET to confirm deletion of a company trustee" in {
      val result = controller(testData(companyId)(companyTrustee)).onPageLoad(0, Company)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(companyTrustee.companyName, Company)
    }

    "return OK and the correct view for a GET to confirm deletion of an individual trustee" in {
      val result = controller(testData(individualId)(individualTrustee)).onPageLoad(0, Individual)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(individualTrustee.fullName, Individual)
    }

    "redirect to Session Expired for a GET if no cached data exists" in {
      val result = controller(dontGetAnyData).onPageLoad(0, Company)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "remove the trustee in a POST request for a company trustee" in {
      val result = controller(testData(companyId)(companyTrustee)).onSubmit(0, Company)(fakeRequest)

      status(result) mustBe SEE_OTHER
      FakeDataCacheConnector.verifyRemoved(trusteeId)
    }

    "remove the trustee in a POST request for an individual trustee" in {
      val result = controller(testData(individualId)(individualTrustee)).onSubmit(0, Individual)(fakeRequest)

      status(result) mustBe SEE_OTHER
      FakeDataCacheConnector.verifyRemoved(trusteeId)
    }

    "redirect to the next page following a POST request" in {
      val result = controller(testData(companyId)(companyTrustee)).onSubmit(0, Company)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "redirect to Session Expired following a POST if no cached data exists" in {
      val result = controller(dontGetAnyData).onSubmit(0, Company)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

  }

}

object ConfirmDeleteTrusteeControllerSpec extends ControllerSpecBase {

  private val scheme = SchemeDetails(
    "test-scheme-name",
    SchemeType.BodyCorporate
  )

  private val trusteeId = TrusteesId(0)
  private val individualId = TrusteeDetailsId(0)
  private val companyId = CompanyDetailsId(0)

  private val individualTrustee = PersonDetails(
    "test-first-name",
    None,
    "test-last-name",
    LocalDate.now()
  )

  private val companyTrustee = CompanyDetails(
    "test-company-name",
    None,
    None
  )

  private val onwardRoute = controllers.routes.IndexController.onPageLoad()

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new ConfirmDeleteTrusteeController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      new FakeNavigator(onwardRoute),
      FakeDataCacheConnector
    )

  private def viewAsString(trusteeName: String, trusteeKind: TrusteeKind) =
    confirmDeleteTrustee(
      frontendAppConfig,
      scheme.schemeName,
      trusteeName,
      routes.ConfirmDeleteTrusteeController.onSubmit(0, trusteeKind)
    )(fakeRequest, messages).toString

  private def testData[I <: TypedIdentifier.PathDependent](id: I)(value: id.Data)(implicit writes: Writes[id.Data]): DataRetrievalAction = {
    val userAnswers = UserAnswers()
      .set(SchemeDetailsId)(scheme)
      .flatMap(_.set(id)(value))
      .asOpt.value

    new FakeDataRetrievalAction(Some(userAnswers.json))
  }

}
