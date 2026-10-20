package no.nav.sosialhjelp.soknad.v2.register

import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.slf4j.MDCContext
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.exceptions.AuthorizationException
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.app.subjecthandler.UserContextElement
import org.slf4j.MDC
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

interface RegisterDataFetcher {
    suspend fun fetchAndSave(
        soknadId: UUID,
    )

    // Implementeres og settes til true av fetchere som skal stoppe hele prosessen
    fun exceptionOnError(): Boolean = false
}

// Fetcher som må kjøres først: Skal bare være 1
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
    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + CoroutineName("Async Fetcher Scope"))

    @PreDestroy
    private fun cancelScope() {
        backgroundScope.cancel("Cancelling async fetcher background scope")
    }

    @Transactional(propagation = Propagation.NEVER)
    fun runAllRegisterDataFetchers(soknadId: UUID) {
        logger.info("Henter Register-data")

        with(createUserContextElement()) {
            runPrimaryFetcher(soknadId, allFetchers.findPrimaryFetcher())
            runSynchronousFetchers(soknadId, allFetchers.filterIsInstance<SynchronousFetcher>())
            runAsyncFetchers(soknadId, allFetchers.filterIsInstance<AsynchronousFetcher>())
        }
    }

    private fun createUserContextElement() =
        UserContextElement(
            userToken = SubjectHandlerUtils.getToken(),
            userId = SubjectHandlerUtils.getUserIdFromToken(),
        )

    private fun UserContextElement.runPrimaryFetcher(
        soknadId: UUID,
        primaryFetcher: PrimaryFetcher?,
    ) {
        if (primaryFetcher == null) error("Finnes ingen PrimaryFetcher")
        runBlocking(MDCContext() + this) {
            runFetcher(soknadId, primaryFetcher)
        }
    }

    private fun UserContextElement.runSynchronousFetchers(
        soknadId: UUID,
        fetchers: List<SynchronousFetcher>,
    ) {
        runBlocking(Dispatchers.IO + MDCContext() + this) {
            fetchers
                .map { fetcher -> async { runFetcher(soknadId, fetcher) } }
                .awaitAll()
        }
    }

    private fun UserContextElement.runAsyncFetchers(
        soknadId: UUID,
        fetchers: List<AsynchronousFetcher>,
    ) {
        val mdcSnapshot = MDC.getCopyOfContextMap()
        backgroundScope.launch(MDCContext(mdcSnapshot) + this) {
            fetchers.forEach { fetcher ->
                runFetcher(soknadId, fetcher)
            }
        }
    }

    private suspend fun runFetcher(
        soknadId: UUID,
        fetcher: RegisterDataFetcher,
    ) {
        runCatching {
            logger.info("${Thread.currentThread().name} running fetcher: ${fetcher::class.simpleName}")
            fetcher.fetchAndSave(soknadId)
            logger.info("${Thread.currentThread().name} finished fetcher: ${fetcher::class.simpleName}")
        }
            .onFailure {
                if (it is AuthorizationException) throw it

                logger.warn("Registerdata-fetcher feilet: ${fetcher::class.simpleName}", it)
                if (fetcher.exceptionOnError()) throw it
            }
    }
}

private fun List<RegisterDataFetcher>.findPrimaryFetcher(): PrimaryFetcher =
    filterIsInstance<PrimaryFetcher>()
        .also { if (it.size > 1) error("Det finnes mer enn en PrimaryFetcher") }
        .firstOrNull()
        ?: error("Fant ikke primaryFetcher")
