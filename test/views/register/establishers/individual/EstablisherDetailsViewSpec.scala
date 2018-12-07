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

package views.register.establishers.individual

import controllers.register.establishers.individual.routes
import forms.register.PersonDetailsFormProvider
import models.person.PersonDetails
import models.{Index, NormalMode}
import org.joda.time.LocalDate
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.QuestionViewBehaviours
import views.html.register.establishers.individual.establisherDetails

class EstablisherDetailsViewSpec extends QuestionViewBehaviours[PersonDetails] {

  val messageKeyPrefix = "establisher_individual"

  override val form = new PersonDetailsFormProvider()()

  def createView(isHubEnabled: Boolean = false): () => HtmlFormat.Appendable = () =>
    establisherDetails(appConfig(isHubEnabled), form, NormalMode, Index(1))(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    establisherDetails(frontendAppConfig, form, NormalMode, Index(1))(fakeRequest, messages)

  val day = LocalDate.now().getDayOfMonth
  val year = LocalDate.now().getYear
  val month = LocalDate.now().getMonthOfYear

  val validData: Map[String, String] = Map(
    "firstName" -> "testFirstName",
    "middleName" -> "testMiddleName",
    "lastName" -> "testLastName",
    "date.day" -> s"$day",
    "date.month" -> s"$month",
    "date.year" -> s"$year"
  )

  "EstablisherDetails view" must {

    behave like normalPage(createView(), messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"))

    behave like pageWithBackLink(createView())

    behave like pageWithTextFields(
      createViewUsingForm,
      messageKeyPrefix,
      routes.EstablisherDetailsController.onSubmit(NormalMode, Index(0)).url,
      "firstName", "middleName", "lastName"
    )

    behave like pageWithDateFields(createViewUsingForm, form)

    "not have a return link" in {
      val doc = asDocument(createView(isHubEnabled = false)())
      assertNotRenderedById(doc, "return-link")
    }
  }

  "EstablisherDetails view with hub enabled" must {
    behave like pageWithReturnLink(createView(isHubEnabled = true), controllers.register.routes.SchemeTaskListController.onPageLoad().url)

    "not have a back link" in {
      val doc = asDocument(createView(isHubEnabled = true)())
      assertNotRenderedById(doc, "back-link")
    }
  }
}
