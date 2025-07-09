package no.nav.sosialhjelp.soknad.v2.integrationtest

import no.nav.sosialhjelp.soknad.api.informasjon.SessionResponse
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus.INNSENDING_FEILET
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus.OPPRETTET
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.scheduled.createMetadata
import no.nav.sosialhjelp.soknad.v2.scheduled.nowMinusDays
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDateTime
import java.util.UUID

class InformasjonIntegrationTest : AbstractIntegrationTest() {
    @BeforeEach
    fun setUp() {
        metadataRepository.deleteAll()
    }

    @Test
    fun `Skal returnere riktige open soknader`() {
        createMetadataAndSoknad(nowMinusDays(10), SoknadStatus.SENDT, personId = userId)
        createMetadataAndSoknad(nowMinusDays(10), INNSENDING_FEILET, personId = userId)
        createMetadataAndSoknad(nowMinusDays(10), OPPRETTET, personId = userId)
        metadataRepository.createMetadata(nowMinusDays(10), SoknadStatus.MOTTATT_FSL, personId = userId)

        val pabegyntSoknadDtos =
            doGet(
                uri = url,
                responseBodyClass = SessionResponse::class.java,
            )
                .let { dto ->
                    assertThat(dto.open).hasSize(2)
                    dto.open
                }

        pabegyntSoknadDtos
            .map { metadataRepository.findByIdOrNull(it.soknadId)!! }
            .also { metadatas ->
                assertThat(metadatas)
                    .allMatch { it.status == OPPRETTET || it.status == INNSENDING_FEILET }
            }
    }

    @Test
    fun `Finnes det soknader som er for gamle skal disse slettes og ikke returneres`() {
        val soknadId1 = createMetadataAndSoknad(nowMinusDays(10), INNSENDING_FEILET, personId = userId)
        val soknadId2 = createMetadataAndSoknad(nowMinusDays(10), OPPRETTET, personId = userId)
        createMetadataAndSoknad(nowMinusDays(20), INNSENDING_FEILET, personId = userId)
        createMetadataAndSoknad(nowMinusDays(15), OPPRETTET, personId = userId)

        metadataRepository.findAll().also { assertThat(it).hasSize(4) }

        doGet(
            uri = url,
            responseBodyClass = SessionResponse::class.java,
        )
            .also { dto ->
                assertThat(dto.open)
                    .hasSize(2)
                    .allMatch { it.soknadId == soknadId1 || it.soknadId == soknadId2 }
            }

        metadataRepository.findAll().also {
            assertThat(it)
                .hasSize(2)
                .allMatch { it.soknadId == soknadId1 || it.soknadId == soknadId2 }
        }
    }

    companion object {
        private val url get() = "/informasjon/session"
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
