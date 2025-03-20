package no.nav.sosialhjelp.soknad.v2.scheduled

import no.nav.sosialhjelp.soknad.nowWithMillis
import no.nav.sosialhjelp.soknad.v2.integrationtest.AbstractIntegrationTest
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.metadata.Tidspunkt
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadJobService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import java.util.UUID

class SoknadJobServiceTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var soknadJobService: SoknadJobService

    @Test
    fun `Finder skal kun returnere Ids fra eksisterende soknader`() {
        createMetadataAndSoknad(LocalDateTime.now().minusDays(15), SoknadStatus.OPPRETTET)
        createMetadataAndSoknad(LocalDateTime.now().minusDays(16), SoknadStatus.OPPRETTET)
        createMetadataAndSoknad(LocalDateTime.now().minusDays(17), SoknadStatus.OPPRETTET)

        val kunMetadata =
            SoknadMetadata(
                soknadId = UUID.randomUUID(),
                personId = "12345612345",
                tidspunkt = Tidspunkt(opprettet = LocalDateTime.now().minusDays(18)),
                status = SoknadStatus.OPPRETTET,
            ).also { soknadMetadataRepository.save(it) }

        soknadJobService.findSoknadIdsOlderThanWithStatus(nowWithMillis().minusDays(14), SoknadStatus.OPPRETTET)
            .also { ids ->
                assertThat(ids)
                    .hasSize(3)
                    .noneMatch { it == kunMetadata.soknadId }
            }
    }

    private fun createMetadataAndSoknad(
        opprettet: LocalDateTime,
        status: SoknadStatus,
    ): UUID {
        val soknadId = soknadMetadataRepository.createMetadata(opprettet, status)
        opprettSoknad(id = soknadId).also { soknadRepository.save(it) }

        return soknadId
    }
}
