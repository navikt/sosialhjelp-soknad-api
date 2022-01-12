package no.nav.sosialhjelp.soknad.client.sts

import no.nav.sosialhjelp.soknad.client.exceptions.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.client.sts.dto.FssToken
import org.slf4j.LoggerFactory.getLogger
import java.time.LocalDateTime
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
        if (shouldRenewToken(cachedFssToken)) {
            log.info("Henter nytt token fra STS")
            return try {
                val fssToken = client.target(baseurl)
                    .queryParam("grant_type", "client_credentials")
                    .queryParam("scope", "openid")
                    .request()
                    .get(FssToken::class.java)
                cachedFssToken = fssToken
                fssToken
            } catch (e: ClientErrorException) {
                log.warn("STS - ${e.response.statusInfo}", e)
                throw TjenesteUtilgjengeligException("STS - ${e.response.statusInfo}. Endpoint=$baseurl", e)
            } catch (e: ServerErrorException) {
                log.error("STS - ${e.response.statusInfo} - Tjenesten er ikke tilgjengelig", e)
                throw TjenesteUtilgjengeligException("STS - ${e.response.statusInfo}. Endpoint=$baseurl", e)
            } catch (e: Exception) {
                log.error("Noe feil skjedde ved henting av token fra STS i FSS.")
                throw TjenesteUtilgjengeligException("Noe feil skjedde ved henting av token fra STS i FSS. Endpoint=$baseurl", e)
            }
        }
        log.debug("Bruker cachet token fra STS")
        return cachedFssToken!!
    }

    fun shouldRenewToken(token: FssToken?): Boolean {
        if (token == null) {
            return true
        }
        return isExpired(token)
    }

    private fun isExpired(token: FssToken): Boolean {
        return token.refreshTime.isBefore(LocalDateTime.now())
    }

    companion object {
        private val log = getLogger(StsClientImpl::class.java)
    }
}
