/*
 * Copyright 2020 HM Revenue & Customs
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
import models.{EntitySpoke, NormalMode, TaskListLink}
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
      doc.getElementById(statusId).text() mustBe
        messages("messages__schemeTaskList__status_visuallyHidden") + " " + messages("messages__schemeTaskList__inProgress")
    }

    "display the correct status if completed" in {
      val doc = asDocument(createView(completed)())
      doc.getElementById(statusId).ownText() mustBe messages("messages__schemeTaskList__completed")
    }

    "display the correct visually hidden text before status if completed" in {
      val doc = asDocument(createView(completed)())
      doc.getElementById(statusId).text() mustBe
        messages("messages__schemeTaskList__status_visuallyHidden") + " " + messages("messages__schemeTaskList__completed")
    }
  }

  "SchemeDetailsTaskListView when in registration" must {

    behave like normalPageWithTitle(createView(), messageKeyPrefix,
      messages("messages__schemeTaskList__title"), schemeDetailsTaskListData().h1)
  }

  "SchemeDetailsTaskListView when in variation" must {

    behave like normalPageWithTitle(createView(schemeDetailsTaskListData().copy(srn = Some("test-srn"))),
      messageKeyPrefix, messages("messages__scheme_details__title"), schemeDetailsTaskListData().h1)
  }

  "SchemeDetailsTaskListView" must {

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

  "SchemeTaskListView Before start section" must {

    val notStarted = schemeDetailsTaskListData().copy(beforeYouStart = beforeYouStartSection(None))
    val inProgress = schemeDetailsTaskListData().copy(beforeYouStart = beforeYouStartSection(Some(false)))
    val completed = schemeDetailsTaskListData().copy(beforeYouStart = beforeYouStartSection(Some(true)))

    behave like simpleSection(
      linkId = "section-before-you-start-link",
      linkUrl = beforeYouStartSection().entities.head.link.target,
      linkText = beforeYouStartSection().entities.head.link.text,
      statusId = "section-beforeYouStart-status",
      notStarted = notStarted,
      inProgress = inProgress,
      completed = completed)

    "display correct before you start header" in {
      val doc = asDocument(createView(notStarted)())
      assertRenderedByIdWithText(doc, id = "section-before-you-start-header", text = messages("messages__scheme_details__title"))
    }

    "display correct h3" in {
      val doc = asDocument(createView(notStarted)())
      assertRenderedByIdWithText(doc, id = "section-information-h3", text = "h3")
    }
  }

  "SchemeTaskListView Working knowledge of pensions section" must {
    val notStarted = schemeDetailsTaskListData().copy(workingKnowledge = Some(wkSection(None)))
    val inProgress = schemeDetailsTaskListData().copy(workingKnowledge = Some(wkSection(Some(false))))
    val completed = schemeDetailsTaskListData().copy(workingKnowledge = Some(wkSection(Some(true))))

    behave like simpleSection(
      linkId = "section-working-knowledge-link",
      linkUrl = wkSection().entities.head.link.target,
      linkText = wkSection().entities.head.link.text,
      statusId = "section-working-knowledge-status",
      notStarted = notStarted,
      inProgress = inProgress,
      completed = completed)
  }

  "SchemeTaskListView About section" must {
    val view = createView(schemeDetailsTaskListData())

    "display correct header" in {
      val doc = asDocument(view())
      assertRenderedByIdWithText(doc, id = "section-about-header", text = "about header")
    }


    Seq(("0", "members", "messages__schemeTaskList__inProgress"), ("1", "benefits and insurance", "messages__schemeTaskList__completed"),
      ("2", "bank details", "messages__schemeTaskList__completed")
    ).foreach { case (index, aboutType, msg) =>

      s"display the about $aboutType section with correct link" in {

        view must haveLinkWithText(
          url = schemeDetailsTaskListData().about.entities(index.toInt).link.target,
          linkText = schemeDetailsTaskListData().about.entities(index.toInt).link.text,
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

    "no establishers" must {
      val journeyTaskListNoEstablisher: SchemeDetailsTaskList = SchemeDetailsTaskList("h1", None, beforeYouStartSection(),
        SchemeDetailsTaskListEntitySection(None, Nil, None), None,
        Some(SchemeDetailsTaskListEntitySection(
          None,
          Seq(EntitySpoke(TaskListLink(messages("messages__schemeTaskList__sectionEstablishers_add_link"),
            controllers.register.establishers.routes.EstablisherKindController.onPageLoad(NormalMode, 0, None).url))),
          None
        )), Seq.empty,
        Some(SchemeDetailsTaskListEntitySection(
          None,
          Seq(EntitySpoke(TaskListLink(messages("messages__schemeTaskList__sectionTrustees_add_link"),
            controllers.register.trustees.routes.TrusteeKindController.onPageLoad(NormalMode, 0, None).url))),
          None
        )), Seq.empty, None)
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

    "establishers defined and not completed" must {

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

    "establisherCompanyEntity" must {

      val view = createView(schemeDetailsTaskListData(establishers = establisherCompanyEntity))

      "display the correct link" in {

        view must haveLinkWithText(
          url = controllers.register.establishers.routes.AddEstablisherController.onPageLoad(NormalMode, None).url,
          linkText = messages("messages__schemeTaskList__sectionEstablishers_change_link"),
          linkId = "section-establishers-link"
        )
      }

      s"display the establisher company section with correct links" in {

        for (i <- 0 to 3) {

          view must haveLinkWithText(
            url = schemeDetailsTaskListData(establishers = establisherCompanyEntity).establishers.head.entities(i).link.target,
            linkText = schemeDetailsTaskListData(establishers = establisherCompanyEntity).establishers.head.entities(i).link.text,
            linkId = s"section-establishers-link-0-$i"
          )
        }

        view must haveLinkWithText(
          url = schemeDetailsTaskListData(establishers = establisherCompanyEntity).establishers(1).entities.head.link.target,
          linkText = schemeDetailsTaskListData(establishers = establisherCompanyEntity).establishers(1).entities.head.link.text,
          linkId = s"section-establishers-link-1-0"
        )
      }
    }

  }

  "SchemeTaskListView Trustees section" must {

    "no trustees" must {

      def journeyTaskListNoTrustees(text: Option[String] = None): SchemeDetailsTaskList =
        SchemeDetailsTaskList("h1", None, beforeYouStartSection(),
          SchemeDetailsTaskListEntitySection(None, Nil, None), None,
          Some(SchemeDetailsTaskListEntitySection(None,
            Seq(EntitySpoke(TaskListLink(Message("messages__schemeTaskList__sectionEstablishers_add_link"),
              controllers.register.establishers.routes.EstablisherKindController.onPageLoad(NormalMode, 0, None).url))),
            None)
          ), Seq.empty,
          Some(SchemeDetailsTaskListEntitySection(
            None,
            Seq(EntitySpoke(TaskListLink(Message("messages__schemeTaskList__sectionTrustees_add_link"),
              controllers.register.trustees.routes.TrusteeKindController.onPageLoad(NormalMode, 0, None).url))),
            None,
            Message(text.getOrElse("")))), Seq.empty, None
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
        assertRenderedByIdWithText(doc, id = "section-trustees-header-additional-text-0", text = "additional text")
      }

    }

    "trustees defined and not completed" must {

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

    "trusteeCompanyEntity" must {

      val view = createView(schemeDetailsTaskListData(trustees = trusteeCompanyEntity))

      "display the correct link" in {

        view must haveLinkWithText(
          url = controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode, None).url,
          linkText = messages("messages__schemeTaskList__sectionTrustees_change_link"),
          linkId = "section-trustees-link"
        )
      }

      s"display the trustee company section with correct links" in {

        for (i <- 0 to 2) {

          view must haveLinkWithText(
            url = schemeDetailsTaskListData(trustees = trusteeCompanyEntity).trustees.head.entities(i).link.target,
            linkText = schemeDetailsTaskListData(trustees = trusteeCompanyEntity).trustees.head.entities(i).link.text,
            linkId = s"section-trustees-link-0-$i"
          )
        }

        view must haveLinkWithText(
          url = schemeDetailsTaskListData(trustees = trusteeCompanyEntity).trustees(1).entities.head.link.target,
          linkText = schemeDetailsTaskListData(trustees = trusteeCompanyEntity).trustees(1).entities.head.link.text,
          linkId = s"section-trustees-link-1-0"
        )
      }
    }
  }

  "SchemeTaskListView Declaration section" must {
    "not display where no declaration section in view model" in {
      val doc = asDocument(createView(schemeDetailsTaskListData())())
      assertNotRenderedById(doc, id = "section-declaration-header")
    }

    "display correct heading where there is a declaration section in view model" in {
      val doc = asDocument(createView(schemeDetailsTaskListData()
        .copy(declaration = Some(SchemeDetailsTaskListEntitySection(None, Nil, Some("messages__schemeTaskList__sectionDeclaration_header")))))())
      assertRenderedByIdWithText(doc, id = "section-declaration-header", text = messages("messages__schemeTaskList__sectionDeclaration_header"))
    }

    "display correct text for the Declaration section where there is a declaration section but no link" in {
      val doc = asDocument(createView(schemeDetailsTaskListData()
        .copy(declaration = Some(SchemeDetailsTaskListEntitySection(None, Nil, Some("messages__schemeTaskList__sectionDeclaration_header")))))())
      assertNotRenderedById(doc, id = "section-declaration-link")
    }

    "display correct link and no text for the Declaration section where there is a declaration section and a link" in {
      val completed = schemeDetailsTaskListData().copy(declaration =
        Some(SchemeDetailsTaskListEntitySection(None, Seq(EntitySpoke(TaskListLink(text = "text", target = "target"))), Some("messages__schemeTaskList__sectionDeclaration_header"))))
      val doc = asDocument(createView(completed)())

      assertNotRenderedById(doc, id = "section-declaration-text")
      createView(completed) must haveLinkWithText(
        url = "target",
        linkText = "text",
        linkId = "section-declaration-link"
      )
    }
  }

  val view: schemeDetailsTaskList = app.injector.instanceOf[schemeDetailsTaskList]

  private def createView(schemeDetailsList: SchemeDetailsTaskList = schemeDetailsTaskListData()): () => HtmlFormat.Appendable = () =>
    view(schemeDetailsList)(fakeRequest, messages)


}

object SchemeDetailsTaskListViewSpec extends ViewSpecBase {
  private lazy val beforeYouStartLinkText = Message("messages__schemeTaskList__before_you_start_link_text")
  private lazy val aboutMembersLinkText = Message("messages__schemeTaskList__about_members_link_text")
  private lazy val aboutBenefitsAndInsuranceLinkText = Message("messages__schemeTaskList__about_benefits_and_insurance_link_text")
  private lazy val aboutBankDetailsLinkText = Message("messages__schemeTaskList__about_bank_details_link_text")
  private lazy val workingKnowledgeLinkText = Message("messages__schemeDetailsTaskList__working_knowledge_link_text")
  private lazy val changeEstablisherLinkText = Message("messages__schemeTaskList__sectionEstablishers_change_link")
  private lazy val individualLinkText = Message("messages__schemeTaskList__individual_link")
  private lazy val changeTrusteesLinkText = Message("messages__schemeTaskList__sectionTrustees_change_link")
  private val testAboutHeader = "testabout"
  private val pageHeader = Message("messages__schemeTaskList__title")
  private val messageKeyPrefix = "schemeTaskList"

  private def schemeDetailsTaskListData(srn: Option[String] = None,
                                        establishers: Seq[SchemeDetailsTaskListEntitySection] = establishers,
                                        trustees: Seq[SchemeDetailsTaskListEntitySection] = trustees
                                       ): SchemeDetailsTaskList = SchemeDetailsTaskList("h1", srn,
    beforeYouStartSection(), aboutSection, Some(wkSection()),
    Some(addEstablisherHeader()), establishers, Some(addTrusteesHeader()),
    trustees, None)

  private def beforeYouStartSection(isCompleted: Option[Boolean] = Some(false)): SchemeDetailsTaskListEntitySection = {
    SchemeDetailsTaskListEntitySection(None, Seq(
      EntitySpoke(TaskListLink(beforeYouStartLinkText,
        controllers.routes.SchemeNameController.onPageLoad(NormalMode).url), isCompleted
      )), Some("h3"))
  }

  private def aboutSection: SchemeDetailsTaskListEntitySection = {
    SchemeDetailsTaskListEntitySection(None, Seq(
      EntitySpoke(TaskListLink(aboutMembersLinkText,
        controllers.routes.WhatYouWillNeedMembersController.onPageLoad().url), Some(false)),
      EntitySpoke(TaskListLink(aboutBenefitsAndInsuranceLinkText,
        controllers.routes.WhatYouWillNeedBenefitsInsuranceController.onPageLoad().url), Some(true)),
      EntitySpoke(TaskListLink(aboutBankDetailsLinkText,
        controllers.routes.WhatYouWillNeedBankDetailsController.onPageLoad().url), Some(true))), Some("about header"))
  }

  private def wkSection(isCompleted: Option[Boolean] = Some(false)): SchemeDetailsTaskListEntitySection = {
    SchemeDetailsTaskListEntitySection(None, Seq(EntitySpoke(TaskListLink(workingKnowledgeLinkText,
      controllers.routes.WhatYouWillNeedWorkingKnowledgeController.onPageLoad().url), isCompleted)), None)
  }

  private def addEstablisherHeader(): SchemeDetailsTaskListEntitySection = {
    SchemeDetailsTaskListEntitySection(None, Seq(EntitySpoke(TaskListLink(changeEstablisherLinkText,
      controllers.register.establishers.routes.AddEstablisherController.onPageLoad(NormalMode, None).url))), None)
  }

  private def addTrusteesHeader(): SchemeDetailsTaskListEntitySection = {
    SchemeDetailsTaskListEntitySection(None, Seq(EntitySpoke(TaskListLink(changeTrusteesLinkText,
      controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode, None).url))), None)
  }

  private def establisherCompanyEntity: Seq[SchemeDetailsTaskListEntitySection] = {
    Seq(SchemeDetailsTaskListEntitySection(Some(false),
      Seq(
        EntitySpoke(TaskListLink(messages("messages__schemeTaskList__add_details", "test company"),
          establisherCompanyRoutes.WhatYouWillNeedCompanyDetailsController.onPageLoad(NormalMode, None, 0).url), None),
        EntitySpoke(TaskListLink(messages("messages__schemeTaskList__add_address", "test company"),
          establisherCompanyRoutes.WhatYouWillNeedCompanyAddressController.onPageLoad(NormalMode, None, 0).url), None),
        EntitySpoke(TaskListLink(messages("messages__schemeTaskList__add_contact", "test company"),
          establisherCompanyRoutes.WhatYouWillNeedCompanyContactDetailsController.onPageLoad(NormalMode, None, 0).url), None),
        EntitySpoke(TaskListLink(messages("messages__schemeTaskList__add_directors", "test company"),
          establisherCompanyRoutes.AddCompanyDirectorsController.onPageLoad(NormalMode, None, 0).url, None))
      ),
      Some("test company")),

      SchemeDetailsTaskListEntitySection(Some(true), Seq(EntitySpoke(TaskListLink(individualLinkText,
        controllers.register.establishers.individual.routes.CheckYourAnswersDetailsController.onPageLoad(NormalMode, 1, None).url),
        Some(true))),
        Some("firstName lastName")))
  }

  private def trusteeCompanyEntity: Seq[SchemeDetailsTaskListEntitySection] = {
    Seq(SchemeDetailsTaskListEntitySection(Some(false),
      Seq(
        EntitySpoke(TaskListLink(messages("messages__schemeTaskList__add_details", "test company"),
          trusteeCompanyRoutes.WhatYouWillNeedCompanyDetailsController.onPageLoad(NormalMode, 0, None).url), None),
        EntitySpoke(TaskListLink(messages("messages__schemeTaskList__add_address", "test company"),
          trusteeCompanyRoutes.WhatYouWillNeedCompanyAddressController.onPageLoad(NormalMode, 0, None).url), None),
        EntitySpoke(TaskListLink(messages("messages__schemeTaskList__add_contact", "test company"),
          trusteeCompanyRoutes.WhatYouWillNeedCompanyContactDetailsController.onPageLoad(NormalMode, 0, None).url), None)
      ),
      Some("test company")),

      SchemeDetailsTaskListEntitySection(Some(true), Seq(EntitySpoke(TaskListLink(individualLinkText,
        controllers.register.trustees.individual.routes.CheckYourAnswersIndividualDetailsController.onPageLoad(NormalMode, 1, None).url),
        Some(true))),
        Some("firstName lastName")))
  }

  private def establishers: Seq[SchemeDetailsTaskListEntitySection] = {
    Seq(SchemeDetailsTaskListEntitySection(Some(false), Seq(EntitySpoke(TaskListLink(individualLinkText,
      controllers.register.establishers.individual.routes.EstablisherNameController.onPageLoad(NormalMode, 0, None).url),
      Some(false))),
      Some("firstName lastName")),

      SchemeDetailsTaskListEntitySection(Some(true), Seq(EntitySpoke(TaskListLink(individualLinkText,
        controllers.register.establishers.individual.routes.CheckYourAnswersDetailsController.onPageLoad(NormalMode, 1, None).url),
        Some(true))),
        Some("firstName lastName")))
  }

  private def trustees: Seq[SchemeDetailsTaskListEntitySection] = {
    Seq(SchemeDetailsTaskListEntitySection(Some(false), Seq(EntitySpoke(TaskListLink(individualLinkText,
      controllers.register.trustees.individual.routes.TrusteeNameController.onPageLoad(NormalMode, 0, None).url),
      Some(false))),
      Some("firstName lastName")),
      SchemeDetailsTaskListEntitySection(Some(true), Seq(EntitySpoke(TaskListLink(individualLinkText,
        controllers.register.trustees.individual.routes.CheckYourAnswersIndividualDetailsController.onPageLoad(NormalMode, 0, None).url),
        Some(true))),
        Some("firstName lastName")))
  }

}


