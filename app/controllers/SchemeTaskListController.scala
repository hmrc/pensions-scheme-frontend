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


  private def sectionLink(sectionHeader: String): Link =
    Link(s"$sectionHeader link", s"$sectionHeader target")

  private def genJourneyTaskListSection(header: String, isCompleted: Option[Boolean] = None) =
    JourneyTaskListSection(isCompleted = isCompleted, link = sectionLink(header), header = Some(header))


  private val aboutHeader = "about"
  private val establisher1Header = "establisher1"
  private val establisher2Header = "establisher2"
  private val establisher3Header = "establisher3"

  private val trustee1Header = "trustee1"
  private val trustee2Header = "trustee2"
  private val trustee3Header = "trustee3"

  private val workingKnowledgeHeader = "workingknowledge"


  private val about = genJourneyTaskListSection(header = aboutHeader, isCompleted = Some(true))
  private val establishers: Seq[JourneyTaskListSection] = Seq(
    genJourneyTaskListSection(header = establisher1Header, isCompleted = Some(true)),
    genJourneyTaskListSection(header = establisher2Header, isCompleted = None),
    genJourneyTaskListSection(header = establisher3Header, isCompleted = Some(false))
  )

  private val trustees: Seq[JourneyTaskListSection] = Seq(
    genJourneyTaskListSection(header = trustee1Header, isCompleted = Some(false)),
    genJourneyTaskListSection(header = trustee2Header, isCompleted = None),
    genJourneyTaskListSection(header = trustee3Header, isCompleted = Some(true))
  )

  private val workingKnowledge: JourneyTaskListSection =
    genJourneyTaskListSection(header = workingKnowledgeHeader, isCompleted = Some(true))

  private val declaration: Option[Link] = Some(sectionLink("declaration"))


  private val journeyTaskList: JourneyTaskList = JourneyTaskList(about, establishers, trustees, workingKnowledge, declaration)

  def onPageLoad: Action[AnyContent] = authenticate.async {
    implicit request =>





      val jtlSection = JourneyTaskListSection(None, Link("linkText", "linkTarget"), None)
      val journeyTL = JourneyTaskList(jtlSection, Seq(jtlSection), Seq(jtlSection), jtlSection, None)
        Future.successful(Ok(schemeTaskList(appConfig, journeyTaskList)))
      }
}
