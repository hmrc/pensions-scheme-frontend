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

import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.service_unavailable

class ServiceUnavailableViewSpec extends ViewBehaviours {

  def view: () => HtmlFormat.Appendable = () => service_unavailable(frontendAppConfig)(fakeRequest, messages)

  "Service Unavailable view" must {

    behave like normalPage(view, "service_unavailable", messages("messages__service_unavailable__title"))

    "have a gov uk link" in {
      val doc = asDocument(view())
      assertRenderedById(doc, "gov-uk-link")
    }
  }
}
