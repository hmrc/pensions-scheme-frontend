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

package views.register.establishers

import controllers.register.establishers.routes
import forms.register.establishers.AddEstablisherFormProvider
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.individual.EstablisherDetailsId
import identifiers.register.establishers.partnership.PartnershipDetailsId
import models.person.PersonDetails
import models.register.{Establisher, EstablisherCompanyEntity, EstablisherIndividualEntityNonHnS, EstablisherPartnershipEntity}
import models.{CompanyDetails, NormalMode, UpdateMode}
import org.joda.time.LocalDate
import play.api.data.Form
import play.twirl.api.HtmlFormat
import utils.UserAnswers
import views.behaviours.{EntityListBehaviours, QuestionViewBehaviours}
import views.html.register.establishers.addEstablisher

class AddEstablisherViewSpec extends QuestionViewBehaviours[Option[Boolean]] with EntityListBehaviours {

  private def companyUrl(index: Int) = controllers.register.establishers.company.routes.CompanyDetailsController.onPageLoad(NormalMode, None, index).url

  private val messageKeyPrefix = "establishers__add"
  private val postCall = routes.AddEstablisherController.onSubmit _


  private val companyDetails = CompanyDetails(
    "Establisher Company"
  )

  private val individualDetails = PersonDetails(
    "John",
    None,
    "Doe",
    LocalDate.now()
  )

  private val userAnswers =
    UserAnswers()
      .set(CompanyDetailsId(0))(companyDetails)
      .flatMap(_.set(EstablisherDetailsId(1))(individualDetails))
      .asOpt
      .value

  private val johnDoe = EstablisherIndividualEntityNonHnS(EstablisherDetailsId(0), "John Doe", false, false, true, 3)
  private val testCompany = EstablisherCompanyEntity(CompanyDetailsId(1), "Establisher Company", false, true, true, 3)
  private val testPartnership = EstablisherPartnershipEntity(PartnershipDetailsId(2), "Establisher Partnership", false, true, true, 3)

  private val establishers = Seq(johnDoe, testCompany, testPartnership)
  private val establisher = Seq(johnDoe)

  val form: Form[Option[Boolean]] = new AddEstablisherFormProvider()(establishers)

  private def createView(establishers: Seq[Establisher[_]] = Seq.empty, enableSubmission:Boolean = false, isHnSEnabled: Boolean = false): () => HtmlFormat.Appendable = () =>
    addEstablisher(frontendAppConfig, form, NormalMode, establishers, None, None)(fakeRequest, messages)

  private def createViewUsingForm(establishers: Seq[Establisher[_]] = Seq.empty, enableSubmission:Boolean = false, isHnSEnabled: Boolean = false): Form[Boolean] => HtmlFormat.Appendable =
    (form: Form[Boolean]) =>
    addEstablisher(frontendAppConfig, form, NormalMode, establishers, None, None)(fakeRequest, messages)

  private def createUpdateView(establishers: Seq[Establisher[_]] = Seq.empty, enableSubmission:Boolean = false, isHnSEnabled: Boolean = false): () => HtmlFormat.Appendable = () =>
    addEstablisher(frontendAppConfig, form, UpdateMode, establishers, None, Some("srn"))(fakeRequest, messages)

  "AddEstablisher view" must {
    behave like normalPage(createView(), messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"))

    "when there are no establishers" when {
      "not show the yes no inputs" in {
        val doc = asDocument(createView()())
        doc.select("#value-yes").size() mustEqual 0
        doc.select("#value-no").size() mustEqual 0
      }

      "show the add establisher text" in {
        val doc = asDocument(createView()())
        doc must haveDynamicText("messages__establishers__add_hint")
      }

      "do disable the submit button" in {
        val doc = asDocument(createView()())
        doc.getElementById("submit").hasAttr("disabled") mustBe true
      }
    }

    behave like entityList(createView(), createView(establishers), establishers, frontendAppConfig)

    behave like entityListWithMultipleRecords(createView(), createView(establishers), establishers, frontendAppConfig)

    behave like entityListWithSingleRecord(createView(), createView(establisher), establisher, frontendAppConfig)

    "display all the partially added establisher names with yes/No buttons" in {
      val doc = asDocument(createView(establishers)())
      doc.select("#value-yes").size() mustEqual 1
      doc.select("#value-no").size() mustEqual 1
    }

    behave like pageWithReturnLink(createView(), getReturnLink)

    behave like pageWithReturnLinkAndSrn(createUpdateView(), getReturnLinkWithSrn)

    "do enable the submit button when enableSubmission is true" in {
      val doc = asDocument(createView(enableSubmission = true)())
      doc.getElementById("submit").hasAttr("disabled") mustBe false
    }

    "do not display the status if displayStatus is false" in {
      val doc = asDocument(createView(establishers = establishers, isHnSEnabled = true)())
      doc mustNot haveDynamicText("site.complete")
      doc mustNot haveDynamicText("site.incomplete")
    }

    "display the status if displayStatus is true" in {
      val doc = asDocument(createView(establishers = establishers)())
      doc must haveDynamicText("site.complete")
      doc must haveDynamicText("site.incomplete")
    }
  }

  "AddEstablisher view with toggle on" must {

    behave like addEntityList(
      createView(establishers = establishers, isHnSEnabled = true),
      establishers,
      "Establisher",
      Seq("Individual", "Company", "Partnership")
    )
  }
}
