/*
 * Copyright 2020 HM Revenue & Customs
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

package audit

import org.scalatest.{MustMatchers, WordSpec}

class UserResearchEventSpec extends WordSpec with MustMatchers {


  val externalId: String = "test-external-id"
  val name = "test name"
  val email = "test@test.com"

  "URagreementSchemeEvent" must {

    "return a UserResearchEvent when UserResearchDetails page is submitted" in {
      val expected = UserResearchEvent(externalId, name, email)
      val actual = UserResearchEvent(externalId, name, email)

      actual mustBe expected
      actual.auditType mustBe "URagreementSchemeEvent"
      actual.details mustBe  Map("externalId" -> externalId, "name" -> name, "email" -> email)
    }
  }
}

