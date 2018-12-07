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

package views.register.establishers.company.director

import controllers.register.establishers.company.director.routes.ConfirmDeleteDirectorController
import controllers.register.establishers.company.routes.AddCompanyDirectorsController
import models.NormalMode
import views.behaviours.ViewBehaviours
import views.html.register.establishers.company.director.confirmDeleteDirector

class ConfirmDeleteDirectorViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "confirmDeleteDirector"

  private val directorName = "John Doe"
  private val postCall = ConfirmDeleteDirectorController.onSubmit(0, 0)
  private val cancelCall = AddCompanyDirectorsController.onSubmit(NormalMode, 0)

  private def createView(isHubEnabled:Boolean = false) = () =>
    confirmDeleteDirector(appConfig(isHubEnabled), directorName, postCall, cancelCall)(fakeRequest, messages)

  "ConfirmDeleteDirector view" must {
    behave like normalPage(createView(), messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__heading").format("John Doe"))

    behave like pageWithBackLink(createView())

    behave like pageWithSubmitButton(createView())

    "have a cancel link" in {
      val doc = asDocument(createView()())
      assertLink(doc, "cancel", cancelCall.url)
    }
  }

  "ConfirmDeleteDirector view with hub enabled" must {

    "not have a back link" in {
      val doc = asDocument(createView(isHubEnabled = true)())
      assertNotRenderedById(doc, "back-link")
    }
  }

}
