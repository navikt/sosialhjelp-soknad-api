package no.nav.sosialhjelp.soknad.v2.integrationtest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.sosialhjelp.soknad.api.dittnav.dto.PabegyntSoknadDto
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.scheduled.createMetadata
import no.nav.sosialhjelp.soknad.v2.scheduled.nowMinusDays
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDateTime
import java.util.UUID

class DittNavIntegrationTest : AbstractIntegrationTest() {
    @BeforeEach
    override fun before() {
        useTokenX = true
        super.before()
    }

    @Test
    fun `Skal returnere riktig antall open soknader`() {
        createMetadataAndSoknad(nowMinusDays(5), SoknadStatus.OPPRETTET, personId = userId)
        createMetadataAndSoknad(nowMinusDays(5), SoknadStatus.INNSENDING_FEILET, personId = userId)
        createMetadataAndSoknad(nowMinusDays(5), SoknadStatus.SENDT, personId = userId)

        doGet(uri = url, responseBodyClass = String::class.java)
            .let { jacksonObjectMapper().readValue(it) as List<PabegyntSoknadDto> }
            .also { assertThat(it).hasSize(2) }
            .map { soknadMetadataRepository.findByIdOrNull(UUID.fromString(it.grupperingsId)) }
            .also { metadata ->
                assertThat(metadata)
                    .hasSize(2)
                    .allMatch { it!!.status == SoknadStatus.OPPRETTET || it.status == SoknadStatus.INNSENDING_FEILET }
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
