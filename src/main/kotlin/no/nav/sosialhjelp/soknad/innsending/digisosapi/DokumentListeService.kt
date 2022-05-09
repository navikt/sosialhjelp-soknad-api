package no.nav.sosialhjelp.soknad.innsending.digisosapi

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.kotlin.utils.logger
import no.nav.sosialhjelp.soknad.common.filedetection.FileDetectionUtils
import no.nav.sosialhjelp.soknad.common.filedetection.MimeTypes
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedlegg
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.innsending.InnsendingService
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilMetadata
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilOpplasting
import no.nav.sosialhjelp.soknad.pdf.SosialhjelpPdfGenerator
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream

@Component
class DokumentListeService(
    private val innsendingService: InnsendingService,
    private val sosialhjelpPdfGenerator: SosialhjelpPdfGenerator,
) {

    fun lagDokumentListe(soknadUnderArbeid: SoknadUnderArbeid): List<FilOpplasting> {
        val internalSoknad = soknadUnderArbeid.jsonInternalSoknad
        if (internalSoknad == null) {
            throw RuntimeException("Kan ikke sende forsendelse til FIKS fordi søknad mangler")
        } else if (!soknadUnderArbeid.erEttersendelse && internalSoknad.soknad == null) {
            throw RuntimeException("Kan ikke sende søknad fordi søknaden mangler")
        } else if (soknadUnderArbeid.erEttersendelse && internalSoknad.vedlegg == null) {
            throw RuntimeException("Kan ikke sende ettersendelse fordi vedlegg mangler")
        }
        val antallVedleggForsendelse: Int

        val filOpplastinger = mutableListOf<FilOpplasting>()

        if (soknadUnderArbeid.erEttersendelse) {
            filOpplastinger.add(lagDokumentForEttersendelsePdf(internalSoknad, soknadUnderArbeid.eier))
            filOpplastinger.add(lagDokumentForBrukerkvitteringPdf())
            val dokumenterForVedlegg = lagDokumentListeForVedlegg(soknadUnderArbeid)
            antallVedleggForsendelse = dokumenterForVedlegg.size
            filOpplastinger.addAll(dokumenterForVedlegg)
        } else {
            filOpplastinger.add(lagDokumentForSaksbehandlerPdf(internalSoknad))
            filOpplastinger.add(lagDokumentForJuridiskPdf(internalSoknad))
            filOpplastinger.add(lagDokumentForBrukerkvitteringPdf())
            val dokumenterForVedlegg = lagDokumentListeForVedlegg(soknadUnderArbeid)
            antallVedleggForsendelse = dokumenterForVedlegg.size
            filOpplastinger.addAll(dokumenterForVedlegg)
        }
        val antallFiksDokumenter = filOpplastinger.size
        log.info("Antall vedlegg: $antallFiksDokumenter. Antall vedlegg lastet opp av bruker: $antallVedleggForsendelse")

        try {
            val opplastedeVedleggstyper = internalSoknad.vedlegg.vedlegg.filter { it.status == "LastetOpp" }
            var antallBrukerOpplastedeVedlegg = 0
            opplastedeVedleggstyper.forEach { antallBrukerOpplastedeVedlegg += it.filer.size }
            if (antallVedleggForsendelse != antallBrukerOpplastedeVedlegg) {
                log.warn("Ulikt antall vedlegg i vedlegg.json og forsendelse til Fiks. vedlegg.json: $antallBrukerOpplastedeVedlegg, forsendelse til Fiks: $antallVedleggForsendelse. Er ettersendelse: ${soknadUnderArbeid.erEttersendelse}")
            }
        } catch (e: RuntimeException) {
            log.debug("Ignored exception")
        }
        return filOpplastinger
    }

    private fun lagDokumentForSaksbehandlerPdf(jsonInternalSoknad: JsonInternalSoknad): FilOpplasting {
        val filnavn = "Soknad.pdf"
        val soknadPdf = sosialhjelpPdfGenerator.generate(jsonInternalSoknad, false)
        return opprettFilOpplastingFraByteArray(filnavn, MimeTypes.APPLICATION_PDF, soknadPdf)
    }

    private fun lagDokumentListeForVedlegg(soknadUnderArbeid: SoknadUnderArbeid): List<FilOpplasting> {
        val opplastedeVedlegg = innsendingService.hentAlleOpplastedeVedleggForSoknad(soknadUnderArbeid)
        return opplastedeVedlegg.map { opprettDokumentForVedlegg(it) }
    }

    private fun lagDokumentForEttersendelsePdf(internalSoknad: JsonInternalSoknad, eier: String): FilOpplasting {
        val filnavn = "ettersendelse.pdf"
        val pdf = sosialhjelpPdfGenerator.generateEttersendelsePdf(internalSoknad, eier)
        return opprettFilOpplastingFraByteArray(filnavn, MimeTypes.APPLICATION_PDF, pdf)
    }

    private fun lagDokumentForBrukerkvitteringPdf(): FilOpplasting {
        val filnavn = "Brukerkvittering.pdf"
        val pdf = sosialhjelpPdfGenerator.generateBrukerkvitteringPdf()
        return opprettFilOpplastingFraByteArray(filnavn, MimeTypes.APPLICATION_PDF, pdf)
    }

    private fun lagDokumentForJuridiskPdf(internalSoknad: JsonInternalSoknad): FilOpplasting {
        val filnavn = "Soknad-juridisk.pdf"
        val pdf = sosialhjelpPdfGenerator.generate(internalSoknad, true)
        return opprettFilOpplastingFraByteArray(filnavn, MimeTypes.APPLICATION_PDF, pdf)
    }

    private fun opprettDokumentForVedlegg(opplastetVedlegg: OpplastetVedlegg): FilOpplasting {
        val bytes = opplastetVedlegg.data
        val detectedMimeType = FileDetectionUtils.getMimeType(bytes)
        val mimetype = if (detectedMimeType.equals(MimeTypes.TEXT_X_MATLAB, ignoreCase = true)) MimeTypes.APPLICATION_PDF else detectedMimeType
        return FilOpplasting(
            metadata = FilMetadata(
                filnavn = opplastetVedlegg.filnavn,
                mimetype = mimetype,
                storrelse = bytes.size.toLong()
            ),
            data = ByteArrayInputStream(bytes)
        )
    }

    private fun opprettFilOpplastingFraByteArray(filnavn: String, mimetype: String, bytes: ByteArray): FilOpplasting {
        return FilOpplasting(
            metadata = FilMetadata(
                filnavn = filnavn,
                mimetype = mimetype,
                storrelse = bytes.size.toLong()
            ),
            data = ByteArrayInputStream(bytes)
        )
    }

    companion object {
        private val log by logger()
    }
}
