package no.nav.sosialhjelp.soknad.innsending.digisosapi

import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.SoknadServiceOld
import no.nav.sosialhjelp.soknad.pdf.SosialhjelpPdfGenerator
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringService
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.MimeTypes
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class DokumentListeServiceTest {
    private val sosialhjelpPdfGenerator: SosialhjelpPdfGenerator = mockk()
    private val mellomlagringService: MellomlagringService = mockk()

    private val dokumentListeService = DokumentListeService(sosialhjelpPdfGenerator, mellomlagringService)

    private val eier = "12345678910"

    @Test
    fun `skal lage opplastingsListe med dokumenter for soknad`() {
        val soknadUnderArbeid =
            SoknadUnderArbeid(
                versjon = 1L,
                behandlingsId = "behandlingsid",
                tilknyttetBehandlingsId = null,
                eier = eier,
                jsonInternalSoknad = SoknadServiceOld.createEmptyJsonInternalSoknad(eier),
                status = SoknadUnderArbeidStatus.UNDER_ARBEID,
                opprettetDato = LocalDateTime.now(),
                sistEndretDato = LocalDateTime.now(),
            )

        every { sosialhjelpPdfGenerator.generate(any(), any()) } returns byteArrayOf(1, 2, 3)
        every { sosialhjelpPdfGenerator.generateBrukerkvitteringPdf() } returns byteArrayOf(1, 2, 3)

        every { mellomlagringService.getAllVedlegg(any()).size } returns 1

        val filOpplastings = dokumentListeService.getFilOpplastingList(soknadUnderArbeid)

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
}
