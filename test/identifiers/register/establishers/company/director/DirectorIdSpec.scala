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

package identifiers.register.establishers.company.director

import identifiers.register.establishers.company.OtherDirectorsId
import models.register.establishers.company.director.DirectorDetails
import org.joda.time.LocalDate
import org.scalatest.{Matchers, OptionValues, WordSpec}
import utils.UserAnswers

class DirectorIdSpec extends WordSpec with Matchers with OptionValues{
  import DirectorIdSpec._

  "DirectorId" when {
    "remove OtherDirectors data regardless of value" in {
      val newAnswers=userAnswers.remove(DirectorId(0,0)).asOpt.value
      newAnswers.get(otherDirectorsId) shouldNot be(defined)
    }
  }
}


object DirectorIdSpec extends OptionValues {

  private val establisherIndex = 0
  private val otherDirectorsId = OtherDirectorsId(establisherIndex)
  private val directorDetails:DirectorDetails = DirectorDetails("firstName",None,"SecondName",LocalDate.now)
  private val userAnswers = UserAnswers()
    .set(otherDirectorsId)(true)
    .flatMap(_.set(DirectorDetailsId(0,0))(directorDetails))
    .asOpt.value
}
