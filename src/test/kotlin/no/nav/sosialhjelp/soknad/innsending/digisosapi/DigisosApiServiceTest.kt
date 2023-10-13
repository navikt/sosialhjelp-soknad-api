package no.nav.sosialhjelp.soknad.innsending.digisosapi

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.unmockkObject
import io.mockk.verify
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknadsmottaker
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.repository.soknadmetadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.repository.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.repository.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.repository.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.repository.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.OldSoknadService.Companion.createEmptyJsonInternalSoknad
import no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid.SoknadUnderArbeidService
import no.nav.sosialhjelp.soknad.metrics.PrometheusMetricsService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.LocalDateTime

internal class DigisosApiServiceTest {
    private val digisosApiV2Client: DigisosApiV2Client = mockk()
    private val soknadUnderArbeidService: SoknadUnderArbeidService = mockk()
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository = mockk()
    private val soknadMetadataRepository: SoknadMetadataRepository = mockk()
    private val dokumentListeService: DokumentListeService = mockk()
    private val prometheusMetricsService: PrometheusMetricsService = mockk(relaxed = true)

    private val digisosApiService = DigisosApiService(
        digisosApiV2Client,
        soknadUnderArbeidService,
        soknadUnderArbeidRepository,
        soknadMetadataRepository,
        dokumentListeService,
        prometheusMetricsService,
        Clock.systemDefaultZone()
    )

    private val eier = "12345678910"

    @BeforeEach
    fun setUpBefore() {
        clearAllMocks()

        mockkObject(MiljoUtils)
        every { MiljoUtils.isNonProduction() } returns true
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(MiljoUtils)
    }

    @Test
    fun tilleggsinformasjonJson() {
        val soknad = JsonSoknad().withMottaker(JsonSoknadsmottaker().withEnhetsnummer("1234"))
        val tilleggsinformasjonJson = digisosApiService.getTilleggsinformasjonJson(soknad)
        assertThat(tilleggsinformasjonJson).isEqualTo("{\"enhetsnummer\":\"1234\"}")
    }

    @Test
    fun tilleggsinformasjonJson_withNoEnhetsnummer_shouldSetEnhetsnummerToNull() {
        val soknad = JsonSoknad().withMottaker(JsonSoknadsmottaker())
        val tilleggsinformasjonJson = digisosApiService.getTilleggsinformasjonJson(soknad)
        assertThat(tilleggsinformasjonJson).isEqualTo("{}")
    }

    @Test
    fun tilleggsinformasjonJson_withNoMottaker_shouldThrowException() {
        val soknad = JsonSoknad()
        assertThatExceptionOfType(IllegalStateException::class.java)
            .isThrownBy { digisosApiService.getTilleggsinformasjonJson(soknad) }
    }

    @Test
    fun etterInnsendingSkalSoknadUnderArbeidSlettes() {
        mockkObject(MiljoUtils)
        every { MiljoUtils.isNonProduction() } returns true
        every { MiljoUtils.environmentName } returns "test"

        val soknadUnderArbeid = SoknadUnderArbeid(
            versjon = 1L,
            behandlingsId = "behandlingsid",
            tilknyttetBehandlingsId = null,
            eier = eier,
            jsonInternalSoknad = createEmptyJsonInternalSoknad(eier),
            status = SoknadUnderArbeidStatus.UNDER_ARBEID,
            opprettetDato = LocalDateTime.now(),
            sistEndretDato = LocalDateTime.now()
        )

        val soknadMetadata = SoknadMetadata(
            id = 1L,
            behandlingsId = "behandlingsid",
            fnr = "12345678910",
            opprettetDato = LocalDateTime.now(),
            sistEndretDato = LocalDateTime.now()
        )

        every { dokumentListeService.getFilOpplastingList(any()) } returns emptyList()
        every { digisosApiV2Client.krypterOgLastOppFiler(any(), any(), any(), any(), any(), any(), any()) } returns "digisosid"
        every { soknadUnderArbeidService.settInnsendingstidspunktPaSoknad(any()) } just runs
        every { soknadMetadataRepository.hent(any()) } returns soknadMetadata
        every { soknadMetadataRepository.oppdater(any()) } just runs
        every { soknadUnderArbeidRepository.slettSoknad(any(), any()) } just runs

        digisosApiService.sendSoknad(soknadUnderArbeid, "token", "0301")

        verify(exactly = 1) { soknadUnderArbeidRepository.slettSoknad(any(), any()) }

        unmockkObject(MiljoUtils)
    }
}
