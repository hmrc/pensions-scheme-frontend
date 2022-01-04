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

package views.register.establishers

import forms.register.establishers.AddEstablisherFormProvider
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.individual.EstablisherNameId
import identifiers.register.establishers.partnership.PartnershipDetailsId
import models.register.{Establisher, EstablisherCompanyEntity, EstablisherIndividualEntity, EstablisherPartnershipEntity}
import models.{NormalMode, UpdateMode}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.{EntityListBehaviours, QuestionViewBehaviours}
import views.html.register.establishers.addEstablisher

class AddEstablisherViewSpec extends QuestionViewBehaviours[Option[Boolean]] with EntityListBehaviours {

  private val messageKeyPrefix = "establishers__add"

  private val johnDoe = EstablisherIndividualEntity(EstablisherNameId(0), "John Doe", isDeleted = false,
    isCompleted = false, isNewEntity = true, noOfRecords = 3)
  private val testCompany = EstablisherCompanyEntity(CompanyDetailsId(1), "Establisher Company", isDeleted = false,
    isCompleted = true, isNewEntity = true, noOfRecords = 3)
  private val testPartnership = EstablisherPartnershipEntity(PartnershipDetailsId(2), "Establisher Partnership",
    isDeleted = false, isCompleted = true, isNewEntity = true, noOfRecords = 3)

  private val establishers = Seq(johnDoe, testCompany, testPartnership)
  private val establisher = Seq(johnDoe)

  val form: Form[Option[Boolean]] = new AddEstablisherFormProvider()(establishers)

  val view: addEstablisher = app.injector.instanceOf[addEstablisher]

  private def createView(establishers: Seq[Establisher[_]] = Seq.empty): () => HtmlFormat.Appendable = () =>
    view(form, NormalMode, establishers, None, None)(fakeRequest, messages)

  private def createUpdateView(establishers: Seq[Establisher[_]] = Seq.empty): () => HtmlFormat.Appendable = () =>
    view(form, UpdateMode, establishers, None, Some("srn"))(fakeRequest, messages)

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

    }

    behave like entityList(createView(), createView(establishers), establishers, frontendAppConfig, noOfListItems = 3)

    behave like entityListWithMultipleRecords(createView(), createView(establishers), establishers, frontendAppConfig)

    behave like entityListWithSingleRecord(createView(), createView(establisher), establisher, frontendAppConfig)

    "display all the partially added establisher names with yes/No buttons" in {
      val doc = asDocument(createView(establishers)())
      doc.select("#value-yes").size() mustEqual 1
      doc.select("#value-no").size() mustEqual 1
    }

    behave like pageWithReturnLink(createView(), getReturnLink)

    behave like pageWithReturnLinkAndSrn(createUpdateView(), getReturnLinkWithSrn)
  }

  "AddEstablisher view for all types" must {

    behave like addEntityList(
      createView(establishers),
      establishers,
      "Establisher",
      Seq("Individual", "Company", "Partnership")
    )
  }
}
