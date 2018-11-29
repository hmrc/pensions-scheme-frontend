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

package controllers

import config.FrontendAppConfig
import controllers.actions._
import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import viewmodels.{JourneyTaskList, JourneyTaskListSection, Link}
import views.html.schemeTaskList

import scala.concurrent.Future

class SchemeTaskListController @Inject()(appConfig: FrontendAppConfig,
                                         override val messagesApi: MessagesApi,
                                         authenticate: AuthAction
                                        ) extends FrontendController with I18nSupport {


  private def sectionLink(sectionHeader: String, linkText: String): Link =
    Link(linkText, s"$sectionHeader link")

  private def genJourneyTaskListSection(header: Option[String], isCompleted: Option[Boolean] = None, linkText: String) =
    JourneyTaskListSection(isCompleted = isCompleted, link = sectionLink(header.getOrElse(""), linkText), header = header)

  private val about = genJourneyTaskListSection(header = None, isCompleted = Some(true),
    linkText = "aboutLinkText")
  private val establishers: Seq[JourneyTaskListSection] = Seq(
    genJourneyTaskListSection(header = None, isCompleted = Some(true), linkText = ""),
    genJourneyTaskListSection(header = None, isCompleted = None, linkText = ""),
    genJourneyTaskListSection(header = None, isCompleted = Some(false), linkText = "")
  )

  private val trustees: Seq[JourneyTaskListSection] = Seq(
    genJourneyTaskListSection(header = None, isCompleted = Some(false), linkText = ""),
    genJourneyTaskListSection(header = None, isCompleted = None, linkText = ""),
    genJourneyTaskListSection(header = None, isCompleted = Some(true), linkText = "")
  )

  private val workingKnowledge: JourneyTaskListSection =
    genJourneyTaskListSection(header = None, isCompleted = Some(true),
      linkText = "workingKnowledgeLinkText")


  private val journeyTaskList: JourneyTaskList = JourneyTaskList(about, establishers, trustees, workingKnowledge, None)
  def onPageLoad: Action[AnyContent] = authenticate.async {
    implicit request =>
      Future.successful(Ok(schemeTaskList(appConfig, journeyTaskList)))
  }
}
