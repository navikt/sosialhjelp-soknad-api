package no.nav.sosialhjelp.soknad.innsending.digisosapi

import com.fasterxml.jackson.core.JsonProcessingException
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator.ensureValidSoknad
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator.ensureValidVedlegg
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sosialhjelp.metrics.Event
import no.nav.sosialhjelp.metrics.MetricsFactory
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata.VedleggMetadata
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata.VedleggMetadataListe
import no.nav.sosialhjelp.soknad.business.pdfmedpdfbox.SosialhjelpPdfGenerator
import no.nav.sosialhjelp.soknad.common.filedetection.FileDetectionUtils.getMimeType
import no.nav.sosialhjelp.soknad.common.filedetection.MimeTypes.APPLICATION_PDF
import no.nav.sosialhjelp.soknad.common.filedetection.MimeTypes.TEXT_X_MATLAB
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.domain.OpplastetVedlegg
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.domain.Vedleggstatus
import no.nav.sosialhjelp.soknad.innsending.HenvendelseService
import no.nav.sosialhjelp.soknad.innsending.InnsendingService
import no.nav.sosialhjelp.soknad.innsending.JsonVedleggUtils.getVedleggFromInternalSoknad
import no.nav.sosialhjelp.soknad.innsending.SenderUtils.createPrefixedBehandlingsIdInNonProd
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilMetadata
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilOpplasting
import no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid.SoknadUnderArbeidService
import no.nav.sosialhjelp.soknad.metrics.MetricsUtils.navKontorTilInfluxNavn
import no.nav.sosialhjelp.soknad.metrics.SoknadMetricsService
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream

