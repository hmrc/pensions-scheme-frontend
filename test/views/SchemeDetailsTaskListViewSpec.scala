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

import controllers.register.establishers.company.{routes => establisherCompanyRoutes}
import controllers.register.trustees.company.{routes => trusteeCompanyRoutes}
import models.{EntitySpoke, Link, NormalMode}
import play.twirl.api.HtmlFormat
import viewmodels._
import views.behaviours.ViewBehaviours
import views.html.schemeDetailsTaskList

class SchemeDetailsTaskListViewSpec extends ViewBehaviours {

  import SchemeDetailsTaskListViewSpec._

  private def simpleSection(linkId: String, linkUrl: String, linkText: String, statusId: String,
                            notStarted: SchemeDetailsTaskList, inProgress: SchemeDetailsTaskList, completed: SchemeDetailsTaskList) {


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
      doc.getElementById(statusId).ownText() mustBe messages("messages__schemeTaskList__inProgress")
    }

    "display the correct visually hidden text before status if in progress" in {
      val doc = asDocument(createView(inProgress)())
      doc.getElementById(statusId).text() mustBe messages("messages__schemeTaskList__status_visuallyHidden") + " " + messages("messages__schemeTaskList__inProgress")
    }

    "display the correct status if completed" in {
      val doc = asDocument(createView(completed)())
      doc.getElementById(statusId).ownText() mustBe messages("messages__schemeTaskList__completed")
    }

