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

package views.register.establishers.company

import play.api.data.Form
import controllers.register.establishers.company.routes
import forms.register.establishers.company.AddCompanyDirectorsFormProvider
import views.behaviours.{EditableItemListBehaviours, YesNoViewBehaviours}
import models.NormalMode
import models.register.establishers.company.director.DirectorDetails
import org.joda.time.LocalDate
import views.html.register.establishers.company.addCompanyDirectors

class AddCompanyDirectorsViewSpec extends YesNoViewBehaviours with EditableItemListBehaviours {

  private val establisherIndex = 1
  private val companyName = "MyCo Ltd"
  private val maxDirectors = frontendAppConfig.maxDirectors

  // scalastyle:off magic.number
  private val johnDoe = DirectorDetails("John", None, "Doe", new LocalDate(1862, 6, 9))
  private val joeBloggs = DirectorDetails("Joe", None, "Bloggs", new LocalDate(1969, 7, 16))
  // scalastyle:on magic.number

  val messageKeyPrefix = "addCompanyDirectors"

  val form = new AddCompanyDirectorsFormProvider()()

  private def createView(directors: Seq[DirectorDetails] = Nil) =
    () =>
      addCompanyDirectors(
        frontendAppConfig,
        form,
        NormalMode,
        establisherIndex,
        companyName,
        directors
      )(fakeRequest, messages)

  private def createViewUsingForm(directors: Seq[DirectorDetails] = Nil) =
    (form: Form[_]) =>
      addCompanyDirectors(
        frontendAppConfig,
        form,
        NormalMode,
        establisherIndex,
        companyName,
        directors
      )(fakeRequest, messages)

  "AddCompanyDirectors view" must {

    behave like normalPage(createView(), messageKeyPrefix, messages("messages__addCompanyDirectors__heading"))

    behave like pageWithBackLink(createView())

    behave like pageWithSecondaryHeader(createView(), companyName)

    behave like yesNoPage(
      createViewUsingForm(Seq(johnDoe)),
      messageKeyPrefix,
      routes.AddCompanyDirectorsController.onSubmit(NormalMode, 0).url,
      expectedHintKey = Some("lede")
    )

    "not show the yes no inputs if there are no directors" in {
      val doc = asDocument(createView()())
      doc.select("legend > span").size() mustBe 0
    }

    "show the add director text when there are no directors" in {
      val doc = asDocument(createView()())
      doc must haveDynamicText("messages__addCompanyDirectors_lede")
    }

    "show the Add a Director button when there are no directors" in {
      val doc = asDocument(createView()())
      val submit = doc.select("button#submit")
      submit.size() mustBe 1
      submit.first().text() mustBe messages("messages__addCompanyDirectors_add_director")
    }

    val directors: Seq[DirectorDetails] = Seq(johnDoe, joeBloggs)

    behave like editableItemList(createView(), createView(directors), (establisherIndex, directors))

    "show the Continue button when there are directors" in {
      val doc = asDocument(createViewUsingForm(Seq(johnDoe))(form))
      val submit = doc.select("button#submit")
      submit.size() mustBe 1
      submit.first().text() mustBe messages("site.save_and_continue")
    }

    "not show the yes no inputs if there are 10 or more directors" in {
      val doc = asDocument(createViewUsingForm(Seq.fill(maxDirectors)(johnDoe))(form))
      doc.select("legend > span").size() mustBe 0
    }

    "show the maximum number of directors message when there are 10 or more directors" in {
      val doc = asDocument(createView(Seq.fill(maxDirectors)(johnDoe))())
      doc must haveDynamicText("messages__addCompanyDirectors_at_maximum")
      doc must haveDynamicText("messages__addCompanyDirectors_tell_us_if_you_have_more")
    }

  }

}
