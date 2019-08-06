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

package views.register.trustees

import controllers.register.trustees.routes
import forms.register.trustees.AddTrusteeFormProvider
import identifiers.SchemeTypeId
import identifiers.register.trustees.{IsTrusteeNewId, TrusteeKindId}
import identifiers.register.trustees.company.CompanyDetailsId
import identifiers.register.trustees.individual.TrusteeDetailsId
import models.person.PersonDetails
import models.register.SchemeType.SingleTrust
import models.register.trustees.TrusteeKind
import models.register.{SchemeType, Trustee, TrusteeIndividualEntity}
import models.{CompanyDetails, NormalMode, UpdateMode}
import org.joda.time.LocalDate
import play.api.data.Form
import utils.{Enumerable, UserAnswers}
import views.behaviours.{EntityListBehaviours, YesNoViewBehaviours}
import views.html.register.trustees.addTrustee

class AddTrusteeViewSpec extends YesNoViewBehaviours with EntityListBehaviours with Enumerable.Implicits {

  private val messageKeyPrefix = "addTrustee"
  private val companyDetails = CompanyDetails(
    "Trustee Company"
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
      .flatMap(_.set(SchemeTypeId)(SchemeType.SingleTrust))
      .flatMap(_.set(IsTrusteeNewId(0))(true))
      .flatMap(_.set(TrusteeKindId(0))(TrusteeKind.Company))
      .flatMap(_.set(TrusteeDetailsId(1))(trusteeDetails))
      .flatMap(_.set(TrusteeKindId(1))(TrusteeKind.Individual))
      .flatMap(_.set(IsTrusteeNewId(1))(true))
      .asOpt
      .value

  private def trustees(toggled:Boolean): Seq[Trustee[_]] = userAnswers.allTrustees(toggled)
  private val fullTrustees: Seq[TrusteeIndividualEntity] = (0 to 9).map(index => TrusteeIndividualEntity(
    TrusteeDetailsId(index), "trustee name", isDeleted = false, isCompleted = false, isNewEntity = true, 10, Some(SingleTrust.toString)))

  val form = new AddTrusteeFormProvider()()

  private def createView(trustees: Seq[Trustee[_]] = Seq.empty, enable: Boolean = true) = () =>
    addTrustee(frontendAppConfig, form, NormalMode, trustees, None, None, enableSubmission = enable)(fakeRequest, messages)

  private def createUpdateView(trustees: Seq[Trustee[_]] = Seq.empty) = () =>
    addTrustee(frontendAppConfig, form, UpdateMode, trustees, None, Some("srn"), enableSubmission = true)(fakeRequest, messages)

  private def createViewUsingForm(trustees: Seq[Trustee[_]] = Seq.empty) = (form: Form[Boolean]) =>
    addTrustee(frontendAppConfig, form, NormalMode, trustees, None, None, enableSubmission = true)(fakeRequest, messages)

  "AddTrustee view" must {
    behave like normalPage(createView(), messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__heading"))

    behave like pageWithReturnLink(createView(), getReturnLink)

    behave like pageWithReturnLinkAndSrn(createUpdateView(), getReturnLinkWithSrn)

    behave like yesNoPage(
      createViewUsingForm(trustees(false)),
      messageKeyPrefix,
      routes.AddTrusteeController.onSubmit(NormalMode, None).url,
      "_text",
      expectedHintKey = Some("_lede")
    )

    // TODO: PODS-2940 Needs attention - extra test for toggle true

    "when there are no trustees" when {
      "do not show the yes no inputs" in {
        val doc = asDocument(createView()())
        doc.select("legend > span").size() mustBe 0
      }

      "show the add trustee text" in {
        val doc = asDocument(createView()())
        doc must haveDynamicText(s"messages__${messageKeyPrefix}__lede")
      }

      "disable the submit button" in {
        val doc = asDocument(createView(enable = false)())
        doc.getElementById("submit").hasAttr("disabled") mustBe true
      }

      "enable the submit button" in {
        val doc = asDocument(createView()())
        doc.getElementById("submit").hasAttr("disabled") mustBe false
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

    behave like entityList(createView(), createView(trustees(false), false), trustees(false), frontendAppConfig)

    // TODO: PODS-2940 Needs attention - extra test for toggle true

    "display all the partially added trustee names with yes/No buttons if the maximum trustees are not added yet" in {
      val doc = asDocument(createView(trustees(false))())
      doc.select("#value-yes").size() mustEqual 1
      doc.select("#value-no").size() mustEqual 1

    }

    // TODO: PODS-2940 Needs attention - extra test for toggle true

  }
}
