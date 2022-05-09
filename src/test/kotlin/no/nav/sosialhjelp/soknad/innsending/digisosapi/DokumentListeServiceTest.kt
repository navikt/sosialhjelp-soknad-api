package no.nav.sosialhjelp.soknad.innsending.digisosapi

import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.soknad.common.filedetection.MimeTypes
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedlegg
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggType
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.Vedleggstatus
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.InnsendingService
import no.nav.sosialhjelp.soknad.innsending.SoknadService
import no.nav.sosialhjelp.soknad.pdf.SosialhjelpPdfGenerator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class DokumentListeServiceTest {

    private val innsendingService: InnsendingService = mockk()
    private val sosialhjelpPdfGenerator: SosialhjelpPdfGenerator = mockk()

    private val dokumentListeService = DokumentListeService(innsendingService, sosialhjelpPdfGenerator)

    @Test
    fun skalLageOpplastingsListeMedDokumenterForSoknad() {
        val soknadUnderArbeid = createSoknadUnderArbeid("12345678910")

        every { sosialhjelpPdfGenerator.generate(any(), any()) } returns byteArrayOf(1, 2, 3)
        every { sosialhjelpPdfGenerator.generateBrukerkvitteringPdf() } returns byteArrayOf(1, 2, 3)
        every { innsendingService.hentAlleOpplastedeVedleggForSoknad(any()) } returns lagOpplastetVedlegg()

        val filOpplastings = dokumentListeService.lagDokumentListe(soknadUnderArbeid)

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

        val soknadUnderArbeid = createSoknadUnderArbeid("eier")
        soknadUnderArbeid.tilknyttetBehandlingsId = "123"
        soknadUnderArbeid.jsonInternalSoknad = lagInternalSoknadForEttersending()
        val fiksDokumenter = dokumentListeService.lagDokumentListe(soknadUnderArbeid)
        assertThat(fiksDokumenter.size).isEqualTo(3)
        assertThat(fiksDokumenter[0].metadata.filnavn).isEqualTo("ettersendelse.pdf")
        assertThat(fiksDokumenter[1].metadata.filnavn).isEqualTo("Brukerkvittering.pdf")
        assertThat(fiksDokumenter[2].metadata.filnavn).isEqualTo("FILNAVN")
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
            OpplastetVedlegg(
                eier = "eier",
                vedleggType = OpplastetVedleggType("type|tilleggsinfo"),
                data = byteArrayOf(1, 2, 3),
                soknadId = 123L,
                filnavn = "FILNAVN",
                sha512 = "sha512"
            )
        )
    }

    private fun createSoknadUnderArbeid(eier: String): SoknadUnderArbeid {
        return SoknadUnderArbeid(
            versjon = 1L,
            behandlingsId = "behandlingsid",
            tilknyttetBehandlingsId = null,
            eier = eier,
            jsonInternalSoknad = SoknadService.createEmptyJsonInternalSoknad(eier),
            status = SoknadUnderArbeidStatus.UNDER_ARBEID,
            opprettetDato = LocalDateTime.now(),
            sistEndretDato = LocalDateTime.now()
        )
    }
}
