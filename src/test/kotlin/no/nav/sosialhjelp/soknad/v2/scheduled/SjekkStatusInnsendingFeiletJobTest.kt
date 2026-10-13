package no.nav.sosialhjelp.soknad.v2.scheduled

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import no.nav.sosialhjelp.soknad.innsending.digisosapi.InnsendingStatusDto
import no.nav.sosialhjelp.soknad.innsending.digisosapi.ReadClient
import no.nav.sosialhjelp.soknad.v2.json.generate.TimestampUtil
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.opprettSoknadMetadata
import no.nav.sosialhjelp.soknad.v2.scheduled.jobs.SjekkStatusInnsendingFeiletJob
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

class SjekkStatusInnsendingFeiletJobTest : AbstractJobTest() {
    @Autowired
    private lateinit var sjekkStatusInnsendingFeiletJob: SjekkStatusInnsendingFeiletJob

    @MockkBean
    private lateinit var readClient: ReadClient

    private val requestedIds = slot<List<UUID>>()

    @BeforeEach
    fun setup() {
        metadataRepository.deleteAll()
    }

    @Test
    fun `Alle relevante soknader som er mottatt hos FIKS far oppdatert status og metadata`() {
        val soknader = List(2) { metadataRepository.save(failedMetadata()) }
        val mottattTidspunkt = LocalDateTime.now().minusDays(1)
        val mottatteSoknader =
            soknader.mapIndexed { index, metadata ->
                mottattSoknad(
                    metadata.soknadId,
                    mottattTidspunkt,
                    UUID.randomUUID(),
                )
            }

        every { readClient.getInnsendingsstatus(any()) } returns mottatteSoknader

        sjekkStatusInnsendingFeiletJob.sjekkStatusInnsendingFeilet()

        verify(exactly = 1) { readClient.getInnsendingsstatus(capture(requestedIds)) }
        assertThat(requestedIds.captured).containsExactlyInAnyOrderElementsOf(soknader.map { it.soknadId })
        soknader.forEachIndexed { index, metadata ->
            val oppdatert = metadataRepository.findByIdOrNull(metadata.soknadId)!!
            val forventetMottattTidspunkt =
                TimestampUtil.convertInstantToLocalDateTime(Instant.ofEpochMilli(mottatteSoknader[index].opprettet))
            assertThat(oppdatert.status).isEqualTo(SoknadStatus.SENDT)
            assertThat(oppdatert.digisosId).isEqualTo(mottatteSoknader[index].digisosId)
            assertThat(oppdatert.mottakerKommunenummer).isEqualTo("0301")
            assertThat(oppdatert.tidspunkt.sendtInn).isEqualTo(forventetMottattTidspunkt)
        }
    }

    @Test
    fun `Bare soknadene som er mottatt hos FIKS far oppdatert status`() {
        val soknader = List(2) { metadataRepository.save(failedMetadata()) }
        val mottattTidspunkt = LocalDateTime.now().minusDays(1)
        every { readClient.getInnsendingsstatus(any()) } returns
                listOf(mottattSoknad(soknader.first().soknadId, mottattTidspunkt))

        sjekkStatusInnsendingFeiletJob.sjekkStatusInnsendingFeilet()

        verify(exactly = 1) { readClient.getInnsendingsstatus(capture(requestedIds)) }
        assertThat(requestedIds.captured).containsExactlyInAnyOrderElementsOf(soknader.map { it.soknadId })
        assertThat(metadataRepository.findByIdOrNull(soknader.first().soknadId)!!.status).isEqualTo(SoknadStatus.SENDT)
        assertThat(metadataRepository.findByIdOrNull(soknader.last().soknadId)!!.status).isEqualTo(SoknadStatus.INNSENDING_FEILET)
    }

    @Test
    fun `Ingen soknader far oppdatert status nar FIKS ikke har mottatt noen`() {
        val soknader = List(2) { metadataRepository.save(failedMetadata()) }
        every { readClient.getInnsendingsstatus(any()) } returns emptyList()

        sjekkStatusInnsendingFeiletJob.sjekkStatusInnsendingFeilet()

        verify(exactly = 1) { readClient.getInnsendingsstatus(capture(requestedIds)) }
        assertThat(requestedIds.captured).containsExactlyInAnyOrderElementsOf(soknader.map { it.soknadId })
        soknader.forEach { metadata ->
            assertThat(metadataRepository.findByIdOrNull(metadata.soknadId)!!.status)
                .isEqualTo(SoknadStatus.INNSENDING_FEILET)
        }
    }

    @Test
    fun `Sjekker ikke FIKS nar det ikke finnes soknader med status innsending feilet`() {
        metadataRepository.save(opprettSoknadMetadata(status = SoknadStatus.OPPRETTET))

        sjekkStatusInnsendingFeiletJob.sjekkStatusInnsendingFeilet()

        verify(exactly = 0) { readClient.getInnsendingsstatus(any()) }
    }

    private fun failedMetadata() =
        opprettSoknadMetadata(
            status = SoknadStatus.INNSENDING_FEILET,
            innsendtDato = null,
        )

    private fun mottattSoknad(
        soknadId: UUID,
        opprettet: LocalDateTime,
        digisosId: UUID = UUID.randomUUID(),
    ) = InnsendingStatusDto(
        navEksternRefId = soknadId,
        digisosId = digisosId,
        kommunenummer = "0301",
        opprettet = opprettet.toInstant(ZoneOffset.UTC).toEpochMilli(),
    )
}
