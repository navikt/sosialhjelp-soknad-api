package no.nav.sosialhjelp.soknad.innsending.digisosapi

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.unmockkObject
import io.mockk.verify
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknadsmottaker
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.soknad.business.pdfmedpdfbox.SosialhjelpPdfGenerator
import no.nav.sosialhjelp.soknad.common.MiljoUtils
import no.nav.sosialhjelp.soknad.common.filedetection.MimeTypes
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.domain.OpplastetVedlegg
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.domain.VedleggType
import no.nav.sosialhjelp.soknad.domain.Vedleggstatus
import no.nav.sosialhjelp.soknad.innsending.HenvendelseService
import no.nav.sosialhjelp.soknad.innsending.InnsendingService
import no.nav.sosialhjelp.soknad.innsending.SoknadService.Companion.createEmptyJsonInternalSoknad
import no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid.SoknadUnderArbeidService
import no.nav.sosialhjelp.soknad.metrics.SoknadMetricsService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class DigisosApiServiceTest {
    private val digisosApiClient: DigisosApiClient = mockk()
    private val sosialhjelpPdfGenerator: SosialhjelpPdfGenerator = mockk()
    private val innsendingService: InnsendingService = mockk()
    private val henvendelseService: HenvendelseService = mockk()
    private val soknadUnderArbeidService: SoknadUnderArbeidService = mockk()
    private val soknadMetricsService: SoknadMetricsService = mockk()
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository = mockk()

    private val digisosApiService = DigisosApiService(
        digisosApiClient,
        sosialhjelpPdfGenerator,
        innsendingService,
        henvendelseService,
        soknadUnderArbeidService,
        soknadMetricsService,
        soknadUnderArbeidRepository
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
    fun skalLageOpplastingsListeMedDokumenterForSoknad() {
        val soknadUnderArbeid = SoknadUnderArbeid()
            .withJsonInternalSoknad(createEmptyJsonInternalSoknad("12345678910"))
            .withEier("eier")

        every { sosialhjelpPdfGenerator.generate(any(), any()) } returns byteArrayOf(1, 2, 3)
        every { sosialhjelpPdfGenerator.generateBrukerkvitteringPdf() } returns byteArrayOf(1, 2, 3)
        every { innsendingService.hentAlleOpplastedeVedleggForSoknad(any()) } returns lagOpplastetVedlegg()

        val filOpplastings = digisosApiService.lagDokumentListe(soknadUnderArbeid)

        val metadataFil1 = filOpplastings[0].metadata
        assertThat(metadataFil1.filnavn).isEqualTo("Soknad.pdf")
        assertThat(metadataFil1.mimetype).isEqualTo(MimeTypes.APPLICATION_PDF)
        val metadataFil3 = filOpplastings[1].metadata
        assertThat(metadataFil3.filnavn).isEqualTo("Soknad-juridisk.pdf")
        assertThat(metadataFil3.mimetype).isEqualTo(MimeTypes.APPLICATION_PDF)
        val metadataFil4 = filOpplastings[2].metadata
        assertThat(metadataFil4.filnavn).isEqualTo("Brukerkvittering.pdf")
        assertThat(metadataFil4.mimetype).isEqualTo(MimeTypes.APPLICATION_PDF)
    }

    @Test
    fun hentDokumenterFraSoknadReturnererTreDokumenterForEttersendingMedEtVedlegg() {
        every { innsendingService.hentAlleOpplastedeVedleggForSoknad(any()) } returns lagOpplastetVedlegg()
        every { sosialhjelpPdfGenerator.generateEttersendelsePdf(any(), any()) } returns byteArrayOf(1, 2, 3)
        every { sosialhjelpPdfGenerator.generateBrukerkvitteringPdf() } returns byteArrayOf(1, 2, 3)

        val fiksDokumenter = digisosApiService.lagDokumentListe(
            SoknadUnderArbeid()
                .withTilknyttetBehandlingsId("123")
                .withJsonInternalSoknad(lagInternalSoknadForEttersending())
                .withEier("eier")
        )
        assertThat(fiksDokumenter.size).isEqualTo(3)
        assertThat(fiksDokumenter[0].metadata.filnavn).isEqualTo("ettersendelse.pdf")
        assertThat(fiksDokumenter[1].metadata.filnavn).isEqualTo("Brukerkvittering.pdf")
        assertThat(fiksDokumenter[2].metadata.filnavn).isEqualTo("FILNAVN")
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

        val soknadUnderArbeid = SoknadUnderArbeid()
            .withJsonInternalSoknad(createEmptyJsonInternalSoknad("12345678910"))
            .withEier("eier")

        every { sosialhjelpPdfGenerator.generate(any(), any()) } returns byteArrayOf(1, 2, 3)
        every { sosialhjelpPdfGenerator.generateBrukerkvitteringPdf() } returns byteArrayOf(1, 2, 3)
        every { digisosApiClient.krypterOgLastOppFiler(any(), any(), any(), any(), any(), any(), any()) } returns "digisosid"
        every { soknadUnderArbeidService.settInnsendingstidspunktPaSoknad(any()) } just runs
        every { henvendelseService.oppdaterMetadataVedAvslutningAvSoknad(any(), any(), any(), any()) } just runs
        every { innsendingService.hentAlleOpplastedeVedleggForSoknad(any()) } returns lagOpplastetVedlegg()
        every { soknadUnderArbeidRepository.slettSoknad(any(), any()) } just runs
        every { soknadMetricsService.reportSendSoknadMetrics(any(), any()) } just runs

        digisosApiService.sendSoknad(soknadUnderArbeid, "token", "0301")

        verify(exactly = 1) { soknadUnderArbeidRepository.slettSoknad(any(), any()) }

        unmockkObject(MiljoUtils)
    }

    private fun lagInternalSoknadForEttersending(): JsonInternalSoknad {
        val jsonFiler = mutableListOf<JsonFiler>()
        jsonFiler.add(JsonFiler().withFilnavn("FILNAVN").withSha512("sha512"))
        val jsonVedlegg = mutableListOf<JsonVedlegg>()
        jsonVedlegg.add(
            JsonVedlegg()
                .withStatus(Vedleggstatus.LastetOpp.name)
                .withType("type")
                .withTilleggsinfo("tilleggsinfo")
                .withFiler(jsonFiler)
        )
        return JsonInternalSoknad().withVedlegg(JsonVedleggSpesifikasjon().withVedlegg(jsonVedlegg))
    }

    private fun lagOpplastetVedlegg(): List<OpplastetVedlegg> {
        return mutableListOf(
            OpplastetVedlegg()
                .withFilnavn("FILNAVN")
                .withSha512("sha512")
                .withVedleggType(VedleggType("type|tilleggsinfo"))
                .withData(byteArrayOf(1, 2, 3))
        )
    }
}
