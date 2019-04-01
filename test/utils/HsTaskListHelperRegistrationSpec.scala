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

package utils

import utils.behaviours.HsTaskListHelperBehaviour

class HsTaskListHelperRegistrationSpec extends HsTaskListHelperBehaviour {

  "beforeYouStartSection " must {
    behave like beforeYouStartSection()
  }

  "aboutSection " must {
    behave like aboutSection()
  }

  "workingKnowledgeSection " must {
    behave like workingKnowledgeSection()
  }

  "addEstablisherHeader " must {

    behave like addEstablisherHeader()
  }

  "addTrusteeHeader " must {

    behave like addTrusteeHeader()
  }

  "establishers" must {

    behave like establishersSection()
  }

  "trustees" must {

    behave like trusteesSection()
  }

  "declarationEnabled" must {

    behave like declarationEnabled()
  }

  "declarationLink" must {

    behave like declarationLink()
  }
}

