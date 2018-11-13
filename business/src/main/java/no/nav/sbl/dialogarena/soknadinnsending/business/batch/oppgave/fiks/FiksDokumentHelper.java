package no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.fiks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import no.ks.svarut.servicesv9.Dokument;
import no.nav.sbl.dialogarena.detect.Detect;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fiks.DokumentKrypterer;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.vedlegg.*;
import no.nav.sbl.sosialhjelp.InnsendingService;
import no.nav.sbl.sosialhjelp.domain.*;
import org.apache.cxf.attachment.ByteDataSource;
import org.slf4j.Logger;

import javax.activation.DataHandler;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator.ensureValidSoknad;
import static no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator.ensureValidVedlegg;
import static org.slf4j.LoggerFactory.getLogger;

public class FiksDokumentHelper {
    private static final Logger logger = getLogger(FiksDokumentHelper.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private final ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();

    private final boolean skalKryptere;
    private DokumentKrypterer dokumentKrypterer;
    private InnsendingService innsendingService;

    public FiksDokumentHelper(boolean skalKryptere, DokumentKrypterer dokumentKrypterer, InnsendingService innsendingService) {
        this.skalKryptere = skalKryptere;
        this.dokumentKrypterer = dokumentKrypterer;
        this.innsendingService = innsendingService;
    }

    Dokument lagDokumentForSoknadJson(JsonInternalSoknad internalSoknad) {
        final String filnavn = "soknad.json";
        final String mimetype = "application/json";
        byte[] soknadJson = mapJsonSoknadTilFil(internalSoknad.getSoknad());

        ByteDataSource dataSource = krypterOgOpprettByteDatasource(filnavn, soknadJson);
        return new Dokument()
                .withFilnavn(filnavn)
                .withMimetype(mimetype)
                .withEkskluderesFraPrint(true)
                .withData(new DataHandler(dataSource));
    }

    Dokument lagDokumentForVedleggJson(JsonInternalSoknad internalSoknad) {
        final String filnavn = "vedlegg.json";
        final String mimetype = "application/json";
        byte[] vedleggJson = mapJsonVedleggTilFil(internalSoknad.getVedlegg());

        ByteDataSource dataSource = krypterOgOpprettByteDatasource(filnavn, vedleggJson);
        return new Dokument()
                .withFilnavn(filnavn)
                .withMimetype(mimetype)
                .withEkskluderesFraPrint(true)
                .withData(new DataHandler(dataSource));
    }

    Dokument lagDokumentForPdf(JsonInternalSoknad internalSoknad) {
        final String filnavn = "Soknad.pdf";
        final String mimetype = "application/pdf";
        byte[] soknadPdf = null;//hent fra ny funksjonalitet

        ByteDataSource dataSource = krypterOgOpprettByteDatasource(filnavn, soknadPdf);
        return new Dokument()
                .withFilnavn(filnavn)
                .withMimetype(mimetype)
                .withEkskluderesFraPrint(false)
                .withData(new DataHandler(dataSource));
    }

    Dokument lagDokumentForJuridiskPdf(JsonInternalSoknad internalSoknad) {
        final String filnavn = "Soknad-juridisk.pdf";
        final String mimetype = "application/pdf";
        byte[] juridiskPdf = null;//hent fra ny funksjonalitet

        ByteDataSource dataSource = krypterOgOpprettByteDatasource(filnavn, juridiskPdf);
        return new Dokument()
                .withFilnavn(filnavn)
                .withMimetype(mimetype)
                .withEkskluderesFraPrint(false)
                .withData(new DataHandler(dataSource));
    }

    Dokument lagDokumentForEttersendelsePdf(JsonInternalSoknad internalSoknad) {
        final String filnavn = "ettersendelse.pdf";
        final String mimetype = "application/pdf";
        byte[] ettersendelsePdf = null;//hent fra ny funksjonalitet

        ByteDataSource dataSource = krypterOgOpprettByteDatasource(filnavn, ettersendelsePdf);
        return new Dokument()
                .withFilnavn(filnavn)
                .withMimetype(mimetype)
                .withEkskluderesFraPrint(false)
                .withData(new DataHandler(dataSource));
    }

    List<Dokument> lagDokumentListeForVedlegg(SoknadUnderArbeid soknadUnderArbeid, JsonInternalSoknad internalSoknad) {
        final List<OpplastetVedlegg> opplastedeVedlegg = innsendingService.hentAlleOpplastedeVedleggForSoknad(soknadUnderArbeid);
        final List<JsonVedlegg> opplastedeJsonVedlegg = hentJsonVedleggFraInternalSoknad(internalSoknad);

        List<Dokument> fiksDokumenter = new ArrayList<>();
        for (OpplastetVedlegg opplastetVedlegg : opplastedeVedlegg) {
            for (JsonVedlegg jsonVedlegg : opplastedeJsonVedlegg) {
                if (opplastetVedlegg.getVedleggType().equals(new VedleggType(jsonVedlegg.getType(), jsonVedlegg.getTilleggsinfo()))) {
                    for (JsonFiler jsonFil : jsonVedlegg.getFiler()) {
                        if (jsonFilOgOpplastetVedleggErDetSammeVedlegget(jsonFil, opplastetVedlegg)) {
                            fiksDokumenter.add(opprettDokumentForVedlegg(opplastetVedlegg, jsonFil));
                        }
                    }
                }
            }
        }
        return fiksDokumenter;
    }

    boolean jsonFilOgOpplastetVedleggErDetSammeVedlegget(JsonFiler jsonFil, OpplastetVedlegg opplastetVedlegg) {
        if (jsonFil != null && jsonFil.getSha512().equals(opplastetVedlegg.getSha512()) && jsonFil.getFilnavn().equals(opplastetVedlegg.getFilnavn())) {
            return true;
        }
        return false;
    }

    Dokument opprettDokumentForVedlegg(OpplastetVedlegg opplastetVedlegg, JsonFiler jsonFil) {
        String filnavn = jsonFil.getFilnavn();
        if (filnavn.equals("L7")) {
            filnavn = "Brukerkvittering.pdf";
        }
        return new Dokument()
                .withFilnavn(filnavn)
                .withMimetype(Detect.CONTENT_TYPE.transform(opplastetVedlegg.getData()))
                .withEkskluderesFraPrint(true)
                .withData(new DataHandler(krypterOgOpprettByteDatasource(filnavn, opplastetVedlegg.getData())));
    }

    private List<JsonVedlegg> hentJsonVedleggFraInternalSoknad(JsonInternalSoknad internalSoknad) {
        if (internalSoknad.getVedlegg().getVedlegg() != null && !internalSoknad.getVedlegg().getVedlegg().isEmpty()) {
            List<JsonVedlegg> alleJsonVedleggFraInternalSoknad = internalSoknad.getVedlegg().getVedlegg();
            return alleJsonVedleggFraInternalSoknad.stream()
                    .filter(jsonVedlegg -> Vedlegg.Status.LastetOpp.name().equalsIgnoreCase(jsonVedlegg.getStatus()))
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    ByteDataSource krypterOgOpprettByteDatasource(String filnavn, byte[] fil) {
        if (skalKryptere) {
            fil = dokumentKrypterer.krypterData(fil);
        }

        ByteDataSource dataSource = new ByteDataSource(fil);
        dataSource.setName(filnavn);
        dataSource.setContentType("application/octet-stream");
        return dataSource;
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
