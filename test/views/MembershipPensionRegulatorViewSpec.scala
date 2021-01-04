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

package views

import models.NormalMode
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.membershipPensionRegulator

class MembershipPensionRegulatorViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "membership_pension_regulator"

  val view: membershipPensionRegulator = app.injector.instanceOf[membershipPensionRegulator]

  def createView: () => HtmlFormat.Appendable = () => view(NormalMode, None)(fakeRequest, messages)

  "MembershipPensionRegulator view" must {

    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__h1"),
      "_p1", "_p2")

    behave like pageWithSubmitButton(createView)

    behave like pageWithReturnLink(createView, getReturnLink)
  }
}

