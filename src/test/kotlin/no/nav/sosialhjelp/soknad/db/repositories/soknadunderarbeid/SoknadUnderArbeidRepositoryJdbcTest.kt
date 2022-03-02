package no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.business.db.RepositoryTestSupport
import no.nav.sosialhjelp.soknad.common.exceptions.SamtidigOppdateringException
import no.nav.sosialhjelp.soknad.common.exceptions.SoknadLaastException
import no.nav.sosialhjelp.soknad.config.DbTestConfig
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggRepository
import no.nav.sosialhjelp.soknad.domain.OpplastetVedlegg
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.domain.VedleggType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [DbTestConfig::class])
@ActiveProfiles("test")
internal class SoknadUnderArbeidRepositoryJdbcTest {

    @Inject
    private val soknadRepositoryTestSupport: RepositoryTestSupport? = null

    @Inject
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository? = null

    @Inject
    private val opplastetVedleggRepository: OpplastetVedleggRepository? = null

    @AfterEach
    fun tearDown() {
        soknadRepositoryTestSupport!!.jdbcTemplate.update("delete from SOKNAD_UNDER_ARBEID")
        soknadRepositoryTestSupport.jdbcTemplate.update("delete from OPPLASTET_VEDLEGG")
    }

    @Test
    fun opprettSoknadOppretterSoknadUnderArbeidIDatabasen() {
        val soknadUnderArbeidId = soknadUnderArbeidRepository!!.opprettSoknad(lagSoknadUnderArbeid(BEHANDLINGSID), EIER)
        assertThat(soknadUnderArbeidId).isNotNull
    }

    @Test
    fun hentSoknadHenterSoknadUnderArbeidGittRiktigEierOgSoknadId() {
        val soknadUnderArbeidId = soknadUnderArbeidRepository!!.opprettSoknad(
            lagSoknadUnderArbeid(BEHANDLINGSID).withJsonInternalSoknad(JsonInternalSoknad()),
            EIER
        )
        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(soknadUnderArbeidId!!, EIER).get()
        assertThat(soknadUnderArbeid.soknadId).isNotNull
        assertThat(soknadUnderArbeid.versjon).isEqualTo(1L)
        assertThat(soknadUnderArbeid.behandlingsId).isEqualTo(BEHANDLINGSID)
        assertThat(soknadUnderArbeid.tilknyttetBehandlingsId).isEqualTo(TILKNYTTET_BEHANDLINGSID)
        assertThat(soknadUnderArbeid.eier).isEqualTo(EIER)
        assertThat(soknadUnderArbeid.jsonInternalSoknad).isNotNull
        assertThat(soknadUnderArbeid.status).isEqualTo(SoknadUnderArbeidStatus.UNDER_ARBEID)
        assertThat(soknadUnderArbeid.opprettetDato).isEqualTo(OPPRETTET_DATO)
        assertThat(soknadUnderArbeid.sistEndretDato).isEqualTo(SIST_ENDRET_DATO)
    }

    @Test
    fun hentSoknadHenterIngenSoknadUnderArbeidHvisEiesAvAnnenBruker() {
        val soknadUnderArbeidId = soknadUnderArbeidRepository!!.opprettSoknad(lagSoknadUnderArbeid(BEHANDLINGSID), EIER)
        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(soknadUnderArbeidId!!, EIER2)
        assertThat(soknadUnderArbeid).isEmpty
    }

    @Test
    fun hentSoknadHenterSoknadUnderArbeidGittRiktigEierOgBehandlingsId() {
        val soknadUnderArbeidId = soknadUnderArbeidRepository!!.opprettSoknad(lagSoknadUnderArbeid(BEHANDLINGSID), EIER)
        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(BEHANDLINGSID, EIER)
        assertThat(soknadUnderArbeid.soknadId).isEqualTo(soknadUnderArbeidId)
        assertThat(soknadUnderArbeid.behandlingsId).isEqualTo(BEHANDLINGSID)
        assertThat(soknadUnderArbeid.eier).isEqualTo(EIER)
    }