    "display the correct visually hidden text before status if completed" in {
      val doc = asDocument(createView(completed)())
      doc.getElementById(statusId).text() mustBe messages("messages__schemeTaskList__status_visuallyHidden") + " " + messages("messages__schemeTaskList__completed")
    }
  }

  "SchemeDetailsTaskListView" should {

    behave like normalPageWithTitle(createView(), messageKeyPrefix, schemeDetailsTaskListData().pageTitle, schemeDetailsTaskListData().h1)

    "display the correct link when registration" in {
      val view = createView(schemeDetailsTaskListData())
      view must haveLinkWithText(
        url = frontendAppConfig.managePensionsSchemeOverviewUrl.url,
        linkText = messages("messages__complete__saveAndReturnToManagePensionSchemes"),
        linkId = "save-and-return"
      )
    }

    "display the correct link when variations" in {
      val view = createView(schemeDetailsTaskListData(Some("srn")))
      view must haveLinkWithText(
        url = frontendAppConfig.managePensionsSchemeSummaryUrl.format("srn"),
        linkText = messages("messages__complete__returnToSchemeSummary"),
        linkId = "save-and-return"
      )
    }
  }

  "SchemeTaskListView Before start section" should {

    val notStarted = schemeDetailsTaskListData().copy(beforeYouStart = beforeYouStartSection.copy(isCompleted = None))
    val inProgress = schemeDetailsTaskListData().copy(beforeYouStart = beforeYouStartSection.copy(isCompleted = Some(false)))
    val completed = schemeDetailsTaskListData().copy(beforeYouStart = beforeYouStartSection.copy(isCompleted = Some(true)))

    behave like simpleSection(
      linkId = "section-before-you-start-link",
      linkUrl = beforeYouStartSection.link.target,
      linkText = beforeYouStartSection.link.text,
      statusId = "section-beforeYouStart-status",
      notStarted = notStarted,
      inProgress = inProgress,
      completed = completed)

    "display correct h2" in {
      val doc = asDocument(createView(notStarted)())
      assertRenderedByIdWithText(doc, id = "section-before-you-start-header", text = schemeDetailsTaskListData().h2)
    }

    "display correct h3" in {
      val doc = asDocument(createView(notStarted)())
      assertRenderedByIdWithText(doc, id = "section-information-h3", text = schemeDetailsTaskListData().h3.getOrElse(""))
    }
  }

  "SchemeTaskListView Working knowledge of pensions section" should {
    val notStarted = schemeDetailsTaskListData().copy(workingKnowledge = Some(wkSection.copy(isCompleted = None)))
    val inProgress = schemeDetailsTaskListData().copy(workingKnowledge = Some(wkSection.copy(isCompleted = Some(false))))
    val completed = schemeDetailsTaskListData().copy(workingKnowledge = Some(wkSection.copy(isCompleted = Some(true))))

    behave like simpleSection(
      linkId = "section-working-knowledge-link",
      linkUrl = wkSection.link.target,
      linkText = wkSection.link.text,
      statusId = "section-working-knowledge-status",
      notStarted = notStarted,
      inProgress = inProgress,
      completed = completed)
  }

  "SchemeTaskListView About section" should {
    val view = createView(schemeDetailsTaskListData())

    "display correct header" in {
      val doc = asDocument(view())
      assertRenderedByIdWithText(doc, id = "section-about-header", text = schemeDetailsTaskListData().aboutHeader)
    }


    Seq(("0", "members", "messages__schemeTaskList__inProgress"), ("1", "benefits and insurance", "messages__schemeTaskList__completed"),
      ("2", "bank details", "messages__schemeTaskList__completed")
    ).foreach { case (index, aboutType, msg) =>

      s"display the about $aboutType section with correct link" in {

        view must haveLinkWithText(
          url = schemeDetailsTaskListData().about(index.toInt).link.target,
          linkText = schemeDetailsTaskListData().about(index.toInt).link.text,
          linkId = s"section-about-link-$index"
        )
      }

      s"display the about $aboutType section with correct status" in {
        val view = createView(schemeDetailsTaskListData())
        val doc = asDocument(view())
        doc.getElementById(s"section-about-status-$index").ownText mustBe messages(msg)
      }

      s"display the about $aboutType section with correct status and visually hidden text" in {
        val view = createView(schemeDetailsTaskListData())
        val doc = asDocument(view())
        doc.getElementById(s"section-about-status-$index").text() mustBe messages("messages__schemeTaskList__status_visuallyHidden") + " " + messages(msg)
      }
    }
  }

  "SchemeTaskListView Establishers section" when {

    "no establishers" should {
      val journeyTaskListNoEstablisher: SchemeDetailsTaskList = SchemeDetailsTaskList(beforeYouStartSection, "test", Seq.empty, None,
        Some(SchemeDetailsTaskListHeader(
          None,
          Some(Link(messages("messages__schemeTaskList__sectionEstablishers_add_link"),
            controllers.register.establishers.routes.EstablisherKindController.onPageLoad(NormalMode, 0, None).url)),
          None
        )), Seq.empty,
        Some(SchemeDetailsTaskListHeader(
          None,
          Some(Link(messages("messages__schemeTaskList__sectionTrustees_add_link"),
            controllers.register.trustees.routes.TrusteeKindController.onPageLoad(NormalMode, 0, None).url)),
          None
        )), Seq.empty, None, "h1", "h2", None, "pageTitle", None
      )
      val view = createView(journeyTaskListNoEstablisher)

      "display correct header" in {

        val doc = asDocument(view())
        assertRenderedByIdWithText(doc, id = "section-establishers-header", text = messages("messages__schemeTaskList__sectionEstablishers_header"))
      }

      "display the correct link" in {

        view must haveLinkWithText(
          url = controllers.register.establishers.routes.EstablisherKindController.onPageLoad(NormalMode, 0, None).url,
          linkText = messages("messages__schemeTaskList__sectionEstablishers_add_link"),
          linkId = "section-establishers-link"
        )
      }
    }

    "establishers defined and not completed" should {

      val view = createView(schemeDetailsTaskListData())

      "display the correct link" in {

        view must haveLinkWithText(
          url = controllers.register.establishers.routes.AddEstablisherController.onPageLoad(NormalMode, None).url,
          linkText = messages("messages__schemeTaskList__sectionEstablishers_change_link"),
          linkId = "section-establishers-link"
        )
      }

      Seq(("0", "messages__schemeTaskList__inProgress"), ("1", "messages__schemeTaskList__completed")).foreach { case (index, msg) =>

        s"display the first establisher section with correct link and status for item no $index" in {

          view must haveLinkWithText(
            url = schemeDetailsTaskListData().establishers(index.toInt).entities.head.link.target,
            linkText = schemeDetailsTaskListData().establishers(index.toInt).entities.head.link.text,
            linkId = s"section-establishers-link-$index-0"
          )
        }

        s"display the first establisher section with correct status of in progress for item no $index" in {
          val view = createView(schemeDetailsTaskListData())
          val doc = asDocument(view())
          doc.getElementById(s"section-establishers-status-$index-0").ownText() mustBe messages(msg)
        }

        s"display the first establisher section with correct status of in progress for item no $index with visually hidden text" in {
          val view = createView(schemeDetailsTaskListData())
          val doc = asDocument(view())
          doc.getElementById(s"section-establishers-status-$index-0").text() mustBe messages("messages__schemeTaskList__status_visuallyHidden") + " " + messages(msg)
        }
      }

    }

    "establisherCompany" should {

      val view = createView(schemeDetailsTaskListData(establishers = establisherCompany))

      "display the correct link" in {

        view must haveLinkWithText(
          url = controllers.register.establishers.routes.AddEstablisherController.onPageLoad(NormalMode, None).url,
          linkText = messages("messages__schemeTaskList__sectionEstablishers_change_link"),
          linkId = "section-establishers-link"
        )
      }

        s"display the establisher company section with correct links" in {

          for(i <- 0 to 3) {

            view must haveLinkWithText(
              url = schemeDetailsTaskListData(establishers = establisherCompany).establishers(0).entities(i).link.target,
              linkText = schemeDetailsTaskListData(establishers = establisherCompany).establishers(0).entities(i).link.text,
              linkId = s"section-establishers-link-0-$i"
            )
          }

          view must haveLinkWithText(
            url = schemeDetailsTaskListData(establishers = establisherCompany).establishers(1).entities(0).link.target,
            linkText = schemeDetailsTaskListData(establishers = establisherCompany).establishers(1).entities(0).link.text,
            linkId = s"section-establishers-link-1-0"
          )
        }
      }

  }

  "SchemeTaskListView Trustees section" should {

    "no trustees" should {

      def journeyTaskListNoTrustees(text: Option[String] = None): SchemeDetailsTaskList =
        SchemeDetailsTaskList(beforeYouStartSection, "test", Seq.empty, None,
        Some(SchemeDetailsTaskListHeader(
          None,
          Some(Link(messages("messages__schemeTaskList__sectionEstablishers_add_link"),
            controllers.register.establishers.routes.EstablisherKindController.onPageLoad(NormalMode, 0, None).url)),
          None)), Seq.empty,
        Some(SchemeDetailsTaskListHeader(
          None,
          Some(Link(messages("messages__schemeTaskList__sectionTrustees_add_link"),
            controllers.register.trustees.routes.TrusteeKindController.onPageLoad(NormalMode, 0, None).url)),
          None,
          text)), Seq.empty, None, "h1", "h2", None, "pageTitle",
        None
      )

      val view = createView(journeyTaskListNoTrustees())

      "display correct header" in {

        val doc = asDocument(view())
        assertRenderedByIdWithText(doc, id = "section-trustees-header", text = messages("messages__schemeTaskList__sectionTrustees_header"))
      }

      "display the correct link" in {
        val doc = asDocument(view())
        view must haveLinkWithText(
          url = controllers.register.trustees.routes.TrusteeKindController.onPageLoad(NormalMode, 0, None).url,
          linkText = messages("messages__schemeTaskList__sectionTrustees_add_link"),
          linkId = "section-trustees-link"
        )
        assertNotRenderedById(doc, id = "section-trustees-header-additional-text")
      }

      "display additional text" in {
        val view = createView(journeyTaskListNoTrustees(Some("additional text")))
        val doc = asDocument(view())
        assertRenderedByIdWithText(doc, id = "section-trustees-header-additional-text", text = "additional text")
      }

    }

    "trustees defined and not completed" should {

      val view = createView(schemeDetailsTaskListData())

      "display the add link" in {

        view must haveLinkWithText(
          url = controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode, None).url,
          linkText = messages("messages__schemeTaskList__sectionTrustees_change_link"),
          linkId = "section-trustees-link"
        )
      }

      Seq(("0", "messages__schemeTaskList__inProgress"), ("1", "messages__schemeTaskList__completed")).foreach { case (index, msg) =>

        s"display the first trustee section with correct link and status for item no $index" in {

          view must haveLinkWithText(
            url = schemeDetailsTaskListData().trustees(index.toInt).entities.head.link.target,
            linkText = schemeDetailsTaskListData().trustees(index.toInt).entities.head.link.text,
            linkId = s"section-trustees-link-$index-0"
          )
        }

        s"display the first trustee section with correct status of in progress for item no $index" in {
          val view = createView(schemeDetailsTaskListData())
          val doc = asDocument(view())

          doc.getElementById(s"section-trustees-status-$index-0").ownText() mustBe messages(msg)
        }

        s"display the first trustee section with correct status of in progress for item no $index with visually hidden text" in {
          val view = createView(schemeDetailsTaskListData())
          val doc = asDocument(view())

          doc.getElementById(s"section-trustees-status-$index-0").text() mustBe messages("messages__schemeTaskList__status_visuallyHidden") + " " + messages(msg)
        }
      }
    }

    "trusteeCompany" should {

      val view = createView(schemeDetailsTaskListData(trustees = trusteeCompany))

      "display the correct link" in {

        view must haveLinkWithText(
          url = controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode, None).url,
          linkText = messages("messages__schemeTaskList__sectionTrustees_change_link"),
          linkId = "section-trustees-link"
        )
      }

      s"display the trustee company section with correct links" in {

        for(i <- 0 to 2) {

          view must haveLinkWithText(
            url = schemeDetailsTaskListData(trustees = trusteeCompany).trustees(0).entities(i).link.target,
            linkText = schemeDetailsTaskListData(trustees = trusteeCompany).trustees(0).entities(i).link.text,
            linkId = s"section-trustees-link-0-$i"
          )
        }

        view must haveLinkWithText(
          url = schemeDetailsTaskListData(trustees = trusteeCompany).trustees(1).entities(0).link.target,
          linkText = schemeDetailsTaskListData(trustees = trusteeCompany).trustees(1).entities(0).link.text,
          linkId = s"section-trustees-link-1-0"
        )
      }
    }
  }

  "SchemeTaskListView Declaration section" should {
    "not display where no declaration section in view model" in {
      val doc = asDocument(createView(schemeDetailsTaskListData())())
      assertNotRenderedById(doc, id = "section-declaration-header")
    }

    "display correct heading where there is a declaration section in view model" in {
      val doc = asDocument(createView(schemeDetailsTaskListData()
        .copy(declaration = Some(SchemeDetailsTaskListDeclarationSection("messages__schemeTaskList__sectionDeclaration_header", None))))())
      assertRenderedByIdWithText(doc, id = "section-declaration-header", text = messages("messages__schemeTaskList__sectionDeclaration_header"))
    }

    "display correct text for the Declaration section where there is a declaration section but no link" in {
      val doc = asDocument(createView(schemeDetailsTaskListData()
        .copy(declaration = Some(SchemeDetailsTaskListDeclarationSection("messages__schemeTaskList__sectionDeclaration_header", None))))())
      assertNotRenderedById(doc, id = "section-declaration-link")
    }

    "display correct link and no text for the Declaration section where there is a declaration section and a link" in {
      val completed = schemeDetailsTaskListData().copy(declaration =
        Some(SchemeDetailsTaskListDeclarationSection("messages__schemeTaskList__sectionDeclaration_header",
          Some(Link(text = "text", target = "target")))))
      val doc = asDocument(createView(completed)())

      assertNotRenderedById(doc, id = "section-declaration-text")
      createView(completed) must haveLinkWithText(
        url = "target",
        linkText = "text",
        linkId = "section-declaration-link"
      )
    }
  }

  private def createView(schemeDetailsList: SchemeDetailsTaskList = schemeDetailsTaskListData()): () => HtmlFormat.Appendable = () =>
    schemeDetailsTaskList(frontendAppConfig, schemeDetailsList)(fakeRequest, messages)


}

