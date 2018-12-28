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

package controllers.TestOnlyDoNotUseInAppConf.TestEnrolControllerSpec

import config.FeatureSwitchManagementService
import controllers.ControllerSpecBase
import controllers.testOnlyDoNotUseInAppConf.TestFeatureSwitchManagerController
import forms.mappings.Mappings
import org.scalatest.mockito.MockitoSugar
import play.api.Configuration
import play.api.mvc.Call
import play.api.test.Helpers._

class TestFeatureSwitchManagerControllerSpec extends ControllerSpecBase with Mappings with MockitoSugar {

  val config = injector.instanceOf[Configuration]

  def onwardRoute: Call = controllers.routes.WhatYouWillNeedController.onPageLoad()

  val fakeFeatureSwitchManagerService = new FeatureSwitchManagementService {
    override def change(name: String, newValue: Boolean): Unit = ()

    override def get(name: String): Boolean = true

    override def reset(name: String): Unit = ()
  }

  def controller: TestFeatureSwitchManagerController =
    new TestFeatureSwitchManagerController(
      fakeFeatureSwitchManagerService
    )

  "TestFeatureSwitchManager Controller" must {

    "return NO content" when {
      "toggle on is called" in {
        val result = controller.toggleOn("test-toggle")(fakeRequest)
        status(result) mustBe NO_CONTENT
      }

      "toggle off is called" in {
        val result = controller.toggleOff("test-toggle")(fakeRequest)
        status(result) mustBe NO_CONTENT
      }

      "reset is called" in {
        val result = controller.reset("test-toggle")(fakeRequest)
        status(result) mustBe NO_CONTENT
      }
    }
  }
}
