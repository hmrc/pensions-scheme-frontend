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

import models.NormalMode
import play.twirl.api.HtmlFormat
import viewmodels.{JourneyTaskList, JourneyTaskListSection, Link}
import views.behaviours.ViewBehaviours
import views.html.schemeTaskList

class SchemeTaskListViewSpec extends ViewBehaviours {

  private def sectionLink(sectionHeader: String, linkText: String): Link =
    Link(linkText, s"$sectionHeader link")

  private def genJourneyTaskListSection(header: Option[String], isCompleted: Option[Boolean] = None, linkText: String) =
    JourneyTaskListSection(isCompleted = isCompleted, link = sectionLink(header.getOrElse(""), linkText), header = header)

  private val about = genJourneyTaskListSection(header = None, isCompleted = Some(true),
    linkText = "aboutLinkText")

  private val aboutIncomplete = genJourneyTaskListSection(header = None, isCompleted = Some(false),
    linkText = "")

  private val establishers: Seq[JourneyTaskListSection] = Seq(
    genJourneyTaskListSection(header = Some("Company details"), isCompleted = Some(false), linkText = ""),
    genJourneyTaskListSection(header = Some("Organisation details"), isCompleted = Some(true), linkText = "")
  )

  private val trustees: Seq[JourneyTaskListSection] = Seq(
    genJourneyTaskListSection(header = Some("John"), isCompleted = Some(false), linkText = ""),
    genJourneyTaskListSection(header = Some("Rob"), isCompleted = Some(true), linkText = "")
  )

  private val workingKnowledge: JourneyTaskListSection =
    genJourneyTaskListSection(header = None, isCompleted = Some(true),
      linkText = "workingKnowledgeLinkText")