object SchemeDetailsTaskListViewSpec extends ViewSpecBase {
  private lazy val beforeYouStartLinkText = messages("messages__schemeTaskList__before_you_start_link_text")
  private lazy val aboutMembersLinkText = messages("messages__schemeTaskList__about_members_link_text")
  private lazy val aboutBenefitsAndInsuranceLinkText = messages("messages__schemeTaskList__about_benefits_and_insurance_link_text")
  private lazy val aboutBankDetailsLinkText = messages("messages__schemeTaskList__about_bank_details_link_text")
  private lazy val workingKnowledgeLinkText = messages("messages__schemeDetailsTaskList__working_knowledge_link_text")
  private lazy val changeEstablisherLinkText = messages("messages__schemeTaskList__sectionEstablishers_change_link")
  private lazy val individualLinkText = messages("messages__schemeTaskList__individual_link")
  private lazy val changeTrusteesLinkText = messages("messages__schemeTaskList__sectionTrustees_change_link")
  private val testAboutHeader = "testabout"
  private val pageHeader = messages("messages__schemeTaskList__title")
  private val messageKeyPrefix = "schemeTaskList"

  private def schemeDetailsTaskListData(srn: Option[String] = None,
                                        establishers: Seq[SchemeDetailsTaskListEntitySection] = establishers,
                                        trustees: Seq[SchemeDetailsTaskListEntitySection] = trustees
                                       ): SchemeDetailsTaskList = SchemeDetailsTaskList(
    beforeYouStartSection, testAboutHeader, aboutSection, Some(wkSection),
    Some(addEstablisherHeader()), establishers, Some(addTrusteesHeader()),
    trustees, None, "h1", "h2", Some("h3"), "pageTitle", srn)

