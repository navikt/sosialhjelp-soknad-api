package no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.slot
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.ControllerToNewDatamodellProxy
import no.nav.sosialhjelp.soknad.app.exceptions.SamtidigOppdateringException
import no.nav.sosialhjelp.soknad.app.exceptions.SoknadLaastException
import no.nav.sosialhjelp.soknad.app.exceptions.SoknadUnderArbeidIkkeFunnetException
import no.nav.sosialhjelp.soknad.app.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.app.systemdata.SystemdataUpdater
import no.nav.sosialhjelp.soknad.innsending.SoknadServiceOld
import no.nav.sosialhjelp.soknad.innsending.SoknadServiceOld.Companion.createEmptyJsonInternalSoknad
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@ActiveProfiles("no-redis", "test", "test-container")
internal class SoknadUnderArbeidRepositoryJdbcTest {
    @Autowired
    private lateinit var soknadUnderArbeidRepository: SoknadUnderArbeidRepository

    @Autowired
    private lateinit var soknadServiceOld: SoknadServiceOld

    @MockkBean
    private lateinit var systemdataUpdater: SystemdataUpdater

    @BeforeEach
    fun setUp() {
        ControllerToNewDatamodellProxy.nyDatamodellAktiv = false
    }

    // "kopi" av logikken ved opprettelse av ny soknad i SoknadServiceOld
    @Test
    fun `Test ny flyt`() {
        val eier = "12345678901"

        val soknadUnderArbeid =
            SoknadUnderArbeid(
                versjon = 1L,
                behandlingsId = UUID.randomUUID().toString(),
                eier = eier,
                jsonInternalSoknad = createEmptyJsonInternalSoknad(eier, false),
                status = SoknadUnderArbeidStatus.UNDER_ARBEID,
                opprettetDato = LocalDateTime.now().minusSeconds(50).truncatedTo(ChronoUnit.MILLIS),
                sistEndretDato = LocalDateTime.now().minusSeconds(50).truncatedTo(ChronoUnit.MILLIS),
            )
                .also { soknadUnderArbeidRepository.opprettSoknad(it, eier) }
                .let { soknadUnderArbeidRepository.hentSoknad(it.behandlingsId, eier) }

        val eksisterendeSoknad = soknadUnderArbeid.copy()

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, eier)

