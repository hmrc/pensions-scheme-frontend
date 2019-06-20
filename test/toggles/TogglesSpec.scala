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

package toggles

class TogglesSpec extends FeatureToggleBehaviours {

  "is-variations-enabled new feature toggle" should {
    behave like featureToggle("is-variations-enabled", true)
  }

  "is-address-pre-population-enabled new feature toggle" should {
    behave like featureToggle("is-address-pre-population-enabled", true)
  }

  "is-scheme-data-shift-enabled new feature toggle" should {
    behave like featureToggle("is-scheme-data-shift-enabled", true)
  }

  "separate-ref-collection new feature toggle" should {
    behave like featureToggle("separate-ref-collection", false)
  }

  "is-establisher-company-hns new feature toggle" should {
    behave like featureToggle("is-establisher-company-hns", false)
  }

}