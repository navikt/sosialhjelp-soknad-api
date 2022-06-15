package no.nav.sosialhjelp.soknad.vedlegg.virusscan

import no.nav.sosialhjelp.soknad.client.config.RetryUtils
import no.nav.sosialhjelp.soknad.common.MiljoUtils
import no.nav.sosialhjelp.soknad.vedlegg.exceptions.OpplastingException
import no.nav.sosialhjelp.soknad.vedlegg.virusscan.dto.Result
import no.nav.sosialhjelp.soknad.vedlegg.virusscan.dto.ScanResult
import org.slf4j.LoggerFactory.getLogger
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

/**
 * Integrasjonen er kopiert fra https://github.com/navikt/foreldrepengesoknad-api og modifisert til eget bruk
 */
class VirusScanner(
    private val virusScannerWebClient: WebClient,
    private val enabled: Boolean
) {

    fun scan(filnavn: String, data: ByteArray, behandlingsId: String, fileType: String) {
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
            if (MiljoUtils.isNonProduction() && filnavn.startsWith("virustest")) {
                return true
            }
            log.info("Scanner ${data.size} bytes for fileType $fileType (fra Tika)")

            val scanResults = virusScannerWebClient.put()
                .body(BodyInserters.fromValue(data))
                .retrieve()
                .bodyToMono<Array<ScanResult>>()
                .retryWhen(RetryUtils.DEFAULT_RETRY_SERVER_ERRORS)
                .block()

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
        private val log = getLogger(VirusScanner::class.java)
    }
}