    @Test
    fun oppdaterSoknadsdataOppdatererVersjonOgSistEndretDato() {
        val soknadUnderArbeid = lagSoknadUnderArbeid(BEHANDLINGSID)
        val soknadUnderArbeidId = soknadUnderArbeidRepository!!.opprettSoknad(soknadUnderArbeid, EIER)
        soknadUnderArbeid.withSoknadId(soknadUnderArbeidId).withJsonInternalSoknad(JSON_INTERNAL_SOKNAD)
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, EIER)
        val soknadUnderArbeidFraDb = soknadUnderArbeidRepository.hentSoknad(soknadUnderArbeidId!!, EIER).get()
        assertThat(soknadUnderArbeidFraDb.versjon).isEqualTo(2L)
        assertThat(soknadUnderArbeidFraDb.jsonInternalSoknad).isEqualTo(JSON_INTERNAL_SOKNAD)
        assertThat(soknadUnderArbeidFraDb.sistEndretDato).isAfter(SIST_ENDRET_DATO)
    }

    @Test
    fun oppdaterSoknadsdataKasterExceptionVedVersjonskonflikt() {
        val soknadUnderArbeid = lagSoknadUnderArbeid(BEHANDLINGSID)
        val soknadUnderArbeidId = soknadUnderArbeidRepository!!.opprettSoknad(soknadUnderArbeid, EIER)
        soknadUnderArbeid
            .withSoknadId(soknadUnderArbeidId)
            .withJsonInternalSoknad(JSON_INTERNAL_SOKNAD)
            .withVersjon(5L)
        soknadUnderArbeid
            .withJsonInternalSoknad(soknadUnderArbeid.jsonInternalSoknad.withAdditionalProperty("endret", true))
        assertThatExceptionOfType(SamtidigOppdateringException::class.java)
            .isThrownBy {
                soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, EIER)
            }
    }

    @Test
    fun oppdaterSoknadsdataKasterExceptionVedOppdateringAvLaastSoknad() {
        val soknadUnderArbeid = lagSoknadUnderArbeid(BEHANDLINGSID).withStatus(SoknadUnderArbeidStatus.LAAST)
        val soknadUnderArbeidId = soknadUnderArbeidRepository!!.opprettSoknad(soknadUnderArbeid, EIER)
        soknadUnderArbeid.withSoknadId(soknadUnderArbeidId).withJsonInternalSoknad(JSON_INTERNAL_SOKNAD)
        assertThatExceptionOfType(SoknadLaastException::class.java)
            .isThrownBy {
                soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, EIER)
            }
    }

    @Test
    fun oppdaterInnsendingStatusOppdatererStatusOgSistEndretDato() {
        val soknadUnderArbeid = lagSoknadUnderArbeid(BEHANDLINGSID)
        val soknadUnderArbeidId = soknadUnderArbeidRepository!!.opprettSoknad(soknadUnderArbeid, EIER)
        soknadUnderArbeid.withSoknadId(soknadUnderArbeidId).withStatus(SoknadUnderArbeidStatus.LAAST)
        soknadUnderArbeidRepository.oppdaterInnsendingStatus(soknadUnderArbeid, EIER)
        val soknadUnderArbeidFraDb = soknadUnderArbeidRepository.hentSoknad(soknadUnderArbeidId!!, EIER).get()
        assertThat(soknadUnderArbeidFraDb.versjon).isEqualTo(1L)
        assertThat(soknadUnderArbeidFraDb.status).isEqualTo(SoknadUnderArbeidStatus.LAAST)
        assertThat(soknadUnderArbeidFraDb.sistEndretDato).isAfter(SIST_ENDRET_DATO)
    }

    @Test
    fun slettSoknadSletterSoknadUnderArbeidFraDatabasen() {
        val soknadUnderArbeid = lagSoknadUnderArbeid(BEHANDLINGSID)
        val soknadUnderArbeidId = soknadUnderArbeidRepository!!.opprettSoknad(soknadUnderArbeid, EIER)
        soknadUnderArbeid.soknadId = soknadUnderArbeidId
        val opplastetVedleggUuid = opplastetVedleggRepository!!.opprettVedlegg(
            lagOpplastetVedlegg(soknadUnderArbeidId),
            EIER
        )
        soknadUnderArbeidRepository.slettSoknad(soknadUnderArbeid, EIER)
        assertThat(soknadUnderArbeidRepository.hentSoknad(soknadUnderArbeidId!!, EIER)).isEmpty
        assertThat(opplastetVedleggRepository.hentVedlegg(opplastetVedleggUuid, EIER)).isEmpty
    }

    private fun lagSoknadUnderArbeid(behandlingsId: String): SoknadUnderArbeid {
        return SoknadUnderArbeid().withVersjon(1L)
            .withBehandlingsId(behandlingsId)
            .withTilknyttetBehandlingsId(TILKNYTTET_BEHANDLINGSID)
            .withEier(EIER)
            .withJsonInternalSoknad(JSON_INTERNAL_SOKNAD)
            .withStatus(SoknadUnderArbeidStatus.UNDER_ARBEID)
            .withOpprettetDato(OPPRETTET_DATO)
            .withSistEndretDato(SIST_ENDRET_DATO)
    }

    private fun lagOpplastetVedlegg(soknadId: Long?): OpplastetVedlegg {
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
        private const val EIER2 = "22222222222"
        private const val BEHANDLINGSID = "1100020"
        private const val TILKNYTTET_BEHANDLINGSID = "4567"
        private val OPPRETTET_DATO = LocalDateTime.now().minusSeconds(50).truncatedTo(ChronoUnit.MILLIS)
        private val SIST_ENDRET_DATO = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
        private val JSON_INTERNAL_SOKNAD = JsonInternalSoknad()
    }
}
