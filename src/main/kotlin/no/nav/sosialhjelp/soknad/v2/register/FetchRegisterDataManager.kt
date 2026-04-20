package no.nav.sosialhjelp.soknad.v2.register

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.slf4j.MDCContext
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.exceptions.AuthorizationException
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import org.slf4j.MDC
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

interface RegisterDataFetcher {
    suspend fun fetchAndSave(
        soknadId: UUID,
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
    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Transactional(propagation = Propagation.NEVER)
    fun runAllRegisterDataFetchers(soknadId: UUID) {
        if (allFetchers.filterIsInstance<PrimaryFetcher>().size > 1) error("Det finnes mer enn en PrimaryFetcher")

        logger.info("Henter Register-data")

        val currentUserContext =
            UserContextElement(
                userToken = SubjectHandlerUtils.getToken(),
                userId = SubjectHandlerUtils.getUserIdFromToken(),
            )

        runPrimaryFetcher(soknadId, currentUserContext, allFetchers.filterIsInstance<PrimaryFetcher>().firstOrNull())
        runSynchronousFetchers(soknadId, currentUserContext, allFetchers.filterIsInstance<SynchronousFetcher>())
        runAsyncFetchers(soknadId, currentUserContext, allFetchers.filterIsInstance<AsynchronousFetcher>())
    }

    private fun runPrimaryFetcher(
        soknadId: UUID,
        currentUserContext: UserContextElement,
        primaryFetcher: PrimaryFetcher?,
    ) {
        if (primaryFetcher == null) error("Finnes ingen PrimaryFetcher")
        runBlocking(MDCContext() + currentUserContext) {
            runFetcher(soknadId, primaryFetcher)
        }
    }

    private fun runSynchronousFetchers(
        soknadId: UUID,
        currentUserContext: UserContextElement,
        fetchers: List<SynchronousFetcher>,
    ) {
        runBlocking(Dispatchers.IO + MDCContext() + currentUserContext) {
            fetchers
                .map { fetcher -> async { runFetcher(soknadId, fetcher) } }
                .awaitAll()
        }
    }

    private fun runAsyncFetchers(
        soknadId: UUID,
        currentUserContext: UserContextElement,
        fetchers: List<AsynchronousFetcher>,
    ) {
        val mdcSnapshot = MDC.getCopyOfContextMap()
        fetchers.forEach { fetcher ->
            backgroundScope.launch(MDCContext(mdcSnapshot) + currentUserContext) {
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

internal suspend fun currentUserContext(): UserContextElement =
    currentCoroutineContext()[UserContextElement] ?: error("Fant ikke userContext")

class UserContextElement(
    val userToken: String,
    val userId: String,
) : AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<UserContextElement>
}
