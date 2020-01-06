/*
 * Copyright 2020 HM Revenue & Customs
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

package views.register

import models.UpdateMode
import views.behaviours.ViewBehaviours
import views.html.register.cannotMakeChanges

class CannotMakeChangesViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "cannot_Make_Changes"
  val schemeName = "schemeName"
  val srn = Some("A232322")
  private def createView() = () => cannotMakeChanges(frontendAppConfig, srn, Some(schemeName))(fakeRequest, messages)

  "CannotMakeChanges view" must {

    behave like normalPage(createView(), messageKeyPrefix, messages("messages__cannot_Make_Changes__heading"), "_p2")

    "display the correct paragraph P1" in {
      val doc = asDocument(createView()())
      assertContainsText(doc, messages("messages__cannot_Make_Changes__p1",schemeName))
    }
    behave like pageWithReturnLink(createView(), controllers.routes.SchemeTaskListController.onPageLoad(UpdateMode, srn).url)
  }
}