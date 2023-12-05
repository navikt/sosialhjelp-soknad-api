package no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg

import no.nav.sosialhjelp.soknad.db.DbTestConfig
import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils.getSha512FromByteArray
import org.apache.commons.lang3.RandomUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.UUID

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [DbTestConfig::class])
@ActiveProfiles("test")
internal class OpplastetVedleggRepositoryJdbcTest {

    companion object {
        private const val EIER = "12345678901"
        private const val EIER2 = "22222222222"
        private const val TYPE = "bostotte|annetboutgift"
        private const val TYPE2 = "dokumentasjon|aksjer"
        private const val SOKNADID = 1L
        private const val SOKNADID2 = 2L
        private const val SOKNADID3 = 3L
        private const val FILNAVN = "dokumentasjon.pdf"
    }

    @Autowired
    private lateinit var opplastetVedleggRepository: OpplastetVedleggRepository

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @AfterEach
    fun tearDown() {
        jdbcTemplate.update("delete from OPPLASTET_VEDLEGG")
        jdbcTemplate.update("delete from SOKNAD_UNDER_ARBEID")
    }

    @Test
    fun opprettVedleggOppretterOpplastetVedleggIDatabasen() {
        val opplastetVedlegg = lagOpplastetVedlegg()
        val uuidFraDb = opplastetVedleggRepository.opprettVedlegg(opplastetVedlegg, EIER)
        assertThat(uuidFraDb).isEqualTo(opplastetVedlegg.uuid)
    }

    @Test
    fun hentVedleggHenterOpplastetVedleggSomFinnesForGittUuidOgEier() {
        val opplastetVedlegg = lagOpplastetVedlegg()
        opprettOpplastetVedleggOgLagreIDb(opplastetVedlegg, EIER)

        val opplastetVedleggFraDb = opplastetVedleggRepository.hentVedlegg(opplastetVedlegg.uuid, EIER)
        opplastetVedleggFraDb?.let {
            assertThat(it.uuid).isEqualTo(opplastetVedlegg.uuid)
            assertThat(it.eier).isEqualTo(EIER)
            assertThat(it.vedleggType.sammensattType).isEqualTo(TYPE)
            assertThat(it.data).isEqualTo(opplastetVedlegg.data)
            assertThat(it.soknadId).isEqualTo(SOKNADID)
            assertThat(it.filnavn).isEqualTo(FILNAVN)
            assertThat(it.sha512).isEqualTo(opplastetVedlegg.sha512)
        }
            ?: throw IllegalStateException("Opplastet vedlegg er null")
    }

    @Test
    fun hentVedleggForSoknadHenterAlleVedleggForGittSoknadUnderArbeidId() {
        val uuid = opprettOpplastetVedleggOgLagreIDb(lagOpplastetVedlegg(), EIER)
        val uuidSammeSoknadOgEier = opprettOpplastetVedleggOgLagreIDb(lagOpplastetVedlegg(EIER, TYPE2, SOKNADID), EIER)
        opprettOpplastetVedleggOgLagreIDb(lagOpplastetVedlegg(EIER2, TYPE2, SOKNADID2), EIER2)
        opprettOpplastetVedleggOgLagreIDb(lagOpplastetVedlegg(EIER, TYPE, SOKNADID3), EIER)
        val opplastedeVedlegg = opplastetVedleggRepository.hentVedleggForSoknad(SOKNADID, EIER)
        assertThat(opplastedeVedlegg).hasSize(2)
        assertThat(opplastedeVedlegg[0].uuid).isEqualTo(uuid)
        assertThat(opplastedeVedlegg[1].uuid).isEqualTo(uuidSammeSoknadOgEier)
    }

    @Test
    fun slettVedleggSletterOpplastetVedleggMedGittUuidOgEier() {
        val uuid = opprettOpplastetVedleggOgLagreIDb(lagOpplastetVedlegg(), EIER)
        opplastetVedleggRepository.slettVedlegg(uuid, EIER)
        assertThat(opplastetVedleggRepository.hentVedlegg(uuid, EIER)).isNull()
    }

    @Test
    fun slettAlleVedleggForSoknadSletterAlleOpplastedeVedleggForGittSoknadIdOgEier() {
        val uuid = opprettOpplastetVedleggOgLagreIDb(lagOpplastetVedlegg(), EIER)
        val uuidSammeSoknadOgEier = opprettOpplastetVedleggOgLagreIDb(
            lagOpplastetVedlegg(EIER, TYPE, SOKNADID), EIER
        )
        val uuidSammeEierOgAnnenSoknad =
            opprettOpplastetVedleggOgLagreIDb(lagOpplastetVedlegg(EIER, TYPE2, SOKNADID3), EIER)
        opplastetVedleggRepository.slettAlleVedleggForSoknad(SOKNADID, EIER)
        assertThat(opplastetVedleggRepository.hentVedlegg(uuid, EIER)).isNull()
        assertThat(opplastetVedleggRepository.hentVedlegg(uuidSammeSoknadOgEier, EIER)).isNull()
        assertThat(opplastetVedleggRepository.hentVedlegg(uuidSammeEierOgAnnenSoknad, EIER)).isNotNull
    }

    private fun lagOpplastetVedlegg(eier: String, type: String, soknadId: Long): OpplastetVedlegg {
        val data = RandomUtils.nextBytes(10)
        return OpplastetVedlegg(
            eier = eier,
            vedleggType = OpplastetVedleggType(type),
            data = data,
            uuid = UUID.nameUUIDFromBytes(data).toString(),
            soknadId = soknadId,
            filnavn = FILNAVN,
            sha512 = getSha512FromByteArray(data)
        )
    }

    private fun lagOpplastetVedlegg(): OpplastetVedlegg {
        return lagOpplastetVedlegg(EIER, TYPE, SOKNADID)
    }

    private fun opprettOpplastetVedleggOgLagreIDb(opplastetVedlegg: OpplastetVedlegg, eier: String): String {
        return opplastetVedleggRepository.opprettVedlegg(opplastetVedlegg, eier)
    }
}
