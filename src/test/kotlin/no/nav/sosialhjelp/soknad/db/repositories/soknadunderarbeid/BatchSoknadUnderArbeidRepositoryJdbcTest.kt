package no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@ActiveProfiles("no-redis", "test")
internal class BatchSoknadUnderArbeidRepositoryJdbcTest {

    @Autowired
    private lateinit var soknadUnderArbeidRepository: SoknadUnderArbeidRepository

    @Autowired
    private lateinit var batchSoknadUnderArbeidRepository: BatchSoknadUnderArbeidRepository

    @Test
    fun hentSoknaderForBatchSkalFinneGamleSoknader() {
        val skalIkkeSlettes = lagSoknadUnderArbeid(BEHANDLINGSID, 13)
        val skalIkkeSlettesId = soknadUnderArbeidRepository.opprettSoknad(skalIkkeSlettes, EIER)
        val skalSlettes = lagSoknadUnderArbeid("annen_behandlingsid", 14)
        val skalSlettesId = soknadUnderArbeidRepository.opprettSoknad(skalSlettes, EIER)
        val soknader = batchSoknadUnderArbeidRepository.hentGamleSoknadUnderArbeidForBatch()
        assertThat(soknader).hasSize(1)
        assertThat(soknader[0]).isEqualTo(skalSlettesId).isNotEqualTo(skalIkkeSlettesId)
    }

    @Test
    fun slettSoknadGittSoknadUnderArbeidIdSkalSletteSoknad() {
        val soknadUnderArbeid = lagSoknadUnderArbeid(BEHANDLINGSID, 15)
        val soknadUnderArbeidId = soknadUnderArbeidRepository.opprettSoknad(soknadUnderArbeid, EIER)
            ?: throw RuntimeException("Kunne ikke finne søknad")

        batchSoknadUnderArbeidRepository.slettSoknad(soknadUnderArbeidId)
        assertThat(soknadUnderArbeidRepository.hentSoknad(soknadUnderArbeidId, EIER)).isNull()
    }

    private fun lagSoknadUnderArbeid(behandlingsId: String, antallDagerSiden: Int): SoknadUnderArbeid {
        return SoknadUnderArbeid(
            versjon = 1L,
            behandlingsId = behandlingsId,
            tilknyttetBehandlingsId = TILKNYTTET_BEHANDLINGSID,
            eier = EIER,
            jsonInternalSoknad = JSON_INTERNAL_SOKNAD,
            status = SoknadUnderArbeidStatus.UNDER_ARBEID,
            opprettetDato = LocalDateTime.now().minusDays(antallDagerSiden.toLong()).minusMinutes(5),
            sistEndretDato = LocalDateTime.now().minusDays(antallDagerSiden.toLong()).minusMinutes(5)
        )
    }

    companion object {
        private const val EIER = "12345678901"
        private const val BEHANDLINGSID = "1100020"
        private const val TILKNYTTET_BEHANDLINGSID = "4567"
        private val JSON_INTERNAL_SOKNAD = JsonInternalSoknad()
    }
}
