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

package controllers.behaviours

import controllers.ControllerSpecBase
import identifiers.TypedIdentifier
import models.Mode
import models.requests.DataRequest
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.mockito.MockitoSugar
import play.api.mvc.{AnyContent, Result}
import play.api.test.Helpers._
import utils.AllowChangeHelper

import scala.concurrent.Future

trait ControllerAllowChangeBehaviour extends ControllerSpecBase
  with MockitoSugar
  with ScalaFutures
  with OptionValues {

  protected def allowChangeHelper(saveAndContinueButton:Boolean):AllowChangeHelper = new AllowChangeHelper {
    override def hideSaveAndContinueButton(request: DataRequest[AnyContent], newId: TypedIdentifier[Boolean], mode:Mode):Boolean = saveAndContinueButton
  }

  protected val ach:AllowChangeHelper = allowChangeHelper(saveAndContinueButton = false)

  def changeableController(res: AllowChangeHelper => Future[Result]):Unit = {
    "return OK and displays save and continue button when asked to" in {
      val result = res(allowChangeHelper(saveAndContinueButton = false))
      status(result) mustBe OK
      assertRenderedById(asDocument(contentAsString(result)), "submit")
    }

    "return OK and does not display save and continue button when not asked to" in {
      val result = res(allowChangeHelper(saveAndContinueButton = true))
      status(result) mustBe OK
      assertNotRenderedById(asDocument(contentAsString(result)), "submit")
    }
  }

}
