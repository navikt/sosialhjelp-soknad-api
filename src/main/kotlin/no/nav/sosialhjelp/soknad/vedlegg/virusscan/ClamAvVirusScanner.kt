package no.nav.sosialhjelp.soknad.vedlegg.virusscan

import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.kotlin.utils.retry
import no.nav.sosialhjelp.soknad.client.config.RetryUtils.DEFAULT_EXPONENTIAL_BACKOFF_MULTIPLIER
import no.nav.sosialhjelp.soknad.client.config.RetryUtils.DEFAULT_INITIAL_WAIT_INTERVAL_MILLIS
import no.nav.sosialhjelp.soknad.client.config.RetryUtils.DEFAULT_MAX_ATTEMPTS
import no.nav.sosialhjelp.soknad.common.ServiceUtils
import no.nav.sosialhjelp.soknad.domain.model.exception.OpplastingException
import no.nav.sosialhjelp.soknad.vedlegg.virusscan.dto.Result
import no.nav.sosialhjelp.soknad.vedlegg.virusscan.dto.ScanResult
import org.slf4j.LoggerFactory.getLogger
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

/**
 * Integrasjonen er kopiert fra https://github.com/navikt/foreldrepengesoknad-api og modifisert til eget bruk
 */
interface VirusScanner {
    fun scan(filnavn: String, data: ByteArray, behandlingsId: String, fileType: String)
}

class ClamAvVirusScanner(
    private val virusScannerWebClient: WebClient,
    private val enabled: Boolean,
    private val serviceUtils: ServiceUtils
) : VirusScanner {

    override fun scan(filnavn: String, data: ByteArray, behandlingsId: String, fileType: String) {
        if (enabled && isInfected(filnavn, data, behandlingsId, fileType)) {
            throw OpplastingException(
                "Fant virus i fil for behandlingsId $behandlingsId",
                null,
                "vedlegg.opplasting.feil.muligVirus"
            )
        } else if (!enabled) {
            log.info("Virusscanning er ikke aktivert")
        }
    }

    private fun isInfected(filnavn: String, data: ByteArray, behandlingsId: String, fileType: String): Boolean {
        try {
            if (serviceUtils.isNonProduction() && filnavn.startsWith("virustest")) {
                return true
            }
            log.info("Scanner ${data.size} bytes for fileType $fileType (fra Tika)")

            val scanResults = runBlocking {
                retry(
                    attempts = DEFAULT_MAX_ATTEMPTS,
                    initialDelay = DEFAULT_INITIAL_WAIT_INTERVAL_MILLIS,
                    factor = DEFAULT_EXPONENTIAL_BACKOFF_MULTIPLIER,
                    retryableExceptions = arrayOf(HttpServerErrorException::class)
                ) {
                    virusScannerWebClient
                        .put()
                        .body(BodyInserters.fromValue(data))
                        .retrieve()
                        .awaitBody<Array<ScanResult>>()
                }
            }

            if (scanResults.size != 1) {
                log.warn("Uventet respons med lengde ${scanResults.size}, forventet lengde er 1")
                return false
            }

            val scanResult = scanResults[0]
            if (Result.OK == scanResult.result) {
                log.info("Ingen virus i fil")
                return false
            }
            log.warn("Fant virus i fil for behandlingsId $behandlingsId, status ${scanResult.result}")
            return true
        } catch (e: Exception) {
            log.warn("Kunne ikke scanne fil for behandlingsId $behandlingsId", e)
            return false
        }
    }

    companion object {
        private val log = getLogger(ClamAvVirusScanner::class.java)
    }
}
