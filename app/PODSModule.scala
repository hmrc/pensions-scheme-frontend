/*
 * Copyright 2022 HM Revenue & Customs
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
import connectors.{RacdacSubscriptionCacheConnector, SubscriptionCacheConnector, UserAnswersCacheConnector}
import controllers.actions._
import navigators._
import navigators.establishers.individual.{EstablishersIndividualAddressNavigator, EstablishersIndividualContactDetailsNavigator, EstablishersIndividualDetailsNavigator, OldEstablishersIndividualDetailsNavigator}
import navigators.establishers.partnership.partner.PartnerNavigator
import navigators.establishers.partnership.{EstablisherPartnershipAddressNavigator, EstablisherPartnershipContactDetailsNavigator, EstablisherPartnershipDetailsNavigator, OldEstablisherPartnershipDetailsNavigator}
import navigators.trustees.individuals.{TrusteesIndividualAddressNavigator, TrusteesIndividualContactDetailsNavigator, TrusteesIndividualDetailsNavigator}
import navigators.trustees.partnership.{TrusteesPartnershipAddressNavigator, TrusteesPartnershipContactDetailsNavigator, TrusteesPartnershipDetailsNavigator}
import services.{UserAnswersService, UserAnswersServiceEstablishersAndTrusteesImpl, UserAnswersServiceImpl, UserAnswersServiceInsuranceImpl}
import utils.annotations._
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
    navigators.addBinding().to(classOf[EstablishersIndividualDetailsNavigator])
    navigators.addBinding().to(classOf[EstablishersIndividualAddressNavigator])
    navigators.addBinding().to(classOf[EstablishersIndividualContactDetailsNavigator])
    navigators.addBinding().to(classOf[EstablishersNavigator])
    navigators.addBinding().to(classOf[RegisterNavigator])
    navigators.addBinding().to(classOf[TrusteesIndividualDetailsNavigator])
    navigators.addBinding().to(classOf[TrusteesIndividualAddressNavigator])
    navigators.addBinding().to(classOf[TrusteesIndividualContactDetailsNavigator])
    navigators.addBinding().to(classOf[TrusteesNavigator])
    navigators.addBinding().to(classOf[TrusteesPartnershipDetailsNavigator])
    navigators.addBinding().to(classOf[TrusteesPartnershipContactDetailsNavigator])
    navigators.addBinding().to(classOf[TrusteesPartnershipAddressNavigator])
    navigators.addBinding().to(classOf[TrusteesCompanyNavigator])
    navigators.addBinding().to(classOf[EstablisherPartnershipDetailsNavigator])
    navigators.addBinding().to(classOf[EstablisherPartnershipAddressNavigator])
    navigators.addBinding().to(classOf[EstablisherPartnershipContactDetailsNavigator])
    navigators.addBinding().to(classOf[PartnerNavigator])
    navigators.addBinding().to(classOf[VariationsNavigator])
    navigators.addBinding().to(classOf[WorkingKnowledgeNavigator])
    navigators.addBinding().to(classOf[RACDACNavigator])

    bind(classOf[Navigator]).to(classOf[CompoundNavigator])

    bind(classOf[UserAnswersService])
      .annotatedWith(classOf[InsuranceService])
      .to(classOf[UserAnswersServiceInsuranceImpl])

    bind(classOf[UserAnswersService])
      .annotatedWith(classOf[NoChangeFlagService])
      .to(classOf[UserAnswersServiceImpl])

    bind(classOf[UserAnswersCacheConnector])
      .to(classOf[SubscriptionCacheConnector])

    bind(classOf[UserAnswersCacheConnector])
      .annotatedWith(classOf[Racdac])
      .to(classOf[RacdacSubscriptionCacheConnector])

    bind(classOf[DataRetrievalAction])
      .annotatedWith(classOf[Racdac])
      .to(classOf[RacdacDataRetrievalActionImpl])

    bind(classOf[DataRetrievalAction])
      .to(classOf[DataRetrievalActionImpl])

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
      .annotatedWith(classOf[OldEstablishersCompany])
      .to(classOf[OldEstablishersCompanyNavigator])

    bind(classOf[Navigator])
      .annotatedWith(classOf[EstablishersIndividualDetails])
      .to(classOf[EstablishersIndividualDetailsNavigator])

    bind(classOf[Navigator])
      .annotatedWith(classOf[OldEstablishersIndividualDetails])
      .to(classOf[OldEstablishersIndividualDetailsNavigator])

    bind(classOf[Navigator])
      .annotatedWith(classOf[EstablishersPartnership])
      .to(classOf[EstablisherPartnershipDetailsNavigator])

    bind(classOf[Navigator])
      .annotatedWith(classOf[OldEstablishersPartnership])
      .to(classOf[OldEstablisherPartnershipDetailsNavigator])

    bind(classOf[Navigator])
      .annotatedWith(classOf[Establishers])
      .to(classOf[EstablishersNavigator])

    bind(classOf[Navigator])
      .annotatedWith(classOf[Trustees])
      .to(classOf[TrusteesNavigator])

    bind(classOf[Navigator])
      .annotatedWith(classOf[EstablishersCompanyDirector])
      .to(classOf[EstablishersCompanyDirectorNavigator])

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
