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

package views.register

import controllers.register.establishers.partnership.routes
import forms.register.AddPartnersFormProvider
import identifiers.register.establishers.partnership.partner.PartnerDetailsId
import models.person.PersonDetails
import models.register.PartnerEntity
import org.joda.time.LocalDate
import play.api.Application
import play.api.data.Form
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Call
import views.behaviours.{EntityListBehaviours, YesNoViewBehaviours}
import views.html.register.addPartners

class AddPartnersViewSpec extends YesNoViewBehaviours with EntityListBehaviours {

  private val establisherIndex = 1
  private val partnershipName = "MyCo Ltd"
  private val maxPartners = frontendAppConfig.maxPartners

  // scalastyle:off magic.number
  private val johnDoe = PersonDetails("John", None, "Doe", new LocalDate(1862, 6, 9))
  private val joeBloggs = PersonDetails("Joe", None, "Bloggs", new LocalDate(1969, 7, 16))
  // scalastyle:on magic.number

  val messageKeyPrefix = "addPartners"
  private val postUrl: Call = routes.AddPartnersController.onSubmit(establisherIndex)

  val form = new AddPartnersFormProvider()()
  private val johnDoeEntity = PartnerEntity(PartnerDetailsId(0, 0), johnDoe.fullName, isDeleted = false, isCompleted = false)
  private val joeBloggsEntity = PartnerEntity(PartnerDetailsId(0, 1), joeBloggs.fullName, isDeleted = false, isCompleted = true)

  override lazy val app: Application = new GuiceApplicationBuilder().configure(
    "features.is-complete" -> true
  ).build()

  private def createView(partners: Seq[PartnerEntity] = Nil) =
    () =>
      addPartners(
        frontendAppConfig,
        form,
        establisherIndex,
        partnershipName,
        partners,
        postUrl
      )(fakeRequest, messages)

  private def createViewUsingForm(partners: Seq[PartnerEntity] = Nil) =
    (form: Form[_]) =>
      addPartners(
        frontendAppConfig,
        form,
        establisherIndex,
        partnershipName,
        partners,
        postUrl
      )(fakeRequest, messages)

  "AddPartnershipPartners view" must {

    behave like normalPage(createView(), messageKeyPrefix, messages("messages__addPartners__heading"))

    behave like pageWithBackLink(createView())

    behave like pageWithSecondaryHeader(createView(), partnershipName)

    behave like yesNoPage(
      createViewUsingForm(Seq(johnDoeEntity)),
      messageKeyPrefix,
      routes.AddPartnersController.onSubmit(0).url,
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
    }

  }

}
