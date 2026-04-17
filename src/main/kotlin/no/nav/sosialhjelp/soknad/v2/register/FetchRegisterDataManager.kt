package no.nav.sosialhjelp.soknad.v2.register

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

interface RegisterDataFetcher {
    suspend fun fetchAndSave(soknadId: UUID)

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

        runBlocking(userContext + Dispatchers.IO + MDCContext()) {
            runPrimaryFetcher(soknadId, allFetchers.filterIsInstance<PrimaryFetcher>().firstOrNull())
            runSynchronousFetchers(soknadId, allFetchers.filterIsInstance<SynchronousFetcher>())
            runAsyncFetchers(soknadId, allFetchers.filterIsInstance<AsynchronousFetcher>())
        }
    }

    private suspend fun runPrimaryFetcher(
        soknadId: UUID,
        primaryFetcher: PrimaryFetcher?,
    ) {
        if (primaryFetcher == null) error("Finnes ingen PrimaryFetcher")
        runFetcher(soknadId, primaryFetcher)
    }

    private suspend fun runSynchronousFetchers(
        soknadId: UUID,
        fetchers: List<SynchronousFetcher>,
    ) {
        coroutineScope {
            fetchers
                .map { fetcher -> async { runFetcher(soknadId, fetcher) } }
                .awaitAll()
        }
    }

    private suspend fun runAsyncFetchers(
        soknadId: UUID,
        fetchers: List<AsynchronousFetcher>,
    ) {
        val userContext = coroutineContext[UserContext]!!
        fetchers.forEach { fetcher ->
            scope.launch(MDCContext() + userContext) {
                runFetcher(soknadId, fetcher)
            }
        }
    }

    private suspend fun runFetcher(
        soknadId: UUID,
        fetcher: RegisterDataFetcher,
    ) {
        runCatching {
            logger.info("${Thread.currentThread().name} running fetcher: $fetcher")
            fetcher.fetchAndSave(soknadId)
            logger.info("${Thread.currentThread().name} finished fetcher: $fetcher")
        }
            .onFailure {
                if (it is AuthorizationException) throw it

                logger.warn("Registerdata-fetcher feilet: $fetcher", it)
                if (fetcher.exceptionOnError()) throw it
            }
    }
}

class UserContext(
    val token: String,
    val userId: String,
) : CoroutineContext.Element {
    override val key: CoroutineContext.Key<*> get() = Key

    companion object Key : CoroutineContext.Key<UserContext>
}

suspend fun currentUserContext(): UserContext =
    coroutineContext[UserContext] ?: error("No UserContext in coroutine context")
