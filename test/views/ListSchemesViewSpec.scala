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

import config.FrontendAppConfig
import models.SchemeDetail
import play.api.i18n.Messages
import play.api.mvc.Request
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.list_schemes

class ListSchemesViewSpec extends ViewSpecBase with ViewBehaviours{

  import ListSchemesViewSpec._

  implicit val request: Request[_] = fakeRequest

  "list-schemes view" must {

    behave like normalPage(view(frontendAppConfig), "listSchemes", messages("messages__listSchemes__title"))

    behave like pageWithBackLink(view(frontendAppConfig))

    "display a suitable message when there are no schemes to display" in {
      view(frontendAppConfig) must haveElementWithText("noSchemes", messages("messages__listSchemes__noSchemes"))
    }

    "display a link to register a new scheme when there are no schemes to display" in {
      view(frontendAppConfig) must haveLink(controllers.routes.WhatYouWillNeedController.onPageLoad().url, "registerNewScheme")
    }

    "display the correct column headers when there are schemes to display" in {
      val actual = view(frontendAppConfig, fullList)

      actual must haveElementWithText("schemeName", messages("messages__listSchemes__column_schemeName"))
      actual must haveElementWithText("srn", messages("messages__listSchemes__column_srn"))
      actual must haveElementWithText("pstr", messages("messages__listSchemes__column_pstr"))
      actual must haveElementWithText("schemeStatus", messages("messages__listSchemes__column_status"))
    }

    "display the correct rows when there are schemes to display" in {
      val actual = view(frontendAppConfig, fullList)

      //table 1
      actual must haveElementWithText(s"schemeName-1-0", "scheme-name-4")
      actual must haveElementWithText(s"srn-1-0", "reference-number-4")

      actual must haveElementWithText(s"schemeName-1-1", "scheme-name-5")
      actual must haveElementWithText(s"srn-1-1", "reference-number-5")

      actual must haveElementWithText(s"schemeName-1-2", "scheme-name-6")
      actual must haveElementWithText(s"srn-1-2", "reference-number-6")

      //table 2
      actual must haveElementWithText(s"schemeName-2-0", "scheme-name-0")
      actual must haveElementWithText(s"srn-2-0", "reference-number-0")

      actual must haveElementWithText(s"schemeName-2-1", "scheme-name-1")
      actual must haveElementWithText(s"srn-2-1", "reference-number-1")

      actual must haveElementWithText(s"schemeName-2-2", "scheme-name-2")
      actual must haveElementWithText(s"srn-2-2", "reference-number-2")

      actual must haveElementWithText(s"schemeName-2-3", "scheme-name-3")
      actual must haveElementWithText(s"srn-2-3", "reference-number-3")

      actual must haveElementWithText(s"schemeName-2-4", "scheme-name-7")
      actual must haveElementWithText(s"srn-2-4", "reference-number-7")
    }

    "display either PSTR or 'Not assigned' when there is no value" in {
      val actual = view(frontendAppConfig, fullList)

      //table 1
      actual must haveElementWithText("pstr-1-0", "PSTR-4")
      actual must haveElementWithText("pstr-1-1", "PSTR-5")
      actual must haveElementWithText("pstr-1-2", "PSTR-6")

      //table 2
      actual must haveElementWithText("pstr-2-0", "Not assigned")
      actual must haveElementWithText("pstr-2-1", "Not assigned")
      actual must haveElementWithText("pstr-2-2", "Not assigned")
      actual must haveElementWithText("pstr-2-3", "Not assigned")
      actual must haveElementWithText("pstr-2-4", "Not assigned")
    }

    "display the full status value" in {
      val actual = view(frontendAppConfig, fullList)

      //table 1
      actual must haveElementWithText("schemeStatus-1-0", "Open")
      actual must haveElementWithText("schemeStatus-1-1", "De-registered")
      actual must haveElementWithText("schemeStatus-1-2", "Wound-up")

      //table 2
      actual must haveElementWithText("schemeStatus-2-0", "Pending")
      actual must haveElementWithText("schemeStatus-2-1", "Pending information required")
      actual must haveElementWithText("schemeStatus-2-2", "Pending information received")
      actual must haveElementWithText("schemeStatus-2-3", "Rejected")
      actual must haveElementWithText("schemeStatus-2-4", "Rejected under appeal")
    }

    "display the date" in {
      val actual = view(frontendAppConfig, fullList)

      actual must haveElementWithText("schemeDate-1-0", "9 November 2017")
      actual must haveElementWithText("schemeDate-1-1", "10 November 2017")
      actual must haveElementWithText("schemeDate-1-2", "11 November 2017")
    }

    "display the no date text" in {
      val actual = view(frontendAppConfig, fullList)

      actual must haveElementWithText("schemeDate-2-0", "Scheme not open")
      actual must haveElementWithText("schemeDate-2-1", "Scheme not open")
      actual must haveElementWithText("schemeDate-2-2", "Scheme not open")
      actual must haveElementWithText("schemeDate-2-3", "Scheme not open")
      actual must haveElementWithText("schemeDate-2-4", "Scheme not open")
    }

    "display a scheme list with only schemes with a PSTR number" in {
      val actual = view(frontendAppConfig, fullList)

      actual must haveClassWithSize("row-group", 3, "schemeList-1")
      actual must haveElementWithText("schemeName-1-0", "scheme-name-4")
      actual must haveElementWithText("schemeName-1-1", "scheme-name-5")
      actual must haveElementWithText("schemeName-1-2", "scheme-name-6")
    }

    "display a scheme list with only schemes without a PSTR number" in {
      val actual = view(frontendAppConfig, fullList)

      actual must haveClassWithSize("row-group", 5, "schemeList-2")
      actual must haveElementWithText("schemeName-2-0", "scheme-name-0")
      actual must haveElementWithText("schemeName-2-1", "scheme-name-1")
      actual must haveElementWithText("schemeName-2-2", "scheme-name-2")
      actual must haveElementWithText("schemeName-2-3", "scheme-name-3")
      actual must haveElementWithText("schemeName-2-4", "scheme-name-7")
    }

    "show the PSTR table when there are schemes with PSTRs" in {
      val actual = asDocument(view(frontendAppConfig, PSTRSchemeList).apply())

      assertRenderedById(actual, "schemeList-1")
    }

    "not show the PSTR table when there are no schemes with PSTRs" in {
      val actual = asDocument(view(frontendAppConfig, noPSTRSchemeList).apply())

      assertNotRenderedById(actual, "schemeList-1")
    }

    "show the non-PSTR table when there are schemes without PSTRS" in {
      val actual = asDocument(view(frontendAppConfig, noPSTRSchemeList).apply())

      assertRenderedById(actual, "schemeList-2")
    }

    "not show the non-PSTR tables when there are no schemes without PSTRS" in {
      val actual = asDocument(view(frontendAppConfig, PSTRSchemeList).apply())

      assertNotRenderedById(actual, "schemeList-2")
    }

    "show the SRN column and header" when {
      "schemes have never been opened" in {
        val actual = asDocument(view(frontendAppConfig, noPSTRSchemeList).apply())

        assertRenderedById(actual, "srn")

        assertRenderedById(actual, "schemeName-1-1")
      }
    }

    "not show the SRN column and header" when {
      "schemes have been opened" in {
        val actual = asDocument(view(frontendAppConfig, PSTRSchemeList).apply())

        assertNotRenderedById(actual, "srn")
      }
    }

    "show the PSTR column and header" when {
      "schemes have been opened" in {
        val actual = asDocument(view(frontendAppConfig, PSTRSchemeList).apply())

      }
    }

    "not show the PSTR column and header" when {
      "schemes have never been opened" in {
        val actual = asDocument(view(frontendAppConfig, noPSTRSchemeList).apply())

      }
    }

    "show the date column and header" when {
      "schemes have been opened" in {
        val actual = asDocument(view(frontendAppConfig, PSTRSchemeList).apply())

      }
    }

    "not show the date column and header" when {
      "schemes have never been opened" in {
        val actual = asDocument(view(frontendAppConfig, noPSTRSchemeList).apply())

      }
    }
  }
}

