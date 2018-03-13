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

import play.api.data.Form
import controllers.register.establishers.routes
import forms.register.establishers.AddEstablisherFormProvider
import views.behaviours.YesNoViewBehaviours
import models.NormalMode
import org.apache.commons.lang3.RandomStringUtils
import org.jsoup.Jsoup
import play.twirl.api.{Html, HtmlFormat}
import views.html.register.establishers.addEstablisher

class AddEstablisherViewSpec extends YesNoViewBehaviours {

  val messageKeyPrefix = "establishers__add"

  val form = new AddEstablisherFormProvider()()
  val schemeName = "Test Scheme Name"
  //TODO: Add the data for other establisher types when added
  val allEstablishers = Seq("Jo Wilson" -> routes.AddEstablisherController.onPageLoad(NormalMode).url,
    "Paul Douglas" -> routes.AddEstablisherController.onPageLoad(NormalMode).url)

  def createView: () => HtmlFormat.Appendable = () => addEstablisher(frontendAppConfig, form, NormalMode, None,
    schemeName)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) => addEstablisher(frontendAppConfig,
    form, NormalMode, Some(allEstablishers), schemeName)(fakeRequest, messages)

  def createView(establishers: Option[Seq[(String, String)]] = None): Html = addEstablisher(frontendAppConfig, form, NormalMode,
    establishers, schemeName)(fakeRequest, messages)

  "AddEstablisher view" must {

    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"))

    behave like pageWithBackLink(createView)

    behave like yesNoPage(createViewUsingForm, messageKeyPrefix,
      routes.AddEstablisherController.onSubmit(NormalMode).url, "legend_more", Some("hint"))

    "display the initial message without yes/no buttons if no establishers are added yet" in {
      val doc = Jsoup.parse(createView(None).toString())

      doc must haveDynamicText("messages__establishers__add_hint")
      doc.select("#value-yes").size() mustEqual 0
      doc.select("#value-no").size() mustEqual 0
    }

    "display all the partially added establisher names with yes/No buttons if the maximum establishers are not added yet" in {
      val doc = Jsoup.parse(createViewUsingForm(form).toString())
      allEstablishers.foreach { establisherDetails =>
        val (establisherName, url) = establisherDetails
        doc must haveDynamicText(establisherName)
        doc.select("a[id=edit-link]") must haveLink(url)
      }
      doc.select("#value-yes").size() mustEqual 1
      doc.select("#value-no").size() mustEqual 1

    }

    "display all the added establisher names with the maximum limit message without yes/no buttons if all maximum 10" +
      " establishers are already added" in {
      val establishers: Seq[(String, String)] = Seq.fill[String](10)(s"${RandomStringUtils.randomAlphabetic(3)} " +
        s"${RandomStringUtils.randomAlphabetic(5)}").map((_, routes.AddEstablisherController.onPageLoad(NormalMode).url))

      val doc = Jsoup.parse(createView(Some(establishers)).toString())
      establishers.foreach { establisherDetails =>
        val (establisherName, url) = establisherDetails
        doc must haveDynamicText(establisherName)
        doc.select("a[id=edit-link]") must haveLink(url)
      }
      doc must haveDynamicText("messages__establishers__add_limit")
      doc.select("#value-yes").size() mustEqual 0
      doc.select("#value-no").size() mustEqual 0
    }
  }
}
