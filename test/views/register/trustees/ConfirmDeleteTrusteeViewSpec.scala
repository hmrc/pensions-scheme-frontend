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

package views.register.trustees

import models.NormalMode
import models.register.trustees.TrusteeKind.Company
import viewmodels.Message
import views.ViewSpecBase
import views.behaviours.ViewBehaviours
import views.html.register.trustees.confirmDeleteTrustee

class ConfirmDeleteTrusteeViewSpec extends ViewBehaviours {

  import ConfirmDeleteTrusteeViewSpec._

  "ConfirmDeleteTrustee view" must {
    behave like normalPage(createView, messageKeyPrefix, Message(s"messages__${messageKeyPrefix}__heading").withArgs(trusteeName))

    behave like pageWithBackLink(createView)

    behave like pageWithSubmitButton(createView)

    "have a cancel link" in {
      val cancelUrl = controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode).url
      createView must haveLink(cancelUrl, "cancel")
    }
  }

}

object ConfirmDeleteTrusteeViewSpec extends ViewSpecBase {

  private val messageKeyPrefix = "confirmDeleteTrustee"
  private val trusteeName = "test-trustee-name"

  private def createView =
    () => confirmDeleteTrustee(
      frontendAppConfig,
      trusteeName,
      controllers.register.trustees.routes.ConfirmDeleteTrusteeController.onSubmit(0, Company)
    )(fakeRequest, messages)

}
