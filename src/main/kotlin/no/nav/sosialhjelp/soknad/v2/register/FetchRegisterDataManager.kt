package no.nav.sosialhjelp.soknad.v2.register

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.slf4j.MDCContext
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.exceptions.AuthorizationException
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

interface RegisterDataFetcher {
    fun fetchAndSave(
        soknadId: UUID,
        userContext: UserContext,
    )

    // Implementeres og settes til true av fetchere som skal stoppe hele prosessen
    fun exceptionOnError(): Boolean = false
}

// Fetcher som må kjøres først
interface PrimaryFetcher : RegisterDataFetcher

// Fetchere som må kjøres før request returneres
interface SynchronousFetcher : RegisterDataFetcher

// Request kan returnere og disse kan kjøre i bakgrunn
interface AsynchronousFetcher : RegisterDataFetcher

@Service
class FetchRegisterDataManager(
    private val allFetchers: List<RegisterDataFetcher>,
) {
    private val logger by logger()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Transactional(propagation = Propagation.NEVER)
    fun runAllRegisterDataFetchers(soknadId: UUID) {
        logger.info("Henter Register-data")

        val userContext =
            UserContext(
                SubjectHandlerUtils.getToken(),
                SubjectHandlerUtils.getUserIdFromToken(),
            )

        runPrimaryFetcher(soknadId, userContext, allFetchers.filterIsInstance<PrimaryFetcher>().firstOrNull())
        runSynchronousFetchers(soknadId, userContext, allFetchers.filterIsInstance<SynchronousFetcher>())
        runAsyncFetchers(soknadId, userContext, allFetchers.filterIsInstance<AsynchronousFetcher>())
    }

    private fun runPrimaryFetcher(
        soknadId: UUID,
        userContext: UserContext,
        primaryFetcher: PrimaryFetcher?,
    ) {
        if (primaryFetcher == null) error("Finnes ingen PrimaryFetcher")
        runFetcher(soknadId, userContext, primaryFetcher)
    }

    private fun runSynchronousFetchers(
        soknadId: UUID,
        personId: UserContext,
        fetchers: List<SynchronousFetcher>,
    ) {
        runBlocking(Dispatchers.IO + MDCContext()) {
            fetchers
                .map { fetcher -> async { runFetcher(soknadId, personId, fetcher) } }
                .awaitAll()
        }
    }

    private fun runAsyncFetchers(
        soknadId: UUID,
        personId: UserContext,
        fetchers: List<AsynchronousFetcher>,
    ) {
        fetchers.forEach { fetcher ->
            scope.launch(MDCContext()) {
                runFetcher(soknadId, personId, fetcher)
            }
        }
    }

    private fun runFetcher(
        soknadId: UUID,
        userContext: UserContext,
        fetcher: RegisterDataFetcher,
    ) {
        runCatching {
            logger.info("${Thread.currentThread().name} running fetcher: $fetcher")
            fetcher.fetchAndSave(soknadId, userContext)
            logger.info("${Thread.currentThread().name} finished fetcher: $fetcher")
        }
            .onFailure {
                if (it is AuthorizationException) throw it

                logger.warn("Registerdata-fetcher feilet: $fetcher", it)
                if (fetcher.exceptionOnError()) throw it
            }
    }
}

data class UserContext(
    val token: String,
    val userId: String,
)