  private def beforeYouStartSection: SchemeDetailsTaskListSection = {
    SchemeDetailsTaskListSection(Some(false),
      Link(
        messages(beforeYouStartLinkText),
        controllers.routes.SchemeNameController.onPageLoad(NormalMode).url
      ), None)
  }

  private def aboutSection: Seq[SchemeDetailsTaskListSection] = {
    Seq(
      SchemeDetailsTaskListSection(Some(false), Link(aboutMembersLinkText,
        controllers.routes.WhatYouWillNeedMembersController.onPageLoad().url), None),
      SchemeDetailsTaskListSection(Some(true), Link(aboutBenefitsAndInsuranceLinkText,
        controllers.routes.WhatYouWillNeedBenefitsInsuranceController.onPageLoad().url), None),
      SchemeDetailsTaskListSection(Some(true), Link(aboutBankDetailsLinkText,
        controllers.routes.WhatYouWillNeedBankDetailsController.onPageLoad().url), None)
    )
  }

  private def wkSection: SchemeDetailsTaskListSection = {
    SchemeDetailsTaskListSection(Some(false), Link(workingKnowledgeLinkText,
      controllers.routes.WhatYouWillNeedWorkingKnowledgeController.onPageLoad().url), None)
  }

  private def addEstablisherHeader(): SchemeDetailsTaskListHeader = {
    SchemeDetailsTaskListHeader(None, Some(Link(changeEstablisherLinkText,
      controllers.register.establishers.routes.AddEstablisherController.onPageLoad(NormalMode, None).url)), None)
  }