  private val expectedChangeTrusteeHeader = JourneyTaskListSection(
    None,
    Link(messages("messages__schemeTaskList__sectionTrustees_change_link"),
      controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode).url),
    None
  )


  private val journeyTaskList: JourneyTaskList = JourneyTaskList(about, establishers, trustees,
    workingKnowledge, None, expectedChangeTrusteeHeader)

  private def createView(journeyTaskList:JourneyTaskList = journeyTaskList): () => HtmlFormat.Appendable = () =>
    schemeTaskList(frontendAppConfig, journeyTaskList)(fakeRequest, messages)

  private val pageHeader = messages("messages__schemeTaskList__title")
  private val messageKeyPrefix = "schemeTaskList"

  private val addTrusteeHeader = JourneyTaskListSection(
    None,
    Link(messages("messages__schemeTaskList__sectionTrustees_add_link"),
      controllers.register.trustees.routes.HaveAnyTrusteesController.onPageLoad(NormalMode).url),
    None
  )

  "SchemeTaskListView" should {

    behave like normalPageWithTitle(createView(), messageKeyPrefix, pageHeader, pageHeader)

    "display the correct link" in {
      val view = createView(journeyTaskList)
      view must haveLinkWithText(
        url = frontendAppConfig.managePensionsSchemeOverviewUrl.url,
        linkText = messages("messages__complete__saveAndReturnToManagePensionSchemes"),
        linkId = "save-and-return"
      )
    }
  }

  "SchemeTaskListView About the scheme section" should {

    val notStarted = journeyTaskList.copy(about = about.copy(isCompleted = None))
    val inProgress = journeyTaskList.copy(about = about.copy(isCompleted = Some(false)))
    val completed = journeyTaskList.copy(about = about.copy(isCompleted = Some(true)))

    behave like simpleSection(headerId = "section-about-header",
      headerText = "messages__schemeTaskList__sectionAbout_header",
      linkId = "section-about-link",
      linkUrl = about.link.target,
      linkText = about.link.text,
      statusId = "section-about-status",
      notStarted = notStarted,
      inProgress = inProgress,
      completed = completed)
  }



  "SchemeTaskListView Establishers section" when {
    "scheme section not complete" should {
        val journeyTaskListNoEstablisher: JourneyTaskList = JourneyTaskList(aboutIncomplete, Seq.empty, Seq.empty, workingKnowledge, None, addTrusteeHeader)
        val view = createView(journeyTaskListNoEstablisher)

      "display readonly content" in {
        val doc = asDocument(view())
        assertRenderedByIdWithText(doc, id = "section-establishers-scheme-incomplete-text",
          text = messages("messages__schemeTaskList__sectionEstablishers_schemeIncomplete"))
      }

      "display no add link to enter section" in {
        val doc = asDocument(view())
        assertNotRenderedById(doc, id = "section-establishers-add-link")
      }
    }

    "no establishers" should {
      val journeyTaskListNoEstablisher: JourneyTaskList = JourneyTaskList(about, Seq.empty, Seq.empty, workingKnowledge, None,
        JourneyTaskListSection(
          None,
          Link(messages("messages__schemeTaskList__sectionTrustees_add_link"),
            controllers.register.trustees.routes.HaveAnyTrusteesController.onPageLoad(NormalMode).url),
          None
        ))
      val view = createView(journeyTaskListNoEstablisher)

      "display correct header" in {

        val doc = asDocument(view())
        assertRenderedByIdWithText(doc, id = "section-establishers-header", text = messages("messages__schemeTaskList__sectionEstablishers_header"))
      }

      "display the correct link" in {

        view must haveLinkWithText(
          url = controllers.register.establishers.routes.EstablisherKindController.onPageLoad(NormalMode, 0).url,
          linkText = messages("messages__schemeTaskList__sectionEstablishers_add_link"),
          linkId = "section-establishers-add-link"
        )
      }
    }

    "establishers defined and not completed" should {

      val view = createView(journeyTaskList)

      "display the correct link" in {

        view must haveLinkWithText(
          url = controllers.register.establishers.routes.AddEstablisherController.onPageLoad(NormalMode).url,
          linkText = messages("messages__schemeTaskList__sectionEstablishers_change_link"),
          linkId = "section-establishers-change-link"
        )
      }

      Seq(("01", "messages__schemeTaskList__inProgress"), ("02", "messages__schemeTaskList__completed")).foreach { case(index, msg) =>

        s"display the first establisher section with correct link and status for item no $index" in {

          view must haveLinkWithText(
            url = journeyTaskList.establishers(index.toInt-1).link.target,
            linkText = journeyTaskList.establishers(index.toInt-1).link.text,
            linkId = s"section-establishers-link-$index"
          )
        }

        s"display the first establisher section with correct status of in progress for item no $index" in {
          val view = createView(journeyTaskList)
          val doc = asDocument(view())
          doc.getElementById(s"section-establishers-status-$index").text mustBe messages(msg)
        }
      }

    }
  }

  "SchemeTaskListView Trustees section" when {
    "scheme section not complete" should {
      val journeyTaskListNoEstablisher: JourneyTaskList = JourneyTaskList(aboutIncomplete, Seq.empty, Seq.empty, workingKnowledge, None, addTrusteeHeader)
      val view = createView(journeyTaskListNoEstablisher)

      "display readonly content" in {
        val doc = asDocument(view())
        assertRenderedByIdWithText(doc, id = "section-trustees-scheme-incomplete-text",
          text = messages("messages__schemeTaskList__sectionTrustees_schemeIncomplete"))
      }

      "display no add link to enter section" in {
        val doc = asDocument(view())
        assertNotRenderedById(doc, id = "section-trustees-add-link")
      }
    }
  }

  "SchemeTaskListView Trustees section" should {

    "no trustees" should {

      val journeyTaskListNoEstablisher: JourneyTaskList = JourneyTaskList(about, Seq.empty, Seq.empty, workingKnowledge, None,
        JourneyTaskListSection(
          None,
          Link(messages("messages__schemeTaskList__sectionTrustees_add_link"),
            controllers.register.trustees.routes.HaveAnyTrusteesController.onPageLoad(NormalMode).url),
          None
        ))
      val view = createView(journeyTaskListNoEstablisher)

      "display correct header" in {

        val doc = asDocument(view())
        assertRenderedByIdWithText(doc, id = "section-trustees-header", text = messages("messages__schemeTaskList__sectionTrustees_header"))
      }

      "display the correct link" in {
        view must haveLinkWithText(
          url = controllers.register.trustees.routes.HaveAnyTrusteesController.onPageLoad(NormalMode).url,
          linkText = messages("messages__schemeTaskList__sectionTrustees_add_link"),
          linkId = "section-trustees-link"
        )
      }
    }

    "trustees defined and not completed" should {

      val view = createView(journeyTaskList)

      "display the correct link" in {

        view must haveLinkWithText(
          url = controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode).url,
          linkText = messages("messages__schemeTaskList__sectionTrustees_change_link"),
          linkId = "section-trustees-link"
        )
      }

      Seq(("01", "messages__schemeTaskList__inProgress"), ("02", "messages__schemeTaskList__completed")).foreach { case(index, msg) =>

        s"display the first establisher section with correct link and status for item no $index" in {

          view must haveLinkWithText(
            url = journeyTaskList.trustees(index.toInt-1).link.target,
            linkText = journeyTaskList.trustees(index.toInt-1).link.text,
            linkId = s"section-trustees-link-$index"
          )
        }

        s"display the first establisher section with correct status of in progress for item no $index" in {
          val view = createView(journeyTaskList)
          val doc = asDocument(view())

          doc.getElementById(s"section-trustees-status-$index").text mustBe messages(msg)
        }
      }

    }


  }

  "SchemeTaskListView Working knowledge of pensions section" should {
    val notStarted = journeyTaskList.copy(workingKnowledge = workingKnowledge.copy(isCompleted = None))
    val inProgress = journeyTaskList.copy(workingKnowledge = workingKnowledge.copy(isCompleted = Some(false)))
    val completed = journeyTaskList.copy(workingKnowledge = workingKnowledge.copy(isCompleted = Some(true)))

    behave like simpleSection(headerId = "section-workingKnowledge-header",
      headerText = "messages__schemeTaskList__sectionWorkingKnowledge_header",
      linkId = "section-workingKnowledge-link",
      linkUrl = workingKnowledge.link.target,
      linkText = workingKnowledge.link.text,
      statusId = "section-workingKnowledge-status",
      notStarted = notStarted,
      inProgress = inProgress,
      completed = completed)
  }

  "SchemeTaskListView Declaration section" should {
    "display correct heading" in {
      val doc = asDocument(createView(journeyTaskList)())
      assertRenderedByIdWithText(doc, id = "section-declaration-header", text = messages("messages__schemeTaskList__sectionDeclaration_header"))
    }

    "display correct text for the Declaration section where there is no link" in {
      val doc = asDocument(createView(journeyTaskList)())
      assertNotRenderedById(doc, id = "section-declaration-link")
    }

    "display correct link and no text for the Declaration section where there is a link" in {
      val completed = journeyTaskList.copy(declaration = Some(Link(text = "text", target = "target")))
      val doc = asDocument(createView(completed)())

      assertNotRenderedById(doc, id = "section-declaration-text")
      createView(completed) must haveLinkWithText(
        url = "target",
        linkText = "text",
        linkId = "section-declaration-link"
      )
    }
  }

  private def simpleSection(headerId:String, headerText:String, linkId: String, linkUrl: String, linkText: String, statusId: String,
                            notStarted: JourneyTaskList,  inProgress: JourneyTaskList,  completed: JourneyTaskList) {

    "display correct header" in {

      val doc = asDocument(createView(notStarted)())
      assertRenderedByIdWithText(doc, id = headerId, text = messages(headerText))
    }

    "display the correct link" in {

      createView(notStarted) must haveLinkWithText(
        url = linkUrl,
        linkText = messages(linkText),
        linkId = linkId
      )
    }

    "display nothing if not started" in {

      val doc = asDocument(createView(notStarted)())
      assertNotRenderedById(doc, statusId)
    }

    "display the correct status if in progress" in {

      val doc = asDocument(createView(inProgress)())
      doc.getElementById(statusId).text mustBe messages("messages__schemeTaskList__inProgress")
    }

    "display the correct status if completed" in {

      val doc = asDocument(createView(completed)())
      doc.getElementById(statusId).text mustBe messages("messages__schemeTaskList__completed")
    }
  }


}