        soknadUnderArbeidRepository.hentSoknad(soknadUnderArbeid.behandlingsId, eier)
            .also {
                assertThat(it.sistEndretDato).isNotEqualTo(eksisterendeSoknad.sistEndretDato)
            }
    }

    @Test
    fun `Feil i systemdataUpdater skal slette soknad`() {
        SubjectHandlerUtils.setNewSubjectHandlerImpl(StaticSubjectHandlerImpl())

        val soknadUnderArbeidCapturingSlot = slot<SoknadUnderArbeid>()
        every { systemdataUpdater.update(capture(soknadUnderArbeidCapturingSlot)) } throws RuntimeException("Feil i systemdataUpdater")

        assertThatThrownBy {
            soknadServiceOld.startSoknad(UUID.randomUUID().toString(), false)
        }.isInstanceOf(RuntimeException::class.java)

        assertThatThrownBy {
            soknadUnderArbeidRepository.hentSoknad(
                soknadUnderArbeidCapturingSlot.captured.behandlingsId,
                soknadUnderArbeidCapturingSlot.captured.eier,
            )
        }.isInstanceOf(SoknadUnderArbeidIkkeFunnetException::class.java)
    }

    @Test
    fun opprettSoknadOppretterSoknadUnderArbeidIDatabasen() {
        val soknadUnderArbeidId = soknadUnderArbeidRepository.opprettSoknad(lagSoknadUnderArbeid(BEHANDLINGSID), EIER)
        assertThat(soknadUnderArbeidId).isNotNull
    }

    @Test
    fun hentSoknadHenterSoknadUnderArbeidGittRiktigEierOgSoknadId() {
        val lagSoknadUnderArbeid = lagSoknadUnderArbeid(BEHANDLINGSID)
        lagSoknadUnderArbeid.jsonInternalSoknad = JsonInternalSoknad()
        val soknadUnderArbeidId = soknadUnderArbeidRepository.opprettSoknad(lagSoknadUnderArbeid, EIER)
        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(soknadUnderArbeidId!!, EIER)
        assertThat(soknadUnderArbeid?.soknadId).isNotNull
        assertThat(soknadUnderArbeid?.versjon).isEqualTo(1L)
        assertThat(soknadUnderArbeid?.behandlingsId).isEqualTo(BEHANDLINGSID)
        assertThat(soknadUnderArbeid?.eier).isEqualTo(EIER)
        assertThat(soknadUnderArbeid?.jsonInternalSoknad).isNotNull
        assertThat(soknadUnderArbeid?.status).isEqualTo(SoknadUnderArbeidStatus.UNDER_ARBEID)
        assertThat(soknadUnderArbeid?.opprettetDato).isEqualTo(OPPRETTET_DATO)
        assertThat(soknadUnderArbeid?.sistEndretDato).isEqualTo(SIST_ENDRET_DATO)
    }

    @Test
    fun hentSoknadHenterIngenSoknadUnderArbeidHvisEiesAvAnnenBruker() {
        val soknadUnderArbeidId = soknadUnderArbeidRepository.opprettSoknad(lagSoknadUnderArbeid(BEHANDLINGSID), EIER)
        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(soknadUnderArbeidId!!, EIER2)
        assertThat(soknadUnderArbeid).isNull()
    }

    @Test
    fun hentSoknadHenterSoknadUnderArbeidGittRiktigEierOgBehandlingsId() {
        val soknadUnderArbeidId = soknadUnderArbeidRepository.opprettSoknad(lagSoknadUnderArbeid(BEHANDLINGSID), EIER)
        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(BEHANDLINGSID, EIER)
        assertThat(soknadUnderArbeid.soknadId).isEqualTo(soknadUnderArbeidId)
        assertThat(soknadUnderArbeid.behandlingsId).isEqualTo(BEHANDLINGSID)
        assertThat(soknadUnderArbeid.eier).isEqualTo(EIER)
    }

    @Test
    fun oppdaterSoknadsdataOppdatererVersjonOgSistEndretDato() {
        val soknadUnderArbeid = lagSoknadUnderArbeid(BEHANDLINGSID)
        val soknadUnderArbeidId = soknadUnderArbeidRepository.opprettSoknad(soknadUnderArbeid, EIER)
        soknadUnderArbeid.soknadId = soknadUnderArbeidId!!
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, EIER)
        val soknadUnderArbeidFraDb = soknadUnderArbeidRepository.hentSoknad(soknadUnderArbeidId, EIER)
        assertThat(soknadUnderArbeidFraDb?.versjon).isEqualTo(2L)
        assertThat(soknadUnderArbeidFraDb?.jsonInternalSoknad).isEqualTo(JSON_INTERNAL_SOKNAD)
        assertThat(soknadUnderArbeidFraDb?.sistEndretDato).isAfter(SIST_ENDRET_DATO)
    }

    @Test
    fun oppdaterSoknadsdataKasterExceptionVedVersjonskonflikt() {
        val soknadUnderArbeid = lagSoknadUnderArbeid(BEHANDLINGSID)
        val soknadUnderArbeidId = soknadUnderArbeidRepository.opprettSoknad(soknadUnderArbeid, EIER)
        soknadUnderArbeid.soknadId = soknadUnderArbeidId!!
        soknadUnderArbeid.versjon = 5L
        soknadUnderArbeid.jsonInternalSoknad = soknadUnderArbeid.jsonInternalSoknad?.withAdditionalProperty("endret", true)

        assertThatExceptionOfType(SamtidigOppdateringException::class.java)
            .isThrownBy {
                soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, EIER)
            }
    }

    @Test
    fun oppdaterSoknadsdataKasterExceptionVedOppdateringAvLaastSoknad() {
        val soknadUnderArbeid = lagSoknadUnderArbeid(BEHANDLINGSID)
        soknadUnderArbeid.status = SoknadUnderArbeidStatus.LAAST
        val soknadUnderArbeidId = soknadUnderArbeidRepository.opprettSoknad(soknadUnderArbeid, EIER)
        soknadUnderArbeid.soknadId = soknadUnderArbeidId!!
        soknadUnderArbeid.jsonInternalSoknad = JSON_INTERNAL_SOKNAD
        assertThatExceptionOfType(SoknadLaastException::class.java)
            .isThrownBy {
                soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, EIER)
            }
    }

    @Test
    fun oppdaterInnsendingStatusOppdatererStatusOgSistEndretDato() {
        val soknadUnderArbeid = lagSoknadUnderArbeid(BEHANDLINGSID)
        val soknadUnderArbeidId = soknadUnderArbeidRepository.opprettSoknad(soknadUnderArbeid, EIER)
        soknadUnderArbeid.soknadId = soknadUnderArbeidId!!
        soknadUnderArbeid.status = SoknadUnderArbeidStatus.LAAST
        soknadUnderArbeidRepository.oppdaterInnsendingStatus(soknadUnderArbeid, EIER)
        val soknadUnderArbeidFraDb = soknadUnderArbeidRepository.hentSoknad(soknadUnderArbeidId, EIER)
        assertThat(soknadUnderArbeidFraDb?.versjon).isEqualTo(1L)
        assertThat(soknadUnderArbeidFraDb?.status).isEqualTo(SoknadUnderArbeidStatus.LAAST)
        assertThat(soknadUnderArbeidFraDb?.sistEndretDato).isAfter(SIST_ENDRET_DATO)
    }

    @Test
    fun slettSoknadSletterSoknadUnderArbeidFraDatabasen() {
        val soknadUnderArbeid = lagSoknadUnderArbeid(BEHANDLINGSID)
        val soknadUnderArbeidId =
            soknadUnderArbeidRepository.opprettSoknad(soknadUnderArbeid, EIER)
                ?: throw RuntimeException("Kunne ikke lagre soknad")

        soknadUnderArbeid.soknadId = soknadUnderArbeidId

        soknadUnderArbeidRepository.slettSoknad(soknadUnderArbeid, EIER)
        assertThat(soknadUnderArbeidRepository.hentSoknad(soknadUnderArbeidId, EIER)).isNull()
    }

    private fun lagSoknadUnderArbeid(behandlingsId: String): SoknadUnderArbeid {
        return SoknadUnderArbeid(
            versjon = 1L,
            behandlingsId = behandlingsId,
            eier = EIER,
            jsonInternalSoknad = JSON_INTERNAL_SOKNAD,
            status = SoknadUnderArbeidStatus.UNDER_ARBEID,
            opprettetDato = OPPRETTET_DATO,
            sistEndretDato = SIST_ENDRET_DATO,
        )
    }

    companion object {
        private const val EIER = "12345678901"
        private const val EIER2 = "22222222222"
        private val BEHANDLINGSID = UUID.randomUUID().toString()
        private val OPPRETTET_DATO = LocalDateTime.now().minusSeconds(50).truncatedTo(ChronoUnit.MILLIS)
        private val SIST_ENDRET_DATO = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
        private val JSON_INTERNAL_SOKNAD = JsonInternalSoknad()
    }
}
