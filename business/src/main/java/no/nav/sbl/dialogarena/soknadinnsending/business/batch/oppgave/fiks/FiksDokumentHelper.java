package no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.fiks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import no.ks.svarut.servicesv9.Dokument;
import no.nav.sbl.dialogarena.detect.Detect;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fiks.DokumentKrypterer;
import no.nav.sbl.soknadsosialhjelp.json.AdresseMixIn;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon;
import no.nav.sbl.sosialhjelp.InnsendingService;
import no.nav.sbl.sosialhjelp.domain.OpplastetVedlegg;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.pdf.PDFService;
import no.nav.sbl.sosialhjelp.pdfmedpdfbox.SosialhjelpPdfGenerator;
import org.apache.cxf.attachment.ByteDataSource;
import org.slf4j.Logger;

import javax.activation.DataHandler;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.OkonomiMapper.setBekreftelse;
import static no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator.ensureValidSoknad;
import static no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator.ensureValidVedlegg;
import static org.slf4j.LoggerFactory.getLogger;

public class FiksDokumentHelper {
    private static final Logger logger = getLogger(FiksDokumentHelper.class);
    private final ObjectMapper mapper;
    private final ObjectWriter writer;

    private final boolean skalKryptere;
    private DokumentKrypterer dokumentKrypterer;
    private InnsendingService innsendingService;
    private PDFService pdfService;
    private SosialhjelpPdfGenerator sosialhjelpPdfGenerator;

    public FiksDokumentHelper(boolean skalKryptere, DokumentKrypterer dokumentKrypterer, InnsendingService innsendingService, PDFService pdfService, SosialhjelpPdfGenerator sosialhjelpPdfGenerator) {
        this.skalKryptere = skalKryptere;
        this.dokumentKrypterer = dokumentKrypterer;
        this.innsendingService = innsendingService;
        this.pdfService = pdfService;
        this.sosialhjelpPdfGenerator = sosialhjelpPdfGenerator;

        mapper = new ObjectMapper();
        mapper.addMixIn(JsonAdresse.class, AdresseMixIn.class);
        writer = mapper.writerWithDefaultPrettyPrinter();
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

    Dokument lagDokumentForSaksbehandlerPdf(SoknadUnderArbeid soknadUnderArbeid) {
        final String filnavn = "Soknad.pdf";
        final String mimetype = "application/pdf";
        if(soknadUnderArbeid.getSelvstendigNaringsdrivende()) {
            JsonOkonomiopplysninger opplysninger = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger();
            setBekreftelse(opplysninger, "selvstendignaringsdriveende", true, "selvstendignaringsdriveende");
        }
        byte[] soknadPdf = pdfService.genererSaksbehandlerPdf(soknadUnderArbeid.getJsonInternalSoknad(), "/");
        if(soknadUnderArbeid.getSelvstendigNaringsdrivende()) {
            JsonOkonomiopplysninger opplysninger = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger();
            opplysninger.getBekreftelse().removeIf(bekreftelse -> bekreftelse.getType().equalsIgnoreCase("selvstendignaringsdriveende"));
        }
        try {
            sosialhjelpPdfGenerator.generate(soknadUnderArbeid, false);
        } catch (Exception e) {
            logger.warn("Kunne ikke generere Soknad.pdf", e);
        }

        ByteDataSource dataSource = krypterOgOpprettByteDatasource(filnavn, soknadPdf);
        return new Dokument()
                .withFilnavn(filnavn)
                .withMimetype(mimetype)
                .withEkskluderesFraPrint(false)
                .withData(new DataHandler(dataSource));
    }

    Dokument lagDokumentForJuridiskPdf(SoknadUnderArbeid soknadUnderArbeid) {
        final String filnavn = "Soknad-juridisk.pdf";
        final String mimetype = "application/pdf";
        if(soknadUnderArbeid.getSelvstendigNaringsdrivende()) {
            JsonOkonomiopplysninger opplysninger = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger();
            setBekreftelse(opplysninger, "selvstendignaringsdriveende", true, "selvstendignaringsdriveende");
        }
        byte[] juridiskPdf = pdfService.genererJuridiskPdf(soknadUnderArbeid.getJsonInternalSoknad(), "/");
        if(soknadUnderArbeid.getSelvstendigNaringsdrivende()) {
            JsonOkonomiopplysninger opplysninger = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger();
            opplysninger.getBekreftelse().removeIf(bekreftelse -> bekreftelse.getType().equalsIgnoreCase("selvstendignaringsdriveende"));
        }
        try {
            sosialhjelpPdfGenerator.generate(soknadUnderArbeid, true);
        } catch (Exception e) {
            logger.warn("Kunne ikke generere Soknad-juridisk.pdf", e);
        }

        ByteDataSource dataSource = krypterOgOpprettByteDatasource(filnavn, juridiskPdf);
        return new Dokument()
                .withFilnavn(filnavn)
                .withMimetype(mimetype)
                .withEkskluderesFraPrint(false)
                .withData(new DataHandler(dataSource));
    }

    Dokument lagDokumentForBrukerkvitteringPdf(JsonInternalSoknad internalSoknad, boolean erEttersendelse, String eier) {
        final String filnavn = "Brukerkvittering.pdf";
        final String mimetype = "application/pdf";
        byte[] juridiskPdf = pdfService.genererBrukerkvitteringPdf(internalSoknad, "/", erEttersendelse, eier);

        ByteDataSource dataSource = krypterOgOpprettByteDatasource(filnavn, juridiskPdf);
        return new Dokument()
                .withFilnavn(filnavn)
                .withMimetype(mimetype)
                .withEkskluderesFraPrint(true)
                .withData(new DataHandler(dataSource));
    }

    Dokument lagDokumentForEttersendelsePdf(JsonInternalSoknad internalSoknad, String eier) {
        final String filnavn = "ettersendelse.pdf";
        final String mimetype = "application/pdf";
        byte[] ettersendelsePdf = pdfService.genererEttersendelsePdf(internalSoknad, "/", eier);

        ByteDataSource dataSource = krypterOgOpprettByteDatasource(filnavn, ettersendelsePdf);
        return new Dokument()
                .withFilnavn(filnavn)
                .withMimetype(mimetype)
                .withEkskluderesFraPrint(false)
                .withData(new DataHandler(dataSource));
    }

    List<Dokument> lagDokumentListeForVedlegg(SoknadUnderArbeid soknadUnderArbeid) {
        final List<OpplastetVedlegg> opplastedeVedlegg = innsendingService.hentAlleOpplastedeVedleggForSoknad(soknadUnderArbeid);
        return opplastedeVedlegg.stream()
                .map(this::opprettDokumentForVedlegg)
                .collect(Collectors.toList());
    }

    Dokument opprettDokumentForVedlegg(OpplastetVedlegg opplastetVedlegg) {
        final String filnavn = opplastetVedlegg.getFilnavn();
        return new Dokument()
                .withFilnavn(filnavn)
                .withMimetype(Detect.CONTENT_TYPE.transform(opplastetVedlegg.getData()))
                .withEkskluderesFraPrint(true)
                .withData(new DataHandler(krypterOgOpprettByteDatasource(filnavn, opplastetVedlegg.getData())));
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
