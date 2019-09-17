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

package utils.hstasklisthelper

import config.FeatureSwitchManagementService
import identifiers.register.trustees.MoreThanTenTrusteesId
import identifiers.{IsAboutBenefitsAndInsuranceCompleteId, IsAboutMembersCompleteId, SchemeNameId, _}
import models.{Link, Mode, UpdateMode}
import play.api.i18n.Messages
import utils.UserAnswers
import viewmodels._

class HsTaskListHelperVariations(answers: UserAnswers,
                                 viewOnly: Boolean,
                                 srn: Option[String],
                                 featureSwitchManagementService: FeatureSwitchManagementService
                                )(implicit messages: Messages) extends HsTaskListHelper(answers, featureSwitchManagementService) {

  override protected lazy val beforeYouStartLinkText: String = messages("messages__schemeTaskList__scheme_info_link_text")

  override def declarationEnabled(userAnswers: UserAnswers): Boolean = {
    val isTrusteeOptional = userAnswers.get(HaveAnyTrusteesId).contains(false)
    Seq(
      userAnswers.get(IsBeforeYouStartCompleteId),
      userAnswers.get(IsAboutMembersCompleteId),
      userAnswers.get(IsAboutBenefitsAndInsuranceCompleteId),
      Some(userAnswers.allEstablishersCompleted(isHnSPhase1Enabled, isHnSPhase2Enabled, UpdateMode)),
      Some(isTrusteeOptional | userAnswers.isAllTrusteesCompleted(isHnSPhase1Enabled)),
      Some(userAnswers.allTrusteesAfterDelete(isHnSPhase1Enabled).size < 10 || userAnswers.get(MoreThanTenTrusteesId).isDefined)
    ).forall(_.contains(true)) && userAnswers.isUserAnswerUpdated
  }

  def taskList: SchemeDetailsTaskList = {
    val schemeName = answers.get(SchemeNameId).getOrElse("")
    SchemeDetailsTaskList(
      beforeYouStartSection(answers),
      messages("messages__schemeTaskList__about_scheme_header", schemeName),
      aboutSection(answers),
      None,
      addEstablisherHeader(answers, UpdateMode, srn),
      establishers(answers, UpdateMode, srn),
      addTrusteeHeader(answers, UpdateMode, srn),
      trustees(answers, UpdateMode, srn),
      declarationSection(answers),
      answers.get(SchemeNameId).getOrElse(""),
      messages("messages__scheme_details__title"),
      Some(messages("messages__schemeTaskList__scheme_information_link_text")),
      messages("messages__scheme_details__title"),
      srn
    )

  }

  override protected[utils] def aboutSection(userAnswers: UserAnswers): Seq[SchemeDetailsTaskListSection] = {
    val membersLink = userAnswers.get(IsAboutMembersCompleteId) match {
      case Some(true) => Link(aboutMembersLinkText, controllers.routes.CheckYourAnswersMembersController.onPageLoad(UpdateMode, srn).url)
      case _ => Link(aboutMembersLinkText, controllers.routes.WhatYouWillNeedMembersController.onPageLoad().url)
    }

    val benefitsAndInsuranceLink = Link(aboutBenefitsAndInsuranceLinkText,
      controllers.routes.CheckYourAnswersBenefitsAndInsuranceController.onPageLoad(UpdateMode, srn).url)

    Seq(SchemeDetailsTaskListSection(None, membersLink, None),
      SchemeDetailsTaskListSection(None, benefitsAndInsuranceLink, None))
  }

  private def beforeYouStartSection(userAnswers: UserAnswers): SchemeDetailsTaskListSection = {
    SchemeDetailsTaskListSection(
      None,
      beforeYouStartLink(answers, UpdateMode, srn),
      None
    )
  }

  protected[utils] def declarationSection(userAnswers: UserAnswers): Option[SchemeDetailsTaskListDeclarationSection] =
    if (viewOnly) {
      None
    } else {
      Some(SchemeDetailsTaskListDeclarationSection(
        header = "messages__schemeTaskList__sectionDeclaration_header",
        declarationLink = variationDeclarationLink(userAnswers, srn),
        incompleteDeclarationText =
          "messages__schemeTaskList__sectionDeclaration_incomplete_v1",
        "messages__schemeTaskList__sectionDeclaration_incomplete_v2"))
    }

  private[utils] def variationDeclarationLink(userAnswers: UserAnswers, srn: Option[String]): Option[Link] = {
    if (userAnswers.isUserAnswerUpdated) {
      Some(Link(declarationLinkText,
        if (userAnswers.areVariationChangesCompleted(isHnSPhase1Enabled, isHnSPhase2Enabled))
          controllers.routes.VariationDeclarationController.onPageLoad(srn).url
        else
          controllers.register.routes.StillNeedDetailsController.onPageLoad(srn).url
      ))
    } else {
      None
    }
  }

  protected[utils] def addEstablisherHeader(userAnswers: UserAnswers, mode: Mode, srn: Option[String]): Option[SchemeDetailsTaskListHeader] =
    (userAnswers.allEstablishersAfterDelete(isHnSPhase1Enabled, isHnSPhase2Enabled, mode).isEmpty, viewOnly) match {
      case (true, true) => Some(SchemeDetailsTaskListHeader(plainText = Some(noEstablishersText)))
      case (true, false) => Some(SchemeDetailsTaskListHeader(link = typeOfEstablisherLink(addEstablisherLinkText,
        userAnswers.allEstablishers(isHnSPhase1Enabled, isHnSPhase2Enabled, mode).size, srn, mode)))
      case (false, false) => Some(SchemeDetailsTaskListHeader(link = addEstablisherLink(changeEstablisherLinkText, srn, mode)))
      case (false, true) => None
    }

  protected[utils] override def addTrusteeHeader(userAnswers: UserAnswers, mode: Mode, srn: Option[String]): Option[SchemeDetailsTaskListHeader] =
    (userAnswers.allTrusteesAfterDelete(isHnSPhase1Enabled).isEmpty, viewOnly, isHnSPhase1Enabled) match {
      case (true, true, _) => Some(SchemeDetailsTaskListHeader(plainText = Some(noTrusteesText)))
      case (true, false, _) => Some(SchemeDetailsTaskListHeader(
        link = typeOfTrusteeLink(addTrusteesLinkText, userAnswers.allTrustees(isHnSPhase1Enabled).size, srn, mode)))
      case (false, false, false) => {

        val (linkText, additionalText): (String, Option[String]) =
          getTrusteeHeaderText(userAnswers.allTrusteesAfterDelete(isHnSPhase1Enabled).size, userAnswers.get(SchemeTypeId))

        Some(
          SchemeDetailsTaskListHeader(
            link = addTrusteeLink(linkText, srn, mode),
            p1 = additionalText))
      }
      case (false, false, true) => {
        Some(
          SchemeDetailsTaskListHeader(None, Some(Link(changeTrusteesLinkText,
            controllers.register.trustees.routes.AddTrusteeController.onPageLoad(mode, srn).url)), None))
      }
      case (false, true, _) => Some(SchemeDetailsTaskListHeader(header = Some(messages("messages__schemeTaskList__sectionTrustees_header"))))
    }
}
