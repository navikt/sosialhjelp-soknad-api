package no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg

import no.nav.sosialhjelp.soknad.business.db.RepositoryTestSupport
import no.nav.sosialhjelp.soknad.config.DbTestConfig
import no.nav.sosialhjelp.soknad.domain.OpplastetVedlegg
import no.nav.sosialhjelp.soknad.domain.VedleggType
import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils.getSha512FromByteArray
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import javax.inject.Inject

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [DbTestConfig::class])
@ActiveProfiles("test")
internal class BatchOpplastetVedleggRepositoryJdbcTest {

    @Inject
    private val opplastetVedleggRepository: OpplastetVedleggRepository? = null

    @Inject
    private val batchOpplastetVedleggRepository: BatchOpplastetVedleggRepository? = null

    @Inject
    private val soknadRepositoryTestSupport: RepositoryTestSupport? = null

    @AfterEach
    fun tearDown() {
        soknadRepositoryTestSupport!!.jdbcTemplate.update("delete from OPPLASTET_VEDLEGG")
        soknadRepositoryTestSupport.jdbcTemplate.update("delete from SOKNAD_UNDER_ARBEID")
    }

    @Test
    fun slettAlleVedleggForSoknadSletterAlleOpplastedeVedleggForGittSoknadId() {
        val uuid = opprettOpplastetVedleggOgLagreIDb(lagOpplastetVedlegg(), EIER)
        val uuidSammeSoknadOgEier = opprettOpplastetVedleggOgLagreIDb(lagOpplastetVedlegg(EIER, TYPE, SOKNADID), EIER)
        val uuidSammeEierOgAnnenSoknad =
            opprettOpplastetVedleggOgLagreIDb(lagOpplastetVedlegg(EIER, TYPE2, SOKNADID3), EIER)
        batchOpplastetVedleggRepository!!.slettAlleVedleggForSoknad(SOKNADID)
        Assertions.assertThat(opplastetVedleggRepository!!.hentVedlegg(uuid, EIER)).isEmpty
        Assertions.assertThat(opplastetVedleggRepository.hentVedlegg(uuidSammeSoknadOgEier, EIER)).isEmpty
        Assertions.assertThat(opplastetVedleggRepository.hentVedlegg(uuidSammeEierOgAnnenSoknad, EIER)).isPresent
    }

    private fun lagOpplastetVedlegg(
        eier: String = EIER,
        type: String = TYPE,
        soknadId: Long = SOKNADID,
    ): OpplastetVedlegg {
        return OpplastetVedlegg()
            .withEier(eier)
            .withVedleggType(VedleggType(type))
            .withData(DATA)
            .withSoknadId(soknadId)
            .withFilnavn(FILNAVN)
            .withSha512(SHA512)
    }

    private fun opprettOpplastetVedleggOgLagreIDb(opplastetVedlegg: OpplastetVedlegg, eier: String): String {
        return opplastetVedleggRepository!!.opprettVedlegg(opplastetVedlegg, eier)
    }

    companion object {
        private const val EIER = "12345678901"
        private val DATA = byteArrayOf(1, 2, 3, 4)
        private val SHA512 = getSha512FromByteArray(DATA)
        private const val TYPE = "bostotte|annetboutgift"
        private const val TYPE2 = "dokumentasjon|aksjer"
        private const val SOKNADID = 1L
        private const val SOKNADID2 = 2L
        private const val SOKNADID3 = 3L
        private const val FILNAVN = "dokumentasjon.pdf"
    }
}
