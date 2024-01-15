package no.nav.sosialhjelp.soknad.innsending

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.verify
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE_SAMTYKKE
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.app.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.app.systemdata.SystemdataUpdater
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.Vedleggstatus
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.SoknadService.Companion.createEmptyJsonInternalSoknad
import no.nav.sosialhjelp.soknad.innsending.svarut.OppgaveHandterer
import no.nav.sosialhjelp.soknad.inntekt.husbanken.BostotteSystemdata
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.SkatteetatenSystemdata
import no.nav.sosialhjelp.soknad.metrics.PrometheusMetricsService
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.LocalDateTime

internal class SoknadServiceTest {

    private val oppgaveHandterer: OppgaveHandterer = mockk()
    private val systemdataUpdater: SystemdataUpdater = mockk()
    private val innsendingService: InnsendingService = mockk()
    private val soknadMetadataRepository: SoknadMetadataRepository = mockk()
    private val bostotteSystemdata: BostotteSystemdata = mockk()
    private val skatteetatenSystemdata: SkatteetatenSystemdata = mockk()
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository = mockk()
    private val mellomlagringService: MellomlagringService = mockk()
    private val prometheusMetricsService: PrometheusMetricsService = mockk(relaxed = true)

    private val soknadService = SoknadService(
        oppgaveHandterer,
        innsendingService,
        soknadMetadataRepository,
        soknadUnderArbeidRepository,
        systemdataUpdater,
        bostotteSystemdata,
        skatteetatenSystemdata,
        mellomlagringService,
        prometheusMetricsService,
        Clock.systemDefaultZone(),
    )

    @BeforeEach
    fun before() {
        clearAllMocks()

        mockkObject(MiljoUtils)
        every { MiljoUtils.isNonProduction() } returns true
        SubjectHandlerUtils.setNewSubjectHandlerImpl(StaticSubjectHandlerImpl())

        every { systemdataUpdater.update(any()) } just runs
        every { mellomlagringService.kanSoknadHaMellomlagredeVedleggForSletting(any()) } returns false
    }

    @AfterEach
    fun tearDown() {
        SubjectHandlerUtils.resetSubjectHandlerImpl()
        unmockkObject(MiljoUtils)
    }

    @Test
    fun skalStarteSoknad() {
        val soknadMetadata: SoknadMetadata = mockk()
        every { soknadMetadataRepository.hentNesteId() } returns 999_999L
        every { soknadMetadataRepository.opprett(any()) } just runs
        every { soknadMetadata.behandlingsId } returns "123"

        val soknadUnderArbeidSlot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.opprettSoknad(capture(soknadUnderArbeidSlot), any()) } returns 123L

        soknadService.startSoknad("")

        val bekreftelser = soknadUnderArbeidSlot.captured.jsonInternalSoknad!!.soknad.data.okonomi.opplysninger.bekreftelse
        assertThat(bekreftelser.any { harBekreftelseFor(it, UTBETALING_SKATTEETATEN_SAMTYKKE) }).isFalse
        assertThat(bekreftelser.any { harBekreftelseFor(it, BOSTOTTE_SAMTYKKE) }).isFalse
    }

    private fun harBekreftelseFor(bekreftelse: JsonOkonomibekreftelse, bekreftelsesType: String): Boolean {
        return bekreftelse.verdi && bekreftelse.type.equals(bekreftelsesType, ignoreCase = true)
    }

    @Test
    fun skalSendeSoknad() {
        val testType = "testType"
        val testTilleggsinfo = "testTilleggsinfo"
        val testType2 = "testType2"
        val testTilleggsinfo2 = "testTilleggsinfo2"
        val jsonVedlegg = mutableListOf(
            JsonVedlegg()
                .withType(testType)
                .withTilleggsinfo(testTilleggsinfo)
                .withStatus(Vedleggstatus.LastetOpp.toString()),
            JsonVedlegg()
                .withType(testType2)
                .withTilleggsinfo(testTilleggsinfo2)
                .withStatus(Vedleggstatus.LastetOpp.toString()),
        )

        val behandlingsId = "123"
        val eier = "123456"
        val soknadUnderArbeid = SoknadUnderArbeid(
            versjon = 1L,
            behandlingsId = BEHANDLINGSID,
            eier = eier,
            jsonInternalSoknad = createEmptyJsonInternalSoknad(eier),
            status = SoknadUnderArbeidStatus.UNDER_ARBEID,
            opprettetDato = LocalDateTime.now(),
            sistEndretDato = LocalDateTime.now(),
        )
        soknadUnderArbeid.jsonInternalSoknad!!.vedlegg = JsonVedleggSpesifikasjon().withVedlegg(jsonVedlegg)
        every { soknadUnderArbeidRepository.hentSoknad(behandlingsId, any()) } returns soknadUnderArbeid

        val soknadMetadata = createSoknadMetadata()
        every { soknadMetadataRepository.hent(any()) } returns soknadMetadata
        every { soknadMetadataRepository.oppdater(any()) } just runs

        every { oppgaveHandterer.leggTilOppgave(any(), any()) } just runs
        every { innsendingService.oppdaterSoknadUnderArbeid(any()) } just runs

        soknadService.sendSoknad(behandlingsId)

        verify { oppgaveHandterer.leggTilOppgave(behandlingsId, any()) }

        val vedlegg = soknadMetadata.vedlegg
        assertThat(vedlegg?.vedleggListe).hasSize(2)
        assertThat(vedlegg?.vedleggListe?.get(0)?.filnavn).isEqualTo(testType)
        assertThat(vedlegg?.vedleggListe?.get(0)?.skjema).isEqualTo(testType)
        assertThat(vedlegg?.vedleggListe?.get(0)?.tillegg).isEqualTo(testTilleggsinfo)
        assertThat(vedlegg?.vedleggListe?.get(1)?.filnavn).isEqualTo(testType2)
        assertThat(vedlegg?.vedleggListe?.get(1)?.skjema).isEqualTo(testType2)
        assertThat(vedlegg?.vedleggListe?.get(1)?.tillegg).isEqualTo(testTilleggsinfo2)
    }

    @Test
    fun skalAvbryteSoknad() {
        every { soknadUnderArbeidRepository.hentSoknadNullable(BEHANDLINGSID, any()) } returns SoknadUnderArbeid(
            versjon = 1L,
            behandlingsId = BEHANDLINGSID,
            eier = EIER,
            jsonInternalSoknad = createEmptyJsonInternalSoknad(EIER),
            status = SoknadUnderArbeidStatus.UNDER_ARBEID,
            opprettetDato = LocalDateTime.now(),
            sistEndretDato = LocalDateTime.now(),
        )

        every { soknadUnderArbeidRepository.slettSoknad(any(), any()) } just runs
        every { soknadMetadataRepository.hent(any()) } returns createSoknadMetadata()
        every { soknadMetadataRepository.oppdater(any()) } just runs

        soknadService.avbrytSoknad(BEHANDLINGSID, "3")

        verify { soknadUnderArbeidRepository.slettSoknad(any(), any()) }
        verify(exactly = 1) { prometheusMetricsService.reportAvbruttSoknad(false, "3") }
    }

    private fun createSoknadMetadata(): SoknadMetadata {
        return SoknadMetadata(
            id = 0L,
            behandlingsId = BEHANDLINGSID,
            fnr = EIER,
            opprettetDato = LocalDateTime.now(),
            sistEndretDato = LocalDateTime.now(),
        )
    }

    companion object {
        private const val EIER = "Hans og Grete"
        private const val BEHANDLINGSID = "123"
    }
}
