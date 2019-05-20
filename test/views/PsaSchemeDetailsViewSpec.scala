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

package views

import org.jsoup.nodes.Document
import org.jsoup.Jsoup
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import viewmodels.{AnswerRow, AnswerSection, MasterSection, SuperSection}
import views.behaviours.{CheckYourAnswersBehaviours, ViewBehaviours}
import views.html.psa_scheme_details

class PsaSchemeDetailsViewSpec extends CheckYourAnswersBehaviours with ViewBehaviours {

  import PsaSchemeDetailsViewSpec._

  private val messageKeyPrefix = "psaSchemeDetails"
  private val srn = "S2400000007"

  private def emptyAnswerSections: Seq[SuperSection] = Nil

  private def mainHeader: String = "Scheme Details"

  val fakeCall = Call("method", "url")

  def createView: () => HtmlFormat.Appendable = () =>
    psa_scheme_details(
      frontendAppConfig,
      seqMasterSection,
      mainHeader,
      srn
    )(fakeRequest, messages)

  def createViewWithData: Seq[MasterSection] => HtmlFormat.Appendable = sections =>
    psa_scheme_details(
      frontendAppConfig,
      sections,
      mainHeader,
      srn
    )(fakeRequest, messages)

  "PSA scheme details page" must {

    behave like normalPageWithTitle(createView, messageKeyPrefix, mainHeader, mainHeader)

    "display the correct page title" in {
      val doc = asDocument(createView())
      assertPageTitleEqualsMessage(doc, mainHeader)
    }

    "have link for return to scheme details" in {
      Jsoup.parse(createView().toString()).select("a[id=return]") must
          haveLink(s"http://localhost:8204/manage-pension-schemes/pension-scheme-summary/${srn}")
    }

    "correctly display an MasterSection headings" in {
      val doc: Document = asDocument(createViewWithData(seqMasterSection))
      assertRenderedByIdWithText(doc, "masterSection-0-heading", masterSectionHeading)
      assertRenderedByIdWithText(doc, "masterSection-1-heading", masterSectionHeading)
    }

    "correctly display an SuperSection headings" in {
      val doc: Document = asDocument(createViewWithData(seqMasterSection))
      assertRenderedByIdWithText(doc, "superSection-0-0-heading", superSectionHeading)
      assertRenderedByIdWithText(doc, "superSection-1-0-heading", superSectionHeading)
    }

    "correctly display an AnswerSection headings" in {
      val doc: Document = asDocument(createViewWithData(seqMasterSection))
      assertRenderedByIdWithText(doc, "answerSection-0-0-0-heading", answerSectionHeading)
      assertRenderedByIdWithText(doc, "answerSection-1-0-0-heading", answerSectionHeading)
    }

    "correctly display an AnswerSection" in {
      val doc: Document = asDocument(createViewWithData(seqMasterSection))
      assertRenderedByIdWithText(doc, "masterSection-0-heading", masterSectionHeading)
      assertRenderedByIdWithText(doc, "superSection-0-0-heading", superSectionHeading)
      assertRenderedByIdWithText(doc, "answerSection-0-0-0-heading", answerSectionHeading)
      assertRenderedByIdWithText(doc, "cya-0-0-0-0-question", answerRow.label)
      assertRenderedByIdWithText(doc, "cya-0-0-0-0-0-answer", answer1)
      assertRenderedByIdWithText(doc, "cya-0-0-0-0-1-answer", answer2)
    }

  }
}

object PsaSchemeDetailsViewSpec {
  val answer1 = "test-answer-1"
  val answer2 = "test-answer-2"
  val masterSectionHeading = "master H2 heading"
  val superSectionHeading = "super H3 heading"
  val answerSectionHeading = "super H4 heading"

  val answerRow = AnswerRow("test-label", Seq(answer1, answer2), answerIsMessageKey = false, None)

  val answerSection = AnswerSection(Some(answerSectionHeading), rows = Seq(answerRow))

  val superSectio1 = SuperSection(Some(superSectionHeading), Seq(answerSection))
  val superSection2 = SuperSection(Some(superSectionHeading), Seq(answerSection))
  val masterSection1 = MasterSection(Some(masterSectionHeading), Seq(superSectio1))
  val masterSection2 = MasterSection(Some(masterSectionHeading), Seq(superSection2))

  val seqMasterSection = Seq(masterSection1, masterSection2)
}


