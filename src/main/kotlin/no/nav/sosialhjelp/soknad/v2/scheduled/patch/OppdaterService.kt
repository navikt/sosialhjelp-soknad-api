package no.nav.sosialhjelp.soknad.v2.scheduled.patch

import java.util.UUID
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class OppdaterService(
    private val metadataRepository: SoknadMetadataRepository,
) {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun updateAllMetadatas(soknadIds: List<UUID>) {

        val chunked = soknadIds.chunked(500)

        logger.info("***JOB*** Deler oppdateringen i ${chunked.size} batcher for å unngå overbelastning av databasen.")

        chunked.forEachIndexed { index, uuids ->
            if (index % 100 == 0) logger.info("Behandler batch $index av ${chunked.size}.")

            val metadatas = metadataRepository.findAllById(uuids)

            metadatas.map {
                val opprettet = it.tidspunkt.opprettet
                it.copy(tidspunkt = it.tidspunkt.copy(opprettet = opprettet.plusHours(1)))
            }
                .also { updated -> metadataRepository.saveAll(updated) }
        }
    }

    companion object {
        private val logger by logger()
    }
}