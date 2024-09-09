/*
 * Copyright 2024 HM Revenue & Customs
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

package utils.hstasklisthelper.spokes

import models.{Index, Mode, TaskListLink}
import play.api.mvc.Call
import utils.UserAnswers
import viewmodels.Message
import models.SchemeReferenceNumber

trait Spoke {

  def addLink(name: String)(mode: Mode, srn: Option[SchemeReferenceNumber], index: Option[Index]): TaskListLink

  def changeLink(name: String)(mode: Mode, srn: Option[SchemeReferenceNumber], index: Option[Index]): TaskListLink

  def incompleteChangeLink(name: String)(mode: Mode, srn: Option[SchemeReferenceNumber], index: Option[Index]): TaskListLink

  def completeFlag(answers: UserAnswers, index: Option[Index], mode: Mode): Option[Boolean]

  protected def dynamicLinkText(name: String, srn: Option[SchemeReferenceNumber], registrationLinkText: => String,
                                variationsLinkText: => String): Message =
    Message(if (srn.isDefined) variationsLinkText else registrationLinkText, name)
}

trait DetailsSpoke extends Spoke {
  def addLinkUrl(mode: Mode, srn: Option[SchemeReferenceNumber], index: Option[Index]): Call

  def changeLinkUrl(mode: Mode, srn: Option[SchemeReferenceNumber], index: Option[Index]): Call

  override def addLink(name: String)(mode: Mode, srn: Option[SchemeReferenceNumber], index: Option[Index]): TaskListLink =
    TaskListLink(
    Message("messages__schemeTaskList__add_details", name),
    addLinkUrl(mode, srn, index).url
  )

  override def changeLink(name: String)(mode: Mode, srn: Option[SchemeReferenceNumber], index: Option[Index]): TaskListLink =
    TaskListLink(
      dynamicLinkText(name, srn, "messages__schemeTaskList__change_details",
        "messages__schemeTaskList__view_details"),
      changeLinkUrl(mode, srn, index).url
    )

  override def incompleteChangeLink(name: String)(mode: Mode, srn: Option[SchemeReferenceNumber], index: Option[Index])
  : TaskListLink = TaskListLink(
    dynamicLinkText(name, srn, "messages__schemeTaskList__continue_details",
      "messages__schemeTaskList__view_details"),
    addLinkUrl(mode, srn, index).url
  )
}

trait AddressSpoke extends Spoke {
  def addLinkUrl(mode: Mode, srn: Option[SchemeReferenceNumber], index: Option[Index]): Call

  def changeLinkUrl(mode: Mode, srn: Option[SchemeReferenceNumber], index: Option[Index]): Call

  override def addLink(name: String)(mode: Mode, srn: Option[SchemeReferenceNumber], index: Option[Index]): TaskListLink =
    TaskListLink(
    Message("messages__schemeTaskList__add_address", name),
    addLinkUrl(mode, srn, index).url
  )

  override def changeLink(name: String)(mode: Mode, srn: Option[SchemeReferenceNumber], index: Option[Index]): TaskListLink =
    TaskListLink(
    dynamicLinkText(name, srn, "messages__schemeTaskList__change_address",
      "messages__schemeTaskList__view_address"),
    changeLinkUrl(mode, srn, index).url
  )

  override def incompleteChangeLink(name: String)(mode: Mode, srn: Option[SchemeReferenceNumber], index: Option[Index])
  : TaskListLink = TaskListLink(
    dynamicLinkText(name, srn, "messages__schemeTaskList__continue_address",
      "messages__schemeTaskList__view_address"),
    addLinkUrl(mode, srn, index).url
  )
}

trait ContactDetailsSpoke extends Spoke {
  def addLinkUrl(mode: Mode, srn: Option[SchemeReferenceNumber], index: Option[Index]): Call

  def changeLinkUrl(mode: Mode, srn: Option[SchemeReferenceNumber], index: Option[Index]): Call

  override def addLink(name: String)(mode: Mode, srn: Option[SchemeReferenceNumber], index: Option[Index]): TaskListLink =
    TaskListLink(
    Message("messages__schemeTaskList__add_contact", name),
    addLinkUrl(mode, srn, index).url
  )

  override def changeLink(name: String)(mode: Mode, srn: Option[SchemeReferenceNumber], index: Option[Index]): TaskListLink =
    TaskListLink(
    dynamicLinkText(name, srn, "messages__schemeTaskList__change_contact",
      "messages__schemeTaskList__view_contact"),
    changeLinkUrl(mode, srn, index).url
  )

  override def incompleteChangeLink(name: String)(mode: Mode, srn: Option[SchemeReferenceNumber], index: Option[Index])
  : TaskListLink = TaskListLink(
    dynamicLinkText(name, srn, "messages__schemeTaskList__continue_contact",
      "messages__schemeTaskList__view_contact"),
    addLinkUrl(mode, srn, index).url
  )
}
