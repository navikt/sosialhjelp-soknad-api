package no.nav.sosialhjelp.soknad.v2.register

import java.util.UUID
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.exceptions.AuthorizationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

interface RegisterDataFetcher {
    fun fetchAndSave(soknadId: UUID)

    // Implementeres og settes til true av fetchere som skal stoppe hele prosessen
    fun exceptionOnError(): Boolean = false
}

@Service
class RegisterDataService(
    private val allFetchers: List<RegisterDataFetcher>,
) {
    private val logger by logger()

    @Transactional(propagation = Propagation.NEVER)
    fun runAllRegisterDataFetchers(soknadId: UUID) {
        logger.info("Henter Register-data")
        doRunListedFetchers(soknadId = soknadId, listedFetchers = allFetchers)
    }

    private fun doRunListedFetchers(
        soknadId: UUID,
        listedFetchers: List<RegisterDataFetcher>,
    ) {
        listedFetchers.forEach { fetcher ->
            runCatching { fetcher.fetchAndSave(soknadId) }
                .getOrElse {
                    if(it is AuthorizationException) throw it

                    logger.warn("Registerdata-fetcher feilet: $fetcher", it)
                    if (fetcher.exceptionOnError()) throw it
                }
        }
    }
}
