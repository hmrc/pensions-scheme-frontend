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

package views.behaviours

import config.FrontendAppConfig
import models.NormalMode
import models.register.Entity
import views.ViewSpecBase

trait EntityListBehaviours {
  this: ViewSpecBase =>

  // scalastyle:off method.length
  def entityList(emptyView: View, nonEmptyView: View, items: Seq[Entity[_]], appConfig: FrontendAppConfig, noOfListItems: Int): Unit = {
    "behave like a list of items" must {
      "not show the list if there are no items" in {
        val doc = asDocument(emptyView())
        doc.select("table#items").size() mustBe 0
      }

      "show the list when there are one or more items" in {
        val doc = asDocument(nonEmptyView())
        doc.select("table#items").size() mustBe 1
      }

      "display the correct number of items in the list" in {
        val doc = asDocument(nonEmptyView())
        doc.select("#items > tbody > tr").size() mustBe noOfListItems
      }

      "display the correct details for each person" in {
        val doc = asDocument(nonEmptyView())
        items.foreach { item =>
          val name = doc.select(s"#person-${item.index}")
          name.size mustBe 1
          name.first.text mustBe item.name
        }
      }
    }
  }

  def entityListWithMultipleRecords(emptyView: View, nonEmptyView: View, items: Seq[Entity[_]], appConfig: FrontendAppConfig): Unit = {
    entityListMultipleLinks(emptyView: View, nonEmptyView: View, items: Seq[Entity[_]], appConfig: FrontendAppConfig)
  }

  def entityListWithSingleRecord(emptyView: View, nonEmptyView: View, items: Seq[Entity[_]], appConfig: FrontendAppConfig): Unit = {
    entityListSingleLink(emptyView: View, nonEmptyView: View, items: Seq[Entity[_]], appConfig: FrontendAppConfig)
  }

  private def entityListMultipleLinks(emptyView: View, nonEmptyView: View, items: Seq[Entity[_]], appConfig: FrontendAppConfig): Unit = {

    "show delete links " when {
      "multiple records exist" in {
        val doc = asDocument(nonEmptyView())
        items.foreach { item =>
          val link = doc.select(s"#person-${item.index}-delete")
          val visibleText = doc.select(s"#person-${item.index}-delete span").first.text
          val hiddenText = doc.select(s"#person-${item.index}-delete span[class=visually-hidden]").first.text
          link.size mustBe 1
          visibleText mustBe messages("site.remove")
          hiddenText mustBe item.name
          link.first.attr("href") mustBe item.deleteLink(NormalMode, None).get
        }
      }
    }
  }

  def addEntityList(view: View, items: Seq[Entity[_]], entityType: String, entityKind: Seq[String]): Unit = {

    "display the header for name column and kind column" in {
      val doc = asDocument(view())
      val nameColumnHeader = doc.select("#person-header")
      val kindColumnHeader = doc.select("#kind-header")

      nameColumnHeader.text mustBe s"$entityType name"
      kindColumnHeader.text mustBe messages("site.type")
    }

    "display the correct details for each person" in {
      val doc = asDocument(view())
      items.foreach { item =>
        val name = doc.select(s"#person-${item.index}")
        name.size mustBe 1
        name.first.text mustBe item.name
      }
    }

    "display the kind for each person" in {
      val doc = asDocument(view())
      items.foreach { item =>
        val kind = doc.select(s"#kind-${item.index}")
        kind.text mustBe entityKind(item.index)
      }
    }

    "show delete links " when {
      "multiple records exist" in {
        val doc = asDocument(view())
        items.foreach { item =>

          val link = doc.select(s"#person-${item.index}-delete")
          val visibleText = doc.select(s"#person-${item.index}-delete span").first.text
          val hiddenText = doc.select(s"#person-${item.index}-delete span[class=visually-hidden]").first.text

          link.size mustBe 1
          visibleText mustBe messages("site.remove")
          hiddenText mustBe item.name
          link.first.attr("href") mustBe item.deleteLink(NormalMode, None).get
        }
      }
    }

    "not display the status and edit link" in {
      val doc = asDocument(view())
      doc mustNot haveDynamicText("site.complete")
      doc mustNot haveDynamicText("site.incomplete")
      assertNotRenderedById(doc,"person-0-edit")
    }
  }

  private def entityListSingleLink(emptyView: View, nonEmptyView: View, items: Seq[Entity[_]], appConfig: FrontendAppConfig): Unit = {

    "not show delete link" when {
      "only 1 record exists" in {
        val doc = asDocument(nonEmptyView())
        doc.getElementById("person-1-delete") == null mustBe true
      }
    }
  }

  // scalastyle:on method.length

}
