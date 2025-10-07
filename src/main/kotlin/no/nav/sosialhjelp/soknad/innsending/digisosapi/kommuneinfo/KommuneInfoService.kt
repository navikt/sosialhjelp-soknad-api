package no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo

import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import org.springframework.stereotype.Component

@Component
class KommuneInfoService(private val kommuneInfoClient: KommuneInfoClient) {
    fun hentAlleKommuneInfo(): Map<String, KommuneInfo>? {
        return kommuneInfoClient.getAll()
            .associateBy { it.kommunenummer }
            .ifEmpty {
                logger.error("hentAlleKommuneInfo - feiler mot Fiks og cache er tom.")
                null
            }
    }

    companion object {
        private val logger by logger()
    }
}
