package no.nav.sosialhjelp.soknad.v2.register

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import org.springframework.stereotype.Service
import java.util.UUID

interface RegisterDataFetcher {
    fun fetchAndSave(soknadId: UUID)

    // Implementeres og settes til false av fetchere som skal stoppe hele prosessen
    fun continueOnError(): Boolean = true
}

@Service
class RegisterDataService(
    private val allFetchers: List<RegisterDataFetcher>,
) {
    private val logger by logger()

    // TODO Pakker inn logikken for skyggeprod slik at ingen Exception kastes
    fun runAllFetchersForShadowProd(soknadId: UUID) {
        runCatching { doRunListedFetchers(soknadId, allFetchers) }
            .onFailure { logger.warn("NyModell: Feil i henting av Registerdata for skyggeprod", it) }
    }

    fun runAllRegisterDataFetchers(soknadId: UUID) {
        logger.info("NyModell: Henter Register-data")
        doRunListedFetchers(soknadId = soknadId, listedFetchers = allFetchers)
    }

    fun runSpecificFetchers(
        soknadId: UUID,
        listedFetchers: List<RegisterDataFetcher>,
    ) {
        logger.info("NyModell: Henter Register-data: ${listedFetchers.joinToString(separator = ", ")}")
        doRunListedFetchers(soknadId = soknadId, listedFetchers = listedFetchers)
    }

    private fun doRunListedFetchers(
        soknadId: UUID,
        listedFetchers: List<RegisterDataFetcher>,
    ) {
        listedFetchers.forEach { fetcher ->
            runCatching { fetcher.fetchAndSave(soknadId) }
                .onFailure {
                    logger.warn("NyModell: Registerdata-fetcher feilet: $fetcher", it)
                    if (!fetcher.continueOnError()) throw it
                }
        }
    }
}
