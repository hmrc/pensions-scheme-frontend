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

package views

import forms.PayeFormProvider
import play.api.data.Form
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import viewmodels.{Message, PayeViewModel}
import views.behaviours.ViewBehaviours
import views.html.paye

class PayeViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "partnershipPaye"

  val form = new PayeFormProvider()()

  val viewmodel = PayeViewModel(
    postCall = Call("GET", "/"),
    title = Message("messages__partnershipPaye__title"),
    heading = Message("messages__partnershipPaye__heading"),
    hint = Some(Message("messages__common__paye_hint")),
    subHeading = Some(Message("test company name"))
  )

  def createView(isHubEnabled: Boolean = false): () => HtmlFormat.Appendable = () =>
    paye(appConfig(isHubEnabled), form, viewmodel)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    paye(frontendAppConfig, form, viewmodel)(fakeRequest, messages)

  "Paye view" must {

    behave like normalPage(createView(), messageKeyPrefix, pageHeader = messages(s"messages__${messageKeyPrefix}__heading"))

    behave like pageWithBackLink(createView())

    "not have a return link" in {
      val doc = asDocument(createView()())
      assertNotRenderedById(doc, "return-link")
    }
  }

  "Paye view with hub enabled" must {
    behave like pageWithReturnLink(createView(isHubEnabled = true), controllers.register.routes.SchemeTaskListController.onPageLoad().url)

    "not have a back link" in {
      val doc = asDocument(createView(isHubEnabled = true)())
      assertNotRenderedById(doc, "back-link")
    }
  }

  "Paye view" when {
    "rendered" must {
      val payeOptions = Seq("true", "false")

      "contain radio buttons for the value" in {
        val doc = asDocument(createViewUsingForm(form))
        for (option <- payeOptions) {
          assertContainsRadioButton(doc, s"paye_hasPaye-$option", "paye.hasPaye", option, isChecked = false)
        }
      }


      for (option <- payeOptions) {
        s"rendered with a value of '$option'" must {
          s"have the '$option' radio button selected" in {
            val doc = asDocument(createViewUsingForm(form.bind(Map("paye.hasPaye" -> s"$option"))))
            assertContainsRadioButton(doc, s"paye_hasPaye-$option", "paye.hasPaye", option, isChecked = true)

            for (unselectedOption <- payeOptions.filterNot(o => o == option)) {
              assertContainsRadioButton(doc, s"paye_hasPaye-$unselectedOption", "paye.hasPaye", unselectedOption, isChecked = false)
            }
          }
        }
      }
    }
  }
}
