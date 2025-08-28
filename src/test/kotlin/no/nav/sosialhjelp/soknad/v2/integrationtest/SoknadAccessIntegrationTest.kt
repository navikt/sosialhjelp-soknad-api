package no.nav.sosialhjelp.soknad.v2.integrationtest

import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.scheduled.createMetadata
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class SoknadAccessIntegrationTest : AbstractIntegrationTest() {
    @Test
    fun `Status OPPRETTET skal ikke returnere feil`() {
        createMetadataAndSoknad(LocalDateTime.now(), SoknadStatus.OPPRETTET, personId = userId)
            .let { soknadId -> doGetFullResponse(uri = testUrl(soknadId)) }
            .expectStatus().isOk
    }

    @Test
    fun `Status INNSENDING_FEILET skal ikke returnere feil`() {
        createMetadataAndSoknad(LocalDateTime.now(), SoknadStatus.INNSENDING_FEILET, personId = userId)
            .let { soknadId -> doGetFullResponse(uri = testUrl(soknadId)) }
            .expectStatus().isOk
    }

    @Test
    fun `Status SENDT skal returnere feil`() {
        createMetadataAndSoknad(LocalDateTime.now(), SoknadStatus.SENDT, personId = userId)
            .let { soknadId -> doGetFullResponse(uri = testUrl(soknadId)) }
            .expectStatus().isForbidden
    }

    @Test
    fun `Status MOTTATT_FSL skal returnere feil`() {
        createMetadataAndSoknad(LocalDateTime.now(), SoknadStatus.MOTTATT_FSL, personId = userId)
            .let { soknadId -> doGetFullResponse(uri = testUrl(soknadId)) }
            .expectStatus().isForbidden
    }

    companion object {
        private fun testUrl(soknadId: UUID) = "/soknad/$soknadId/isKort"
    }

    private fun createMetadataAndSoknad(
        opprettet: LocalDateTime,
        status: SoknadStatus,
        sendtInn: LocalDateTime = LocalDateTime.now(),
        personId: String,
    ): UUID {
        val soknadId = metadataRepository.createMetadata(opprettet, status, sendtInn = sendtInn, personId = personId)
        opprettSoknad(id = soknadId).also { soknadRepository.save(it) }

        return soknadId
    }
}