object ListSchemesViewSpec {
  val emptyList: List[SchemeDetail] = List.empty[SchemeDetail]

  val fullList: List[SchemeDetail] = List(
    SchemeDetail(
      "scheme-name-0",
      "reference-number-0",
      "Pending",
      None,
      None,
      None,
      None
    ),
    SchemeDetail(
      "scheme-name-1",
      "reference-number-1",
      "Pending Info Required",
      None,
      None,
      None,
      None
    ),
    SchemeDetail(
      "scheme-name-2",
      "reference-number-2",
      "Pending Info Received",
      None,
      None,
      None,
      None
    ),
    SchemeDetail(
      "scheme-name-3",
      "reference-number-3",
      "Rejected",
      None,
      None,
      None,
      None
    ),
    SchemeDetail(
      "scheme-name-4",
      "reference-number-4",
      "Open",
      Option("2017-11-09"),
      Some("PSTR-4"),
      None,
      None
    ),
    SchemeDetail(
      "scheme-name-5",
      "reference-number-5",
      "Deregistered",
      Option("2017-11-10"),
      Some("PSTR-5"),
      None,
      None
    ),
    SchemeDetail(
      "scheme-name-6",
      "reference-number-6",
      "Wound-up",
      Option("2017-11-11"),
      Some("PSTR-6"),
      None,
      None
    ),
    SchemeDetail(
      "scheme-name-7",
      "reference-number-7",
      "Rejected Under Appeal",
      None,
      None,
      None,
      None
    )
  )

