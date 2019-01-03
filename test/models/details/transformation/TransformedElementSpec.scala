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

package models.details.transformation

import models.details.SchemeMemberNumbers
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import viewmodels.{AnswerRow, SuperSection}

class TransformedElementSpec extends WordSpec with MustMatchers with PropertyChecks with OptionValues {

  val members =  SchemeMemberNumbers(current = "1", future =  "2 to 11")

  val row = AnswerRow("messages__psaSchemeDetails__current_scheme_members", Seq("1"), answerIsMessageKey = false, None)

  val element = new TransformedElement[SchemeMemberNumbers]{

    override def transformRows(data: SchemeMemberNumbers): Seq[AnswerRow] = Seq(row)

    override def transformSuperSection(data: SchemeMemberNumbers): SuperSection = ???
  }

  "TransformedElement" must {

    "produce row of answers" when {

      "called transformRow with correct data" in {

        element.transformRow("messages__psaSchemeDetails__current_scheme_members", Seq("1")) must equal(row)
      }

      "called transformRows with correct data" in {

        element.transformRows(members) must equal(Seq(row))
      }
    }
  }
}