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

package views.register

import controllers.register.establishers.partnership.routes
import forms.register.AddPartnersFormProvider
import identifiers.register.establishers.partnership.partner.PartnerDetailsId
import models.NormalMode
import models.person.PersonDetails
import models.register.PartnerEntity
import org.joda.time.LocalDate
import play.api.data.Form
import play.api.mvc.Call
import views.behaviours.{EntityListBehaviours, YesNoViewBehaviours}
import views.html.register.addPartners

class AddPartnersViewSpec extends YesNoViewBehaviours with EntityListBehaviours {

  private val establisherIndex = 1
  private val maxPartners = frontendAppConfig.maxPartners

  // scalastyle:off magic.number
  private val johnDoe = PersonDetails("John", None, "Doe", new LocalDate(1862, 6, 9))
  private val joeBloggs = PersonDetails("Joe", None, "Bloggs", new LocalDate(1969, 7, 16))
  // scalastyle:on magic.number

  val messageKeyPrefix = "addPartners"
  private val postUrl: Call = routes.AddPartnersController.onSubmit(NormalMode, establisherIndex, None)

  val form = new AddPartnersFormProvider()()
  private val johnDoeEntity = PartnerEntity(PartnerDetailsId(0, 0), johnDoe.fullName, isDeleted = false, isCompleted = false)
  private val joeBloggsEntity = PartnerEntity(PartnerDetailsId(0, 1), joeBloggs.fullName, isDeleted = false, isCompleted = true)

  private def createView(partners: Seq[PartnerEntity] = Nil, viewOnly: Boolean = false) =
    () =>
      addPartners(
        frontendAppConfig,
        form,
        partners,
        postUrl,
        None,
        viewOnly,
        NormalMode,
        None
      )(fakeRequest, messages)

  private def createViewUsingForm(partners: Seq[PartnerEntity] = Nil, viewOnly: Boolean = false) =
    (form: Form[_]) =>
      addPartners(
        frontendAppConfig,
        form,
        partners,
        postUrl,
        None,
        viewOnly,
        NormalMode,
        None
      )(fakeRequest, messages)

  "AddPartnershipPartners view" must {

    behave like normalPage(createView(), messageKeyPrefix, messages("messages__addPartners__heading"))

    behave like yesNoPage(
      createViewUsingForm(Seq(johnDoeEntity)),
      messageKeyPrefix,
      routes.AddPartnersController.onSubmit(NormalMode, 0, None).url,
      legendKey = "add_more",
      expectedHintKey = Some("lede")
    )

    "not show the yes no inputs if there are no partners" in {
      val doc = asDocument(createView()())
      doc.select("legend > span").size() mustBe 0
    }

    "show the add partner text when there are no partners" in {
      val doc = asDocument(createView()())
      doc must haveDynamicText("messages__addPartners_lede")
    }

    "do not disable the submit button if there are no partners" in {
      val doc = asDocument(createView()())
      doc.getElementById("submit").hasAttr("disabled") mustBe false
    }

    "show the Add a Partner button when there are no partners" in {
      val doc = asDocument(createView()())
      val submit = doc.select("button#submit")
      submit.size() mustBe 1
      submit.first().text() mustBe messages("messages__addPartners_add_partner")
    }

    val partners: Seq[PartnerEntity] = Seq(johnDoeEntity, joeBloggsEntity)

    behave like entityList(createView(), createView(partners), partners, frontendAppConfig)

    "show the Continue button when there are partners" in {
      val doc = asDocument(createViewUsingForm(Seq(johnDoeEntity))(form))
      val submit = doc.select("button#submit")
      submit.size() mustBe 1
      submit.first().text() mustBe messages("site.save_and_continue")
    }

    "not show the yes no inputs if there are 10 or more partners" in {
      val doc = asDocument(createViewUsingForm(Seq.fill(maxPartners)(johnDoeEntity))(form))
      doc.select("legend > span").size() mustBe 0
    }

    "show the maximum number of partners message when there are 10 or more partners" in {
      val doc = asDocument(createView(Seq.fill(maxPartners)(johnDoeEntity))())
      doc must haveDynamicText("messages__addPartners_at_maximum")
      doc must haveDynamicText("messages__addCompanyDirectorsOrPartners_tell_us_if_you_have_more")
    }

    "not show the yes no inputs in viewOnly mode" in {
      val doc = asDocument(createView(partners, viewOnly = true)())
      doc.select("legend > span").size() mustBe 0
    }

    "not show the Submit button in viewOnly mode" in {
      val doc = asDocument(createViewUsingForm(Seq(johnDoeEntity), viewOnly = true)(form))
      val submit = doc.select("button#submit")
      submit.size() mustBe 0
    }

    "not show delete, edit links or incomplete lozenge, but show view links in viewOnly mode" in {
      val doc = asDocument(createViewUsingForm(Seq(johnDoeEntity), viewOnly = true)(form))
      val editLink = doc.select(s"a[id=person-0-edit]")
      val deleteLink = doc.select(s"a[id=person-0-delete]")
      val viewLink = doc.select(s"a[id=person-0-view]")
      val incompleteLozenge = doc.select(s"span[class=rejected]")
      deleteLink.size() mustBe 0
      editLink.size() mustBe 0
      viewLink.size() mustBe 1
      incompleteLozenge.size() mustBe 0
    }

    "show delete and edit links and incomplete lozenge, but not show view links when viewOnly is false" in {
      val doc = asDocument(createViewUsingForm(Seq(johnDoeEntity), viewOnly = false)(form))
      println(doc)
      val editLink = doc.select(s"a[id=person-0-edit]")
      val deleteLink = doc.select(s"a[id=person-0-delete]")
      val viewLink = doc.select(s"a[id=person-0-view]")
      val incompleteLozenge = doc.select(s"span[class=rejected]")
      deleteLink.size() mustBe 1
      editLink.size() mustBe 1
      viewLink.size() mustBe 0
      incompleteLozenge.size() mustBe 1
    }

    behave like pageWithReturnLink(createView(), getReturnLink)

  }

}
