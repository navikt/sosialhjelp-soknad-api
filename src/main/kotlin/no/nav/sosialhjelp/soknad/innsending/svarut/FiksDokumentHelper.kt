package no.nav.sosialhjelp.soknad.innsending.svarut

import com.fasterxml.jackson.core.JsonProcessingException
import no.ks.fiks.svarut.klient.model.Dokument
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator.ensureValidSoknad
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator.ensureValidVedlegg
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedlegg
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.innsending.InnsendingService
import no.nav.sosialhjelp.soknad.pdf.SosialhjelpPdfGenerator
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.FileDetectionUtils.getMimeType
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.MimeTypes.APPLICATION_JSON
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.MimeTypes.APPLICATION_PDF
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets

class FiksDokumentHelper(
    private val skalKryptere: Boolean,
    private val dokumentKrypterer: DokumentKrypterer,
    private val innsendingService: InnsendingService,
    private val sosialhjelpPdfGenerator: SosialhjelpPdfGenerator
) {
    private val mapper = JsonSosialhjelpObjectMapper.createObjectMapper()

    fun lagDokumentForSoknadJson(internalSoknad: JsonInternalSoknad, map: MutableMap<String, InputStream>): Dokument {
        val filnavn = "soknad.json"
        val soknadJson = mapJsonSoknadTilFil(internalSoknad.soknad)

        val byteArrayInputStream = krypterOgOpprettByteArrayInputStream(soknadJson)
        map[filnavn] = byteArrayInputStream

        return Dokument()
            .withFilnavn(filnavn)
            .withMimeType(APPLICATION_JSON)
            .withEkskluderesFraUtskrift(true)
    }

    fun lagDokumentForVedleggJson(internalSoknad: JsonInternalSoknad, map: MutableMap<String, InputStream>): Dokument {
        val filnavn = "vedlegg.json"
        val vedleggJson = mapJsonVedleggTilFil(internalSoknad.vedlegg)

        val byteArrayInputStream = krypterOgOpprettByteArrayInputStream(vedleggJson)
        map[filnavn] = byteArrayInputStream

        return Dokument()
            .withFilnavn(filnavn)
            .withMimeType(APPLICATION_JSON)
            .withEkskluderesFraUtskrift(true)
    }

    fun lagDokumentForSaksbehandlerPdf(
        internalSoknad: JsonInternalSoknad,
        map: MutableMap<String, InputStream>
    ): Dokument {
        val filnavn = "Soknad.pdf"
        val soknadPdf = sosialhjelpPdfGenerator.generate(internalSoknad, false)
        return genererDokumentFraByteArray(filnavn, APPLICATION_PDF, soknadPdf, false, map)
    }

    fun lagDokumentForJuridiskPdf(internalSoknad: JsonInternalSoknad, map: MutableMap<String, InputStream>): Dokument {
        val filnavn = "Soknad-juridisk.pdf"
        val juridiskPdf = sosialhjelpPdfGenerator.generate(internalSoknad, true)
        return genererDokumentFraByteArray(filnavn, APPLICATION_PDF, juridiskPdf, false, map)
    }

    fun lagDokumentForBrukerkvitteringPdf(map: MutableMap<String, InputStream>): Dokument {
        val filnavn = "Brukerkvittering.pdf"
        val pdf = sosialhjelpPdfGenerator.generateBrukerkvitteringPdf()
        return genererDokumentFraByteArray(filnavn, APPLICATION_PDF, pdf, true, map)
    }

    fun lagDokumentForEttersendelsePdf(
        internalSoknad: JsonInternalSoknad,
        eier: String,
        map: MutableMap<String, InputStream>
    ): Dokument {
        val filnavn = "ettersendelse.pdf"
        val pdf = sosialhjelpPdfGenerator.generateEttersendelsePdf(internalSoknad, eier)
        return genererDokumentFraByteArray(filnavn, APPLICATION_PDF, pdf, false, map)
    }

    private fun genererDokumentFraByteArray(
        filnavn: String,
        mimetype: String,
        bytes: ByteArray,
        eksluderesFraUtskrift: Boolean,
        map: MutableMap<String, InputStream>
    ): Dokument {
        val byteArrayInputStream = krypterOgOpprettByteArrayInputStream(bytes)
        map[filnavn] = byteArrayInputStream
        return Dokument()
            .withFilnavn(filnavn)
            .withMimeType(mimetype)
            .withEkskluderesFraUtskrift(eksluderesFraUtskrift)
    }

    fun lagDokumentListeForVedlegg(
        soknadUnderArbeid: SoknadUnderArbeid,
        map: MutableMap<String, InputStream>
    ): List<Dokument> {
        val opplastedeVedlegg = innsendingService.hentAlleOpplastedeVedleggForSoknad(soknadUnderArbeid)
        return opplastedeVedlegg.map { opprettDokumentForVedlegg(it, map) }
    }

    fun opprettDokumentForVedlegg(opplastetVedlegg: OpplastetVedlegg, map: MutableMap<String, InputStream>): Dokument {
        val filnavn = opplastetVedlegg.filnavn

        val byteArrayInputStream = krypterOgOpprettByteArrayInputStream(opplastetVedlegg.data)
        map[filnavn] = byteArrayInputStream

        val mimeType = getMimeType(opplastetVedlegg.data)
        return Dokument()
            .withFilnavn(filnavn)
            .withMimeType(mimeType)
            .withEkskluderesFraUtskrift(true)
    }

    fun krypterOgOpprettByteArrayInputStream(fil: ByteArray?): ByteArrayInputStream {
        var filForKryptering = fil
        if (skalKryptere) {
            filForKryptering = dokumentKrypterer.krypterData(filForKryptering)
        }
        return ByteArrayInputStream(filForKryptering)
    }

    private fun mapJsonSoknadTilFil(jsonSoknad: JsonSoknad): ByteArray {
        return try {
            val soknad = mapper.writeValueAsString(jsonSoknad)
            ensureValidSoknad(soknad)
            soknad.toByteArray(StandardCharsets.UTF_8)
        } catch (e: JsonProcessingException) {
            logger.error("Kunne ikke konvertere soknad.json til tekststreng", e)
            throw RuntimeException(e)
        }
    }

    private fun mapJsonVedleggTilFil(jsonVedleggSpesifikasjon: JsonVedleggSpesifikasjon): ByteArray {
        return try {
            val jsonVedlegg = mapper.writeValueAsString(jsonVedleggSpesifikasjon)
            ensureValidVedlegg(jsonVedlegg)
            jsonVedlegg.toByteArray(StandardCharsets.UTF_8)
        } catch (e: JsonProcessingException) {
            logger.error("Kunne ikke konvertere vedlegg.json til tekststreng", e)
            throw RuntimeException(e)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(FiksDokumentHelper::class.java)
    }
}
