package no.nav.sosialhjelp.soknad.repository.soknadunderarbeid

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.app.exceptions.SamtidigOppdateringException
import no.nav.sosialhjelp.soknad.app.exceptions.SoknadLaastException
import no.nav.sosialhjelp.soknad.repository.opplastetvedlegg.OpplastetVedlegg
import no.nav.sosialhjelp.soknad.repository.opplastetvedlegg.OpplastetVedleggRepository
import no.nav.sosialhjelp.soknad.repository.opplastetvedlegg.OpplastetVedleggRepositoryJdbc
import no.nav.sosialhjelp.soknad.repository.opplastetvedlegg.OpplastetVedleggType
import no.nav.sosialhjelp.soknad.repository.RepositoryTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

internal class SoknadUnderArbeidRepositoryJdbcTest : RepositoryTest() {

    private var soknadUnderArbeidRepository: SoknadUnderArbeidRepository? = null
    private var opplastetVedleggRepository: OpplastetVedleggRepository? = null

    @Autowired
    private lateinit var transactionTemplate: TransactionTemplate

    @BeforeEach
    fun setup() {
        if (opplastetVedleggRepository == null) {
            opplastetVedleggRepository = OpplastetVedleggRepositoryJdbc(jdbcTemplate)
        }
        if (soknadUnderArbeidRepository == null) {
            soknadUnderArbeidRepository = SoknadUnderArbeidRepositoryJdbc(
                jdbcTemplate, transactionTemplate, opplastetVedleggRepository!!
            )
        }
    }

    @Test
    fun opprettSoknadOppretterSoknadUnderArbeidIDatabasen() {
        val soknadUnderArbeidId = soknadUnderArbeidRepository!!.opprettSoknad(lagSoknadUnderArbeid(BEHANDLINGSID), EIER)
        assertThat(soknadUnderArbeidId).isNotNull
    }

    @Test
    fun hentSoknadHenterSoknadUnderArbeidGittRiktigEierOgSoknadId() {
        val lagSoknadUnderArbeid = lagSoknadUnderArbeid(BEHANDLINGSID)
        lagSoknadUnderArbeid.jsonInternalSoknad = JsonInternalSoknad()
        val soknadUnderArbeidId = soknadUnderArbeidRepository!!.opprettSoknad(lagSoknadUnderArbeid, EIER)
        val soknadUnderArbeid = soknadUnderArbeidRepository!!.hentSoknad(soknadUnderArbeidId!!, EIER)
        assertThat(soknadUnderArbeid?.soknadId).isNotNull
        assertThat(soknadUnderArbeid?.versjon).isEqualTo(1L)
        assertThat(soknadUnderArbeid?.behandlingsId).isEqualTo(BEHANDLINGSID)
        assertThat(soknadUnderArbeid?.tilknyttetBehandlingsId).isEqualTo(TILKNYTTET_BEHANDLINGSID)
        assertThat(soknadUnderArbeid?.eier).isEqualTo(EIER)
        assertThat(soknadUnderArbeid?.jsonInternalSoknad).isNotNull
        assertThat(soknadUnderArbeid?.status).isEqualTo(SoknadUnderArbeidStatus.UNDER_ARBEID)
        assertThat(soknadUnderArbeid?.opprettetDato).isEqualTo(OPPRETTET_DATO)
        assertThat(soknadUnderArbeid?.sistEndretDato).isEqualTo(SIST_ENDRET_DATO)
    }

    @Test
    fun hentSoknadHenterIngenSoknadUnderArbeidHvisEiesAvAnnenBruker() {
        val soknadUnderArbeidId = soknadUnderArbeidRepository!!.opprettSoknad(lagSoknadUnderArbeid(BEHANDLINGSID), EIER)
        val soknadUnderArbeid = soknadUnderArbeidRepository!!.hentSoknad(soknadUnderArbeidId!!, EIER2)
        assertThat(soknadUnderArbeid).isNull()
    }

    @Test
    fun hentSoknadHenterSoknadUnderArbeidGittRiktigEierOgBehandlingsId() {
        val soknadUnderArbeidId = soknadUnderArbeidRepository!!.opprettSoknad(lagSoknadUnderArbeid(BEHANDLINGSID), EIER)
        val soknadUnderArbeid = soknadUnderArbeidRepository!!.hentSoknad(BEHANDLINGSID, EIER)
        assertThat(soknadUnderArbeid.soknadId).isEqualTo(soknadUnderArbeidId)
        assertThat(soknadUnderArbeid.behandlingsId).isEqualTo(BEHANDLINGSID)
        assertThat(soknadUnderArbeid.eier).isEqualTo(EIER)
    }

    @Test
    fun oppdaterSoknadsdataOppdatererVersjonOgSistEndretDato() {
        val soknadUnderArbeid = lagSoknadUnderArbeid(BEHANDLINGSID)
        val soknadUnderArbeidId = soknadUnderArbeidRepository!!.opprettSoknad(soknadUnderArbeid, EIER)
        soknadUnderArbeid.soknadId = soknadUnderArbeidId!!
        soknadUnderArbeidRepository!!.oppdaterSoknadsdata(soknadUnderArbeid, EIER)
        val soknadUnderArbeidFraDb = soknadUnderArbeidRepository!!.hentSoknad(soknadUnderArbeidId, EIER)
        assertThat(soknadUnderArbeidFraDb?.versjon).isEqualTo(2L)
        assertThat(soknadUnderArbeidFraDb?.jsonInternalSoknad).isEqualTo(JSON_INTERNAL_SOKNAD)
        assertThat(soknadUnderArbeidFraDb?.sistEndretDato).isAfter(SIST_ENDRET_DATO)
    }

