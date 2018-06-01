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

      fullList.zipWithIndex.foreach {
        case (scheme, index) =>
          actual must haveElementWithText(s"schemeName-$index", scheme.name)
          actual must haveElementWithText(s"srn-$index", scheme.referenceNumber)
      }
    }

    "display either PSTR or 'Not assigned' when there is no value" in {
      val actual = view(frontendAppConfig, fullList)

      actual must haveElementWithText("pstr-0", "Not assigned")
      actual must haveElementWithText("pstr-1", "Not assigned")
      actual must haveElementWithText("pstr-2", "Not assigned")
      actual must haveElementWithText("pstr-3", "Not assigned")
      actual must haveElementWithText("pstr-4", "PSTR-4")
      actual must haveElementWithText("pstr-5", "PSTR-5")
      actual must haveElementWithText("pstr-6", "PSTR-6")
      actual must haveElementWithText("pstr-7", "Not assigned")
    }

    "display the abridged status not the full value" in {
      val actual = view(frontendAppConfig, fullList)

      actual must haveElementWithText("schemeStatus-0", "Pending")
      actual must haveElementWithText("schemeStatus-1", "Pending")
      actual must haveElementWithText("schemeStatus-2", "Pending")
      actual must haveElementWithText("schemeStatus-3", "Rejected")
      actual must haveElementWithText("schemeStatus-4", "Open")
      actual must haveElementWithText("schemeStatus-5", "Closed")
      actual must haveElementWithText("schemeStatus-6", "Closed")
      actual must haveElementWithText("schemeStatus-7", "Rejected")
    }

    "display the correct style for each status" in {
      val actual = view(frontendAppConfig, fullList)

      actual must haveElementWithClass("schemeStatus-0", "incomplete")
      actual must haveElementWithClass("schemeStatus-1", "incomplete")
      actual must haveElementWithClass("schemeStatus-2", "incomplete")
      actual must haveElementWithClass("schemeStatus-3", "not-started")
      actual must haveElementWithClass("schemeStatus-4", "complete")
      actual must haveElementWithClass("schemeStatus-5", "not-started")
      actual must haveElementWithClass("schemeStatus-6", "not-started")
      actual must haveElementWithClass("schemeStatus-7", "not-started")
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
      None,
      Some("PSTR-4"),
      None,
      None
    ),
    SchemeDetail(
      "scheme-name-5",
      "reference-number-5",
      "Deregistered",
      None,
      Some("PSTR-5"),
      None,
      None
    ),
    SchemeDetail(
      "scheme-name-6",
      "reference-number-6",
      "Wound-up",
      None,
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

  def view(appConfig: FrontendAppConfig, schemes: List[SchemeDetail] = emptyList)
          (implicit request: Request[_], messages: Messages): () => HtmlFormat.Appendable =
    () => list_schemes(appConfig, schemes)

  def viewAsString(appConfig: FrontendAppConfig, schemes: List[SchemeDetail] = emptyList)
                  (implicit request: Request[_], messages: Messages): String = {
    val v = view(appConfig, schemes)
    v().toString()
  }

}