  val PSTRSchemeList: List[SchemeDetail] = List(
    SchemeDetail(
      "scheme-name-4",
      "reference-number-4",
      "Open",
      Option("2017-11-09"),
      Some("PSTR-4"),
      None,
      None
    ),
    SchemeDetail(
      "scheme-name-5",
      "reference-number-5",
      "Deregistered",
      Option("2017-11-10"),
      Some("PSTR-5"),
      None,
      None
    ),
    SchemeDetail(
      "scheme-name-6",
      "reference-number-6",
      "Wound-up",
      Option("2017-11-11"),
      Some("PSTR-6"),
      None,
      None
    )
  )

  val noPSTRSchemeList: List[SchemeDetail] = List(
    SchemeDetail(
      "scheme-name-0",
      "reference-number-0",
      "Pending",
      None,
      None,
      None,
      None
    ),
    SchemeDetail(
      "scheme-name-1",
      "reference-number-1",
      "Pending Info Required",
      None,
      None,
      None,
      None
    ),
    SchemeDetail(
      "scheme-name-2",
      "reference-number-2",
      "Pending Info Received",
      None,
      None,
      None,
      None
    ),
    SchemeDetail(
      "scheme-name-3",
      "reference-number-3",
      "Rejected",
      None,
      None,
      None,
      None
    ),
    SchemeDetail(
      "scheme-name-7",
      "reference-number-7",
      "Rejected Under Appeal",
      None,
      None,
      None,
      None
    )
  )


  def view(appConfig: FrontendAppConfig, schemes: List[SchemeDetail] = emptyList)
          (implicit request: Request[_], messages: Messages): () => HtmlFormat.Appendable =
    () => list_schemes(appConfig, schemes)

  def viewAsString(appConfig: FrontendAppConfig, schemes: List[SchemeDetail] = emptyList)
                  (implicit request: Request[_], messages: Messages): String = {
    val v = view(appConfig, schemes)
    v().toString()
  }

}
