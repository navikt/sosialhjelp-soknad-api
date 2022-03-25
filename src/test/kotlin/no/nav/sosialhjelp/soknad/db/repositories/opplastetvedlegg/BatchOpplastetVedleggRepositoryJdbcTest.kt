package no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg

import no.nav.sosialhjelp.soknad.Application
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils.getSha512FromByteArray
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime
import javax.inject.Inject

@ActiveProfiles(profiles = ["no-redis", "test"])
@SpringBootTest(classes = [Application::class])
internal open class BatchOpplastetVedleggRepositoryJdbcTest {

    @Inject
    private lateinit var soknadUnderArbeidRepository: SoknadUnderArbeidRepository

    @Inject
    private lateinit var opplastetVedleggRepository: OpplastetVedleggRepository

    @Inject
    private lateinit var batchOpplastetVedleggRepository: BatchOpplastetVedleggRepository

    @Inject
    private lateinit var jdbcTemplate: JdbcTemplate

    @AfterEach
    fun tearDown() {
        jdbcTemplate.update("delete from OPPLASTET_VEDLEGG")
        jdbcTemplate.update("delete from SOKNAD_UNDER_ARBEID")
    }

    @Test
    fun slettAlleVedleggForSoknadSletterAlleOpplastedeVedleggForGittSoknadId() {
        // add soknadUnderArbeid first
        val soknadId = soknadUnderArbeidRepository.opprettSoknad(lagSoknadUnderArbeid(BEHANDLINGSID), EIER)!!
        val soknadId3 = soknadUnderArbeidRepository.opprettSoknad(lagSoknadUnderArbeid(BEHANDLINGSID3), EIER)!!

        val uuid = opprettOpplastetVedleggOgLagreIDb(lagOpplastetVedlegg(soknadId = soknadId), EIER)
        val uuidSammeSoknadOgEier = opprettOpplastetVedleggOgLagreIDb(lagOpplastetVedlegg(EIER, TYPE, soknadId), EIER)
        val uuidSammeEierOgAnnenSoknad =
            opprettOpplastetVedleggOgLagreIDb(lagOpplastetVedlegg(EIER, TYPE2, soknadId3), EIER)
        batchOpplastetVedleggRepository.slettAlleVedleggForSoknad(soknadId)
        assertThat(opplastetVedleggRepository.hentVedlegg(uuid, EIER)).isNull()
        assertThat(opplastetVedleggRepository.hentVedlegg(uuidSammeSoknadOgEier, EIER)).isNull()
        assertThat(opplastetVedleggRepository.hentVedlegg(uuidSammeEierOgAnnenSoknad, EIER)).isNotNull
    }

    private fun lagOpplastetVedlegg(
        eier: String = EIER,
        type: String = TYPE,
        soknadId: Long,
    ): OpplastetVedlegg {
        return OpplastetVedlegg(
            eier = eier,
            vedleggType = OpplastetVedleggType(type),
            data = DATA,
            soknadId = soknadId,
            filnavn = FILNAVN,
            sha512 = SHA512
        )
    }

    private fun lagSoknadUnderArbeid(behandlingsId: String): SoknadUnderArbeid {
        return SoknadUnderArbeid(
            versjon = 1L,
            behandlingsId = behandlingsId,
            tilknyttetBehandlingsId = null,
            eier = EIER,
            jsonInternalSoknad = null,
            status = SoknadUnderArbeidStatus.UNDER_ARBEID,
            opprettetDato = LocalDateTime.now(),
            sistEndretDato = LocalDateTime.now()
        )
    }

    private fun opprettOpplastetVedleggOgLagreIDb(opplastetVedlegg: OpplastetVedlegg, eier: String): String {
        return opplastetVedleggRepository.opprettVedlegg(opplastetVedlegg, eier)
    }

    companion object {
        private const val EIER = "12345678901"
        private val DATA = byteArrayOf(1, 2, 3, 4)
        private val SHA512 = getSha512FromByteArray(DATA)
        private const val TYPE = "bostotte|annetboutgift"
        private const val TYPE2 = "dokumentasjon|aksjer"
        private const val BEHANDLINGSID = "behandlingsid"
        private const val BEHANDLINGSID3 = "behandlingsid3"
        private const val FILNAVN = "dokumentasjon.pdf"
    }
}
