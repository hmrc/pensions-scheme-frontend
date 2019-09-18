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

import com.google.inject.AbstractModule
import com.google.inject.multibindings.Multibinder
import connectors.{SubscriptionCacheConnector, UserAnswersCacheConnector}
import controllers.actions._
import navigators._
import services.{UserAnswersService, UserAnswersServiceEstablishersAndTrusteesImpl, UserAnswersServiceImpl, UserAnswersServiceInsuranceImpl}
import utils.annotations.{EstablishersPartner, _}
import utils.{AllowChangeHelper, AllowChangeHelperImpl}

class PODSModule extends AbstractModule {

  //scalastyle:off method.length
  override def configure(): Unit = {

    val navigators = Multibinder.newSetBinder(binder(), classOf[Navigator])
    navigators.addBinding().to(classOf[AboutBankDetailsNavigator])
    navigators.addBinding().to(classOf[AboutBenefitsAndInsuranceNavigator])
    navigators.addBinding().to(classOf[AboutMembersNavigator])
    navigators.addBinding().to(classOf[BeforeYouStartNavigator])
    navigators.addBinding().to(classOf[EstablishersCompanyDirectorNavigator])
    navigators.addBinding().to(classOf[EstablishersCompanyNavigator])
    navigators.addBinding().to(classOf[EstablishersIndividualFeatureSwitchNavigator])
    navigators.addBinding().to(classOf[EstablishersNavigator])
    navigators.addBinding().to(classOf[EstablishersPartnerNavigator])
    navigators.addBinding().to(classOf[EstablishersPartnershipNavigatorOld])
    navigators.addBinding().to(classOf[RegisterNavigator])
    navigators.addBinding().to(classOf[TrusteesIndividualFeatureSwitchNavigator])
    navigators.addBinding().to(classOf[TrusteesNavigator])
    navigators.addBinding().to(classOf[TrusteesPartnershipFeatureSwitchNavigator])
    navigators.addBinding().to(classOf[TrusteesCompanyFeatureSwitchNavigator])
    navigators.addBinding().to(classOf[EstablisherPartnershipFeatureSwitchNavigator])
    navigators.addBinding().to(classOf[VariationsNavigator])
    navigators.addBinding().to(classOf[WorkingKnowledgeNavigator])

    bind(classOf[Navigator]).to(classOf[CompoundNavigator])

    bind(classOf[UserAnswersService])
      .annotatedWith(classOf[InsuranceService])
      .to(classOf[UserAnswersServiceInsuranceImpl])

    bind(classOf[UserAnswersService])
      .annotatedWith(classOf[NoChangeFlagService])
      .to(classOf[UserAnswersServiceImpl])

    bind(classOf[UserAnswersCacheConnector])
      .to(classOf[SubscriptionCacheConnector])

    bind(classOf[UserAnswersService])
      .to(classOf[UserAnswersServiceEstablishersAndTrusteesImpl])

    bind(classOf[Navigator])
      .annotatedWith(classOf[AboutMembers])
      .to(classOf[AboutMembersNavigator])

    bind(classOf[Navigator])
      .annotatedWith(classOf[AboutBenefitsAndInsurance])
      .to(classOf[AboutBenefitsAndInsuranceNavigator])

    bind(classOf[Navigator])
      .annotatedWith(classOf[AboutBankDetails])
      .to(classOf[AboutBankDetailsNavigator])

    bind(classOf[Navigator])
      .annotatedWith(classOf[Register])
      .to(classOf[RegisterNavigator])

    bind(classOf[Navigator])
      .annotatedWith(classOf[BeforeYouStart])
      .to(classOf[BeforeYouStartNavigator])

    bind(classOf[Navigator])
      .annotatedWith(classOf[WorkingKnowledge])
      .to(classOf[WorkingKnowledgeNavigator])

    bind(classOf[Navigator])
      .annotatedWith(classOf[EstablishersCompany])
      .to(classOf[EstablishersCompanyNavigator])

    bind(classOf[Navigator])
      .annotatedWith(classOf[EstablisherPartnership])
      .to(classOf[EstablishersPartnershipNavigatorOld])

    bind(classOf[Navigator])
      .annotatedWith(classOf[Establishers])
      .to(classOf[EstablishersNavigator])

    bind(classOf[Navigator])
      .annotatedWith(classOf[Trustees])
      .to(classOf[TrusteesNavigator])

    bind(classOf[Navigator])
      .annotatedWith(classOf[EstablishersIndividual])
      .to(classOf[EstablishersIndividualNavigatorOld])

    bind(classOf[Navigator])
      .annotatedWith(classOf[EstablishersCompanyDirector])
      .to(classOf[EstablishersCompanyDirectorNavigator])

    bind(classOf[Navigator])
      .annotatedWith(classOf[EstablishersPartner])
      .to(classOf[EstablishersPartnerNavigator])

    bind(classOf[Navigator])
      .annotatedWith(classOf[Variations])
      .to(classOf[VariationsNavigator])

    bind(classOf[AllowChangeHelper])
      .to(classOf[AllowChangeHelperImpl])

    bind(classOf[AllowAccessActionProvider])
      .annotatedWith(classOf[TaskList])
      .to(classOf[AllowAccessActionProviderTaskListImpl])

    bind(classOf[AllowAccessActionProvider])
      .annotatedWith(classOf[NoSuspendedCheck])
      .to(classOf[AllowAccessActionProviderNoSuspendedCheckImpl])

    bind(classOf[AllowAccessActionProvider])
      .to(classOf[AllowAccessActionProviderMainImpl])

  }
}
