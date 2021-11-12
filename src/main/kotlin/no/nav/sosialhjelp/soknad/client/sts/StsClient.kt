package no.nav.sosialhjelp.soknad.client.sts

import no.nav.sosialhjelp.soknad.client.sts.dto.FssToken
import no.nav.sosialhjelp.soknad.consumer.exceptions.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.domain.model.exception.SosialhjelpSoknadApiException
import org.slf4j.LoggerFactory.getLogger
import javax.ws.rs.ClientErrorException
import javax.ws.rs.ServerErrorException
import javax.ws.rs.client.Client

interface StsClient {
    fun ping()
    fun getFssToken(): FssToken
}

class StsClientImpl(
    private val client: Client,
    private val baseurl: String
) : StsClient {

    private var cachedFssToken: FssToken? = null

    override fun ping() {
        client
            .target(baseurl)
            .request()
            .options()
            .use { response ->
                if (response.status != 200) {
                    throw RuntimeException("Feil statuskode ved ping mot STS: ${response.status}, respons: ${response.readEntity(String::class.java)}")
                }
            }
    }

    override fun getFssToken(): FssToken {
        if (shouldRenew(cachedFssToken)) {
            return try {
                client.target(baseurl)
                    .queryParam("grant_type", "client_credentials")
                    .queryParam("scope", "openid")
                    .request()
                    .get(FssToken::class.java)
                    .also { cachedFssToken = it }
            } catch (e: ClientErrorException) {
                log.warn("STS - ${e.response.statusInfo}", e)
                throw SosialhjelpSoknadApiException("STS - ${e.response.statusInfo}. Endpoint=$baseurl", e)
            } catch (e: ServerErrorException) {
                log.error("STS - ${e.response.statusInfo} - Tjenesten er ikke tilgjengelig", e)
                throw TjenesteUtilgjengeligException("STS - ${e.response.statusInfo}. Endpoint=$baseurl", e)
            } catch (e: Exception) {
                log.error("Noe feil skjedde ved henting av token fra STS i FSS.")
                throw SosialhjelpSoknadApiException("Noe feil skjedde ved henting av token fra STS i FSS. Endpoint=$baseurl", e)
            }
        }
        log.debug("Bruker cachet token fra STS")
        return cachedFssToken!!
    }

    private fun shouldRenew(fssToken: FssToken?): Boolean {
        return fssToken?.isExpired ?: true
    }

    companion object {
        private val log = getLogger(StsClient::class.java)
    }
}
