package no.nav.sosialhjelp.soknad.innsending.svarut

import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedlegg
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggType
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.Vedleggstatus
import no.nav.sosialhjelp.soknad.innsending.InnsendingService
import no.nav.sosialhjelp.soknad.innsending.SoknadServiceOld.Companion.createEmptyJsonInternalSoknad
import no.nav.sosialhjelp.soknad.pdf.SosialhjelpPdfGenerator
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.MimeTypes
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.InputStream

internal class FiksDokumentHelperTest {
    private val dokumentKrypterer: DokumentKrypterer = mockk()
    private val innsendingService: InnsendingService = mockk()
    private val sosialhjelpPdfGenerator: SosialhjelpPdfGenerator = mockk()

    private var fiksDokumentHelper: FiksDokumentHelper? = null

    @BeforeEach
    fun setup() {
        every { dokumentKrypterer.krypterData(any()) } returns byteArrayOf(3, 2, 1)
        every { innsendingService.hentAlleOpplastedeVedleggForSoknad(any()) } returns lagOpplastedeVedlegg()
        every { sosialhjelpPdfGenerator.generate(any(), any()) } returns byteArrayOf(1, 2, 3)
        every { sosialhjelpPdfGenerator.generateEttersendelsePdf(any(), any()) } returns byteArrayOf(1, 2, 3)
        every { sosialhjelpPdfGenerator.generateBrukerkvitteringPdf() } returns byteArrayOf(1, 2, 3)

        fiksDokumentHelper = FiksDokumentHelper(false, dokumentKrypterer, innsendingService, sosialhjelpPdfGenerator)
    }

    @Test
    fun lagDokumentForSoknadJsonLagerKorrektDokument() {
        val filnavnInputStreamMap = HashMap<String, InputStream>()
        val soknadJson = fiksDokumentHelper!!.lagDokumentForSoknadJson(
            createEmptyJsonInternalSoknad(EIER),
            filnavnInputStreamMap
        )
        assertThat(soknadJson.filnavn).isEqualTo("soknad.json")
        assertThat(soknadJson.mimeType).isEqualTo(MimeTypes.APPLICATION_JSON)
        assertThat(soknadJson.isEkskluderesFraUtskrift).isTrue
        assertThat(filnavnInputStreamMap).hasSize(1)
        assertThat(filnavnInputStreamMap["soknad.json"]).isNotNull
    }

    @Test
    fun lagDokumentForVedleggJsonLagerKorrektDokument() {
        val filnavnInputStreamMap = HashMap<String, InputStream>()
        val vedleggJson = fiksDokumentHelper!!.lagDokumentForVedleggJson(
            lagInternalSoknadForVedlegg(),
            filnavnInputStreamMap
        )
        assertThat(vedleggJson.filnavn).isEqualTo("vedlegg.json")
        assertThat(vedleggJson.mimeType).isEqualTo(MimeTypes.APPLICATION_JSON)
        assertThat(vedleggJson.isEkskluderesFraUtskrift).isTrue
        assertThat(filnavnInputStreamMap).hasSize(1)
        assertThat(filnavnInputStreamMap["vedlegg.json"]).isNotNull
    }

    @Test
    fun lagDokumentForSaksbehandlerPdfLagerKorrektDokument() {
        val filnavnInputStreamMap = HashMap<String, InputStream>()
        val saksbehandlerPdf = fiksDokumentHelper!!.lagDokumentForSaksbehandlerPdf(
            createEmptyJsonInternalSoknad(EIER),
            filnavnInputStreamMap
        )
        assertThat(saksbehandlerPdf.filnavn).isEqualTo("Soknad.pdf")
        assertThat(saksbehandlerPdf.mimeType).isEqualTo(MimeTypes.APPLICATION_PDF)
        assertThat(saksbehandlerPdf.isEkskluderesFraUtskrift).isFalse
        assertThat(filnavnInputStreamMap).hasSize(1)
        assertThat(filnavnInputStreamMap["Soknad.pdf"]).isNotNull
    }

