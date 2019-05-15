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
import connectors.{SchemeDetailsReadOnlyCacheConnector, SubscriptionCacheConnector, UserAnswersCacheConnector}
import navigators._
import services.{UserAnswersService, UserAnswersServiceEstablishersAndTrusteesImpl, UserAnswersServiceImpl, UserAnswersServiceInsuranceImpl}
import utils.{AllowChangeHelper, AllowChangeHelperImpl, Navigator}
import utils.annotations.{EstablishersPartner, _}

class PODSModule extends AbstractModule {

  //scalastyle:off method.length
  override def configure(): Unit = {

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
      .to(classOf[EstablishersPartnershipNavigator])

    bind(classOf[Navigator])
      .annotatedWith(classOf[TrusteesCompany])
      .to(classOf[TrusteesCompanyNavigator])

    bind(classOf[Navigator])
      .annotatedWith(classOf[Establishers])
      .to(classOf[EstablishersNavigator])

    bind(classOf[Navigator])
      .annotatedWith(classOf[Trustees])
      .to(classOf[TrusteesNavigator])

    bind(classOf[Navigator])
      .annotatedWith(classOf[TrusteesIndividual])
      .to(classOf[TrusteesIndividualNavigator])

    bind(classOf[Navigator])
      .annotatedWith(classOf[EstablishersIndividual])
      .to(classOf[EstablishersIndividualNavigator])

    bind(classOf[Navigator])
      .annotatedWith(classOf[EstablishersCompanyDirector])
      .to(classOf[EstablishersCompanyDirectorNavigator])

    bind(classOf[Navigator])
      .annotatedWith(classOf[EstablishersPartner])
      .to(classOf[EstablishersPartnerNavigator])

    bind(classOf[Navigator])
      .annotatedWith(classOf[TrusteesPartnership])
      .to(classOf[TrusteesPartnershipNavigator])

    bind(classOf[Navigator])
      .annotatedWith(classOf[Variations])
      .to(classOf[VariationsNavigator])

    bind(classOf[AllowChangeHelper])
      .to(classOf[AllowChangeHelperImpl])

  }

}
