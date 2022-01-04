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

package views.register

import models.UpdateMode
import views.behaviours.ViewBehaviours
import views.html.register.stillNeedDetails

class StillNeedDetailsViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "stillNeedDetails"
  val schemeName = "schemeName"
  val srn = Some("A232322")
  val view: stillNeedDetails = app.injector.instanceOf[stillNeedDetails]
  private def createView() = () => view(srn, Some(schemeName))(fakeRequest, messages)

  "StillNeedDetails view" must {

    behave like normalPage(createView(), messageKeyPrefix, messages("messages__stillNeedDetails__heading"), "_p1", "_p2")


    behave like pageWithReturnLink(createView(), controllers.routes.PsaSchemeTaskListController.onPageLoad(UpdateMode, srn).url)
  }
}
