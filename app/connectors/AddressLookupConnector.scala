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

package connectors


import com.google.inject.{ImplementedBy, Inject}
import config.FrontendAppConfig
import models.address.{AddressRecord, TolerantAddress}
import play.api.libs.json.Reads
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

class AddressLookupConnectorImpl @Inject()(http: HttpClient, config: FrontendAppConfig) extends AddressLookupConnector {

  override def addressLookupByPostCode(postCode: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Seq[TolerantAddress]]] = {
    val schemeHc = hc.withExtraHeaders("X-Hmrc-Origin" -> "PODS")

    val addressLookupUrl = s"${config.addressLookUp}/v2/uk/addresses?postcode=$postCode"

    implicit val reads: Reads[Seq[TolerantAddress]] = TolerantAddress.postCodeLookupReads


    http.GET[Seq[TolerantAddress]](addressLookupUrl)(implicitly, schemeHc, implicitly).map(Some(_)) recoverWith {
      case _ => Future.successful(None)
    }
  }
}

@ImplementedBy(classOf[AddressLookupConnectorImpl])
trait AddressLookupConnector {
  def addressLookupByPostCode(postCode: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Seq[TolerantAddress]]]
}
