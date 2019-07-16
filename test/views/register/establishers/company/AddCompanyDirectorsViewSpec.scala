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

package views.register.establishers.company

import controllers.register.establishers.company.routes
import forms.register.establishers.company.AddCompanyDirectorsFormProvider
import identifiers.register.establishers.company.director.DirectorDetailsId
import models.NormalMode
import models.person.PersonDetails
import models.register.DirectorEntityNonHnS
import org.joda.time.LocalDate
import play.api.data.Form
import views.behaviours.{EntityListBehaviours, YesNoViewBehaviours}
import views.html.register.establishers.company.addCompanyDirectors

class AddCompanyDirectorsViewSpec extends YesNoViewBehaviours with EntityListBehaviours {

  private val establisherIndex = 1
  private val maxDirectors = frontendAppConfig.maxDirectors

  // scalastyle:off magic.number
  private val johnDoe = PersonDetails("John", None, "Doe", new LocalDate(1862, 6, 9))
  private val joeBloggs = PersonDetails("Joe", None, "Bloggs", new LocalDate(1969, 7, 16))
  // scalastyle:on magic.number

  val messageKeyPrefix = "addCompanyDirectors"
  private val postCall = routes.AddCompanyDirectorsController.onSubmit _

  val form = new AddCompanyDirectorsFormProvider()()
  private val johnDoeEntity = DirectorEntityNonHnS(DirectorDetailsId(0, 0), johnDoe.fullName, isDeleted = false, isCompleted = false, true, 2)
  private val joeBloggsEntity = DirectorEntityNonHnS(DirectorDetailsId(0, 1), joeBloggs.fullName, isDeleted = false, isCompleted = true, true, 2)

  private def createView(directors: Seq[DirectorEntityNonHnS] = Nil, viewOnly: Boolean = false) =
    () =>
      addCompanyDirectors(
        frontendAppConfig,
        form,
        directors,
        None,
        postCall(NormalMode, None, establisherIndex),
        viewOnly,
        NormalMode,
        None,
        false
      )(fakeRequest, messages)

  private def createUpdateView(directors: Seq[DirectorEntityNonHnS] = Nil, viewOnly: Boolean = false) =
    () =>
      addCompanyDirectors(
        frontendAppConfig,
        form,
        directors,
        None,
        postCall(NormalMode, None, establisherIndex),
        viewOnly,
        NormalMode,
        Some("srn"),
        false
      )(fakeRequest, messages)

  private def createViewUsingForm(directors: Seq[DirectorEntityNonHnS] = Nil, viewOnly: Boolean = false) =
    (form: Form[_]) =>
      addCompanyDirectors(
        frontendAppConfig,
        form,
        directors,
        None,
        postCall(NormalMode, None, establisherIndex),
        viewOnly,
        NormalMode,
        None,
        false
      )(fakeRequest, messages)

  "AddCompanyDirectors view" must {

    behave like normalPage(createView(), messageKeyPrefix, messages("messages__addCompanyDirectors__heading"))

    behave like yesNoPage(
      createViewUsingForm(Seq(johnDoeEntity)),
      messageKeyPrefix,
      routes.AddCompanyDirectorsController.onSubmit(NormalMode, None, 0).url,
      legendKey = "add_more",
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

    "do not disable the submit button if there are no directors" in {
      val doc = asDocument(createView()())
      doc.getElementById("submit").hasAttr("disabled") mustBe false
    }

    "show the Add a Director button when there are no directors" in {
      val doc = asDocument(createView()())
      val submit = doc.select("button#submit")
      submit.size() mustBe 1
      submit.first().text() mustBe messages("messages__addCompanyDirectors_add_director")
    }

    val directors: Seq[DirectorEntityNonHnS] = Seq(johnDoeEntity, joeBloggsEntity)

    behave like entityList(createView(), createView(directors), directors, frontendAppConfig)

    "show the Continue button when there are directors" in {
      val doc = asDocument(createViewUsingForm(Seq(johnDoeEntity))(form))
      val submit = doc.select("button#submit")
      submit.size() mustBe 1
      submit.first().text() mustBe messages("site.save_and_continue")
    }

    "not show the yes no inputs if there are 10 or more directors" in {
      val doc = asDocument(createViewUsingForm(Seq.fill(maxDirectors)(johnDoeEntity))(form))
      doc.select("legend > span").size() mustBe 0
    }

    "show the maximum number of directors message when there are 10 or more directors" in {
      val doc = asDocument(createView(Seq.fill(maxDirectors)(johnDoeEntity))())
      doc must haveDynamicText("messages__addCompanyDirectors_at_maximum")
      doc must haveDynamicText("messages__addCompanyDirectorsOrPartners_tell_us_if_you_have_more")
    }

    "not show the Submit button in viewOnly mode" in {
      val doc = asDocument(createViewUsingForm(Seq(johnDoeEntity), viewOnly = true)(form))
      val submit = doc.select("button#submit")
      submit.size() mustBe 0
    }

    "not show the yes no inputs for viewOnly mode" in {
      val doc = asDocument(createViewUsingForm(directors, viewOnly = true)(form))
      doc.select("legend > span").size() mustBe 0
    }

    "not show delete, edit links or incomplete lozenge, but show view links in viewOnly mode" in {
      val doc = asDocument(createViewUsingForm(Seq(johnDoeEntity), viewOnly = true)(form))
      val editLink = doc.select(s"a[id=person-0-edit]")
      val deleteLink = doc.select(s"a[id=person-0-delete]")
      val incompleteLozenge = doc.select(s"span[class=rejected]")
      val visibleText = doc.select(s"#person-0-view span[aria-hidden=true]").first.text
      val hiddenText = doc.select(s"#person-0-view span[class=visually-hidden]").first.text
      deleteLink.size() mustBe 0
      editLink.size() mustBe 0
      incompleteLozenge.size() mustBe 0
      visibleText mustBe messages("site.view")
      hiddenText mustBe s"${messages("site.view")} John Doe"
    }

    "show edit links and incomplete lozenge, but not show view links when viewOnly is false" in {
      val doc = asDocument(createViewUsingForm(Seq(johnDoeEntity), viewOnly = false)(form))
      val editLink = doc.select(s"a[id=person-0-edit]")
      val viewLink = doc.select(s"a[id=person-0-view]")
      val incompleteLozenge = doc.select(s"span[class=rejected]")
      editLink.size() mustBe 1
      viewLink.size() mustBe 0
      incompleteLozenge.size() mustBe 1
    }

    behave like pageWithReturnLink(createView(), getReturnLink)

    behave like pageWithReturnLinkAndSrn(createUpdateView(), getReturnLinkWithSrn)

    behave like entityListWithSingleRecord(createView(), createView(Seq(johnDoeEntity)), Seq(johnDoeEntity), frontendAppConfig)

    behave like entityListWithMultipleRecords(createView(), createView(directors), directors, frontendAppConfig)
  }
}
