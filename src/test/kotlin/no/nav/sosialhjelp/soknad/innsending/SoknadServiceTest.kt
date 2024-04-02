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
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.app.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.app.systemdata.SystemdataUpdater
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.SoknadService.Companion.createEmptyJsonInternalSoknad
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

    private val systemdataUpdater: SystemdataUpdater = mockk()
    private val soknadMetadataRepository: SoknadMetadataRepository = mockk()
    private val bostotteSystemdata: BostotteSystemdata = mockk()
    private val skatteetatenSystemdata: SkatteetatenSystemdata = mockk()
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository = mockk()
    private val mellomlagringService: MellomlagringService = mockk()
    private val prometheusMetricsService: PrometheusMetricsService = mockk(relaxed = true)

    private val soknadService = SoknadService(
        soknadMetadataRepository,
        soknadUnderArbeidRepository,
        systemdataUpdater,
        bostotteSystemdata,
        skatteetatenSystemdata,
        mellomlagringService,
        prometheusMetricsService,
        Clock.systemDefaultZone()
    )

    @BeforeEach
    fun before() {
        clearAllMocks()

        mockkObject(MiljoUtils)
        every { MiljoUtils.isNonProduction() } returns true
        SubjectHandlerUtils.setNewSubjectHandlerImpl(StaticSubjectHandlerImpl())

        every { systemdataUpdater.update(any()) } just runs
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

        soknadService.startSoknad()

        val bekreftelser = soknadUnderArbeidSlot.captured.jsonInternalSoknad!!.soknad.data.okonomi.opplysninger.bekreftelse
        assertThat(bekreftelser.any { harBekreftelseFor(it, UTBETALING_SKATTEETATEN_SAMTYKKE) }).isFalse
        assertThat(bekreftelser.any { harBekreftelseFor(it, BOSTOTTE_SAMTYKKE) }).isFalse
    }

    private fun harBekreftelseFor(bekreftelse: JsonOkonomibekreftelse, bekreftelsesType: String): Boolean {
        return bekreftelse.verdi && bekreftelse.type.equals(bekreftelsesType, ignoreCase = true)
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
            sistEndretDato = LocalDateTime.now()
        )

        every { soknadUnderArbeidRepository.slettSoknad(any(), any()) } just runs
        every { soknadMetadataRepository.hent(any()) } returns createSoknadMetadata()
        every { soknadMetadataRepository.oppdater(any()) } just runs

        soknadService.avbrytSoknad(BEHANDLINGSID, "3")

        verify { soknadUnderArbeidRepository.slettSoknad(any(), any()) }
        verify(exactly = 1) { prometheusMetricsService.reportAvbruttSoknad("3") }
    }

    private fun createSoknadMetadata(): SoknadMetadata {
        return SoknadMetadata(
            id = 0L,
            behandlingsId = BEHANDLINGSID,
            fnr = EIER,
            opprettetDato = LocalDateTime.now(),
            sistEndretDato = LocalDateTime.now()
        )
    }

    companion object {
        private const val EIER = "Hans og Grete"
        private const val BEHANDLINGSID = "123"
    }
}
