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

import controllers.register.trustees.routes
import forms.register.trustees.AddTrusteeFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import play.api.data.Form
import views.behaviours.YesNoViewBehaviours
import views.html.register.trustees.addTrustee

class AddTrusteeViewSpec extends YesNoViewBehaviours {

  val onwardRoute = routes.AddTrusteeController.onPageLoad(NormalMode).url

  val messageKeyPrefix = "addTrustee"
  val schemeName = "Test scheme name"
  private val maxTrustees = frontendAppConfig.maxTrustees
  val trusteeCompany = ("Trustee Company" -> onwardRoute)
  val trusteeIndividual = ("Trustee Individual" -> onwardRoute)
  val allTrustees = Seq(trusteeCompany, trusteeIndividual)

  val form = new AddTrusteeFormProvider()()

  def createView(trustees: Seq[(String, String)] = Seq.empty)  = () => addTrustee(frontendAppConfig, form, NormalMode, schemeName, trustees)(fakeRequest, messages)

  def createViewUsingForm(trustees: Seq[(String, String)] = Seq.empty)  = (form: Form[Boolean]) => addTrustee(frontendAppConfig, form, NormalMode, schemeName, trustees)(fakeRequest, messages)

  "AddTrustee view" must {
    behave like normalPage(createView(), messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__heading"))

    behave like pageWithBackLink(createView())

    behave like pageWithSecondaryHeader(createView(), schemeName)

    behave like yesNoPage(
      createViewUsingForm(allTrustees),
      messageKeyPrefix,
      routes.AddTrusteeController.onSubmit(NormalMode).url,
      "_text",
      expectedHintKey = Some("_lede")
    )

    "when there are no trustees" when {
      "not show the yes no inputs" in {
        val doc = asDocument(createView()())
        doc.select("legend > span").size() mustBe 0
      }

      "show the add director text" in {
        val doc = asDocument(createView()())
        doc must haveDynamicText(s"messages__${messageKeyPrefix}__lede")
      }
    }

    "when there are 10 directors" when {
      "not show the yes no inputs" in {
        val doc = asDocument(createViewUsingForm(Seq.fill(maxTrustees)(trusteeCompany))(form))
        doc.select("legend > span").size() mustBe 0
      }

      "show the maximum number of directors message" in {
        val doc = asDocument(createView(Seq.fill(maxTrustees)(trusteeIndividual))())
        doc must haveDynamicText("messages__addTrustees_at_maximum")
        doc must haveDynamicText("messages__addTrustees_tell_us_if_you_have_more")
      }
    }

    "display all the partially added trustee names with yes/No buttons if the maximum trustees are not added yet" in {
      val doc = asDocument(createView(allTrustees)())
      allTrustees.foreach { trusteeDetails =>
        val (trusteeName, url) = trusteeDetails
        doc must haveDynamicText(trusteeName)
        doc.select("a[id=edit-link]") must haveLink(url)
      }
      doc.select("#value-yes").size() mustEqual 1
      doc.select("#value-no").size() mustEqual 1

    }

  }
}
