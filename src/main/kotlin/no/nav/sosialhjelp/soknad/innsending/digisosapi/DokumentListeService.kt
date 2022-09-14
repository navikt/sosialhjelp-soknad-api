package no.nav.sosialhjelp.soknad.innsending.digisosapi

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedlegg
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.innsending.InnsendingService
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilMetadata
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilOpplasting
import no.nav.sosialhjelp.soknad.pdf.SosialhjelpPdfGenerator
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.FileDetectionUtils.getMimeType
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.MimeTypes
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
        } else if (internalSoknad.soknad == null) {
            throw RuntimeException("Kan ikke sende søknad fordi søknaden mangler")
        }

        val dokumenterForVedlegg = lagDokumentListeForVedlegg(soknadUnderArbeid)
        val antallVedleggForsendelse = dokumenterForVedlegg.size

        val filOpplastinger = listOf(
            lagDokumentForSaksbehandlerPdf(internalSoknad),
            lagDokumentForJuridiskPdf(internalSoknad),
            lagDokumentForBrukerkvitteringPdf(),
            *dokumenterForVedlegg.toTypedArray()
        ).also {
            log.info("Antall vedlegg: ${it.size}. Antall vedlegg lastet opp av bruker: $antallVedleggForsendelse")
        }

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

    fun lagDokumentListeForV2(soknadUnderArbeid: SoknadUnderArbeid): List<FilOpplasting> {
        val internalSoknad = soknadUnderArbeid.jsonInternalSoknad
        if (internalSoknad == null) {
            throw RuntimeException("Kan ikke sende forsendelse til FIKS fordi søknad mangler")
        } else if (internalSoknad.soknad == null) {
            throw RuntimeException("Kan ikke sende søknad fordi søknaden mangler")
        }

        // todo: lag dokumentListe med metadata om mellomlagrede vedlegg
        val dokumenterForVedlegg = emptyArray<FilOpplasting>() // lagDokumentListeForMellomlagredeVedlegg(soknadUnderArbeid.behandlingsId)

        return listOf(
            lagDokumentForSaksbehandlerPdf(internalSoknad),
            lagDokumentForJuridiskPdf(internalSoknad),
            lagDokumentForBrukerkvitteringPdf(),
            // *dokumenterForVedlegg.toTypedArray()
        ).also {
            log.info("Antall vedlegg: ${it.size}. Antall vedlegg lastet opp av bruker: ${dokumenterForVedlegg.size}")
        }
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
        val mimetype = getMimeType(bytes)
        return opprettFilOpplastingFraByteArray(opplastetVedlegg.filnavn, mimetype, bytes)
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
