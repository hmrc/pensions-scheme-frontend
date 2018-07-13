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

import forms.register.establishers.AddEstablisherFormProvider
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.individual.EstablisherDetailsId
import models.person.PersonDetails
import models.register.Establisher
import models.{CompanyDetails, NormalMode}
import org.joda.time.LocalDate
import play.api.data.Form
import play.twirl.api.HtmlFormat
import utils.UserAnswers
import views.behaviours.{EntityListBehaviours, QuestionViewBehaviours}
import views.html.register.establishers.addEstablisher

class AddEstablisherViewSpec extends QuestionViewBehaviours[Option[Boolean]] with EntityListBehaviours {

  private def companyUrl(index: Int) = controllers.register.establishers.company.routes.CompanyDetailsController.onPageLoad(NormalMode, index).url

  private val messageKeyPrefix = "establishers__add"

  private val schemeName = "Test scheme name"

  private val companyDetails = CompanyDetails(
    "Establisher Company",
    None,
    None
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

  private val establishers = userAnswers.allEstablishers

  val form: Form[Option[Boolean]] = new AddEstablisherFormProvider()(establishers)

  private def createView: () => HtmlFormat.Appendable = () => addEstablisher(frontendAppConfig, form, NormalMode, Seq.empty,
    schemeName)(fakeRequest, messages)

  private def createView(establishers: Seq[Establisher[_]] = Seq.empty): () => HtmlFormat.Appendable = () =>
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

    behave like entityList(createView(), createView(establishers), establishers)

    "display all the partially added establisher names with yes/No buttons" in {
      val doc = asDocument(createView(establishers)())
      doc.select("#value-yes").size() mustEqual 1
      doc.select("#value-no").size() mustEqual 1
    }
  }
}
