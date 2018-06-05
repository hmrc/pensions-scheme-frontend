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

package views.register.establishers

import controllers.register.establishers.routes
import forms.register.establishers.AddEstablisherFormProvider
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.individual.EstablisherDetailsId
import models.person.PersonDetails
import models.{CompanyDetails, NormalMode}
import org.joda.time.LocalDate
import play.api.data.Form
import play.twirl.api.HtmlFormat
import utils.UserAnswers
import viewmodels.EntityKind
import views.behaviours.{EditableItemListBehaviours, QuestionViewBehaviours, YesNoViewBehaviours}
import views.html.register.establishers.addEstablisher

class AddEstablisherViewSpec extends QuestionViewBehaviours[Option[Boolean]] with EditableItemListBehaviours {

  val onwardRoute = routes.AddEstablisherController.onPageLoad(NormalMode).url

  def companyUrl(index: Int) = controllers.register.establishers.company.routes.CompanyDetailsController.onPageLoad(NormalMode, index).url

  def individualUrl(index: Int) = controllers.register.establishers.individual.routes.EstablisherDetailsController.onPageLoad(NormalMode, 0).url

  val messageKeyPrefix = "establishers__add"

  val schemeName = "Test scheme name"

  val establisherCompany = ("Establisher Company" -> companyUrl(0))
  val establisherIndividual = ("John Doe" -> individualUrl(0))

  val companyDetails = CompanyDetails(
    "Establisher Company",
    None,
    None
  )

  val individualDetails = PersonDetails(
    "John",
    None,
    "Doe",
    LocalDate.now()
  )

  val userAnswers =
    UserAnswers()
      .set(CompanyDetailsId(0))(companyDetails)
      .flatMap(_.set(EstablisherDetailsId(1))(individualDetails))
      .asOpt
      .value

  val establishers = userAnswers.allEstablishers

  val form: Form[Option[Boolean]] = new AddEstablisherFormProvider()(establishers)

  def createView: () => HtmlFormat.Appendable = () => addEstablisher(frontendAppConfig, form, NormalMode, Seq.empty,
    schemeName)(fakeRequest, messages)

  def createView(establishers: Seq[(String, String)] = Seq.empty): () => HtmlFormat.Appendable = () =>
    addEstablisher(frontendAppConfig, form, NormalMode, establishers, schemeName)(fakeRequest, messages)

  def createViewUsingForm(establishers: Seq[(String, String)] = Seq.empty): Form[Boolean] => HtmlFormat.Appendable = (form: Form[Boolean]) =>
    addEstablisher(frontendAppConfig, form, NormalMode, establishers, schemeName)(fakeRequest, messages)

  "AddEstablisher view" must {
    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"))

    behave like pageWithBackLink(createView)

    behave like pageWithSecondaryHeader(createView, schemeName)

    "when there are no establishers" when {
      "not show the yes no inputs" in {
        val doc = asDocument(createView())
        doc.select("#value-yes").size() mustEqual 0
        doc.select("#value-no").size() mustEqual 0
      }

      "show the add establisher text" in {
        val doc = asDocument(createView())
        doc must haveDynamicText("messages__establishers__add_hint")
      }
    }

    behave like editableItemList(createView(), createView(establishers), establishers.map(e => (e._1, e._2, EntityKind.Establisher)))

    "display all the partially added establisher names with yes/No buttons" in {
      val doc = asDocument(createView(establishers)())
      doc.select("#value-yes").size() mustEqual 1
      doc.select("#value-no").size() mustEqual 1
    }
  }
}
