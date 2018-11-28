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
import play.twirl.api.HtmlFormat
import viewmodels.{JourneyTaskList, JourneyTaskListSection, Link}
import views.behaviours.ViewBehaviours
import views.html.schemeTaskList

class SchemeTaskListViewSpec extends ViewBehaviours {

  private def sectionLink(sectionHeader: String): Link =
    Link(s"$sectionHeader link", s"$sectionHeader target")

  private def genJourneyTaskListSection(header: String, isCompleted: Option[Boolean] = None) =
    JourneyTaskListSection(isCompleted = None, link = sectionLink(header), header = Some(header))


  private val aboutHeader = "about"
  private val establisher1Header = "establisher1"
  private val establisher2Header = "establisher2"
  private val establisher3Header = "establisher3"

  private val trustee1Header = "trustee1"
  private val trustee2Header = "trustee2"
  private val trustee3Header = "trustee3"

  private val workingKnowledgeHeader = "workingknowledge"


  private val about = genJourneyTaskListSection(header = aboutHeader, isCompleted = Some(true))
  private val establishers: Seq[JourneyTaskListSection] = Seq(
    genJourneyTaskListSection(header = establisher1Header, isCompleted = Some(true)),
    genJourneyTaskListSection(header = establisher2Header, isCompleted = None),
    genJourneyTaskListSection(header = establisher3Header, isCompleted = Some(false))
  )

  private val trustees: Seq[JourneyTaskListSection] = Seq(
    genJourneyTaskListSection(header = trustee1Header, isCompleted = Some(false)),
    genJourneyTaskListSection(header = trustee2Header, isCompleted = None),
    genJourneyTaskListSection(header = trustee3Header, isCompleted = Some(true))
  )

  private val workingKnowledge: JourneyTaskListSection =
    genJourneyTaskListSection(header = workingKnowledgeHeader, isCompleted = Some(true))

  private val declaration: Option[Link] = Some(sectionLink("declaration"))


  private val journeyTaskList: JourneyTaskList = JourneyTaskList(about, establishers, trustees, workingKnowledge, declaration)

  private def createView(journeyTaskList:JourneyTaskList = journeyTaskList): () => HtmlFormat.Appendable = () =>
    schemeTaskList(frontendAppConfig, journeyTaskList)(fakeRequest, messages)

  private val pageHeader = "Pension scheme details"
  private val messageKeyPrefix = "schemeTaskList"

  "SchemeTaskListView" should {

    behave like normalPageWithTitle(createView(), messageKeyPrefix, pageHeader, pageHeader)
  }

  "SchemeTaskListView about section" should {

    "display correct details for the about section" in {
      val doc = asDocument(createView()())
      assertRenderedByIdWithText(doc, id = "section-about-header", text = messages("messages__schemeTaskList__sectionAbout_header"))
    }

    "display the correct link" in {

      createView() must haveLinkWithText(
        url ="/",
        linkText = messages("messages__schemeTaskList__sectionAbout_link"),
        linkId ="section-about-link"
      )
    }

    "display the correct status if completed" in {
      val doc = asDocument(createView()())
      doc.getElementById("section-about-status").text mustBe messages("messages__schemeTaskList__completed")
    }

    "display nothing if not started" in {
      val aa = journeyTaskList
      val journeyTaskList = journeyTaskList copy (
        about =
      )
      val doc = asDocument(createView(journeyTaskList)())
      doc.getElementById("section-about-status").text mustBe messages("messages__schemeTaskList__completed")
    }


  }




}


