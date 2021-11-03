package no.nav.sosialhjelp.soknad.business.batch.oppgave.fiks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import no.ks.fiks.svarut.klient.model.Dokument;
import no.nav.sbl.soknadsosialhjelp.json.AdresseMixIn;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon;
import no.nav.sosialhjelp.soknad.business.InnsendingService;
import no.nav.sosialhjelp.soknad.business.pdfmedpdfbox.SosialhjelpPdfGenerator;
import no.nav.sosialhjelp.soknad.business.util.FileDetectionUtils;
import no.nav.sosialhjelp.soknad.consumer.fiks.DokumentKrypterer;
import no.nav.sosialhjelp.soknad.domain.OpplastetVedlegg;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator.ensureValidSoknad;
import static no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator.ensureValidVedlegg;
import static org.slf4j.LoggerFactory.getLogger;

public class FiksDokumentHelper {
    private static final Logger logger = getLogger(FiksDokumentHelper.class);
    private final ObjectMapper mapper;
    private final ObjectWriter writer;

    private final boolean skalKryptere;
    private final DokumentKrypterer dokumentKrypterer;
    private final InnsendingService innsendingService;
    private final SosialhjelpPdfGenerator sosialhjelpPdfGenerator;

    public FiksDokumentHelper(boolean skalKryptere, DokumentKrypterer dokumentKrypterer, InnsendingService innsendingService, SosialhjelpPdfGenerator sosialhjelpPdfGenerator) {
        this.skalKryptere = skalKryptere;
        this.dokumentKrypterer = dokumentKrypterer;
        this.innsendingService = innsendingService;
        this.sosialhjelpPdfGenerator = sosialhjelpPdfGenerator;

        mapper = new ObjectMapper();
        mapper.addMixIn(JsonAdresse.class, AdresseMixIn.class);
        writer = mapper.writerWithDefaultPrettyPrinter();
    }

    Dokument lagDokumentForSoknadJson(JsonInternalSoknad internalSoknad, Map<String, InputStream> map) {
        final String filnavn = "soknad.json";
        final String mimetype = "application/json";
        byte[] soknadJson = mapJsonSoknadTilFil(internalSoknad.getSoknad());

        var byteArrayInputStream = krypterOgOpprettByteArrayInputStream(soknadJson);
        map.put(filnavn, byteArrayInputStream);

        return new Dokument()
                .withFilnavn(filnavn)
                .withMimeType(mimetype)
                .withEkskluderesFraUtskrift(true);
    }

    Dokument lagDokumentForVedleggJson(JsonInternalSoknad internalSoknad, Map<String, InputStream> map) {
        final String filnavn = "vedlegg.json";
        final String mimetype = "application/json";
        byte[] vedleggJson = mapJsonVedleggTilFil(internalSoknad.getVedlegg());

        var byteArrayInputStream = krypterOgOpprettByteArrayInputStream(vedleggJson);
        map.put(filnavn, byteArrayInputStream);
        return new Dokument()
                .withFilnavn(filnavn)
                .withMimeType(mimetype)
                .withEkskluderesFraUtskrift(true);
    }

    Dokument lagDokumentForSaksbehandlerPdf(JsonInternalSoknad internalSoknad, Map<String, InputStream> map) {
        final String filnavn = "Soknad.pdf";
        final String mimetype = "application/pdf";

        byte[] soknadPdf = sosialhjelpPdfGenerator.generate(internalSoknad, false);
        return genererDokumentFraByteArray(filnavn, mimetype, soknadPdf, false, map);
    }

    Dokument lagDokumentForJuridiskPdf(JsonInternalSoknad internalSoknad, Map<String, InputStream> map) {
        final String filnavn = "Soknad-juridisk.pdf";
        final String mimetype = "application/pdf";

        byte[] juridiskPdf = sosialhjelpPdfGenerator.generate(internalSoknad, true);
        return genererDokumentFraByteArray(filnavn, mimetype, juridiskPdf, false, map);
    }

    Dokument lagDokumentForBrukerkvitteringPdf(Map<String, InputStream> map) {
        final String filnavn = "Brukerkvittering.pdf";
        final String mimetype = "application/pdf";
        byte[] pdf = sosialhjelpPdfGenerator.generateBrukerkvitteringPdf();

        return genererDokumentFraByteArray(filnavn, mimetype, pdf, true, map);
    }

    Dokument lagDokumentForEttersendelsePdf(JsonInternalSoknad internalSoknad, String eier, Map<String, InputStream> map) {
        final String filnavn = "ettersendelse.pdf";
        final String mimetype = "application/pdf";

        byte[] pdf = sosialhjelpPdfGenerator.generateEttersendelsePdf(internalSoknad, eier);
        return genererDokumentFraByteArray(filnavn, mimetype, pdf, false, map);
    }

    private Dokument genererDokumentFraByteArray(String filnavn, String mimetype, byte[] bytes, boolean eksluderesFraUtskrift, Map<String, InputStream> map) {
        var byteArrayInputStream = krypterOgOpprettByteArrayInputStream(bytes);

        map.put(filnavn, byteArrayInputStream);
        return new Dokument()
                .withFilnavn(filnavn)
                .withMimeType(mimetype)
                .withEkskluderesFraUtskrift(eksluderesFraUtskrift);
    }

    List<Dokument> lagDokumentListeForVedlegg(SoknadUnderArbeid soknadUnderArbeid, Map<String, InputStream> map) {
        final List<OpplastetVedlegg> opplastedeVedlegg = innsendingService.hentAlleOpplastedeVedleggForSoknad(soknadUnderArbeid);
        return opplastedeVedlegg.stream()
                .map((OpplastetVedlegg opplastetVedlegg) -> opprettDokumentForVedlegg(opplastetVedlegg, map))
                .collect(Collectors.toList());
    }

    Dokument opprettDokumentForVedlegg(OpplastetVedlegg opplastetVedlegg, Map<String, InputStream> map) {
        final String filnavn = opplastetVedlegg.getFilnavn();

        var byteArrayInputStream = krypterOgOpprettByteArrayInputStream(opplastetVedlegg.getData());
        map.put(filnavn, byteArrayInputStream);

        return new Dokument()
                .withFilnavn(filnavn)
                .withMimeType(FileDetectionUtils.getMimeTypeForSending(opplastetVedlegg.getData()))
                .withEkskluderesFraUtskrift(true);
    }

    ByteArrayInputStream krypterOgOpprettByteArrayInputStream(byte[] fil) {
        if (skalKryptere) {
            fil = dokumentKrypterer.krypterData(fil);
        }
        return new ByteArrayInputStream(fil);
    }

    private byte[] mapJsonSoknadTilFil(JsonSoknad jsonSoknad) {
        try {
            final String soknad = writer.writeValueAsString(jsonSoknad);
            ensureValidSoknad(soknad);
            return soknad.getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            logger.error("Kunne ikke konvertere soknad.json til tekststreng", e);
            throw new RuntimeException(e);
        }
    }

    private byte[] mapJsonVedleggTilFil(JsonVedleggSpesifikasjon jsonVedleggSpesifikasjon) {
        try {
            final String jsonVedlegg = writer.writeValueAsString(jsonVedleggSpesifikasjon);
            ensureValidVedlegg(jsonVedlegg);
            return jsonVedlegg.getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            logger.error("Kunne ikke konvertere vedlegg.json til tekststreng", e);
            throw new RuntimeException(e);
        }
    }

}
