package no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.config.DbTestConfig
import no.nav.sosialhjelp.soknad.config.RepositoryTestSupport
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggRepository
import no.nav.sosialhjelp.soknad.domain.OpplastetVedlegg
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.domain.VedleggType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime
import javax.inject.Inject

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [DbTestConfig::class])
@ActiveProfiles("test")
internal class BatchSoknadUnderArbeidRepositoryJdbcTest {

    @Inject
    private val soknadRepositoryTestSupport: RepositoryTestSupport? = null

    @Inject
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository? = null

    @Inject
    private val opplastetVedleggRepository: OpplastetVedleggRepository? = null

    @Inject
    private val batchSoknadUnderArbeidRepository: BatchSoknadUnderArbeidRepository? = null

    @AfterEach
    fun tearDown() {
        soknadRepositoryTestSupport!!.getJdbcTemplate().update("delete from SOKNAD_UNDER_ARBEID")
        soknadRepositoryTestSupport.getJdbcTemplate().update("delete from OPPLASTET_VEDLEGG")
    }

    @Test
    fun hentSoknaderForBatchSkalFinneGamleSoknader() {
        val skalIkkeSlettes = lagSoknadUnderArbeid(BEHANDLINGSID, 13)
        val skalIkkeSlettesId = soknadUnderArbeidRepository!!.opprettSoknad(skalIkkeSlettes, EIER)
        val skalSlettes = lagSoknadUnderArbeid("annen_behandlingsid", 14)
        val skalSlettesId = soknadUnderArbeidRepository.opprettSoknad(skalSlettes, EIER)
        val soknader = batchSoknadUnderArbeidRepository!!.hentGamleSoknadUnderArbeidForBatch()
        assertThat(soknader).hasSize(1)
        assertThat(soknader[0]).isEqualTo(skalSlettesId).isNotEqualTo(skalIkkeSlettesId)
    }

    @Test
    fun slettSoknadGittSoknadUnderArbeidIdSkalSletteSoknad() {
        val soknadUnderArbeid = lagSoknadUnderArbeid(BEHANDLINGSID, 15)
        val soknadUnderArbeidId = soknadUnderArbeidRepository!!.opprettSoknad(soknadUnderArbeid, EIER)
        soknadUnderArbeid.soknadId = soknadUnderArbeidId
        val opplastetVedleggUuid = opplastetVedleggRepository!!.opprettVedlegg(lagOpplastetVedlegg(soknadUnderArbeidId!!), EIER)
        batchSoknadUnderArbeidRepository!!.slettSoknad(soknadUnderArbeid.soknadId)
        assertThat(soknadUnderArbeidRepository.hentSoknad(soknadUnderArbeidId, EIER)).isEmpty
        assertThat(opplastetVedleggRepository.hentVedlegg(opplastetVedleggUuid, EIER)).isEmpty
    }

    private fun lagSoknadUnderArbeid(behandlingsId: String, antallDagerSiden: Int): SoknadUnderArbeid {
        return SoknadUnderArbeid().withVersjon(1L)
            .withBehandlingsId(behandlingsId)
            .withTilknyttetBehandlingsId(TILKNYTTET_BEHANDLINGSID)
            .withEier(EIER)
            .withJsonInternalSoknad(JSON_INTERNAL_SOKNAD)
            .withStatus(SoknadUnderArbeidStatus.UNDER_ARBEID)
            .withOpprettetDato(LocalDateTime.now().minusDays(antallDagerSiden.toLong()).minusMinutes(5))
            .withSistEndretDato(LocalDateTime.now().minusDays(antallDagerSiden.toLong()).minusMinutes(5))
    }

    private fun lagOpplastetVedlegg(soknadId: Long): OpplastetVedlegg {
        return OpplastetVedlegg()
            .withEier(EIER)
            .withVedleggType(VedleggType("bostotte|annetboutgift"))
            .withData(byteArrayOf(1, 2, 3))
            .withSoknadId(soknadId)
            .withFilnavn("dokumentasjon.pdf")
            .withSha512("aaa")
    }

    companion object {
        private const val EIER = "12345678901"
        private const val BEHANDLINGSID = "1100020"
        private const val TILKNYTTET_BEHANDLINGSID = "4567"
        private val JSON_INTERNAL_SOKNAD = JsonInternalSoknad()
    }
}