    @Test
    fun oppdaterSoknadsdataKasterExceptionVedVersjonskonflikt() {
        val soknadUnderArbeid = lagSoknadUnderArbeid(BEHANDLINGSID)
        val soknadUnderArbeidId = soknadUnderArbeidRepository!!.opprettSoknad(soknadUnderArbeid, EIER)
        soknadUnderArbeid.soknadId = soknadUnderArbeidId!!
        soknadUnderArbeid.versjon = 5L
        soknadUnderArbeid.jsonInternalSoknad = soknadUnderArbeid.jsonInternalSoknad?.withAdditionalProperty("endret", true)

        assertThatExceptionOfType(SamtidigOppdateringException::class.java)
            .isThrownBy {
                soknadUnderArbeidRepository!!.oppdaterSoknadsdata(soknadUnderArbeid, EIER)
            }
    }

    @Test
    fun oppdaterSoknadsdataKasterExceptionVedOppdateringAvLaastSoknad() {
        val soknadUnderArbeid = lagSoknadUnderArbeid(BEHANDLINGSID)
        soknadUnderArbeid.status = SoknadUnderArbeidStatus.LAAST
        val soknadUnderArbeidId = soknadUnderArbeidRepository!!.opprettSoknad(soknadUnderArbeid, EIER)
        soknadUnderArbeid.soknadId = soknadUnderArbeidId!!
        soknadUnderArbeid.jsonInternalSoknad = JSON_INTERNAL_SOKNAD
        assertThatExceptionOfType(SoknadLaastException::class.java)
            .isThrownBy {
                soknadUnderArbeidRepository!!.oppdaterSoknadsdata(soknadUnderArbeid, EIER)
            }
    }

    @Test
    fun oppdaterInnsendingStatusOppdatererStatusOgSistEndretDato() {
        val soknadUnderArbeid = lagSoknadUnderArbeid(BEHANDLINGSID)
        val soknadUnderArbeidId = soknadUnderArbeidRepository!!.opprettSoknad(soknadUnderArbeid, EIER)
        soknadUnderArbeid.soknadId = soknadUnderArbeidId!!
        soknadUnderArbeid.status = SoknadUnderArbeidStatus.LAAST
        soknadUnderArbeidRepository!!.oppdaterInnsendingStatus(soknadUnderArbeid, EIER)
        val soknadUnderArbeidFraDb = soknadUnderArbeidRepository!!.hentSoknad(soknadUnderArbeidId, EIER)
        assertThat(soknadUnderArbeidFraDb?.versjon).isEqualTo(1L)
        assertThat(soknadUnderArbeidFraDb?.status).isEqualTo(SoknadUnderArbeidStatus.LAAST)
        assertThat(soknadUnderArbeidFraDb?.sistEndretDato).isAfter(SIST_ENDRET_DATO)
    }

    @Test
    fun slettSoknadSletterSoknadUnderArbeidFraDatabasen() {
        val soknadUnderArbeid = lagSoknadUnderArbeid(BEHANDLINGSID)
        val soknadUnderArbeidId = soknadUnderArbeidRepository!!.opprettSoknad(soknadUnderArbeid, EIER)
        soknadUnderArbeid.soknadId = soknadUnderArbeidId!!
        val opplastetVedleggUuid = opplastetVedleggRepository!!.opprettVedlegg(
            lagOpplastetVedlegg(soknadUnderArbeidId),
            EIER
        )
        soknadUnderArbeidRepository!!.slettSoknad(soknadUnderArbeid, EIER)
        assertThat(soknadUnderArbeidRepository!!.hentSoknad(soknadUnderArbeidId, EIER)).isNull()
        assertThat(opplastetVedleggRepository!!.hentVedlegg(opplastetVedleggUuid, EIER)).isNull()
    }

    private fun lagSoknadUnderArbeid(behandlingsId: String): SoknadUnderArbeid {
        return SoknadUnderArbeid(
            versjon = 1L,
            behandlingsId = behandlingsId,
            tilknyttetBehandlingsId = TILKNYTTET_BEHANDLINGSID,
            eier = EIER,
            jsonInternalSoknad = JSON_INTERNAL_SOKNAD,
            status = SoknadUnderArbeidStatus.UNDER_ARBEID,
            opprettetDato = OPPRETTET_DATO,
            sistEndretDato = SIST_ENDRET_DATO
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
        private const val EIER2 = "22222222222"
        private const val BEHANDLINGSID = "1100020"
        private const val TILKNYTTET_BEHANDLINGSID = "4567"
        private val OPPRETTET_DATO = LocalDateTime.now().minusSeconds(50).truncatedTo(ChronoUnit.MILLIS)
        private val SIST_ENDRET_DATO = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
        private val JSON_INTERNAL_SOKNAD = JsonInternalSoknad()
    }
}