    @Test
    fun lagDokumentForJuridiskPdfLagerKorrektDokument() {
        val filnavnInputStreamMap = HashMap<String, InputStream>()
        val juridiskPdf = fiksDokumentHelper!!.lagDokumentForJuridiskPdf(
            createEmptyJsonInternalSoknad(EIER),
            filnavnInputStreamMap
        )
        assertThat(juridiskPdf.filnavn).isEqualTo("Soknad-juridisk.pdf")
        assertThat(juridiskPdf.mimeType).isEqualTo(MimeTypes.APPLICATION_PDF)
        assertThat(juridiskPdf.isEkskluderesFraUtskrift).isFalse
        assertThat(filnavnInputStreamMap).hasSize(1)
        assertThat(filnavnInputStreamMap["Soknad-juridisk.pdf"]).isNotNull
    }

    @Test
    fun lagDokumentForBrukerkvitteringPdfLagerKorrektDokument() {
        val filnavnInputStreamMap = HashMap<String, InputStream>()
        val brukerkvitteringPdf = fiksDokumentHelper!!.lagDokumentForBrukerkvitteringPdf(filnavnInputStreamMap)
        assertThat(brukerkvitteringPdf.filnavn).isEqualTo("Brukerkvittering.pdf")
        assertThat(brukerkvitteringPdf.mimeType).isEqualTo(MimeTypes.APPLICATION_PDF)
        assertThat(brukerkvitteringPdf.isEkskluderesFraUtskrift).isTrue
        assertThat(filnavnInputStreamMap).hasSize(1)
        assertThat(filnavnInputStreamMap["Brukerkvittering.pdf"]).isNotNull
    }

    @Test
    fun lagDokumentForEttersendelsePdfLagerKorrektDokument() {
        val filnavnInputStreamMap = HashMap<String, InputStream>()
        val ettersendelsePdf = fiksDokumentHelper!!.lagDokumentForEttersendelsePdf(
            createEmptyJsonInternalSoknad(EIER),
            EIER,
            filnavnInputStreamMap
        )
        assertThat(ettersendelsePdf.filnavn).isEqualTo("ettersendelse.pdf")
        assertThat(ettersendelsePdf.mimeType).isEqualTo(MimeTypes.APPLICATION_PDF)
        assertThat(ettersendelsePdf.isEkskluderesFraUtskrift).isFalse
        assertThat(filnavnInputStreamMap).hasSize(1)
        assertThat(filnavnInputStreamMap["ettersendelse.pdf"]).isNotNull
    }

    @Test
    fun lagDokumentListeForVedleggReturnererRiktigeVedlegg() {
        val filnavnInputStreamMap = HashMap<String, InputStream>()
        val dokumenter = fiksDokumentHelper!!.lagDokumentListeForVedlegg(mockk(), filnavnInputStreamMap)
        assertThat(dokumenter).hasSize(3)
        assertThat(dokumenter[0].filnavn).isEqualTo(FILNAVN)
        assertThat(dokumenter[1].filnavn).isEqualTo(ANNET_FILNAVN)
        assertThat(dokumenter[2].filnavn).isEqualTo(TREDJE_FILNAVN)
    }

    @Test
    fun opprettDokumentForVedleggOppretterDokumentKorrekt() {
        val filnavnInputStreamMap = HashMap<String, InputStream>()
        val opplastetVedlegg = OpplastetVedlegg(
            eier = "eier",
            vedleggType = OpplastetVedleggType("$TYPE|$TILLEGGSINFO"),
            data = DATA,
            soknadId = 123L,
            filnavn = FILNAVN,
            sha512 = SHA512
        )
        val dokument = fiksDokumentHelper!!.opprettDokumentForVedlegg(opplastetVedlegg, filnavnInputStreamMap)
        assertThat(dokument.filnavn).isEqualTo(FILNAVN)
        assertThat(dokument.mimeType).isEqualTo("application/octet-stream")
        assertThat(dokument.isEkskluderesFraUtskrift).isTrue
        assertThat(filnavnInputStreamMap).hasSize(1)
        assertThat(filnavnInputStreamMap[FILNAVN]).isNotNull
    }

    @Test
    fun krypterOgOpprettByteDatasourceKryptererHvisSkalKryptereErTrue() {
        fiksDokumentHelper = FiksDokumentHelper(true, dokumentKrypterer, innsendingService, sosialhjelpPdfGenerator)
        val byteArrayInputStream = fiksDokumentHelper!!.krypterOgOpprettByteArrayInputStream(DATA)
        assertThat(byteArrayInputStream.readAllBytes()[0]).isEqualTo(3.toByte())
    }

