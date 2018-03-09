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

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.twirl.api.Html
import base.SpecBase
import org.jsoup.select.Elements
import org.scalatest.Assertion
import org.scalatest.matchers.{MatchResult, Matcher}

trait ViewSpecBase extends SpecBase {

  def haveLink(url: String): Matcher[Elements] = Matcher[Elements] {
    elements =>
      val href = elements.attr("href")
      MatchResult(
        href == url,
        s"href $href is not equal to the url $url",
        s"href $href is equal to the url $url"
      )
  }

  def haveDynamicText(messageKey: String, args: Any*): Matcher[Document] = Matcher[Document] {
    document =>
      val text = messages(messageKey, args:_*)
      MatchResult(
        document.toString.contains(text),
        s"text $text is not rendered on the page",
        s"text $text is rendered on the page"
      )
  }

  def haveLabelAndValue(forElement: String, expectedLabel: String, expectedValue: String): Matcher[Document] = Matcher[Document] {
    document =>
      val labels = document.getElementsByAttributeValue("for", forElement)
      val label = labels.first.text
      val value = document.getElementById(forElement).attr("value")
      MatchResult(
        label == expectedLabel &&  value == expectedValue,
        s"text box with label: $label and value : $value is not correct",
        s"text box has correct label: $label and correct value: $value"
      )
  }

  def haveErrorOnSummary(id: String, expectedErrorMessage: String): Matcher[Document] = Matcher[Document] {
    document =>
      val href = document.select(s"a[href='#${id}']").text()
      MatchResult(
        (document.select("#error-summary-heading").size() != 0 && href == expectedErrorMessage),
        s"Error $expectedErrorMessage for field with id $id is not displayed on error summary",
        s"Error $expectedErrorMessage for field with id $id is displayed on error summary"
      )
  }

  def asDocument(html: Html): Document = Jsoup.parse(html.toString())

  def assertEqualsMessage(doc: Document, cssSelector: String, expectedMessageKey: String): Assertion =
    assertEqualsValue(doc, cssSelector, messages(expectedMessageKey))

  def assertEqualsValue(doc : Document, cssSelector : String, expectedValue: String): Assertion = {
    val elements = doc.select(cssSelector)

    if(elements.isEmpty) throw new IllegalArgumentException(s"CSS Selector $cssSelector wasn't rendered.")

    //<p> HTML elements are rendered out with a carriage return on some pages, so discount for comparison
    assert(elements.first().html().replace("\n", "") == expectedValue)
  }

  def assertPageTitleEqualsMessage(doc: Document, expectedMessage: String): Assertion = {
    val headers = doc.getElementsByTag("h1")
    headers.size mustBe 1
    headers.first.text.replaceAll("\u00a0", " ") mustBe expectedMessage.replaceAll("&nbsp;", " ")
  }

  def assertContainsText(doc:Document, text: String): Assertion = assert(doc.toString.contains(text), "\n\ntext " + text + " was not rendered on the page.\n")

  def assertContainsMessages(doc: Document, expectedMessageKeys: String*): Unit = {
    for (key <- expectedMessageKeys) assertContainsText(doc, messages(key))
  }

  def assertRenderedById(doc: Document, id: String): Assertion = {
    assert(doc.getElementById(id) != null, "\n\nElement " + id + " was not rendered on the page.\n")
  }

  def assertNotRenderedById(doc: Document, id: String): Assertion = {
    assert(doc.getElementById(id) == null, "\n\nElement " + id + " was rendered on the page.\n")
  }

  def assertRenderedByIdWithText(doc: Document, id: String, text: String): Assertion = {
    val element = doc.getElementById(id)
    assert(element != null, "\n\nElement " + id + " was not rendered on the page.\n")
    assert(element.text().equals(text), s"\n\nElement $id had text '${element.text()}' not '$text'.\n")
  }

  def assertRenderedByCssSelector(doc: Document, cssSelector: String): Assertion = {
    assert(!doc.select(cssSelector).isEmpty, "Element " + cssSelector + " was not rendered on the page.")
  }

  def assertNotRenderedByCssSelector(doc: Document, cssSelector: String): Assertion = {
    assert(doc.select(cssSelector).isEmpty, "\n\nElement " + cssSelector + " was rendered on the page.\n")
  }

  def assertContainsLabel(doc: Document, forElement: String, expectedText: String, expectedHintText: Option[String] = None): Any = {
    val labels = doc.getElementsByAttributeValue("for", forElement)
    assert(labels.size == 1, s"\n\nLabel for $forElement was not rendered on the page.")
    val label = labels.select("span")
    assert(label.first.text() == expectedText, s"\n\nLabel for $forElement was not $expectedText")

    if (expectedHintText.isDefined) {
      assert(labels.first.getElementsByClass("form-hint").first.text == expectedHintText.get,
        s"\n\nLabel for $forElement did not contain hint text $expectedHintText")
    }
  }

  def assertElementHasClass(doc: Document, id: String, expectedClass: String): Assertion = {
    assert(doc.getElementById(id).hasClass(expectedClass), s"\n\nElement $id does not have class $expectedClass")
  }

  def assertContainsRadioButton(doc: Document, id: String, name: String, value: String, isChecked: Boolean): Assertion = {
    assertRenderedById(doc, id)
    val radio = doc.getElementById(id)

    assert(radio.attr("name") == name, s"\n\nElement $id does not have name $name")
    assert(radio.attr("value") == value, s"\n\nElement $id does not have value $value")
    isChecked match {
      case true => assert(radio.attr("checked") == "checked", s"\n\nElement $id is not checked")
      case _ => assert(!radio.hasAttr("checked") && radio.attr("checked") != "checked", s"\n\nElement $id is checked")
    }
  }

  def assertContainsSelectOption(doc: Document, id: String, label: String, value: String, isChecked: Boolean): Assertion = {
    assertRenderedById(doc, id)
    val select = doc.getElementById(id)

    assert(select.text == label, s"\n\nElement $id does not have label $label")
    assert(select.attr("value") == value, s"\n\nElement $id does not have value $value")
    isChecked match {
      case true => assert(select.hasAttr("selected"), s"\n\nElement $id is not selected")
      case _ => assert(!select.hasAttr("selected"), s"\n\nElement $id is selected")
    }
  }

  def assertLink(doc: Document, linkId: String, url: String): Assertion = {
    val link = doc.select(s"a[id=$linkId]")
    assert(link.size() == 1, s"\n\nLink $linkId is not displayed")
    val href = link.attr("href")
    assert(href == url, s"\n\nLink $linkId has href $href no $url")
  }

}
