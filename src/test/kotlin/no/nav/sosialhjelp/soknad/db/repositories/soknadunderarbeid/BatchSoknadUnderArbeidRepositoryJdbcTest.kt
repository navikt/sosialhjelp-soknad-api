package no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid

import jakarta.inject.Inject
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.TestApplication
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedlegg
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggRepository
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@ActiveProfiles(profiles = ["no-redis", "test"])
@SpringBootTest(classes = [TestApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class BatchSoknadUnderArbeidRepositoryJdbcTest {

    @Inject
    private lateinit var jdbcTemplate: JdbcTemplate

    @Inject
    private lateinit var soknadUnderArbeidRepository: SoknadUnderArbeidRepository

    @Inject
    private lateinit var opplastetVedleggRepository: OpplastetVedleggRepository

    @Inject
    private lateinit var batchSoknadUnderArbeidRepository: BatchSoknadUnderArbeidRepository

    @AfterEach
    fun tearDown() {
        jdbcTemplate.update("delete from OPPLASTET_VEDLEGG")
        jdbcTemplate.update("delete from SOKNAD_UNDER_ARBEID")
    }

    @Test
    fun hentSoknaderForBatchSkalFinneGamleSoknader() {
        val skalIkkeSlettes = lagSoknadUnderArbeid(BEHANDLINGSID, 13)
        val skalIkkeSlettesId = soknadUnderArbeidRepository.opprettSoknad(skalIkkeSlettes, EIER)
        val skalSlettes = lagSoknadUnderArbeid("annen_behandlingsid", 15)
        val skalSlettesId = soknadUnderArbeidRepository.opprettSoknad(skalSlettes, EIER)
        val soknader = batchSoknadUnderArbeidRepository.hentGamleSoknadUnderArbeidForBatch()
        assertThat(soknader).hasSize(1)
        assertThat(soknader[0]).isEqualTo(skalSlettesId).isNotEqualTo(skalIkkeSlettesId)
    }

    @Test
    fun slettSoknadGittSoknadUnderArbeidIdSkalSletteSoknad() {
        val soknadUnderArbeid = lagSoknadUnderArbeid(BEHANDLINGSID, 15)
        val soknadUnderArbeidId = soknadUnderArbeidRepository.opprettSoknad(soknadUnderArbeid, EIER)
        soknadUnderArbeid.soknadId = soknadUnderArbeidId!!
        val opplastetVedleggUuid = opplastetVedleggRepository.opprettVedlegg(lagOpplastetVedlegg(soknadUnderArbeidId), EIER)
        batchSoknadUnderArbeidRepository.slettSoknad(soknadUnderArbeid.soknadId)
        assertThat(soknadUnderArbeidRepository.hentSoknad(soknadUnderArbeidId, EIER)).isNull()
        assertThat(opplastetVedleggRepository.hentVedlegg(opplastetVedleggUuid, EIER)).isNull()
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

    private fun lagOpplastetVedlegg(soknadId: Long): OpplastetVedlegg {
        return OpplastetVedlegg(
            eier = EIER,
            vedleggType = OpplastetVedleggType("bostotte|annetboutgift"),
            data = byteArrayOf(1, 2, 3),
            soknadId = soknadId,
            filnavn = "dokumentasjon.pdf",
            sha512 = "aaa"
        )
    }

    companion object {
        private const val EIER = "12345678901"
        private const val BEHANDLINGSID = "1100020"
        private const val TILKNYTTET_BEHANDLINGSID = "4567"
        private val JSON_INTERNAL_SOKNAD = JsonInternalSoknad()
    }
}
