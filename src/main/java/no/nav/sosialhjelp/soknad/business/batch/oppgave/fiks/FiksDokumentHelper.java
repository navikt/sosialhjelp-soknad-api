package no.nav.sosialhjelp.soknad.business.batch.oppgave.fiks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import no.finn.unleash.Unleash;
import no.ks.svarut.servicesv9.Dokument;
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
import org.apache.cxf.attachment.ByteDataSource;
import org.slf4j.Logger;

import javax.activation.DataHandler;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator.ensureValidSoknad;
import static no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator.ensureValidVedlegg;
import static no.nav.sosialhjelp.soknad.business.util.JsonVedleggUtils.FEATURE_UTVIDE_VEDLEGGJSON;
import static no.nav.sosialhjelp.soknad.business.util.JsonVedleggUtils.addHendelseTypeAndHendelseReferanse;
import static org.slf4j.LoggerFactory.getLogger;

public class FiksDokumentHelper {
    private static final Logger logger = getLogger(FiksDokumentHelper.class);
    private final ObjectMapper mapper;
    private final ObjectWriter writer;

    private final boolean skalKryptere;
    private DokumentKrypterer dokumentKrypterer;
    private InnsendingService innsendingService;
    private SosialhjelpPdfGenerator sosialhjelpPdfGenerator;
    private final Unleash unleash;

    public FiksDokumentHelper(boolean skalKryptere, DokumentKrypterer dokumentKrypterer, InnsendingService innsendingService, SosialhjelpPdfGenerator sosialhjelpPdfGenerator, Unleash unleash) {
        this.skalKryptere = skalKryptere;
        this.dokumentKrypterer = dokumentKrypterer;
        this.innsendingService = innsendingService;
        this.sosialhjelpPdfGenerator = sosialhjelpPdfGenerator;
        this.unleash = unleash;

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

    Dokument lagDokumentForVedleggJson(JsonInternalSoknad internalSoknad, boolean isSoknad) {
        final String filnavn = "vedlegg.json";
        final String mimetype = "application/json";

        JsonVedleggSpesifikasjon jsonVedleggSpesifikasjon = internalSoknad.getVedlegg();
        addHendelseTypeAndHendelseReferanse(jsonVedleggSpesifikasjon, isSoknad, unleash.isEnabled(FEATURE_UTVIDE_VEDLEGGJSON, false));
        byte[] vedleggJson = mapJsonVedleggTilFil(jsonVedleggSpesifikasjon);

        ByteDataSource dataSource = krypterOgOpprettByteDatasource(filnavn, vedleggJson);
        return new Dokument()
                .withFilnavn(filnavn)
                .withMimetype(mimetype)
                .withEkskluderesFraPrint(true)
                .withData(new DataHandler(dataSource));
    }

    Dokument lagDokumentForSaksbehandlerPdf(JsonInternalSoknad internalSoknad) {
        final String filnavn = "Soknad.pdf";
        final String mimetype = "application/pdf";

        byte[] soknadPdf = sosialhjelpPdfGenerator.generate(internalSoknad, false);
        return genererDokumentFraByteArray(filnavn, mimetype, soknadPdf, false);
    }

    Dokument lagDokumentForJuridiskPdf(JsonInternalSoknad internalSoknad) {
        final String filnavn = "Soknad-juridisk.pdf";
        final String mimetype = "application/pdf";

        byte[] juridiskPdf = sosialhjelpPdfGenerator.generate(internalSoknad, true);
        return genererDokumentFraByteArray(filnavn, mimetype, juridiskPdf, false);
    }

    Dokument lagDokumentForBrukerkvitteringPdf() {
        final String filnavn = "Brukerkvittering.pdf";
        final String mimetype = "application/pdf";
        byte[] pdf = sosialhjelpPdfGenerator.generateBrukerkvitteringPdf();

        return genererDokumentFraByteArray(filnavn, mimetype, pdf, true);
    }

    Dokument lagDokumentForEttersendelsePdf(JsonInternalSoknad internalSoknad, String eier) {
        final String filnavn = "ettersendelse.pdf";
        final String mimetype = "application/pdf";

        byte[] pdf = sosialhjelpPdfGenerator.generateEttersendelsePdf(internalSoknad, eier);
        return genererDokumentFraByteArray(filnavn, mimetype, pdf, false);
    }

    private Dokument genererDokumentFraByteArray(String filnavn, String mimetype, byte[] bytes, boolean eksluderesFraPrint) {
        ByteDataSource dataSource = krypterOgOpprettByteDatasource(filnavn, bytes);
        return new Dokument()
                .withFilnavn(filnavn)
                .withMimetype(mimetype)
                .withEkskluderesFraPrint(eksluderesFraPrint)
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
                .withMimetype(FileDetectionUtils.getMimeType(opplastetVedlegg.getData()))
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
