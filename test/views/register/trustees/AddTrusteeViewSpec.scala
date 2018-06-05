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

package views.register.trustees

import controllers.register.trustees.routes
import forms.register.trustees.AddTrusteeFormProvider
import identifiers.register.trustees.company.CompanyDetailsId
import identifiers.register.trustees.individual.TrusteeDetailsId
import models.person.PersonDetails
import models.{CompanyDetails, NormalMode}
import org.joda.time.LocalDate
import play.api.data.Form
import utils.UserAnswers
import viewmodels.EntityKind
import views.behaviours.{EditableItemListBehaviours, YesNoViewBehaviours}
import views.html.register.trustees.addTrustee

class AddTrusteeViewSpec extends YesNoViewBehaviours with EditableItemListBehaviours {

  private val onwardRoute = routes.AddTrusteeController.onPageLoad(NormalMode).url

  private def companyUrl(index: Int) = controllers.register.trustees.company.routes.CompanyDetailsController.onPageLoad(NormalMode, index).url

  private def individualUrl(index: Int) = controllers.register.trustees.individual.routes.TrusteeDetailsController.onPageLoad(NormalMode, 0).url

  private val messageKeyPrefix = "addTrustee"
  private val schemeName = "Test scheme name"
  private val maxTrustees = frontendAppConfig.maxTrustees
  private val trusteeCompany = "Trustee Company" -> companyUrl(0)
  private val trusteeIndividual = "John Doe" -> individualUrl(0)
  private val companyDetails = CompanyDetails(
    "Trustee Company",
    None,
    None
  )

  private val trusteeDetails = PersonDetails(
    "John",
    None,
    "Doe",
    LocalDate.now()
  )

  private val userAnswers =
    UserAnswers()
      .set(CompanyDetailsId(0))(companyDetails)
      .flatMap(_.set(TrusteeDetailsId(1))(trusteeDetails))
      .asOpt
      .value

  private val trustees = userAnswers.allTrustees
  private val fullTrustees = (0 to 9).map(index => ("trustee name", companyUrl(index)))

  val form = new AddTrusteeFormProvider()()

  private def createView(trustees: Seq[(String, String)] = Seq.empty) = () =>
    addTrustee(frontendAppConfig, form, NormalMode, schemeName, trustees)(fakeRequest, messages)

  private def createViewUsingForm(trustees: Seq[(String, String)] = Seq.empty) = (form: Form[Boolean]) =>
    addTrustee(frontendAppConfig, form, NormalMode, schemeName, trustees)(fakeRequest, messages)

  "AddTrustee view" must {
    behave like normalPage(createView(), messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__heading"))

    behave like pageWithBackLink(createView())

    behave like pageWithSecondaryHeader(createView(), schemeName)

    behave like yesNoPage(
      createViewUsingForm(trustees),
      messageKeyPrefix,
      routes.AddTrusteeController.onSubmit(NormalMode).url,
      "_text",
      expectedHintKey = Some("_lede")
    )

    "when there are no trustees" when {
      "not show the yes no inputs" in {
        val doc = asDocument(createView()())
        doc.select("legend > span").size() mustBe 0
      }

      "show the add trustee text" in {
        val doc = asDocument(createView()())
        doc must haveDynamicText(s"messages__${messageKeyPrefix}__lede")
      }
    }

    "when there are 10 trustees" when {
      "not show the yes no inputs" in {
        val doc = asDocument(createViewUsingForm(fullTrustees)(form))
        doc.select("legend > span").size() mustBe 0
      }

      "show the maximum number of trustees message" in {
        val doc = asDocument(createView(fullTrustees)())
        doc must haveDynamicText("messages__addTrustees_at_maximum")
        doc must haveDynamicText("messages__addTrustees_tell_us_if_you_have_more")
      }
    }

    behave like editableItemList(createView(), createView(trustees), trustees.map(t => (t._1, t._2, EntityKind.Trustee)))
    "display all the partially added trustee names with yes/No buttons if the maximum trustees are not added yet" in {
      val doc = asDocument(createView(trustees)())
      doc.select("#value-yes").size() mustEqual 1
      doc.select("#value-no").size() mustEqual 1

    }

  }
}
