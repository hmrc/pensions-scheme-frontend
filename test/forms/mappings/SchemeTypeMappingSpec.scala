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

package forms.mappings

import forms.behaviours.SchemeTypeBehaviours
import models.register.SchemeType
import play.api.data.Form


class SchemeTypeMappingSpec extends SchemeTypeBehaviours {


  private val requiredTypeKey = "messages__scheme_type__error__required"
  private val invalidTypeKey = "messages__error__scheme_type_invalid"
  private val requiredOtherKey = "messages__error__scheme_type_information"
  private val lengthOtherKey = "messages__error__scheme_type_other_length"
  private val invalidOtherKey = "messages__error__scheme_type_other_invalid"

  "A form with a SchemeType" should {
    val mapping = schemeTypeMapping(
      requiredTypeKey,
      invalidTypeKey,
      requiredOtherKey,
      lengthOtherKey,
      invalidOtherKey
    )

    val testForm: Form[SchemeType] = Form("schemeDetails" -> mapping)

    behave like formWithSchemeType(testForm,
      requiredTypeKey,
      invalidTypeKey,
      requiredOtherKey,
      lengthOtherKey,
      invalidOtherKey
    )
  }


}
