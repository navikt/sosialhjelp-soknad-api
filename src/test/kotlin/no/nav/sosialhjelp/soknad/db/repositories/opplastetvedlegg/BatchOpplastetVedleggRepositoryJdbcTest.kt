package no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg

import no.nav.sosialhjelp.soknad.config.DbTestConfig
import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils.getSha512FromByteArray
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import javax.inject.Inject

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [DbTestConfig::class])
@ActiveProfiles("test")
internal class BatchOpplastetVedleggRepositoryJdbcTest {

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
        val uuid = opprettOpplastetVedleggOgLagreIDb(lagOpplastetVedlegg(), EIER)
        val uuidSammeSoknadOgEier = opprettOpplastetVedleggOgLagreIDb(lagOpplastetVedlegg(EIER, TYPE, SOKNADID), EIER)
        val uuidSammeEierOgAnnenSoknad =
            opprettOpplastetVedleggOgLagreIDb(lagOpplastetVedlegg(EIER, TYPE2, SOKNADID3), EIER)
        batchOpplastetVedleggRepository.slettAlleVedleggForSoknad(SOKNADID)
        assertThat(opplastetVedleggRepository.hentVedlegg(uuid, EIER)).isNull()
        assertThat(opplastetVedleggRepository.hentVedlegg(uuidSammeSoknadOgEier, EIER)).isNull()
        assertThat(opplastetVedleggRepository.hentVedlegg(uuidSammeEierOgAnnenSoknad, EIER)).isNotNull
    }

    private fun lagOpplastetVedlegg(
        eier: String = EIER,
        type: String = TYPE,
        soknadId: Long = SOKNADID,
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

    private fun opprettOpplastetVedleggOgLagreIDb(opplastetVedlegg: OpplastetVedlegg, eier: String): String {
        return opplastetVedleggRepository.opprettVedlegg(opplastetVedlegg, eier)
    }

    companion object {
        private const val EIER = "12345678901"
        private val DATA = byteArrayOf(1, 2, 3, 4)
        private val SHA512 = getSha512FromByteArray(DATA)
        private const val TYPE = "bostotte|annetboutgift"
        private const val TYPE2 = "dokumentasjon|aksjer"
        private const val SOKNADID = 1L
        private const val SOKNADID3 = 3L
        private const val FILNAVN = "dokumentasjon.pdf"
    }
}
