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
import identifiers.register.IsAboutSchemeCompleteId
import identifiers.register.adviser.IsWorkingKnowledgeCompleteId
import identifiers.register.establishers.company.{CompanyDetailsId => EstablisherCompanyDetailsId}
import identifiers.register.trustees.company.{CompanyDetailsId => TrusteeCompanyDetailsId}
import identifiers.register.establishers.individual.EstablisherDetailsId
import identifiers.register.establishers.partnership.{PartnershipDetailsId => EstablisherPartnershipDetailsId}
import identifiers.register.trustees.partnership.{PartnershipDetailsId => TrusteePartnershipDetailsId}
import identifiers.register.trustees.individual.TrusteeDetailsId
import javax.inject.Inject
import models.register.Entity
import models.{JourneyTaskList, JourneyTaskListSection, Link, NormalMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.UserAnswers
import views.html.schemeTaskList

import scala.concurrent.Future

class SchemeTaskListController @Inject()(appConfig: FrontendAppConfig,
                                         override val messagesApi: MessagesApi,
                                         authenticate: AuthAction,
                                         getData: DataRetrievalAction,
                                         requiredData: DataRequiredAction
                                       ) extends FrontendController with I18nSupport with Retrievals {

  def onPageLoad: Action[AnyContent] = (authenticate andThen getData andThen requiredData).async {
    implicit request =>
      val journey = request.userAnswers

      val taskList = JourneyTaskList(
                        aboutSection(journey),
                        establishers(journey),
                        trustees(journey),
                        workingKnowledgeSection(journey),
                        declarationLink(journey))

        Future.successful(Ok(schemeTaskList(appConfig, taskList)))
      }

  private def aboutSection(journey: UserAnswers) = JourneyTaskListSection(
    journey.get(IsAboutSchemeCompleteId),
    Link("messages__schemeTaskList__about_link_text",
      controllers.register.routes.SchemeDetailsController.onPageLoad(NormalMode).url),
    Some("messages__schemeTaskList__about_header"))

  private def workingKnowledgeSection(journey: UserAnswers) = JourneyTaskListSection(
    journey.get(IsWorkingKnowledgeCompleteId),
    Link("messages__schemeTaskList__working_knowledge_add_link",
      controllers.register.routes.SchemeDetailsController.onPageLoad(NormalMode).url),
    None)

  private def declarationLink(journey: UserAnswers): Option[Link] =
    if(declarationEnabled(journey))
      Some(Link("messages__schemeTaskList__declaration_link",
        controllers.register.routes.DeclarationController.onPageLoad().url))
    else None

  private def declarationEnabled(ua: UserAnswers): Boolean =
    (ua.get(IsAboutSchemeCompleteId), ua.get(IsWorkingKnowledgeCompleteId)) match {
      case (Some(true), Some(true)) if(ua.allEstablishersAfterDelete.forall(_.isCompleted) && (ua.allTrusteesAfterDelete.forall(_.isCompleted))) => true
      case _ => false
    }

  private def establishers(journey: UserAnswers):Seq[JourneyTaskListSection] = {
    val establishers = journey.allEstablishersAfterDelete
    if(establishers.isEmpty)
      Seq(JourneyTaskListSection(
        None,
        Link("messages__schemeTaskList__establishers_add_link",
          controllers.register.establishers.routes.EstablisherKindController.onPageLoad(NormalMode, journey.establishersCount).url),
        None))
    else
    listOfSections(establishers)

  }

  private def trustees(journey: UserAnswers):Seq[JourneyTaskListSection] = {
    val trustees = journey.allTrusteesAfterDelete
    if(trustees.isEmpty)
      Seq(JourneyTaskListSection(
        None,
        Link("messages__schemeTaskList__trustees_add_link",
          controllers.register.trustees.routes.TrusteeKindController.onPageLoad(NormalMode, journey.trusteesCount).url),
        None))
    else
      listOfSections(trustees)

  }

  private def listOfSections(sections: Seq[Entity[_]]): Seq[JourneyTaskListSection] =
    for(section <- sections) yield
      JourneyTaskListSection(
        Some(section.isCompleted),
        Link(linkText(section),
          section.editLink),
        Some(section.name))

  private def linkText(item: Entity[_]): String =
    item.id match {
      case EstablisherCompanyDetailsId | TrusteeCompanyDetailsId => "Company details"
      case EstablisherDetailsId | TrusteeDetailsId => "Individual details"
      case EstablisherPartnershipDetailsId | TrusteePartnershipDetailsId => "Partnership details"
    }

}
