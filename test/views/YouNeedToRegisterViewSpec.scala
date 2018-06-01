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
import views.behaviours.ViewBehaviours
import views.html.youNeedToRegister

class YouNeedToRegisterViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "youNeedToRegister"

  private def createView = () => youNeedToRegister(frontendAppConfig)(fakeRequest, messages)

  "YouNeedToRegister view" must {
    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__heading"), "_lede", "_p1", "_p2")

    "have button to redirect to register as pension administrator" in {
      Jsoup.parse(createView().toString()).select("a[id=redirect-to-psa]") must
        haveLink(frontendAppConfig.registerSchemeAdministratorUrl)
    }

    "have link to redirect to Gov UK" in {
      Jsoup.parse(createView().toString()).select("a[id=gov-uk-link]") must
        haveLink("https://www.gov.uk/")
    }
  }
}
