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

package views

import models.UpdateMode
import play.api.mvc.Call
import viewmodels.{AlreadyDeletedViewModel, Message}
import views.behaviours.ViewBehaviours
import views.html.alreadyDeleted

class AlreadyDeletedViewSpec extends ViewBehaviours {
  val messageKeyPrefix = "alreadyDeleted_"
  val deletedEntity = "Test entity"

  val expectedGuidanceKeys = Seq(
    Message(s"messages__${messageKeyPrefix}_lede", deletedEntity)
  )

  def viewmodel = AlreadyDeletedViewModel(Message("messages__alreadyDeleted__director_title"), deletedEntity, Call("GET", "/"))
  def updatedViewmodel = AlreadyDeletedViewModel(
    Message("messages__alreadyDeleted__director_title"), deletedEntity, Call("GET", "/"), Some("srn"), Some("Scheme Name"))

  val view: alreadyDeleted = app.injector.instanceOf[alreadyDeleted]

  def createView = () => view(viewmodel)(fakeRequest, messages)
  def createUpdateView = () => view(updatedViewmodel)(fakeRequest, messages)

  "Already Deleted view" must {

    behave like normalPageWithTitle(
      createView,
      messageKeyPrefix,
      Message(s"messages__${messageKeyPrefix}_director_title", deletedEntity),
      Message(s"messages__${messageKeyPrefix}_heading", deletedEntity),
      "text"
    )


    "display the correct guidance" in {
      val doc = asDocument(createView())
      for (key <- expectedGuidanceKeys) assertContainsText(doc, key)
    }

    "display button to take the user back to the list" in {
      createView must haveLink(
        "/",
        "return-to-list"
      )
    }

    behave like pageWithReturnLink(createView, getReturnLink)

    behave like pageWithReturnLinkAndSrn(createUpdateView, getReturnLinkWithSrn)

  }
}