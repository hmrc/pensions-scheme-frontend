/*
 * Copyright 2021 HM Revenue & Customs
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

package services

import base.SpecBase
import connectors.{UserAnswersCacheConnector, _}
import identifiers.racdac.RACDACNameId
import models.FeatureToggle.{Disabled, Enabled}
import models.FeatureToggleName.RACDAC
import models._
import models.requests.OptionalDataRequest
import org.mockito.Matchers.{any, eq => eqTo}
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{AsyncWordSpec, BeforeAndAfterEach, MustMatchers}
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsNumber, JsObject, Json}
import play.api.mvc.AnyContent
import play.api.mvc.Results.Ok
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.http.HeaderCarrier
import utils.UserAnswers
import viewmodels.Message

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, ZoneOffset}
import scala.concurrent.Future

class UrlsPartialServiceSpec extends AsyncWordSpec with MustMatchers with MockitoSugar with BeforeAndAfterEach with ScalaFutures {

  import UrlsPartialServiceSpec._

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val request: OptionalDataRequest[AnyContent] =
    OptionalDataRequest(FakeRequest("", ""), "id", Some(UserAnswers(schemeNameJsonOption)), Some(PsaId("A0000000")))

  def service: UrlsPartialService =
    new UrlsPartialService(messagesApi, frontendAppConfig, dataCacheConnector,
      lockConnector, updateConnector, minimalPsaConnector, mockFeatureToggleService)

  override def beforeEach(): Unit = {
    reset(mockFeatureToggleService)
    when(mockFeatureToggleService.get(any())(any(), any())).thenReturn(Future.successful(Disabled(RACDAC)))
    when(minimalPsaConnector.getMinimalFlags(eqTo(psaId))(any(), any()))
      .thenReturn(Future.successful(PSAMinimalFlags(isSuspended = false, isDeceased = false, rlsFlag = false)))
    when(dataCacheConnector.fetch(any())(any(), any())).thenReturn(Future.successful(Some(schemeNameJsonOption)))
    when(dataCacheConnector.removeAll(any())(any(), any())).thenReturn(Future.successful(Ok))
    when(dataCacheConnector.lastUpdated(any())(any(), any()))
      .thenReturn(Future.successful(Some(JsNumber(BigDecimal(timestamp)))))

    when(lockConnector.getLockByPsa(any())(any(), any()))
      .thenReturn(Future.successful(Some(SchemeVariance(psaId, srn))))
    when(updateConnector.fetch(any())(any(), any()))
      .thenReturn(Future.successful(Some(schemeNameJsonOption)))
    when(updateConnector.lastUpdated(any())(any(), any()))
      .thenReturn(Future.successful(Some(JsNumber(BigDecimal(timestamp)))))
    super.beforeEach()
  }

  "schemeLinks" must {
    "return the relevant links" when {
      "racdac switched off then all possible links are displayed but not RAC/DAC link" in {
        whenReady(service.schemeLinks(psaId)) { result =>
          result mustBe subscriptionLinks ++ variationLinks
        }
      }

      "racdac switched on and only non-RAC/DAC scheme in progress then continue register scheme and " +
        "declare RAC/DAC links are displayed including delete scheme link" in {
        when(mockFeatureToggleService.get(any())(any(), any())).thenReturn(Future.successful(Enabled(RACDAC)))
        whenReady(service.schemeLinks(psaId)) { result =>
          result mustBe subscriptionLinksRACDAC ++ variationLinks
        }
      }

      "racdac switched on and only RAC/DAC scheme in progress then register new scheme and " +
        "declare RAC/DAC links are displayed including delete scheme link" in {
        implicit val request: OptionalDataRequest[AnyContent] =
          OptionalDataRequest(FakeRequest("", ""), "id", Some(UserAnswers(schemeNameRACDACJsonOption)), Some(PsaId("A0000000")))
        when(mockFeatureToggleService.get(any())(any(), any())).thenReturn(Future.successful(Enabled(RACDAC)))

        val subscriptionLinksRACDAC: Seq[OverviewLink] = registerLink ++ Seq(
          OverviewLink("continue-declare-racdac", frontendAppConfig.declareAsRACDACUrl,
            Message("messages__schemeOverview__declare_racdac_continue", schemeName, deleteDate)),
          OverviewLink("delete-registration", frontendAppConfig.deleteSubscriptionUrl,
            Message("messages__schemeOverview__scheme_subscription_delete", schemeName))
        )

        whenReady(service.schemeLinks(psaId)) { result =>
          result mustBe subscriptionLinksRACDAC ++ variationLinks
        }
      }

      "racdac switched on but no schemes in progress then register new scheme and " +
        "declare RAC/DAC links are displayed but not delete scheme link" in {
        implicit val request: OptionalDataRequest[AnyContent] =
          OptionalDataRequest(FakeRequest("", ""), "id", None, Some(PsaId("A0000000")))
        when(mockFeatureToggleService.get(any())(any(), any())).thenReturn(Future.successful(Enabled(RACDAC)))

        val subscriptionLinksRACDAC = registerLink ++ Seq(
          OverviewLink("declare-racdac", frontendAppConfig.declareAsRACDACUrl,
            Message("messages__schemeOverview__declare_racdac"))
        )

        whenReady(service.schemeLinks(psaId)) { result =>
          result mustBe subscriptionLinksRACDAC ++ variationLinks
        }
      }

      "there is no ongoing subscription" in {
        implicit val request: OptionalDataRequest[AnyContent] =
          OptionalDataRequest(FakeRequest("", ""), "id", None, Some(PsaId("A0000000")))
        when(lockConnector.getLockByPsa(any())(any(), any())).thenReturn(Future.successful(None))
        whenReady(service.schemeLinks(psaId)) { result =>
          result mustBe registerLink
        }
      }

      "there is no lock for any scheme" in {
        when(lockConnector.getLockByPsa(eqTo(psaId))(any(), any())).thenReturn(Future.successful(None))
        whenReady(service.schemeLinks(psaId)) {
          _ mustBe subscriptionLinks
        }
      }

      "when there is a lock for a scheme but the scheme is not in the update collection" in {
        when(updateConnector.fetch(any())(any(), any()))
          .thenReturn(Future.successful(None))

        whenReady(service.schemeLinks(psaId)) {
          _ mustBe subscriptionLinks
        }
      }

    }
  }

  "checkIfSchemeCanBeRegistered" must {

    "redirect to the cannot start registration page if called when psa is suspended" in {
      when(minimalPsaConnector.getMinimalFlags(eqTo(psaId))(any(), any()))
        .thenReturn(Future.successful(PSAMinimalFlags(isSuspended = true, isDeceased = false, rlsFlag = false)))

      val result = service.checkIfSchemeCanBeRegistered(psaId)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe cannotStartRegistrationUrl
    }

    "redirect to the cannot start registration page if called when psa is dead" in {
      when(minimalPsaConnector.getMinimalFlags(eqTo(psaId))(any(), any()))
        .thenReturn(Future.successful(PSAMinimalFlags(isSuspended = false, isDeceased = true, rlsFlag = false)))

      val result = service.checkIfSchemeCanBeRegistered(psaId)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe youMustContactHMRCUrl
    }

    "redirect to the register scheme page if called when psa is not suspended" in {
      when(minimalPsaConnector.getMinimalFlags(eqTo(psaId))(any(), any()))
        .thenReturn(Future.successful(PSAMinimalFlags(isSuspended = false, isDeceased = false, rlsFlag = false)))
      implicit val request: OptionalDataRequest[AnyContent] =
        OptionalDataRequest(FakeRequest("", ""), "id", None, Some(PsaId("A0000000")))

      val result = service.checkIfSchemeCanBeRegistered(psaId)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe frontendAppConfig.registerUrl
    }

    "redirect to continue register a scheme page if called when psa is not suspended" in {
      when(minimalPsaConnector.getMinimalFlags(eqTo(psaId))(any(), any()))
        .thenReturn(Future.successful(PSAMinimalFlags(isSuspended = false, isDeceased = false, rlsFlag = false)))

      val result = service.checkIfSchemeCanBeRegistered(psaId)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe frontendAppConfig.continueUrl
    }

    "redirect to cannot start registration page if called when psa is suspended" in {
      when(minimalPsaConnector.getMinimalFlags(eqTo(psaId))(any(), any()))
        .thenReturn(Future.successful(PSAMinimalFlags(isSuspended = true, isDeceased = false, rlsFlag = false)))

      val result = service.checkIfSchemeCanBeRegistered(psaId)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe cannotStartRegistrationUrl
    }

    "redirect to cannot start registration page if  scheme details are found with scheme name missing and srn number present" in {
      implicit val request: OptionalDataRequest[AnyContent] =
        OptionalDataRequest(FakeRequest("", ""), "id", Some(UserAnswers(schemeSrnNumberOnlyData)), Some(PsaId("A0000000")))
      when(dataCacheConnector.removeAll(eqTo("id"))(any(), any())).thenReturn(Future(Ok))
      when(minimalPsaConnector.getMinimalFlags(eqTo(psaId))(any(), any()))
        .thenReturn(Future.successful(PSAMinimalFlags(isSuspended = false, isDeceased = false, rlsFlag = false)))

      val result = service.checkIfSchemeCanBeRegistered(psaId)

      status(result) mustBe SEE_OTHER
      verify(dataCacheConnector, times(1)).removeAll(any())(any(), any())
      redirectLocation(result).value mustBe frontendAppConfig.registerUrl
    }

  }
}

object UrlsPartialServiceSpec extends SpecBase with MockitoSugar {
  private val mockFeatureToggleService: FeatureToggleService = mock[FeatureToggleService]
  val psaName: String = "John Doe"
  val schemeName = "Test Scheme Name"
  val timestamp: Long = System.currentTimeMillis
  private val psaId = "A0000000"
  private val srn = "srn"
  private val formatter = DateTimeFormatter.ofPattern("dd MMMM YYYY")

  private val deleteDate = LocalDate.now(ZoneOffset.UTC).plusDays(frontendAppConfig.daysDataSaved).format(formatter)

  val cannotStartRegistrationUrl: String = frontendAppConfig.cannotStartRegUrl
  val youMustContactHMRCUrl: String = frontendAppConfig.youMustContactHMRCUrl

  val schemeNameJsonOption: JsObject = Json.obj("schemeName" -> schemeName)
  val schemeNameRACDACJsonOption: JsObject = Json.obj("racdac" -> Json.obj(RACDACNameId.toString -> schemeName))
  val schemeSrnNumberOnlyData: JsObject =
    Json.obj("submissionReferenceNumber" -> Json.obj("schemeReferenceNumber" -> srn))



  private val registerLink = Seq(OverviewLink("register-new-scheme", frontendAppConfig.canBeRegisteredUrl,
    Message("messages__schemeOverview__scheme_subscription")))

  private val subscriptionLinks = Seq(OverviewLink("continue-registration",frontendAppConfig.canBeRegisteredUrl,
    Message("messages__schemeOverview__scheme_subscription_continue", schemeName, deleteDate)),
    OverviewLink("delete-registration", frontendAppConfig.deleteSubscriptionUrl,
      Message("messages__schemeOverview__scheme_subscription_delete", schemeName)))

  private val subscriptionLinksRACDAC = Seq(
      OverviewLink("continue-registration",frontendAppConfig.canBeRegisteredUrl,
        Message("messages__schemeOverview__scheme_subscription_continue", schemeName, deleteDate)),
    OverviewLink("declare-racdac", frontendAppConfig.declareAsRACDACUrl,
      Message("messages__schemeOverview__declare_racdac")),
      OverviewLink("delete-registration", frontendAppConfig.deleteSubscriptionUrl,
        Message("messages__schemeOverview__scheme_subscription_delete", schemeName))
    )

  private val variationLinks = Seq(OverviewLink("continue-variation", frontendAppConfig.viewUrl.format(srn),
    Message("messages__schemeOverview__scheme_variations_continue", schemeName, deleteDate)),
    OverviewLink("delete-variation", frontendAppConfig.deleteVariationsUrl.format(srn),
      Message("messages__schemeOverview__scheme_variations_delete", schemeName)))

  private val dataCacheConnector: UserAnswersCacheConnector = mock[UserAnswersCacheConnector]
  private val minimalPsaConnector: MinimalPsaConnector = mock[MinimalPsaConnector]
  private val lockConnector = mock[PensionSchemeVarianceLockConnector]
  private val updateConnector = mock[UpdateSchemeCacheConnector]

}




