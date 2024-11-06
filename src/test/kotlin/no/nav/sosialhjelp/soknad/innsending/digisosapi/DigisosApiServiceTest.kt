package no.nav.sosialhjelp.soknad.innsending.digisosapi

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.unmockkObject
import io.mockk.verify
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknadsmottaker
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.TimestampFixer
import no.nav.sosialhjelp.soknad.innsending.SoknadServiceOld.Companion.createEmptyJsonInternalSoknad
import no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid.SoknadUnderArbeidService
import no.nav.sosialhjelp.soknad.metrics.PrometheusMetricsService
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggStatus
import no.nav.sosialhjelp.soknad.v2.json.compare.ShadowProductionManager
import no.nav.sosialhjelp.soknad.v2.shadow.SoknadV2AdapterService
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringService
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
    private val mellomlagringService: MellomlagringService = mockk()
    private val prometheusMetricsService: PrometheusMetricsService = mockk(relaxed = true)
    private val shadowProductionManager: ShadowProductionManager = mockk(relaxed = true)
    private val v2RegisterDataAdapter: SoknadV2AdapterService = mockk(relaxed = true)
    private val maskinportenV2Service: MaskinportenV2Service = mockk()

    private val digisosApiService =
        DigisosApiService(
            digisosApiV2Client,
            soknadUnderArbeidService,
            soknadUnderArbeidRepository,
            soknadMetadataRepository,
            dokumentListeService,
            prometheusMetricsService,
            Clock.systemDefaultZone(),
            shadowProductionManager,
            v2RegisterDataAdapter,
            mellomlagringService,
            maskinportenV2Service,
        )

    private val eier = "12345678910"

    @BeforeEach
    fun setUpBefore() {
        clearAllMocks()

        mockkObject(MiljoUtils)
        every { MiljoUtils.isNonProduction() } returns true
        every { soknadUnderArbeidService.settInnsendingstidspunktPaSoknad(any(), any()) } just runs
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
        every { mellomlagringService.getAllVedlegg(any(String::class)) } returns emptyList()

        val soknadUnderArbeid =
            SoknadUnderArbeid(
                versjon = 1L,
                behandlingsId = "behandlingsid",
                eier = eier,
                jsonInternalSoknad = createEmptyJsonInternalSoknad(eier, false),
                status = SoknadUnderArbeidStatus.UNDER_ARBEID,
                opprettetDato = LocalDateTime.now(),
                sistEndretDato = LocalDateTime.now(),
            )

        val soknadMetadata =
            SoknadMetadata(
                id = 1L,
                behandlingsId = "behandlingsid",
                fnr = "12345678910",
                opprettetDato = LocalDateTime.now(),
                sistEndretDato = LocalDateTime.now(),
                kortSoknad = false,
            )

        every { dokumentListeService.getFilOpplastingList(any()) } returns emptyList()
        every {
            digisosApiV2Client.krypterOgLastOppFiler(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
            )
        } returns "digisosid"
        every {
            soknadUnderArbeidService.settInnsendingstidspunktPaSoknad(
                any(),
            )
        } just runs
        every { soknadMetadataRepository.hent(any()) } returns soknadMetadata
        every { soknadMetadataRepository.oppdater(any()) } just runs
        every { soknadUnderArbeidRepository.slettSoknad(any(), any()) } just runs

        digisosApiService.sendSoknad(soknadUnderArbeid, "token", "0301")

        verify(exactly = 1) { soknadUnderArbeidRepository.slettSoknad(any(), any()) }

        unmockkObject(MiljoUtils)
    }

    @Test
    fun `Vedlegg med status AlleredeSendt og filer skal fil-referanser fjernes`() {
        mockkObject(MiljoUtils)
        every { MiljoUtils.isNonProduction() } returns true
        every { MiljoUtils.environmentName } returns "test"
        every { mellomlagringService.getAllVedlegg(any(String::class)) } returns emptyList()

        val soknadUnderArbeid =
            SoknadUnderArbeid(
                versjon = 1L,
                behandlingsId = "behandlingsid",
                eier = eier,
                jsonInternalSoknad =
                    createEmptyJsonInternalSoknad(eier, false)
                        .withVedlegg(JsonVedleggSpesifikasjon().withVedlegg(createVedlegg(VedleggStatus.VedleggAlleredeSendt))),
                status = SoknadUnderArbeidStatus.UNDER_ARBEID,
                opprettetDato = LocalDateTime.now(),
                sistEndretDato = LocalDateTime.now(),
            )

        val soknadMetadata =
            SoknadMetadata(
                id = 1L,
                behandlingsId = "behandlingsid",
                fnr = "12345678910",
                opprettetDato = LocalDateTime.now(),
                sistEndretDato = LocalDateTime.now(),
                kortSoknad = false,
            )

        every { dokumentListeService.getFilOpplastingList(any()) } returns emptyList()
        every {
            digisosApiV2Client.krypterOgLastOppFiler(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
            )
        } returns "digisosid"
        every {
            soknadUnderArbeidService.settInnsendingstidspunktPaSoknad(
                any(),
            )
        } just runs
        every { soknadMetadataRepository.hent(any()) } returns soknadMetadata
        every { soknadMetadataRepository.oppdater(any()) } just runs
        every { soknadUnderArbeidRepository.slettSoknad(any(), any()) } just runs

        digisosApiService.sendSoknad(soknadUnderArbeid, "token", "0301")

        assertThat(soknadUnderArbeid.jsonInternalSoknad!!.vedlegg.vedlegg.first().filer).hasSize(0)

        unmockkObject(MiljoUtils)
    }

    @Test
    fun `Vedlegg med status LastetOpp men filer som ikke finne skal fil-referanser fjernes`() {
        mockkObject(MiljoUtils)
        every { MiljoUtils.isNonProduction() } returns true
        every { MiljoUtils.environmentName } returns "test"
        every { mellomlagringService.getAllVedlegg(any(String::class)) } returns emptyList()

        val soknadUnderArbeid =
            SoknadUnderArbeid(
                versjon = 1L,
                behandlingsId = "behandlingsid",
                eier = eier,
                jsonInternalSoknad =
                    createEmptyJsonInternalSoknad(eier, false)
                        .withVedlegg(JsonVedleggSpesifikasjon().withVedlegg(createVedlegg(VedleggStatus.LastetOpp))),
                status = SoknadUnderArbeidStatus.UNDER_ARBEID,
                opprettetDato = LocalDateTime.now(),
                sistEndretDato = LocalDateTime.now(),
            )

        val soknadMetadata =
            SoknadMetadata(
                id = 1L,
                behandlingsId = "behandlingsid",
                fnr = "12345678910",
                opprettetDato = LocalDateTime.now(),
                sistEndretDato = LocalDateTime.now(),
                kortSoknad = false,
            )

        every { dokumentListeService.getFilOpplastingList(any()) } returns emptyList()
        every {
            digisosApiV2Client.krypterOgLastOppFiler(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
            )
        } returns "digisosid"
        every {
            soknadUnderArbeidService.settInnsendingstidspunktPaSoknad(
                any(),
            )
        } just runs
        every { soknadMetadataRepository.hent(any()) } returns soknadMetadata
        every { soknadMetadataRepository.oppdater(any()) } just runs
        every { soknadUnderArbeidRepository.slettSoknad(any(), any()) } just runs

        digisosApiService.sendSoknad(soknadUnderArbeid, "token", "0301")

        assertThat(soknadUnderArbeid.jsonInternalSoknad!!.vedlegg.vedlegg.first().filer).hasSize(0)

        unmockkObject(MiljoUtils)
    }

    private fun createVedlegg(status: VedleggStatus): List<JsonVedlegg> {
        return listOf(
            JsonVedlegg().withStatus(status.toString())
                .withFiler(
                    listOf(
                        JsonFiler().withFilnavn("fil1.pdf").withSha512("sha512"),
                        JsonFiler().withFilnavn("fil2.pdf").withSha512("sha512"),
                    ),
                ),
        )
    }

    @Test
    fun `Verifiser at broken timestamps fikses`() {
        val json = createJsonInternalSoknadWithInvalidTimestamps()

        TimestampFixer.fixBrokenTimestamps(json)
            .also { isAnyTimestampsChanged ->
                assertThat(isAnyTimestampsChanged).isTrue()
                assertThat(json.soknad.innsendingstidspunkt).matches(REGEX)
                json.soknad.data.okonomi.opplysninger.bekreftelse
                    .forEach { assertThat(it.bekreftelsesDato).matches(REGEX) }
            }
    }

    @Test
    fun `Skal ikke fikse godkjente timestamps`() {
        val validTimestamp = "2023-10-10T10:10:10.999Z"

        val json =
            JsonInternalSoknad().withSoknad(
                JsonSoknad()
                    .withInnsendingstidspunkt(validTimestamp)
                    .withData(
                        JsonData().withOkonomi(
                            JsonOkonomi().withOpplysninger(
                                JsonOkonomiopplysninger().withBekreftelse(
                                    listOf(
                                        JsonOkonomibekreftelse().withBekreftelsesDato("2024-09-09T09:09:09Z"),
                                    ),
                                ),
                            ),
                        ),
                    ),
            )

        TimestampFixer.fixBrokenTimestamps(json)
            .also { anyTimestampFixed ->
                assertThat(anyTimestampFixed).isTrue()
                assertThat(json.soknad.innsendingstidspunkt).isEqualTo(validTimestamp)
                assertThat(json.soknad.innsendingstidspunkt).matches(REGEX)

                json.soknad.data.okonomi.opplysninger.bekreftelse.forEach {
                    assertThat(it.bekreftelsesDato).matches(REGEX)
                }
            }
    }

    @Test
    fun `Fikse tidspunkt aksepterer null-objekter eller ingen bekreftelser`() {
        val json =
            JsonInternalSoknad().withSoknad(
                JsonSoknad().withInnsendingstidspunkt("2023-10-10T10:10:10.041Z"),
            )

        TimestampFixer.fixBrokenTimestamps(json)
            .also { anyTimestampChanged ->
                assertThat(anyTimestampChanged).isFalse()
                assertThat(json.soknad.innsendingstidspunkt).matches(REGEX)
                assertThat(json.soknad.data).isNull()
            }
    }

    companion object {
        private const val REGEX =
            "^[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]T[0-9][0-9]:[0-9][0-9]" +
                ":[0-9][0-9].[0-9][0-9]*Z$"
    }

    private fun createJsonInternalSoknadWithInvalidTimestamps(): JsonInternalSoknad {
        return createEmptyJsonInternalSoknad(eier, false)
            .withSoknad(
                JsonSoknad()
                    .withInnsendingstidspunkt("2024-09-05T21:34:37Z")
                    .withData(
                        JsonData()
                            .withOkonomi(
                                JsonOkonomi()
                                    .withOpplysninger(
                                        JsonOkonomiopplysninger()
                                            .withBekreftelse(
                                                listOf(
                                                    JsonOkonomibekreftelse().withBekreftelsesDato("2024-09-05T21:34:37Z"),
                                                    JsonOkonomibekreftelse().withBekreftelsesDato("2024-09-05T21:34:37Z"),
                                                ),
                                            ),
                                    ),
                            ),
                    ),
            )
    }
}
