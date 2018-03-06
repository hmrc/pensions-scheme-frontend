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

package forms.register.establishers.company.director

import forms.behaviours.FormBehaviours
import models.{Field, Required}
import models.register.establishers.company.director.DirectorDetails
import org.joda.time.LocalDate

class DirectorDetailsFormProviderSpec extends FormBehaviours {

  val day = LocalDate.now().getDayOfMonth
  val month = LocalDate.now().getMonthOfYear
  val year = LocalDate.now().getYear

  val validData: Map[String, String] = Map(
    "firstName" -> "testFirstName",
    "lastName" -> "testLastName",
    "date.day" -> s"$day",
    "date.month" -> s"$month",
    "date.year" -> s"$year"
  )

  val form = new DirectorDetailsFormProvider()()

  val date = new LocalDate(year, month, day)

  "DirectorDetails form" must {
    behave like questionForm(DirectorDetails("testFirstName", "testLastName",date))

    behave like formWithMandatoryTextFields(
      Field("firstName", Required -> "messages__error__first_name"),
      Field("lastName", Required -> "messages__error__last_name"),
      Field("date.day", Required -> "messages__error__date"),
      Field("date.month", Required -> "messages__error__date"),
      Field("date.year", Required -> "messages__error__date")
    )
  }
}
