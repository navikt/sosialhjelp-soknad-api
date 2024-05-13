package no.nav.sosialhjelp.soknad.v2.register

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import org.springframework.stereotype.Service
import java.util.UUID

interface RegisterDataFetcher {
    fun fetchAndSave(soknadId: UUID)
}

@Service
class RegisterDataService(
    private val fetchers: List<RegisterDataFetcher>,
) {
    private val logger by logger()

    fun runAllRegisterDataFetchers(soknadId: UUID) {
        logger.info("NyModell: Henter Register-data")
        doRunListedFetchers(soknadId = soknadId, listedFetchers = fetchers)
    }

    fun runSpecificFetchers(
        soknadId: UUID,
        listedHandlers: List<RegisterDataFetcher>,
    ) {
        logger.info("NyModell: Henter Register-data: ${listedHandlers.joinToString(separator = ", ")}")
        doRunListedFetchers(soknadId = soknadId, listedFetchers = listedHandlers)
    }

    private fun doRunListedFetchers(
        soknadId: UUID,
        listedFetchers: List<RegisterDataFetcher>,
    ) {
        listedFetchers.forEach {
            runCatching { it.fetchAndSave(soknadId) }
                .onFailure { logger.error("Feil i innhenting av Register-data", it) }
        }
    }
}
