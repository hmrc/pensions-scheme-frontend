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

package base

import config.FrontendAppConfig
import controllers.actions._
import org.jsoup.nodes.Document
import org.scalatest.Assertion
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice._
import play.api.Environment
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.inject.{Injector, bind}
import play.api.mvc.{AnyContentAsEmpty, MessagesControllerComponents}
import play.api.test.FakeRequest
import uk.gov.hmrc.crypto.ApplicationCrypto

trait SpecBase
  extends PlaySpec
    with GuiceOneAppPerSuite {
  protected def crypto: ApplicationCrypto = injector.instanceOf[ApplicationCrypto]

  def injector: Injector = app.injector

  def frontendAppConfig: FrontendAppConfig = injector.instanceOf[FrontendAppConfig]

  def messagesApi: MessagesApi = injector.instanceOf[MessagesApi]

  def fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("", "/foo")

  implicit def messages: Messages = messagesApi.preferred(fakeRequest)

  def environment: Environment = injector.instanceOf[Environment]

  def controllerComponents: MessagesControllerComponents = injector.instanceOf[MessagesControllerComponents]

  def appRunning(): Unit = app

  def assertNotRenderedById(doc: Document, id: String): Assertion = {
    assert(doc.getElementById(id) == null, "\n\nElement " + id + " was rendered on the page.\n")
  }

  def assertRenderedById(doc: Document, id: String): Assertion =
    assert(doc.getElementById(id) != null, "\n\nElement " + id + " was not rendered on the page.\n")

  def modules(dataRetrievalAction: DataRetrievalAction): Seq[GuiceableModule] = Seq(
    bind[AuthAction].toInstance(FakeAuthAction),
    bind[AllowAccessActionProvider].toInstance(FakeAllowAccessProvider()),
    bind[DataRetrievalAction].toInstance(dataRetrievalAction)
  )

  def applicationBuilder(
                          dataRetrievalAction: DataRetrievalAction,
                          extraModules: Seq[GuiceableModule] = Seq.empty
                        ): GuiceApplicationBuilder = {
    new GuiceApplicationBuilder()
      .overrides(
        extraModules ++ modules(dataRetrievalAction): _*
      )
  }
}

object SpecBase extends SpecBase
