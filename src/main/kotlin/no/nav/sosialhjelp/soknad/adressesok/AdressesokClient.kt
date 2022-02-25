package no.nav.sosialhjelp.soknad.adressesok

import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.kotlin.utils.retry
import no.nav.sosialhjelp.soknad.adressesok.dto.AdressesokResultDto
import no.nav.sosialhjelp.soknad.client.azure.AzureadService
import no.nav.sosialhjelp.soknad.client.config.RetryUtils
import no.nav.sosialhjelp.soknad.client.exceptions.PdlApiException
import no.nav.sosialhjelp.soknad.client.exceptions.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.client.pdl.AdressesokDto
import no.nav.sosialhjelp.soknad.client.pdl.PdlApiQuery.ADRESSE_SOK
import no.nav.sosialhjelp.soknad.client.pdl.PdlClient
import no.nav.sosialhjelp.soknad.common.Constants.BEARER
import org.slf4j.LoggerFactory.getLogger
import javax.ws.rs.ProcessingException
import javax.ws.rs.WebApplicationException
import javax.ws.rs.client.Client
import javax.ws.rs.core.HttpHeaders.AUTHORIZATION

open class AdressesokClient(
    client: Client,
    baseurl: String,
    private val azureadService: AzureadService,
    private val pdlScope: String
) : PdlClient(client, baseurl) {

    open fun getAdressesokResult(variables: Map<String, Any>): AdressesokResultDto? {
        return try {
            val response = runBlocking {
                retry(
                    attempts = RetryUtils.DEFAULT_MAX_ATTEMPTS,
                    initialDelay = RetryUtils.DEFAULT_INITIAL_WAIT_INTERVAL_MILLIS,
                    factor = RetryUtils.DEFAULT_EXPONENTIAL_BACKOFF_MULTIPLIER,
                    retryableExceptions = arrayOf(WebApplicationException::class, ProcessingException::class)
                ) {
                    baseRequest
                        .header(AUTHORIZATION, BEARER + azureadService.getSystemToken(pdlScope))
                        .post(requestEntity(ADRESSE_SOK, variables), String::class.java)
                }
            }
            val pdlResponse = pdlMapper.readValue<AdressesokDto>(response)
            pdlResponse.checkForPdlApiErrors()
            pdlResponse.data?.sokAdresse
        } catch (e: PdlApiException) {
            log.warn("PDL - feil oppdaget i response: {}", e.message, e)
            throw e
        } catch (e: Exception) {
            log.error("Kall til PDL feilet (adresseSok)")
            throw TjenesteUtilgjengeligException("Noe uventet feilet ved kall til PDL", e)
        }
    }

    companion object {
        private val log = getLogger(AdressesokClient::class.java)
    }
}
