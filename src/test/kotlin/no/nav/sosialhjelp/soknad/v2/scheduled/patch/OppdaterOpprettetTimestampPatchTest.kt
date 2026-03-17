package no.nav.sosialhjelp.soknad.v2.scheduled.patch

import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.v2.json.generate.TimestampUtil.nowWithMillis
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.v2.metadata.Tidspunkt
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.jdbc.core.JdbcAggregateTemplate
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("no-redis", "test", "test-container")
class OppdaterOpprettetTimestampPatchTest {

    @Autowired
    private lateinit var oppdaterOpprettetTimestampPatch: OppdaterOpprettetTimestampPatch

    @Autowired
    private lateinit var template: JdbcAggregateTemplate

    @Autowired
    private lateinit var metadataRepository: SoknadMetadataRepository

    private val totalMetadatas = 500000
    private val antallThreads = 10

    fun setup() {
        runBlocking {

            val start = LocalDateTime.now()
            logger.info("Starter generering ($start) av $totalMetadatas metadatas")

            (1..antallThreads)
                .map {
                    async(Dispatchers.IO){
                        createMetadatas(totalMetadatas/antallThreads)
                    }
                }.awaitAll()

            val end = LocalDateTime.now()
            logger.info("Jobb tok ${Duration.between(start, end)} for å generere $totalMetadatas metadatas.")
        }
    }

    private suspend fun createMetadatas(antall: Int) {
        val metadatas: MutableList<SoknadMetadata> = mutableListOf()

        for (i in 1..antall) {
            SoknadMetadata(
                soknadId = UUID.randomUUID(),
                personId = "12345678910",
                tidspunkt = Tidspunkt(opprettet = nowWithMillis().minusHours(2).minusMinutes(1))
            ).also { metadatas.add(it) }

            if (i % 25000 == 0) {
                logger.info("${Thread.currentThread().name}: Generert $i metadatas.")
                template.insertAll(metadatas)
                metadatas.clear()
            }
        }
    }

    @Test
    @Disabled("Testen tar for lang tid, og er kun ment for manuell kjøring ved behov.")
    fun oppdaterOpprettetTimestamp() {
        metadataRepository.deleteAll()
        setup()

        metadataRepository.findSoknadIdsOlderThan(nowWithMillis().minusHours(2))
            .also { soknadIds -> assertThat(soknadIds).hasSize(totalMetadatas) }

        oppdaterOpprettetTimestampPatch.oppdaterOpprettetTimestamp()

        metadataRepository.findSoknadIdsOlderThan(nowWithMillis().minusHours(2))
            .also { soknadIds -> assertThat(soknadIds).isEmpty() }
    }

    companion object {
        private val logger by logger()
    }
}