  private def addTrusteesHeader(): SchemeDetailsTaskListHeader = {
    SchemeDetailsTaskListHeader(None, Some(Link(changeTrusteesLinkText,
      controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode, None).url)), None)
  }

  private def establisherCompany: Seq[SchemeDetailsTaskListEntitySection] = {
    Seq(SchemeDetailsTaskListEntitySection(Some(false),
      Seq(
        EntitySpoke(Link(messages("messages__schemeTaskList__add_details", "test company"),
          establisherCompanyRoutes.WhatYouWillNeedCompanyDetailsController.onPageLoad(NormalMode, None, 0).url), None),
        EntitySpoke(Link(messages("messages__schemeTaskList__add_address", "test company"),
          establisherCompanyRoutes.WhatYouWillNeedCompanyAddressController.onPageLoad(NormalMode, None, 0).url), None),
        EntitySpoke(Link(messages("messages__schemeTaskList__add_contact", "test company"),
          establisherCompanyRoutes.WhatYouWillNeedCompanyContactDetailsController.onPageLoad(NormalMode, None, 0).url), None),
        EntitySpoke(Link(messages("messages__schemeTaskList__add_directors", "test company"),
          establisherCompanyRoutes.AddCompanyDirectorsController.onPageLoad(NormalMode, None, 0).url, None))
      ),
      Some("test company")),

      SchemeDetailsTaskListEntitySection(Some(true), Seq(EntitySpoke(Link(individualLinkText,
        controllers.register.establishers.individual.routes.CheckYourAnswersDetailsController.onPageLoad(NormalMode, 1, None).url),
        Some(true))),
        Some("firstName lastName")))
  }

  private def trusteeCompany: Seq[SchemeDetailsTaskListEntitySection] = {
    Seq(SchemeDetailsTaskListEntitySection(Some(false),
      Seq(
        EntitySpoke(Link(messages("messages__schemeTaskList__add_details", "test company"),
          trusteeCompanyRoutes.WhatYouWillNeedCompanyDetailsController.onPageLoad(NormalMode, 0, None).url), None),
        EntitySpoke(Link(messages("messages__schemeTaskList__add_address", "test company"),
          trusteeCompanyRoutes.WhatYouWillNeedCompanyAddressController.onPageLoad(NormalMode, 0, None).url), None),
        EntitySpoke(Link(messages("messages__schemeTaskList__add_contact", "test company"),
          trusteeCompanyRoutes.WhatYouWillNeedCompanyContactDetailsController.onPageLoad(NormalMode, 0, None).url), None)
      ),
      Some("test company")),

      SchemeDetailsTaskListEntitySection(Some(true), Seq(EntitySpoke(Link(individualLinkText,
        controllers.register.trustees.individual.routes.CheckYourAnswersIndividualDetailsController.onPageLoad(NormalMode, 1, None).url),
        Some(true))),
        Some("firstName lastName")))
  }

  private def establishers: Seq[SchemeDetailsTaskListEntitySection] = {
    Seq(SchemeDetailsTaskListEntitySection(Some(false), Seq(EntitySpoke(Link(individualLinkText,
      controllers.register.establishers.individual.routes.EstablisherNameController.onPageLoad(NormalMode, 0, None).url),
      Some(false))),
      Some("firstName lastName")),

      SchemeDetailsTaskListEntitySection(Some(true), Seq(EntitySpoke(Link(individualLinkText,
        controllers.register.establishers.individual.routes.CheckYourAnswersDetailsController.onPageLoad(NormalMode, 1, None).url),
        Some(true))),
        Some("firstName lastName")))
  }

  private def trustees: Seq[SchemeDetailsTaskListEntitySection] = {
    Seq(SchemeDetailsTaskListEntitySection(Some(false), Seq(EntitySpoke(Link(individualLinkText,
      controllers.register.trustees.individual.routes.TrusteeNameController.onPageLoad(NormalMode, 0, None).url),
      Some(false))),
      Some("firstName lastName")),
      SchemeDetailsTaskListEntitySection(Some(true), Seq(EntitySpoke(Link(individualLinkText,
        controllers.register.trustees.individual.routes.CheckYourAnswersIndividualDetailsController.onPageLoad(NormalMode, 0, None).url),
        Some(true))),
        Some("firstName lastName")))
  }

}


