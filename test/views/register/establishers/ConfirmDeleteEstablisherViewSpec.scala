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

package views.register.establishers

import controllers.register.establishers.routes._
import models.register.establishers.EstablisherKind
import models.{Index, NormalMode}
import viewmodels.Message
import views.ViewSpecBase
import views.behaviours.ViewBehaviours
import views.html.register.establishers.confirmDeleteEstablisher

class ConfirmDeleteEstablisherViewSpec extends ViewBehaviours {

  import ConfirmDeleteEstablisherViewSpec._

  "ConfirmDeleteEstablisher view" must {
    behave like normalPage(createView(), messageKeyPrefix, Message(s"messages__${messageKeyPrefix}__heading").withArgs(establisherName))

    behave like pageWithSubmitButton(createView())

    "have a cancel link" in {
      val doc = asDocument(createView()())
      assertLink(doc, "cancel", cancelCall.url)
    }

    "have the correct hint text where specified" in {
      val hintText = "test hint"
      val doc = asDocument(createView(hintText = Some(hintText))())
      assertRenderedByIdWithText(doc,"delete-hint", hintText)
    }
    "have no hint text where not specified" in {
      val doc = asDocument(createView()())
      assertNotRenderedById(doc,"delete-hint")
    }
  }
}

object ConfirmDeleteEstablisherViewSpec extends ViewSpecBase {

  val messageKeyPrefix = "confirmDeleteEstablisher"

  private val firstIndex = Index(0)
  private val establisherName = "John Doe"
  private val postCall = ConfirmDeleteEstablisherController.onSubmit(firstIndex, EstablisherKind.Indivdual)
  private val cancelCall = AddEstablisherController.onSubmit(NormalMode)

  private def createView(hintText:Option[String] = None) = () =>
    confirmDeleteEstablisher(
      frontendAppConfig,
      establisherName,
      hintText,
      postCall,
      cancelCall
    )(fakeRequest, messages)

}
