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

package views.register

import play.api.data.Form
import controllers.register.routes
import forms.register.AddEstablisherFormProvider
import views.behaviours.YesNoViewBehaviours
import models.NormalMode
import org.apache.commons.lang3.RandomStringUtils
import org.jsoup.Jsoup
import play.twirl.api.{Html, HtmlFormat}
import views.html.register.addEstablisher

class AddEstablisherViewSpec extends YesNoViewBehaviours {

  val messageKeyPrefix = "establishers__add"

  val form = new AddEstablisherFormProvider()()
  val schemeName = "Test Scheme Name"

  def createView: () => HtmlFormat.Appendable = () => addEstablisher(frontendAppConfig, form, NormalMode, None, schemeName)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) => addEstablisher(frontendAppConfig, form, NormalMode,
    Some(Seq("Jo Wilson", "Paul Douglas")), schemeName)(fakeRequest, messages)

  def createView(establishers: Option[Seq[String]] = None): Html = addEstablisher(frontendAppConfig, form, NormalMode,
    establishers, schemeName)(fakeRequest, messages)

  "AddEstablisher view" must {

    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"))

    behave like yesNoPage(createViewUsingForm, messageKeyPrefix, "legend_more",
      routes.AddEstablisherController.onSubmit(NormalMode).url)

    "display the initial message without yes/no buttons if no establishers are added yet" in {
      val doc = Jsoup.parse(createView(None).toString())

      doc must haveDynamicText("messages__establishers__add_hint")
      doc.select("#value-yes").size() mustEqual 0
      doc.select("#value-no").size() mustEqual 0
    }

    "display all the partially added establisher names with yes/No buttons if the maximum establishers are not added yet" in {
      val doc = Jsoup.parse(createViewUsingForm(form).toString())
      doc must haveDynamicText("Jo Wilson")
      doc must haveDynamicText("Paul Douglas")
      doc.select("#value-yes").size() mustEqual 1
      doc.select("#value-no").size() mustEqual 1
      doc.select("a[id=edit-link]") must haveLink(controllers.register.routes.AddEstablisherController.onPageLoad(NormalMode).url)
    }

    "display all the added establisher names with the maximum limit message without yes/no buttons if all 10 establishers are already added" in {
      val establishers: Seq[String] = Seq.fill[String](10)(s"${RandomStringUtils.randomAlphabetic(3)} " +
        s"${RandomStringUtils.randomAlphabetic(5)}")

      val doc = Jsoup.parse(createView(Some(establishers)).toString())
      establishers.foreach{ name =>
        doc must haveDynamicText(name)
      }
      doc must haveDynamicText("messages__establishers__add_limit")
      doc.select("#value-yes").size() mustEqual 0
      doc.select("#value-no").size() mustEqual 0
    }
  }
}