    @Test
    fun krypterOgOpprettByteDatasourceKryptererIkkeHvisSkalKryptereErFalse() {
        val byteArrayInputStream = fiksDokumentHelper!!.krypterOgOpprettByteArrayInputStream(DATA)
        assertThat(byteArrayInputStream.readAllBytes()[0]).isEqualTo(1.toByte())
    }

    private fun lagOpplastedeVedlegg(): List<OpplastetVedlegg> {
        val opplastedeVedlegg = mutableListOf<OpplastetVedlegg>()
        opplastedeVedlegg.add(
            OpplastetVedlegg(
                eier = "eier",
                vedleggType = OpplastetVedleggType("$TYPE|$TILLEGGSINFO"),
                data = DATA,
                soknadId = 123L,
                filnavn = FILNAVN,
                sha512 = SHA512
            )
        )
        opplastedeVedlegg.add(
            OpplastetVedlegg(
                eier = "eier",
                vedleggType = OpplastetVedleggType("$TYPE2|$TILLEGGSINFO2"),
                data = DATA,
                soknadId = 123L,
                filnavn = ANNET_FILNAVN,
                sha512 = ANNEN_SHA512
            )
        )
        opplastedeVedlegg.add(
            OpplastetVedlegg(
                eier = "eier",
                vedleggType = OpplastetVedleggType("$TYPE2|$TILLEGGSINFO2"),
                data = DATA,
                soknadId = 123L,
                filnavn = TREDJE_FILNAVN,
                sha512 = TREDJE_SHA512
            )
        )
        return opplastedeVedlegg
    }

    private fun lagInternalSoknadForVedlegg(): JsonInternalSoknad {
        val jsonVedlegg = mutableListOf<JsonVedlegg>()
        jsonVedlegg.add(
            JsonVedlegg()
                .withStatus(Vedleggstatus.VedleggKreves.name)
                .withType(TYPE)
                .withTilleggsinfo(TILLEGGSINFO2)
        )
        jsonVedlegg.add(
            JsonVedlegg()
                .withStatus(Vedleggstatus.LastetOpp.name)
                .withType(TYPE)
                .withTilleggsinfo(TILLEGGSINFO)
                .withFiler(lagJsonFiler(FILNAVN, SHA512))
        )
        jsonVedlegg.add(
            JsonVedlegg()
                .withStatus(Vedleggstatus.LastetOpp.name)
                .withType(TYPE2)
                .withTilleggsinfo(TILLEGGSINFO2)
                .withFiler(lagJsonFilerMedToFiler(ANNET_FILNAVN, ANNEN_SHA512, TREDJE_FILNAVN, TREDJE_SHA512))
        )
        return JsonInternalSoknad()
            .withVedlegg(
                JsonVedleggSpesifikasjon()
                    .withVedlegg(jsonVedlegg)
            )
    }

    private fun lagJsonFiler(filnavn: String, sha512: String): MutableList<JsonFiler> {
        val filer = mutableListOf<JsonFiler>()
        filer.add(
            JsonFiler()
                .withFilnavn(filnavn)
                .withSha512(sha512)
        )
        return filer
    }

    private fun lagJsonFilerMedToFiler(filnavn: String, sha: String, filnavn2: String, sha2: String): List<JsonFiler> {
        val jsonFiler = lagJsonFiler(filnavn, sha)
        jsonFiler.add(
            JsonFiler()
                .withFilnavn(filnavn2)
                .withSha512(sha2)
        )
        return jsonFiler
    }

    companion object {
        private const val FILNAVN = "vedlegg.pdf"
        private const val ANNET_FILNAVN = "annetVedlegg.jpg"
        private const val SHA512 = "sha512"
        private const val ANNEN_SHA512 = "annensha512"
        private const val TREDJE_FILNAVN = "tredjeVedlegg.jpg"
        private const val TREDJE_SHA512 = "tredjesha512"
        private const val TYPE = "bostotte"
        private const val TILLEGGSINFO = "annetboutgift"
        private const val TYPE2 = "dokumentasjon"
        private const val TILLEGGSINFO2 = "aksjer"
        private const val EIER = "12345678910"
        private val DATA = byteArrayOf(1, 2, 3)
    }
}
