package no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg

import jakarta.inject.Inject
import no.nav.sosialhjelp.soknad.TestApplication
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

@ActiveProfiles(profiles = ["no-redis", "test"])
@SpringBootTest(classes = [TestApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class OpplastetVedleggRepositoryJdbcTest {

    companion object {
        private const val EIER = "12345678901"
        private const val EIER2 = "22222222222"
        private const val BEHANDLINGSID = "behandlingsid"
        private const val BEHANDLINGSID2 = "behandlingsid2"
        private const val BEHANDLINGSID3 = "behandlingsid3"
        private val DATA = byteArrayOf(1, 2, 3, 4)
        private val SHA512 = getSha512FromByteArray(DATA)
        private const val TYPE = "bostotte|annetboutgift"
        private const val TYPE2 = "dokumentasjon|aksjer"
        private const val FILNAVN = "dokumentasjon.pdf"
    }

    @Inject
    private lateinit var soknadUnderArbeidRepository: SoknadUnderArbeidRepository

    @Inject
    private lateinit var opplastetVedleggRepository: OpplastetVedleggRepository

    @Inject
    private lateinit var jdbcTemplate: JdbcTemplate

    @AfterEach
    fun tearDown() {
        jdbcTemplate.update("delete from OPPLASTET_VEDLEGG")
        jdbcTemplate.update("delete from SOKNAD_UNDER_ARBEID")
    }

    @Test
    fun opprettVedleggOppretterOpplastetVedleggIDatabasen() {
        val soknadId = soknadUnderArbeidRepository.opprettSoknad(lagSoknadUnderArbeid(BEHANDLINGSID), EIER)!!

        val opplastetVedlegg = lagOpplastetVedlegg(soknadId)
        val uuidFraDb = opplastetVedleggRepository.opprettVedlegg(opplastetVedlegg, EIER)
        assertThat(uuidFraDb).isEqualTo(opplastetVedlegg.uuid)
    }

    @Test
    fun hentVedleggHenterOpplastetVedleggSomFinnesForGittUuidOgEier() {
        val soknadId = soknadUnderArbeidRepository.opprettSoknad(lagSoknadUnderArbeid(BEHANDLINGSID), EIER)!!

        val uuid = opprettOpplastetVedleggOgLagreIDb(lagOpplastetVedlegg(soknadId), EIER)
        val opplastetVedleggFraDb = opplastetVedleggRepository.hentVedlegg(uuid, EIER)
        assertThat(opplastetVedleggFraDb?.uuid).isEqualTo(uuid)
        assertThat(opplastetVedleggFraDb?.eier).isEqualTo(EIER)
        assertThat(opplastetVedleggFraDb?.vedleggType?.sammensattType).isEqualTo(TYPE)
        assertThat(opplastetVedleggFraDb?.data).isEqualTo(DATA)
        assertThat(opplastetVedleggFraDb?.soknadId).isEqualTo(soknadId)
        assertThat(opplastetVedleggFraDb?.filnavn).isEqualTo(FILNAVN)
        assertThat(opplastetVedleggFraDb?.sha512).isEqualTo(SHA512)
    }

    @Test
    fun hentVedleggForSoknadHenterAlleVedleggForGittSoknadUnderArbeidId() {
        val soknadId = soknadUnderArbeidRepository.opprettSoknad(lagSoknadUnderArbeid(BEHANDLINGSID), EIER)!!
        val soknadId2 = soknadUnderArbeidRepository.opprettSoknad(lagSoknadUnderArbeid(BEHANDLINGSID2), EIER)!!
        val soknadId3 = soknadUnderArbeidRepository.opprettSoknad(lagSoknadUnderArbeid(BEHANDLINGSID3), EIER)!!

        val uuid = opprettOpplastetVedleggOgLagreIDb(lagOpplastetVedlegg(soknadId), EIER)
        val uuidSammeSoknadOgEier = opprettOpplastetVedleggOgLagreIDb(lagOpplastetVedlegg(EIER, TYPE2, soknadId), EIER)
        opprettOpplastetVedleggOgLagreIDb(lagOpplastetVedlegg(EIER2, TYPE2, soknadId2), EIER2)
        opprettOpplastetVedleggOgLagreIDb(lagOpplastetVedlegg(EIER, TYPE, soknadId3), EIER)
        val opplastedeVedlegg = opplastetVedleggRepository.hentVedleggForSoknad(soknadId, EIER)
        assertThat(opplastedeVedlegg).hasSize(2)
        assertThat(opplastedeVedlegg[0].uuid).isEqualTo(uuid)
        assertThat(opplastedeVedlegg[1].uuid).isEqualTo(uuidSammeSoknadOgEier)
    }

    @Test
    fun slettVedleggSletterOpplastetVedleggMedGittUuidOgEier() {
        val soknadId = soknadUnderArbeidRepository.opprettSoknad(lagSoknadUnderArbeid(BEHANDLINGSID), EIER)!!

        val uuid = opprettOpplastetVedleggOgLagreIDb(lagOpplastetVedlegg(soknadId), EIER)
        opplastetVedleggRepository.slettVedlegg(uuid, EIER)
        assertThat(opplastetVedleggRepository.hentVedlegg(uuid, EIER)).isNull()
    }

    @Test
    fun slettAlleVedleggForSoknadSletterAlleOpplastedeVedleggForGittSoknadIdOgEier() {
        val soknadId = soknadUnderArbeidRepository.opprettSoknad(lagSoknadUnderArbeid(BEHANDLINGSID), EIER)!!
        val soknadId2 = soknadUnderArbeidRepository.opprettSoknad(lagSoknadUnderArbeid(BEHANDLINGSID2), EIER)!!

        val uuid = opprettOpplastetVedleggOgLagreIDb(lagOpplastetVedlegg(soknadId), EIER)
        val uuidSammeSoknadOgEier = opprettOpplastetVedleggOgLagreIDb(lagOpplastetVedlegg(EIER, TYPE, soknadId), EIER)
        val uuidSammeEierOgAnnenSoknad =
            opprettOpplastetVedleggOgLagreIDb(lagOpplastetVedlegg(EIER, TYPE2, soknadId2), EIER)
        opplastetVedleggRepository.slettAlleVedleggForSoknad(soknadId, EIER)
        assertThat(opplastetVedleggRepository.hentVedlegg(uuid, EIER)).isNull()
        assertThat(opplastetVedleggRepository.hentVedlegg(uuidSammeSoknadOgEier, EIER)).isNull()
        assertThat(opplastetVedleggRepository.hentVedlegg(uuidSammeEierOgAnnenSoknad, EIER)).isNotNull
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

    private fun lagOpplastetVedlegg(eier: String, type: String, soknadId: Long): OpplastetVedlegg {
        return OpplastetVedlegg(
            eier = eier,
            vedleggType = OpplastetVedleggType(type),
            data = DATA,
            soknadId = soknadId,
            filnavn = FILNAVN,
            sha512 = SHA512
        )
    }

    private fun lagOpplastetVedlegg(soknadId: Long): OpplastetVedlegg {
        return lagOpplastetVedlegg(EIER, TYPE, soknadId)
    }

    private fun opprettOpplastetVedleggOgLagreIDb(opplastetVedlegg: OpplastetVedlegg, eier: String): String {
        return opplastetVedleggRepository.opprettVedlegg(opplastetVedlegg, eier)
    }
}
