/*
 * Copyright 2022 HM Revenue & Customs
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

import base.SpecBase
import org.jsoup.Jsoup
import viewmodels.Message
import views.behaviours.ViewBehaviours
import views.html.youNeedToRegister

class YouNeedToRegisterViewSpec extends ViewBehaviours with SpecBase{

  val messageKeyPrefix = "youNeedToRegister"

  val message1: String = Message("messages__youNeedToRegister__p1__link").resolve
  val message2: String = Message("messages__youNeedToRegister__p2__link").resolve

  val link1 = s"""<a id="psa-gov-uk-link" href="${frontendAppConfig.pensionAdministratorGovUkLink}">$message1</a>"""
  val link2 = s"""<a id="psp-gov-uk-link" href="${frontendAppConfig.pensionPractitionerGovUkLink}">$message2</a>"""

  val view: youNeedToRegister = app.injector.instanceOf[youNeedToRegister]

  private def createView = () => view()(fakeRequest, messages)

  "YouNeedToRegister view" must {
    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__heading"), "_lede")

    "display the pensionPractitionerGovUkLink" in {
      Jsoup.parse(createView().toString()) must haveDynamicText(s"messages__${messageKeyPrefix}__p1",link1)
    }

    "display the pensionAdministratorGovUkLink" in {
      Jsoup.parse(createView().toString()) must haveDynamicText(s"messages__${messageKeyPrefix}__p2",link2)
    }

    "have button to redirect to register as pension administrator" in {
      Jsoup.parse(createView().toString()).select("a[id=redirect-to-psa]") must
        haveLink(frontendAppConfig.registerSchemeAdministratorUrl)
    }

    "have link to redirect to Gov UK" in {
      Jsoup.parse(createView().toString()).select("a[id=gov-uk-link]") must
        haveLink(frontendAppConfig.govUkLink)
    }
  }
}
