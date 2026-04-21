package no.nav.sosialhjelp.soknad.v2.register

import java.util.UUID
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
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
        logger.info("Henter Register-data")

        createUserContextElement()
            .also { userContext ->
                runPrimaryFetcher(soknadId, userContext, allFetchers.findPrimaryFetcher())
                runSynchronousFetchers(soknadId, userContext, allFetchers.filterIsInstance<SynchronousFetcher>())
                runAsyncFetchers(soknadId, userContext, allFetchers.filterIsInstance<AsynchronousFetcher>())
            }
    }

    private fun createUserContextElement() =
        UserContextElement(
            userToken = SubjectHandlerUtils.getToken(),
            userId = SubjectHandlerUtils.getUserIdFromToken(),
        )

    private fun runPrimaryFetcher(
        soknadId: UUID,
        userContext: UserContextElement,
        primaryFetcher: PrimaryFetcher?,
    ) {
        if (primaryFetcher == null) error("Finnes ingen PrimaryFetcher")
        runBlocking(MDCContext() + userContext) {
            runFetcher(soknadId, primaryFetcher)
        }
    }

    private fun runSynchronousFetchers(
        soknadId: UUID,
        userContext: UserContextElement,
        fetchers: List<SynchronousFetcher>,
    ) {
        runBlocking(Dispatchers.IO + MDCContext() + userContext) {
            fetchers
                .map { fetcher -> async { runFetcher(soknadId, fetcher) } }
                .awaitAll()
        }
    }

    private fun runAsyncFetchers(
        soknadId: UUID,
        userContext: UserContextElement,
        fetchers: List<AsynchronousFetcher>,
    ) {
        val mdcSnapshot = MDC.getCopyOfContextMap()
        fetchers.forEach { fetcher ->
            backgroundScope.launch(MDCContext(mdcSnapshot) + userContext) {
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
            logger.info("MDCContext: ${MDC.getCopyOfContextMap()}")
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

private fun List<RegisterDataFetcher>.findPrimaryFetcher(): PrimaryFetcher =
    filterIsInstance<PrimaryFetcher>()
        .also { if (it.size > 1) error("Det finnes mer enn en PrimaryFetcher") }
        .firstOrNull()
        ?: error("Fant ikke primaryFetcher")
