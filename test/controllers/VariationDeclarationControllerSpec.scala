/*
 * Copyright 2022 HM Revenue & Customs
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

import audit.{AuditService, TcmpAuditEvent}
import connectors._
import controllers.actions._
import identifiers.{PstrId, SchemeNameId, TcmpChangedId}
import org.mockito.ArgumentMatchers.{eq => eqTo, _}
import org.mockito.ArgumentCaptor
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures.whenReady
import play.api.inject.bind
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.mvc.Call
import play.api.mvc.Results.Ok
import play.api.test.Helpers._
import utils.UserAnswers
import views.html.variationDeclaration

import scala.concurrent.Future

class VariationDeclarationControllerSpec
  extends ControllerSpecBase
    with MockitoSugar
    with BeforeAndAfterEach {

  private val schemeName = "Test Scheme Name"
  private val srnNumber = "S12345"
  private val srn: Option[String] = Some(srnNumber)

  def postCall: Call = routes.VariationDeclarationController.onClickAgree(srn)

  def onwardRoute: Call = register.routes.SchemeVariationsSuccessController.onPageLoad(srnNumber)

  def validData(extraData: JsObject = Json.obj()): FakeDataRetrievalAction =
    new FakeDataRetrievalAction(
      Some(
        Json.obj(
          SchemeNameId.toString -> schemeName,
          PstrId.toString -> "pstr"
        ) ++ extraData
      )
    )

  private val pensionsSchemeConnector: PensionsSchemeConnector = mock[PensionsSchemeConnector]
  private val lockConnector: PensionSchemeVarianceLockConnector = mock[PensionSchemeVarianceLockConnector]
  private val updateSchemeCacheConnector: UpdateSchemeCacheConnector = mock[UpdateSchemeCacheConnector]
  private val schemeDetailsReadOnlyCacheConnector: SchemeDetailsReadOnlyCacheConnector = mock[SchemeDetailsReadOnlyCacheConnector]
  private val auditService: AuditService = mock[AuditService]
  private val schemeDetailsConnector: SchemeDetailsConnector = mock[SchemeDetailsConnector]

  private val view = injector.instanceOf[variationDeclaration]

  override def beforeEach {
    reset(
      pensionsSchemeConnector,
      lockConnector,
      updateSchemeCacheConnector,
      schemeDetailsReadOnlyCacheConnector,
      auditService,
      schemeDetailsConnector
    )
  }

  private def viewAsString(): String = view(Some(schemeName), srn, postCall)(fakeRequest, messages).toString

  "VariationDeclarationController" must {

    "return OK and the correct view for a GET when update cache has srn" in {
      when(updateSchemeCacheConnector.fetch(eqTo(srnNumber))(any(), any()))
        .thenReturn(Future.successful(Some(JsString("srn"))))

      val app = applicationBuilder(
        dataRetrievalAction = validData(),
        extraModules = Seq(bind[UpdateSchemeCacheConnector].toInstance(updateSchemeCacheConnector))
      ).build()

      val controller = app.injector.instanceOf[VariationDeclarationController]

      val result = controller.onPageLoad(srn)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to tasklist and the correct view for a GET when update cache does not have srn" in {
      when(updateSchemeCacheConnector.fetch(any())(any(), any()))
        .thenReturn(Future.successful(None))

      val app = applicationBuilder(
        dataRetrievalAction = validData(),
        extraModules = Seq(bind[UpdateSchemeCacheConnector].toInstance(updateSchemeCacheConnector))
      ).build()

      val controller = app.injector.instanceOf[VariationDeclarationController]

      val result = controller.onPageLoad(srn)(fakeRequest)

      status(result) mustBe SEE_OTHER
    }

    "redirect to the next page on clicking agree and continue and NOT audit TCMP as no change made" in {
      when(pensionsSchemeConnector.updateSchemeDetails(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(()))
      when(updateSchemeCacheConnector.removeAll(any())(any(), any()))
        .thenReturn(Future.successful(Ok))
      when(schemeDetailsReadOnlyCacheConnector.removeAll(any())(any(), any()))
        .thenReturn(Future.successful(Ok))
      when(lockConnector.releaseLock(any(), any())(any(), any()))
        .thenReturn(Future.successful((): Unit))
      when(schemeDetailsConnector.getSchemeDetails(any(), any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(UserAnswers(Json.obj())))

      val app = applicationBuilder(
        dataRetrievalAction = validData(),
        extraModules = Seq(
          bind[UpdateSchemeCacheConnector].toInstance(updateSchemeCacheConnector),
          bind[PensionsSchemeConnector].toInstance(pensionsSchemeConnector),
          bind[SchemeDetailsReadOnlyCacheConnector].toInstance(schemeDetailsReadOnlyCacheConnector),
          bind[PensionSchemeVarianceLockConnector].toInstance(lockConnector),
          bind[AuditService].toInstance(auditService),
          bind[SchemeDetailsConnector].toInstance(schemeDetailsConnector)
        )
      ).build()

      val controller = app.injector.instanceOf[VariationDeclarationController]

      val result = controller.onClickAgree(srn)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
      verify(auditService, times(0)).sendExtendedEvent(any())(any(), any())
    }

    "redirect to the next page on clicking agree and continue and audit updated TCMP" in {
      when(pensionsSchemeConnector.updateSchemeDetails(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(()))
      when(updateSchemeCacheConnector.removeAll(any())(any(), any()))
        .thenReturn(Future.successful(Ok))
      when(schemeDetailsReadOnlyCacheConnector.removeAll(any())(any(), any()))
        .thenReturn(Future.successful(Ok))
      when(lockConnector.releaseLock(any(), any())(any(), any()))
        .thenReturn(Future.successful((): Unit))
      when(schemeDetailsConnector.getSchemeDetails(any(), any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(UserAnswers(Json.obj("benefits" -> "opt2"))))

      val app = applicationBuilder(
        dataRetrievalAction = validData(
          extraData = Json.obj(
            "moneyPurchaseBenefits" -> "01",
            "benefits" -> "opt1",
            TcmpChangedId.toString -> true
          )
        ),
        extraModules = Seq(
          bind[UpdateSchemeCacheConnector].toInstance(updateSchemeCacheConnector),
          bind[PensionsSchemeConnector].toInstance(pensionsSchemeConnector),
          bind[SchemeDetailsReadOnlyCacheConnector].toInstance(schemeDetailsReadOnlyCacheConnector),
          bind[PensionSchemeVarianceLockConnector].toInstance(lockConnector),
          bind[AuditService].toInstance(auditService),
          bind[SchemeDetailsConnector].toInstance(schemeDetailsConnector)
        )
      ).build()

      val controller = app.injector.instanceOf[VariationDeclarationController]

      val result = controller.onClickAgree(srn)(fakeRequest)

      val argCaptor = ArgumentCaptor.forClass(classOf[TcmpAuditEvent])

      val auditEvent = TcmpAuditEvent(
        psaId = "A0000000",
        tcmp = "01",
        payload = Json.obj(
          "moneyPurchaseBenefits" -> "01",
          "benefits" -> "opt1",
          SchemeNameId.toString -> schemeName,
          PstrId.toString -> "pstr",
          "isTcmpChanged" -> true,
          "declaration" -> true
        ),
        auditType = "TaxationCollectiveMoneyPurchaseAuditEvent"
      )

      whenReady(result) {
        response =>
          response.header.status mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(onwardRoute.url)
          verify(auditService, times(1)).sendExtendedEvent(argCaptor.capture())(any(), any())
          argCaptor.getValue mustBe auditEvent
      }
    }
  }
}




