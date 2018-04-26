package connectors

import com.google.inject.Inject
import config.FrontendAppConfig
import play.api.libs.ws.WSClient
import uk.gov.hmrc.crypto.ApplicationCrypto

class PSANameCacheConnector  @Inject() (
                                         config: FrontendAppConfig,
                                         http: WSClient,
                                         crypto: ApplicationCrypto
                                       ) extends MicroserviceCacheConnector(config, http, crypto) {

  override protected def url(id: String) = s"${config.pensionsSchemeUrl}/pensions-scheme/psa-name/scheme/$id"

}
