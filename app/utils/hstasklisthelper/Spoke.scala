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

package utils.hstasklisthelper

import models.{Mode, TaskListLink}
import play.api.mvc.Call
import utils.UserAnswers
import viewmodels.Message

trait Spoke {

  protected def dynamicContentForChangeLink(name: String, srn: Option[String], registrationMessageKey: => String, variationsMessageKey: => String): Message =
    Message(if (srn.isDefined) variationsMessageKey else registrationMessageKey, name)

  def addLink(name: String)(mode: Mode, srn: Option[String], index: Int): TaskListLink

  def changeLink(name: String)(mode: Mode, srn: Option[String], index: Int): TaskListLink

  def incompleteChangeLink(name: String)(mode: Mode, srn: Option[String], index: Int): TaskListLink

  def completeFlag(answers: UserAnswers, index: Int, mode: Mode): Option[Boolean]
}

trait DetailsSpoke extends Spoke {
  def addLinkUrl(mode: Mode, srn: Option[String], index: Int): Call

  def changeLinkUrl(mode: Mode, srn: Option[String], index: Int): Call

  override def addLink(name: String)(mode: Mode, srn: Option[String], index: Int): TaskListLink = TaskListLink(
    Message("messages__schemeTaskList__add_details", name),
    addLinkUrl(mode, srn, index).url
  )

  override def changeLink(name: String)(mode: Mode, srn: Option[String], index: Int): TaskListLink =
    TaskListLink(
      dynamicContentForChangeLink(name, srn, "messages__schemeTaskList__change_details",
        "messages__schemeTaskList__view_details"),
      changeLinkUrl(mode, srn, index).url
    )

  override def incompleteChangeLink(name: String)(mode: Mode, srn: Option[String], index: Int): TaskListLink = TaskListLink(
    dynamicContentForChangeLink(name, srn, "messages__schemeTaskList__change_details",
      "messages__schemeTaskList__view_details"),
    addLinkUrl(mode, srn, index).url
  )
}

trait AddressSpoke extends Spoke {
  def addLinkUrl(mode: Mode, srn: Option[String], index: Int): Call

  def changeLinkUrl(mode: Mode, srn: Option[String], index: Int): Call

  override def addLink(name: String)(mode: Mode, srn: Option[String], index: Int): TaskListLink = TaskListLink(
    Message("messages__schemeTaskList__add_address", name),
    addLinkUrl(mode, srn, index).url
  )

  override def changeLink(name: String)(mode: Mode, srn: Option[String], index: Int): TaskListLink = TaskListLink(
    dynamicContentForChangeLink(name, srn, "messages__schemeTaskList__change_address", "messages__schemeTaskList__view_address"),
    changeLinkUrl(mode, srn, index).url
  )

  override def incompleteChangeLink(name: String)(mode: Mode, srn: Option[String], index: Int): TaskListLink = TaskListLink(
    dynamicContentForChangeLink(name, srn, "messages__schemeTaskList__change_address", "messages__schemeTaskList__view_address"),
    addLinkUrl(mode, srn, index).url
  )
}

trait ContactDetailsSpoke extends Spoke {
  def addLinkUrl(mode: Mode, srn: Option[String], index: Int): Call

  def changeLinkUrl(mode: Mode, srn: Option[String], index: Int): Call

  override def addLink(name: String)(mode: Mode, srn: Option[String], index: Int): TaskListLink = TaskListLink(
    Message("messages__schemeTaskList__add_contact", name),
    addLinkUrl(mode, srn, index).url
  )

  override def changeLink(name: String)(mode: Mode, srn: Option[String], index: Int): TaskListLink = TaskListLink(
    dynamicContentForChangeLink(name, srn, "messages__schemeTaskList__change_contact", "messages__schemeTaskList__view_contact"),
    changeLinkUrl(mode, srn, index).url
  )

  override def incompleteChangeLink(name: String)(mode: Mode, srn: Option[String], index: Int): TaskListLink = TaskListLink(
    dynamicContentForChangeLink(name, srn, "messages__schemeTaskList__change_contact", "messages__schemeTaskList__view_contact"),
    addLinkUrl(mode, srn, index).url
  )
}