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

import com.google.inject.AbstractModule
import connectors.{DataCacheConnector, PSANameCacheConnector}
import navigators._
import utils.annotations._
import utils.Navigator

class PODSModule extends AbstractModule {

  override def configure(): Unit = {

    bind(classOf[DataCacheConnector])
      .annotatedWith(classOf[PSAName])
      .to(classOf[PSANameCacheConnector])

    bind(classOf[Navigator])
      .annotatedWith(classOf[EstablishersIndividual])
      .to(classOf[EstablishersIndividualNavigator])

    bind(classOf[Navigator])
      .annotatedWith(classOf[Register])
      .to(classOf[RegisterNavigator])

    bind(classOf[Navigator])
      .annotatedWith(classOf[EstablishersCompany])
        .to(classOf[EstablishersCompanyNavigator])

    bind(classOf[Navigator])
      .annotatedWith(classOf[TrusteesCompany])
      .to(classOf[TrusteesCompanyNavigator])

    bind(classOf[Navigator])
      .annotatedWith(classOf[TrusteesIndividual])
      .to(classOf[TrusteesIndividualNavigator])

    bind(classOf[Navigator])
      .annotatedWith(classOf[EstablishersCompanyDirector])
      .to(classOf[EstablishersCompanyDirectorNavigator])

    bind(classOf[Navigator])
      .annotatedWith(classOf[Establishers])
      .to(classOf[EstablishersNavigator])

    bind(classOf[Navigator])
      .annotatedWith(classOf[Trustees])
      .to(classOf[TrusteesNavigator])

    bind(classOf[Navigator])
      .annotatedWith(classOf[Adviser])
      .to(classOf[AdviserNavigator])

  }

}
