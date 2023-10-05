package no.nav.sosialhjelp.soknad.repository.opplastetvedlegg

import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.BatchOpplastetVedleggRepository
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.BatchOpplastetVedleggRepositoryJdbc
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedlegg
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggRepository
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggRepositoryJdbc
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggType
import no.nav.sosialhjelp.soknad.repository.RepositoryTest
import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils.getSha512FromByteArray
import org.apache.commons.lang3.RandomUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class BatchOpplastetVedleggRepositoryJdbcTest : RepositoryTest() {

    private var opplastetVedleggRepository: OpplastetVedleggRepository? = null
    private var batchOpplastetVedleggRepository: BatchOpplastetVedleggRepository? = null

    @BeforeEach
    fun setup() {
        if (opplastetVedleggRepository == null) {
            opplastetVedleggRepository = OpplastetVedleggRepositoryJdbc(jdbcTemplate)
        }
        if (batchOpplastetVedleggRepository == null) {
            batchOpplastetVedleggRepository = BatchOpplastetVedleggRepositoryJdbc(jdbcTemplate)
        }
    }

    @Test
    fun slettAlleVedleggForSoknadSletterAlleOpplastedeVedleggForGittSoknadId() {
        val uuid = opprettOpplastetVedleggOgLagreIDb(lagOpplastetVedlegg(), EIER)
        val uuidSammeSoknadOgEier = opprettOpplastetVedleggOgLagreIDb(
            lagOpplastetVedlegg(EIER, TYPE, SOKNADID),
            EIER
        )
        val uuidSammeEierOgAnnenSoknad =
            opprettOpplastetVedleggOgLagreIDb(
                lagOpplastetVedlegg(EIER, TYPE2, SOKNADID3), EIER
            )
        batchOpplastetVedleggRepository!!.slettAlleVedleggForSoknad(SOKNADID)
        assertThat(opplastetVedleggRepository!!.hentVedlegg(uuid, EIER)).isNull()
        assertThat(opplastetVedleggRepository!!.hentVedlegg(uuidSammeSoknadOgEier, EIER)).isNull()
        assertThat(opplastetVedleggRepository!!.hentVedlegg(uuidSammeEierOgAnnenSoknad, EIER)).isNotNull
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
            sha512 = getSha512FromByteArray(DATA)
        )
    }

    private fun opprettOpplastetVedleggOgLagreIDb(opplastetVedlegg: OpplastetVedlegg, eier: String): String {
        return opplastetVedleggRepository!!.opprettVedlegg(opplastetVedlegg, eier)
    }

    companion object {
        private const val EIER = "12345678901"
        private val DATA = RandomUtils.nextBytes(10)
        private const val TYPE = "bostotte|annetboutgift"
        private const val TYPE2 = "dokumentasjon|aksjer"
        private const val SOKNADID = 1L
        private const val SOKNADID3 = 3L
        private const val FILNAVN = "dokumentasjon.pdf"
    }
}
