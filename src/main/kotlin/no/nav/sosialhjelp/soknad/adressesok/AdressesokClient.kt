package no.nav.sosialhjelp.soknad.adressesok

import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.kotlin.utils.retry
import no.nav.sosialhjelp.soknad.adressesok.dto.AdressesokResultDto
import no.nav.sosialhjelp.soknad.client.pdl.AdressesokDto
import no.nav.sosialhjelp.soknad.client.pdl.PdlClient
import no.nav.sosialhjelp.soknad.client.sts.StsClient
import no.nav.sosialhjelp.soknad.consumer.exceptions.PdlApiException
import no.nav.sosialhjelp.soknad.consumer.exceptions.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.consumer.pdl.common.PdlApiQuery
import no.nav.sosialhjelp.soknad.consumer.pdl.common.Utils.pdlMapper
import no.nav.sosialhjelp.soknad.consumer.retry.RetryUtils
import org.slf4j.LoggerFactory.getLogger
import javax.ws.rs.ProcessingException
import javax.ws.rs.WebApplicationException
import javax.ws.rs.client.Client

open class AdressesokClient(
    client: Client,
    baseurl: String,
    stsClient: StsClient
) : PdlClient(client, baseurl, stsClient) {

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
                        .post(requestEntity(PdlApiQuery.ADRESSE_SOK, variables), String::class.java)
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
