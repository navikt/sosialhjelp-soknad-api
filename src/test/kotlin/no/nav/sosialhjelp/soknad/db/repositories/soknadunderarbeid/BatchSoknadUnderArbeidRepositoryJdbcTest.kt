package no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@ActiveProfiles("no-redis", "test", "test-container")
internal class BatchSoknadUnderArbeidRepositoryJdbcTest {
    @Autowired
    private lateinit var soknadUnderArbeidRepository: SoknadUnderArbeidRepository

    @Autowired
    private lateinit var batchSoknadUnderArbeidRepository: BatchSoknadUnderArbeidRepository

    @Test
    fun hentSoknaderForBatchSkalFinneGamleSoknader() {
        val skalIkkeSlettes = lagSoknadUnderArbeid(BEHANDLINGSID, 13)
        val skalIkkeSlettesId = soknadUnderArbeidRepository.opprettSoknad(skalIkkeSlettes, EIER)
        val skalSlettes = lagSoknadUnderArbeid(UUID.randomUUID().toString(), 14)
        val skalSlettesId = soknadUnderArbeidRepository.opprettSoknad(skalSlettes, EIER)
        val soknader = batchSoknadUnderArbeidRepository.hentGamleSoknadUnderArbeidForBatch()
        assertThat(soknader).hasSize(1)
        assertThat(soknader[0]).isEqualTo(skalSlettesId).isNotEqualTo(skalIkkeSlettesId)
    }

    @Test
    fun slettSoknadGittSoknadUnderArbeidIdSkalSletteSoknad() {
        val soknadUnderArbeid = lagSoknadUnderArbeid(BEHANDLINGSID, 15)
        val soknadUnderArbeidId =
            soknadUnderArbeidRepository.opprettSoknad(soknadUnderArbeid, EIER)
                ?: throw RuntimeException("Kunne ikke finne søknad")

        batchSoknadUnderArbeidRepository.slettSoknad(soknadUnderArbeidId)
        assertThat(soknadUnderArbeidRepository.hentSoknad(soknadUnderArbeidId, EIER)).isNull()
    }

    @Test
    fun `Slettejobb skal slette soknader som er eldre enn 14 dager fra opprettet, ikke sistendret`() {
        val id =
            lagSoknadUnderArbeid(BEHANDLINGSID, 20)
                .apply { sistEndretDato = LocalDateTime.now().minusDays(2) }
                .let { soknadUnderArbeidRepository.opprettSoknad(it, EIER) }

        val gamleSoknader = batchSoknadUnderArbeidRepository.hentGamleSoknadUnderArbeidForBatch()
        assertThat(gamleSoknader.size).isEqualTo(1)
        assertThat(gamleSoknader).contains(id)
    }

    private fun lagSoknadUnderArbeid(
        behandlingsId: String,
        antallDagerSiden: Int,
    ): SoknadUnderArbeid {
        return SoknadUnderArbeid(
            versjon = 1L,
            behandlingsId = behandlingsId,
            eier = EIER,
            jsonInternalSoknad = JSON_INTERNAL_SOKNAD,
            status = SoknadUnderArbeidStatus.UNDER_ARBEID,
            opprettetDato = LocalDateTime.now().minusDays(antallDagerSiden.toLong()).minusMinutes(5),
            sistEndretDato = LocalDateTime.now().minusDays(antallDagerSiden.toLong()).minusMinutes(5),
        )
    }

    companion object {
        private const val EIER = "12345678901"
        private val BEHANDLINGSID = UUID.randomUUID().toString()
        private val JSON_INTERNAL_SOKNAD = JsonInternalSoknad()
    }
}
