package no.nav.sosialhjelp.soknad.v2.integrationtest

import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.scheduled.createMetadata
import no.nav.sosialhjelp.soknad.v2.scheduled.nowMinusDays
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class DittNavIntegrationTest : AbstractIntegrationTest() {
    @Test
    fun `Skal returnere riktig antall open soknader`() {
        createMetadataAndSoknad(nowMinusDays(5), SoknadStatus.OPPRETTET, personId = userId)
        createMetadataAndSoknad(nowMinusDays(5), SoknadStatus.INNSENDING_FEILET, personId = userId)
        createMetadataAndSoknad(nowMinusDays(5), SoknadStatus.SENDT, personId = userId)

        doGet(uri = url, responseBodyClass = List::class.java)
            .also {
                it.get(1)
            }
    }

    private fun createMetadataAndSoknad(
        opprettet: LocalDateTime,
        status: SoknadStatus,
        sendtInn: LocalDateTime = LocalDateTime.now(),
        personId: String,
    ): UUID {
        val soknadId = soknadMetadataRepository.createMetadata(opprettet, status, sendtInn = sendtInn, personId = personId)
        opprettSoknad(id = soknadId).also { soknadRepository.save(it) }

        return soknadId
    }

    companion object {
        private val url get() = "/dittnav/pabegynte/aktive"
    }
}
