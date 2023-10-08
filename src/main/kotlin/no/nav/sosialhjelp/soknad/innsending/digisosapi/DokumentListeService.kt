package no.nav.sosialhjelp.soknad.innsending.digisosapi

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.repository.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilMetadata
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilOpplasting
import no.nav.sosialhjelp.soknad.pdf.SosialhjelpPdfGenerator
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringService
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.MimeTypes
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream

@Component
class DokumentListeService(
    private val sosialhjelpPdfGenerator: SosialhjelpPdfGenerator,
    private val mellomlagringService: MellomlagringService
) {

    fun getFilOpplastingList(soknadUnderArbeid: SoknadUnderArbeid): List<FilOpplasting> {
        val internalSoknad = soknadUnderArbeid.jsonInternalSoknad
        if (internalSoknad == null) {
            throw RuntimeException("Kan ikke sende forsendelse til FIKS fordi søknad mangler")
        } else if (internalSoknad.soknad == null) {
            throw RuntimeException("Kan ikke sende søknad fordi søknaden mangler")
        }

        val mellomlagredeVedlegg = mellomlagringService.getAllVedlegg(soknadUnderArbeid.behandlingsId)

        return listOf(
            lagDokumentForSaksbehandlerPdf(internalSoknad),
            lagDokumentForJuridiskPdf(internalSoknad),
            lagDokumentForBrukerkvitteringPdf()
        ).also {
            log.info("Antall vedlegg: ${it.size}. Antall mellomlagrede vedlegg: ${mellomlagredeVedlegg.size}")
        }
    }

    private fun lagDokumentForSaksbehandlerPdf(jsonInternalSoknad: JsonInternalSoknad): FilOpplasting {
        val filnavn = "Soknad.pdf"
        val soknadPdf = sosialhjelpPdfGenerator.generate(jsonInternalSoknad, false)
        return opprettFilOpplastingFraByteArray(filnavn, soknadPdf)
    }

    private fun lagDokumentForBrukerkvitteringPdf(): FilOpplasting {
        val filnavn = "Brukerkvittering.pdf"
        val pdf = sosialhjelpPdfGenerator.generateBrukerkvitteringPdf()
        return opprettFilOpplastingFraByteArray(filnavn, pdf)
    }

    private fun lagDokumentForJuridiskPdf(internalSoknad: JsonInternalSoknad): FilOpplasting {
        val filnavn = "Soknad-juridisk.pdf"
        val pdf = sosialhjelpPdfGenerator.generate(internalSoknad, true)
        return opprettFilOpplastingFraByteArray(filnavn, pdf)
    }

    private fun opprettFilOpplastingFraByteArray(filnavn: String, bytes: ByteArray): FilOpplasting {
        return FilOpplasting(
            metadata = FilMetadata(
                filnavn = filnavn,
                mimetype = MimeTypes.APPLICATION_PDF,
                storrelse = bytes.size.toLong()
            ),
            data = ByteArrayInputStream(bytes)
        )
    }

    companion object {
        private val log by logger()
    }
}
