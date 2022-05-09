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
import no.nav.sosialhjelp.soknad.common.MiljoUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.HenvendelseService
import no.nav.sosialhjelp.soknad.innsending.SoknadService.Companion.createEmptyJsonInternalSoknad
import no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid.SoknadUnderArbeidService
import no.nav.sosialhjelp.soknad.metrics.SoknadMetricsService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class DigisosApiV1ServiceTest {
    private val digisosApiV1Client: DigisosApiV1Client = mockk()
    private val henvendelseService: HenvendelseService = mockk()
    private val soknadUnderArbeidService: SoknadUnderArbeidService = mockk()
    private val soknadMetricsService: SoknadMetricsService = mockk()
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository = mockk()
    private val dokumentListeService: DokumentListeService = mockk()

    private val digisosApiV1Service = DigisosApiV1Service(
        digisosApiV1Client,
        henvendelseService,
        soknadUnderArbeidService,
        soknadMetricsService,
        soknadUnderArbeidRepository,
        dokumentListeService
    )

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
        val tilleggsinformasjonJson = digisosApiV1Service.getTilleggsinformasjonJson(soknad)
        assertThat(tilleggsinformasjonJson).isEqualTo("{\"enhetsnummer\":\"1234\"}")
    }

    @Test
    fun tilleggsinformasjonJson_withNoEnhetsnummer_shouldSetEnhetsnummerToNull() {
        val soknad = JsonSoknad().withMottaker(JsonSoknadsmottaker())
        val tilleggsinformasjonJson = digisosApiV1Service.getTilleggsinformasjonJson(soknad)
        assertThat(tilleggsinformasjonJson).isEqualTo("{}")
    }

    @Test
    fun tilleggsinformasjonJson_withNoMottaker_shouldThrowException() {
        val soknad = JsonSoknad()
        assertThatExceptionOfType(IllegalStateException::class.java)
            .isThrownBy { digisosApiV1Service.getTilleggsinformasjonJson(soknad) }
    }

    @Test
    fun etterInnsendingSkalSoknadUnderArbeidSlettes() {
        mockkObject(MiljoUtils)
        every { MiljoUtils.isNonProduction() } returns true
        every { MiljoUtils.environmentName } returns "test"

        val soknadUnderArbeid = createSoknadUnderArbeid("12345678910")

        every { dokumentListeService.lagDokumentListe(any()) } returns emptyList()
        every { digisosApiV1Client.krypterOgLastOppFiler(any(), any(), any(), any(), any(), any(), any()) } returns "digisosid"
        every { soknadUnderArbeidService.settInnsendingstidspunktPaSoknad(any()) } just runs
        every { henvendelseService.oppdaterMetadataVedAvslutningAvSoknad(any(), any(), any(), any()) } just runs
        every { soknadUnderArbeidRepository.slettSoknad(any(), any()) } just runs
        every { soknadMetricsService.reportSendSoknadMetrics(any(), any()) } just runs

        digisosApiV1Service.sendSoknad(soknadUnderArbeid, "token", "0301")

        verify(exactly = 1) { soknadUnderArbeidRepository.slettSoknad(any(), any()) }

        unmockkObject(MiljoUtils)
    }

    private fun createSoknadUnderArbeid(eier: String): SoknadUnderArbeid {
        return SoknadUnderArbeid(
            versjon = 1L,
            behandlingsId = "behandlingsid",
            tilknyttetBehandlingsId = null,
            eier = eier,
            jsonInternalSoknad = createEmptyJsonInternalSoknad(eier),
            status = SoknadUnderArbeidStatus.UNDER_ARBEID,
            opprettetDato = LocalDateTime.now(),
            sistEndretDato = LocalDateTime.now()
        )
    }
}
