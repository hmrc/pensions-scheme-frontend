/*
 * Copyright 2019 HM Revenue & Customs
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

package views.register.establishers.partnership.partner

import controllers.register.establishers.partnership.partner.routes.ConfirmDeletePartnerController
import controllers.register.establishers.partnership.routes.AddPartnersController
import play.twirl.api.HtmlFormat
import viewmodels.Message
import views.behaviours.ViewBehaviours
import views.html.register.establishers.partnership.partner.confirmDeletePartner

class ConfirmDeletePartnerViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "confirmDeletePartner"

  private val partnerName = "John Doe"
  private val postCall = ConfirmDeletePartnerController.onSubmit(establisherIndex = 0, partnerIndex = 0)
  private val cancelCall = AddPartnersController.onSubmit(index = 0)

  private def createView(): () => HtmlFormat.Appendable = () =>
    confirmDeletePartner(
      frontendAppConfig,
      partnerName,
      postCall,
      cancelCall
    )(fakeRequest, messages)

  "ConfirmDeleteDirector view" must {
    behave like normalPage(createView(), messageKeyPrefix, Message(s"messages__${messageKeyPrefix}__heading").withArgs("John Doe"))

    behave like pageWithSubmitButton(createView())

    "have a cancel link" in {
      val doc = asDocument(createView()())
      assertLink(doc, "cancel", cancelCall.url)
    }
  }
}
