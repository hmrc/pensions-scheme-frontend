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

package forms.dataPrefill

import forms.mappings.{DataPrefillConstraints, Mappings}
import play.api.data.Form
import play.api.data.Forms.list

import javax.inject.Inject



class DataPrefillCheckboxFormProvider @Inject() extends Mappings with DataPrefillConstraints {
  def apply(entityCount: Int, requiredError: String, noneSelectedWithValueError: String, moreThanTenError: String): Form[List[Int]] =
    Form(
      "value" -> list[Int](int(requiredError)).verifying(
        noValueInList(
          errorKey = requiredError
        ),
        noneSelectedWithValue(
          errorKey = noneSelectedWithValueError
        ),
        moreThanTen(
          errorKey = moreThanTenError,
          entityCount
        )
      )
    )
}
