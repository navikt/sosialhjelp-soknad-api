package no.nav.sosialhjelp.soknad.v2.register

import no.nav.sosialhjelp.soknad.app.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class FetchRegisterDataManagerTest {
    @Test
    fun `should run asynchronous fetchers sequentially`() {
        val firstFetcherStarted = CountDownLatch(1)
        val finishFirstFetcher = CountDownLatch(1)
        val secondFetcherStarted = CountDownLatch(1)
        val primaryFetcher =
            object : PrimaryFetcher {
                override suspend fun fetchAndSave(soknadId: UUID) = Unit
            }
        val firstFetcher =
            object : AsynchronousFetcher {
                override suspend fun fetchAndSave(soknadId: UUID) {
                    firstFetcherStarted.countDown()
                    check(finishFirstFetcher.await(5, TimeUnit.SECONDS))
                }
            }
        val secondFetcher =
            object : AsynchronousFetcher {
                override suspend fun fetchAndSave(soknadId: UUID) {
                    secondFetcherStarted.countDown()
                }
            }
        val manager = FetchRegisterDataManager(listOf(primaryFetcher, firstFetcher, secondFetcher))

        try {
            SubjectHandlerUtils.setNewSubjectHandlerImpl(StaticSubjectHandlerImpl())
            manager.runAllRegisterDataFetchers(UUID.randomUUID())

            assertTrue(firstFetcherStarted.await(5, TimeUnit.SECONDS))
            assertFalse(secondFetcherStarted.await(200, TimeUnit.MILLISECONDS))

            finishFirstFetcher.countDown()
            assertTrue(secondFetcherStarted.await(5, TimeUnit.SECONDS))
        } finally {
            finishFirstFetcher.countDown()
            SubjectHandlerUtils.resetSubjectHandlerImpl()
        }
    }
}
