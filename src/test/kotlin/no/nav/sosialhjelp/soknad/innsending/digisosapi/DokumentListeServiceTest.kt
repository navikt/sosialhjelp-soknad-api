package no.nav.sosialhjelp.soknad.innsending.digisosapi

import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.soknad.common.filedetection.MimeTypes
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedlegg
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggType
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
    fun `skal lage opplastingsListe med dokumenter for soknad - digisosApi v1`() {
        val soknadUnderArbeid = createSoknadUnderArbeid("12345678910")

        every { sosialhjelpPdfGenerator.generate(any(), any()) } returns byteArrayOf(1, 2, 3)
        every { sosialhjelpPdfGenerator.generateBrukerkvitteringPdf() } returns byteArrayOf(1, 2, 3)
        every { innsendingService.hentAlleOpplastedeVedleggForSoknad(any()) } returns lagOpplastetVedlegg()

        val filOpplastings = dokumentListeService.lagDokumentListe(soknadUnderArbeid)

        val metadataFil1 = filOpplastings[0].metadata
        assertThat(metadataFil1.filnavn).isEqualTo("Soknad.pdf")
        assertThat(metadataFil1.mimetype).isEqualTo(MimeTypes.APPLICATION_PDF)
        val metadataFil2 = filOpplastings[1].metadata
        assertThat(metadataFil2.filnavn).isEqualTo("Soknad-juridisk.pdf")
        assertThat(metadataFil2.mimetype).isEqualTo(MimeTypes.APPLICATION_PDF)
        val metadataFil3 = filOpplastings[2].metadata
        assertThat(metadataFil3.filnavn).isEqualTo("Brukerkvittering.pdf")
        assertThat(metadataFil3.mimetype).isEqualTo(MimeTypes.APPLICATION_PDF)
        val metadataFil4 = filOpplastings[3].metadata
        assertThat(metadataFil4.filnavn).isEqualTo("FILNAVN")
        assertThat(metadataFil4.mimetype).isEqualTo("application/octet-stream")
        assertThat(metadataFil4.storrelse).isEqualTo(3)
    }

    @Test
    fun `skal lage opplastingsListe med dokumenter for soknad - digisosApi v2`() {
        val soknadUnderArbeid = createSoknadUnderArbeid("12345678910")

        every { sosialhjelpPdfGenerator.generate(any(), any()) } returns byteArrayOf(1, 2, 3)
        every { sosialhjelpPdfGenerator.generateBrukerkvitteringPdf() } returns byteArrayOf(1, 2, 3)

        val filOpplastings = dokumentListeService.lagDokumentListeForV2(soknadUnderArbeid)

        val metadataFil1 = filOpplastings[0].metadata
        assertThat(metadataFil1.filnavn).isEqualTo("Soknad.pdf")
        assertThat(metadataFil1.mimetype).isEqualTo(MimeTypes.APPLICATION_PDF)
        val metadataFil2 = filOpplastings[1].metadata
        assertThat(metadataFil2.filnavn).isEqualTo("Soknad-juridisk.pdf")
        assertThat(metadataFil2.mimetype).isEqualTo(MimeTypes.APPLICATION_PDF)
        val metadataFil3 = filOpplastings[2].metadata
        assertThat(metadataFil3.filnavn).isEqualTo("Brukerkvittering.pdf")
        assertThat(metadataFil3.mimetype).isEqualTo(MimeTypes.APPLICATION_PDF)
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