class DigisosApiService(
    private val digisosApiClient: DigisosApiClient,
    private val sosialhjelpPdfGenerator: SosialhjelpPdfGenerator,
    private val innsendingService: InnsendingService,
    private val henvendelseService: HenvendelseService,
    private val soknadUnderArbeidService: SoknadUnderArbeidService,
    private val soknadMetricsService: SoknadMetricsService,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository
) {
    private val objectMapper = JsonSosialhjelpObjectMapper.createObjectMapper()

    fun lagDokumentListe(soknadUnderArbeid: SoknadUnderArbeid): List<FilOpplasting> {
        val internalSoknad = soknadUnderArbeid.jsonInternalSoknad
        if (internalSoknad == null) {
            throw RuntimeException("Kan ikke sende forsendelse til FIKS fordi søknad mangler")
        } else if (!soknadUnderArbeid.erEttersendelse() && internalSoknad.soknad == null) {
            throw RuntimeException("Kan ikke sende søknad fordi søknaden mangler")
        } else if (soknadUnderArbeid.erEttersendelse() && internalSoknad.vedlegg == null) {
            throw RuntimeException("Kan ikke sende ettersendelse fordi vedlegg mangler")
        }
        val antallVedleggForsendelse: Int

        val filOpplastinger = mutableListOf<FilOpplasting>()

        if (soknadUnderArbeid.erEttersendelse()) {
            filOpplastinger.add(lagDokumentForEttersendelsePdf(internalSoknad, soknadUnderArbeid.eier))
            filOpplastinger.add(lagDokumentForBrukerkvitteringPdf())
            val dokumenterForVedlegg = lagDokumentListeForVedlegg(soknadUnderArbeid)
            antallVedleggForsendelse = dokumenterForVedlegg.size
            filOpplastinger.addAll(dokumenterForVedlegg)
        } else {
            filOpplastinger.add(lagDokumentForSaksbehandlerPdf(soknadUnderArbeid))
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
                log.warn("Ulikt antall vedlegg i vedlegg.json og forsendelse til Fiks. vedlegg.json: $antallBrukerOpplastedeVedlegg, forsendelse til Fiks: $antallVedleggForsendelse. Er ettersendelse: ${soknadUnderArbeid.erEttersendelse()}")
            }
        } catch (e: RuntimeException) {
            log.debug("Ignored exception")
        }
        return filOpplastinger
    }

    private fun sendOgKrypter(
        soknadJson: String,
        tilleggsinformasjonJson: String,
        vedleggJson: String,
        filOpplastinger: List<FilOpplasting>,
        kommunenr: String,
        navEnhetsnavn: String,
        behandlingsId: String,
        token: String?
    ): String {
        val event = lagForsoktSendtDigisosApiEvent(navEnhetsnavn)

        return try {
            digisosApiClient.krypterOgLastOppFiler(soknadJson, tilleggsinformasjonJson, vedleggJson, filOpplastinger, kommunenr, behandlingsId, token)
        } catch (e: Exception) {
            event.setFailed()
            throw e
        } finally {
            event.report()
        }
    }

    private fun lagForsoktSendtDigisosApiEvent(navEnhetsnavn: String): Event {
        val event = MetricsFactory.createEvent("fiks.digisosapi.sendt")
        event.addTagToReport("mottaker", navKontorTilInfluxNavn(navEnhetsnavn))
        return event
    }

    private fun lagDokumentForSaksbehandlerPdf(soknadUnderArbeid: SoknadUnderArbeid): FilOpplasting {
        val filnavn = "Soknad.pdf"
        val soknadPdf = sosialhjelpPdfGenerator.generate(soknadUnderArbeid.jsonInternalSoknad, false)
        return opprettFilOpplastingFraByteArray(filnavn, APPLICATION_PDF, soknadPdf)
    }

    private fun lagDokumentListeForVedlegg(soknadUnderArbeid: SoknadUnderArbeid): List<FilOpplasting> {
        val opplastedeVedlegg = innsendingService.hentAlleOpplastedeVedleggForSoknad(soknadUnderArbeid)
        return opplastedeVedlegg.map { opprettDokumentForVedlegg(it) }
    }

    private fun lagDokumentForEttersendelsePdf(internalSoknad: JsonInternalSoknad, eier: String): FilOpplasting {
        val filnavn = "ettersendelse.pdf"
        val pdf = sosialhjelpPdfGenerator.generateEttersendelsePdf(internalSoknad, eier)
        return opprettFilOpplastingFraByteArray(filnavn, APPLICATION_PDF, pdf)
    }

    private fun lagDokumentForBrukerkvitteringPdf(): FilOpplasting {
        val filnavn = "Brukerkvittering.pdf"
        val pdf = sosialhjelpPdfGenerator.generateBrukerkvitteringPdf()
        return opprettFilOpplastingFraByteArray(filnavn, APPLICATION_PDF, pdf)
    }

    private fun lagDokumentForJuridiskPdf(internalSoknad: JsonInternalSoknad): FilOpplasting {
        val filnavn = "Soknad-juridisk.pdf"
        val pdf = sosialhjelpPdfGenerator.generate(internalSoknad, true)
        return opprettFilOpplastingFraByteArray(filnavn, APPLICATION_PDF, pdf)
    }

    private fun opprettDokumentForVedlegg(opplastetVedlegg: OpplastetVedlegg): FilOpplasting {
        val pdf = opplastetVedlegg.data
        val detectedMimeType = getMimeType(opplastetVedlegg.data)
        val mimetype = if (detectedMimeType.equals(TEXT_X_MATLAB, ignoreCase = true)) APPLICATION_PDF else detectedMimeType
        return FilOpplasting(
            metadata = FilMetadata(
                filnavn = opplastetVedlegg.filnavn,
                mimetype = mimetype,
                storrelse = pdf.size.toLong()
            ),
            data = ByteArrayInputStream(pdf)
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

    fun sendSoknad(soknadUnderArbeid: SoknadUnderArbeid, token: String?, kommunenummer: String): String {
        var behandlingsId = soknadUnderArbeid.behandlingsId
        soknadUnderArbeidService.settInnsendingstidspunktPaSoknad(soknadUnderArbeid)
        log.info("Starter innsending av søknad med behandlingsId {}, skal sendes til DigisosApi", behandlingsId)
        val vedlegg = convertToVedleggMetadataListe(soknadUnderArbeid)
        henvendelseService.oppdaterMetadataVedAvslutningAvSoknad(behandlingsId, vedlegg, soknadUnderArbeid, true)
        val filOpplastinger = lagDokumentListe(soknadUnderArbeid)
        log.info("Laster opp {}", filOpplastinger.size)
        val soknadJson = getSoknadJson(soknadUnderArbeid)
        val tilleggsinformasjonJson = getTilleggsinformasjonJson(soknadUnderArbeid.jsonInternalSoknad.soknad)
        val vedleggJson = getVedleggJson(soknadUnderArbeid)

        behandlingsId = createPrefixedBehandlingsIdInNonProd(behandlingsId)
        val enhetsnummer = soknadUnderArbeid.jsonInternalSoknad.soknad.mottaker.enhetsnummer
        val navEnhetsnavn = soknadUnderArbeid.jsonInternalSoknad.soknad.mottaker.navEnhetsnavn
        log.info("Starter kryptering av filer for $behandlingsId, skal sende til kommune $kommunenummer med enhetsnummer $enhetsnummer og navenhetsnavn $navEnhetsnavn")
        val digisosId = sendOgKrypter(
            soknadJson,
            tilleggsinformasjonJson,
            vedleggJson,
            filOpplastinger,
            kommunenummer,
            navEnhetsnavn,
            behandlingsId,
            token
        )

        slettSoknadUnderArbeidEtterSendingTilFiks(soknadUnderArbeid)

        soknadMetricsService.reportSendSoknadMetrics(getUserIdFromToken(), soknadUnderArbeid, vedlegg.vedleggListe)
        return digisosId
    }

    fun getSoknadJson(soknadUnderArbeid: SoknadUnderArbeid): String {
        return try {
            val soknadJson = objectMapper.writeValueAsString(soknadUnderArbeid.jsonInternalSoknad.soknad)
            ensureValidSoknad(soknadJson)
            soknadJson
        } catch (e: JsonProcessingException) {
            throw IllegalArgumentException("Klarer ikke serialisere sonadJson", e)
        }
    }

    fun getTilleggsinformasjonJson(soknad: JsonSoknad?): String {
        if (soknad == null || soknad.mottaker == null) {
            log.error("Soknad eller soknadsmottaker er null ved sending av søknad. Dette skal ikke skje.")
            throw IllegalStateException("Soknad eller soknadsmottaker er null ved sending av søknad.")
        }

        val enhetsnummer = soknad.mottaker.enhetsnummer
        if (enhetsnummer == null) {
            log.error("Enhetsnummer er null ved sending av søknad. Den blir lagt til i tilleggsinformasjon-filen med <null> som verdi.")
        }
        val tilleggsinformasjonJson = JsonTilleggsinformasjon(enhetsnummer)

        return try {
            objectMapper.writeValueAsString(tilleggsinformasjonJson)
        } catch (e: JsonProcessingException) {
            throw IllegalArgumentException("Klarer ikke serialisere tilleggsinformasjonJson", e)
        }
    }

    fun getVedleggJson(soknadUnderArbeid: SoknadUnderArbeid): String {
        return try {
            val vedleggJson = objectMapper.writeValueAsString(soknadUnderArbeid.jsonInternalSoknad.vedlegg)
            ensureValidVedlegg(vedleggJson)
            vedleggJson
        } catch (e: JsonProcessingException) {
            throw IllegalArgumentException("Klarer ikke serialisere vedleggJson", e)
        }
    }

    private fun convertToVedleggMetadataListe(soknadUnderArbeid: SoknadUnderArbeid): VedleggMetadataListe {
        val vedleggMetadataListe = VedleggMetadataListe()
        vedleggMetadataListe.vedleggListe = getVedleggFromInternalSoknad(soknadUnderArbeid)
            .map {
                val vedleggMetadata = VedleggMetadata()
                vedleggMetadata.skjema = it.type
                vedleggMetadata.tillegg = it.tilleggsinfo
                vedleggMetadata.filnavn = it.type
                vedleggMetadata.status = Vedleggstatus.valueOf(it.status)
                vedleggMetadata
            }
        return vedleggMetadataListe
    }

    private fun slettSoknadUnderArbeidEtterSendingTilFiks(soknadUnderArbeid: SoknadUnderArbeid) {
        log.info("Sletter SoknadUnderArbeid, behandlingsid {}", soknadUnderArbeid.behandlingsId)
        soknadUnderArbeidRepository.slettSoknad(soknadUnderArbeid, soknadUnderArbeid.eier)
    }

    companion object {
        private val log = LoggerFactory.getLogger(DigisosApiService::class.java)
    }
}
