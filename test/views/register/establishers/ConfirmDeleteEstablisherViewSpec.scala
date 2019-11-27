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

package views.register.establishers

import controllers.register.establishers.routes._
import forms.register.establishers.ConfirmDeleteEstablisherFormProvider
import models.register.establishers.EstablisherKind
import models.{Index, NormalMode}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.ViewSpecBase
import views.behaviours.YesNoViewBehaviours
import views.html.register.establishers.confirmDeleteEstablisher

class ConfirmDeleteEstablisherViewSpec extends YesNoViewBehaviours {

  import ConfirmDeleteEstablisherViewSpec._

  override val form: Form[Boolean] = formLocal

  "ConfirmDeleteEstablisher view" must {

    behave like normalPage(createView(), messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__heading", establisherName))

    behave like yesNoPageWithLegend(createView = createViewUsingForm(), messageKeyPrefix = messageKeyPrefix,
      expectedFormAction = postCall.url)

    behave like pageWithReturnLink(createView(), url = getReturnLink)

    behave like pageWithReturnLinkAndSrn(createUpdateView(), getReturnLinkWithSrn)

    "have the correct hint text where specified" in {
      val hintText = "test hint"
      val doc = asDocument(createView(hintText = Some(hintText))())
      assertRenderedByIdWithText(doc,"delete-hint", hintText)
    }
    "have no hint text where not specified" in {
      val doc = asDocument(createView()())
      assertNotRenderedById(doc,"delete-hint")
    }
  }
}

object ConfirmDeleteEstablisherViewSpec extends ViewSpecBase {

  val messageKeyPrefix = "confirmDeleteEstablisher"

  val formLocal = new ConfirmDeleteEstablisherFormProvider()()

  private val firstIndex = Index(0)
  private val establisherName = "John Doe"
  private val postCall = ConfirmDeleteEstablisherController.onSubmit(NormalMode, firstIndex, EstablisherKind.Indivdual, None)
  private val cancelCall = AddEstablisherController.onSubmit(NormalMode, None)

  private def createView(hintText:Option[String] = None) =
    () => confirmDeleteEstablisher(
      frontendAppConfig,
      formLocal,
      establisherName,
      hintText,
      postCall,
      None,
      None
    )(fakeRequest, messages)
  private def createUpdateView(hintText:Option[String] = None) =
    () => confirmDeleteEstablisher(
      frontendAppConfig,
      formLocal,
      establisherName,
      hintText,
      postCall,
      None,
      Some("srn")
    )(fakeRequest, messages)

  def createViewUsingForm(hintText:Option[String] = None): Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    confirmDeleteEstablisher(
      frontendAppConfig,
      form,
      establisherName,
      hintText,
      postCall,
      None,
      None
    )(fakeRequest, messages)

}
